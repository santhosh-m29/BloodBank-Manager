package bloodbank.service;
import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import bloodbank.model.*;
import bloodbank.ui.Menu;
import bloodbank.utility.*;

/**
 * Phase-1 (50% Milestone) regression test suite.
 * Validates authentication, donor registration, blood donation, laboratory validation,
 * donation registration, blood-bank inventory, patient creation, blood-request creation,
 * basic local availability checking, priority mapping, persistence, and explicit Phase-2 deferrals.
 */
public final class SystemTest {
    private static int checks;

    private static void check(boolean value, String message) {
        checks++;
        if (!value) throw new AssertionError(message);
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private static void denied(ThrowingRunnable action, String message) {
        boolean failed = false;
        try {
            action.run();
        } catch (IllegalArgumentException | IllegalStateException | UnsupportedOperationException ex) {
            failed = true;
        } catch (Exception ex) {
            failed = false;
        }
        check(failed, message);
    }

    private static void unsupported(ThrowingRunnable action, String message) {
        boolean caught = false;
        try {
            action.run();
        } catch (UnsupportedOperationException ex) {
            caught = true;
        } catch (Exception ex) {
            caught = false;
        }
        check(caught, message + " [UnsupportedOperationException]");
    }

    private static final class TestClock extends Clock {
        private Instant instant = Instant.parse("2026-10-06T06:00:00Z");
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }
        public Clock withZone(ZoneId zone) {
            return this;
        }
        public Instant instant() {
            return instant;
        }
        void advance(int days) {
            instant = instant.plus(Duration.ofDays(days));
        }
    }

    static final class Fixture implements AutoCloseable {
        final Path directory;
        final FileManager files;
        final TestClock clock = new TestClock();
        BloodBankService service;

        Fixture() throws IOException {
            directory = Files.createTempDirectory("bbms-test-");
            files = new FileManager(directory);
            files.lock();
            service = new BloodBankService(DemoData.create(), files, clock);
        }

        void as(String username) {
            String password = switch (username) {
                case "admin" -> "admin123";
                case "hosp1", "hosp2" -> "hosp1234";
                case "patient" -> "patient123";
                default -> "donor123";
            };
            check(service.login(username, password), "Login " + username);
        }

        BloodDonation collect(int qty) {
            as("donor");
            return service.donate("BB001", qty);
        }

        BloodDonation stock(int qty) {
            BloodDonation d = collect(qty);
            as("admin");
            service.labTest(d.getTransactionId(), true, "Recorded PASS reference TEST");
            service.registerDonation(d.getTransactionId());
            return d;
        }

        void reload() throws IOException {
            service = new BloodBankService(files.loadData(), files, clock);
        }

        public void close() throws IOException {
            files.close();
            try (var paths = Files.walk(directory)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) Files.delete(path);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        donationAndAuthentication();
        phase1WorkflowAndPersistence();
        priorityAndPhase2Guardrails();
        authorizationAndValidation();
        persistenceAndFailure();
        consolePhase1();
        System.out.println("PASS: " + checks + " regression assertions across Phase-1 workflow groups.");
    }

    private static void donationAndAuthentication() throws Exception {
        try (Fixture f = new Fixture()) {
            BloodBankService s = f.service;
            denied(() -> s.donate("BB001", 1), "Unauthenticated donation denied");
            check(!s.login("donor", "incorrect"), "Wrong password denied");
            BloodDonation d = f.collect(3);
            check(d.getUnits().size() == 3 && d.getStatus().equals("PENDING_TEST"), "Donor creates traceable pending units");
            check(((Donor) s.currentUser()).viewProfile(s.donations(), s.today()).contains(d.getTransactionId()), "Profile embeds history");
            denied(() -> s.donate("BB001", 1), "Donation interval enforced");
            denied(() -> s.registerDonation(d.getTransactionId()), "Donor cannot register blood");
            f.as("admin");
            check(s.ownInventory().getTotalStock(s.today()) == 0, "Untested blood unavailable");
            denied(() -> s.registerDonation(d.getTransactionId()), "Registration before test denied");
            s.labTest(d.getTransactionId(), true, "Lab reference 1");
            check(s.ownInventory().getTotalStock(s.today()) == 0, "Passed blood still unavailable before registration");
            denied(() -> s.labTest(d.getTransactionId(), false, "Again"), "Retesting denied");
            s.registerDonation(d.getTransactionId());
            check(s.ownInventory().getTotalStock(s.today()) == 3, "Registration activates all units");
            denied(() -> s.registerDonation(d.getTransactionId()), "Duplicate registration denied");
            // Returned inventory and model objects cannot mutate authoritative state.
            s.ownInventory().removeBloodUnit(d.getUnits().get(0).getBloodUnitId());
            check(s.ownInventory().getTotalStock(s.today()) == 3, "Read objects are detached");
            f.as("donor");
            denied(() -> s.changePassword("wrong", "newpassword"), "Password change verifies old secret");
            s.changePassword("donor123", "newpassword");
            s.logout();
            denied(s::currentUser, "Logout clears session");
            check(!s.login("donor", "donor123") && s.login("donor", "newpassword"), "Password change works");
            check(!s.login("donor", "bad"), "Failed login resets session");
            denied(s::currentUser, "No stale authenticated session");
            f.reload();
            check(f.service.login("donor", "newpassword"), "Hashed credential persists");
            byte[] bytes = Files.readAllBytes(f.files.getFilePath());
            check(!new String(bytes, java.nio.charset.StandardCharsets.ISO_8859_1).contains("newpassword"), "Snapshot contains no plaintext password");
        }
        try (Fixture f = new Fixture()) {
            BloodDonation d = f.collect(2);
            f.as("admin");
            f.service.labTest(d.getTransactionId(), false, "Lab reference FAIL");
            check(f.service.donations().get(0).getStatus().equals("REJECTED"), "Failed donation rejected");
            check(f.service.ownInventory().getTotalStock(f.service.today()) == 0, "Failed blood never usable");
            denied(() -> f.service.registerDonation(d.getTransactionId()), "Failed donation cannot register");
        }
    }

    private static void phase1WorkflowAndPersistence() throws Exception {
        try (Fixture f = new Fixture()) {
            // 1. Donor collection
            f.as("donor");
            BloodDonation donation = f.service.donate("BB001", 4);
            check(donation.getStatus().equals("PENDING_TEST"), "Phase-1: Donation created in PENDING_TEST state");
            check(donation.getUnits().size() == 4, "Phase-1: 4 BloodUnit instances created");

            // 2. Lab test (PASS)
            f.as("admin");
            f.service.labTest(donation.getTransactionId(), true, "Lab note PASS");
            check(f.service.donations().get(0).getStatus().equals("TEST_PASSED"), "Phase-1: Donation marked TEST_PASSED");
            check(f.service.ownInventory().getTotalStock(f.service.today()) == 0, "Phase-1: Passed blood not in usable stock before registration");

            // 3. Register approved donation into central inventory
            f.service.registerDonation(donation.getTransactionId());
            check(f.service.ownInventory().getTotalStock(f.service.today()) == 4, "Phase-1: Blood units registered into blood bank inventory");
            check(f.service.ownInventory().getStockForGroup("A+", f.service.today()) == 4, "Phase-1: Usable A+ stock is 4");

            // 4. Hospital creates patient
            f.as("hosp1");
            Patient newPatient = new Patient("PAT099", "Sarah Connor", 33, "Female", "9876543210", "123 Resistance Way", "sarah", "sarah123", "A+", "Trauma", "Dr. Clara", 2, "HOSP001");
            f.service.createPatient(newPatient);
            check(f.service.patients().stream().anyMatch(p -> p.getPersonId().equals("PAT099")), "Phase-1: Hospital patient created");

            // 5. Hospital creates blood request
            BloodRequest request = f.service.requestBlood("PAT099", 2, "HIGH");
            check(request.getStatus().equals("PENDING"), "Phase-1: Request created in PENDING state");
            check(request.getUnitsRequested() == 2, "Phase-1: Request quantity is 2");
            check(request.getBloodGroup().equals("A+"), "Phase-1: Request blood group is A+");
            check(request.getHospitalId().equals("HOSP001"), "Phase-1: Request linked to hospital");

            // 6. Check local inventory availability
            check(!request.checkAvailability(f.service.ownInventory(), f.service.today()), "Phase-1: Local hospital stock is insufficient (0 units)");

            // 7. Persistence verification across restart
            f.reload();
            f.as("hosp1");
            check(f.service.patients().stream().anyMatch(p -> p.getPersonId().equals("PAT099")), "Phase-1: Created patient persisted across reload");
            check(f.service.requests().stream().anyMatch(r -> r.getTransactionId().equals(request.getTransactionId())), "Phase-1: Request persisted across reload");
            f.as("admin");
            check(f.service.ownInventory().getTotalStock(f.service.today()) == 4, "Phase-1: Central inventory persisted across reload");
            check(f.service.donations().stream().anyMatch(d -> d.getTransactionId().equals(donation.getTransactionId())), "Phase-1: Donation persisted across reload");
        }
    }

    private static void priorityAndPhase2Guardrails() throws Exception {
        try (Fixture f = new Fixture()) {
            f.as("hosp1");
            BloodRequest reqLow = f.service.requestBlood("PAT001", 1, "LOW");
            BloodRequest reqMed = f.service.requestBlood("PAT001", 1, "MEDIUM");
            BloodRequest reqHigh = f.service.requestBlood("PAT001", 1, "HIGH");
            BloodRequest reqCrit = f.service.requestBlood("PAT001", 1, "CRITICAL");

            // Priority mapping: CRITICAL(1), HIGH(2), MEDIUM(3), LOW(4)
            check(reqCrit.priorityDispatch() == 1, "Priority CRITICAL maps to 1");
            check(reqHigh.priorityDispatch() == 2, "Priority HIGH maps to 2");
            check(reqMed.priorityDispatch() == 3, "Priority MEDIUM maps to 3");
            check(reqLow.priorityDispatch() == 4, "Priority LOW maps to 4");

            // Priority sorting verification
            List<BloodRequest> sorted = f.service.requests();
            check(sorted.get(0).getUrgency().equals("CRITICAL"), "Sorted critical first");
            check(sorted.get(1).getUrgency().equals("HIGH"), "Sorted high second");
            check(sorted.get(2).getUrgency().equals("MEDIUM"), "Sorted medium third");
            check(sorted.get(3).getUrgency().equals("LOW"), "Sorted low fourth");

            // Phase-2 Guardrails: Verify deferred features throw UnsupportedOperationException
            f.as("admin");
            unsupported(() -> f.service.approveRequest(reqCrit.getTransactionId()), "approveRequest deferred");
            unsupported(() -> f.service.rejectRequest(reqCrit.getTransactionId(), "test"), "rejectRequest deferred");
            unsupported(() -> f.service.dispatchTransfer(reqCrit.getTransactionId()), "dispatchTransfer deferred");
            unsupported(() -> f.service.transfers(), "transfers deferred");
            f.as("hosp1");
            unsupported(() -> f.service.requestFromBank(reqCrit.getTransactionId(), "BB001"), "requestFromBank deferred");
            unsupported(() -> f.service.shortage(reqCrit.getTransactionId()), "shortage deferred");
            unsupported(() -> f.service.issueBlood(reqCrit.getTransactionId()), "issueBlood deferred");

            // Model-level Phase-2 guardrails
            BloodBank bank = new BloodBank("BB999", "Test", "Addr", "1234567890", "Manager");
            Hospital hosp = new Hospital("H999", "Test", "Addr", "1234567890", "Type", "112");
            BloodTransfer transfer = new BloodTransfer("TR999", f.service.today(), "BB999", "H999", "A+", 1, "REQ999");
            unsupported(() -> bank.transferBlood(transfer, hosp, f.service.today()), "BloodBank.transferBlood deferred");
            unsupported(() -> transfer.transferUnits(bank.getInventory(), hosp.getInventory(), f.service.today()), "BloodTransfer.transferUnits deferred");
            unsupported(transfer::recordTransfer, "BloodTransfer.recordTransfer deferred");

            BloodUnit unit = new BloodUnit("U999", "A+", "D999", f.service.today(), f.service.today().plusDays(42));
            unsupported(() -> unit.reserve("REQ999", f.service.today()), "BloodUnit.reserve deferred");
            unsupported(() -> unit.release("REQ999"), "BloodUnit.release deferred");
            unsupported(() -> unit.transfer("REQ999", f.service.today()), "BloodUnit.transfer deferred");
            unsupported(() -> unit.issue("REQ999", "PAT001", f.service.today()), "BloodUnit.issue deferred");
            unsupported(() -> unit.expire(f.service.today()), "BloodUnit.expire deferred");

            unsupported(() -> reqCrit.route("BB001"), "BloodRequest.route deferred");
            unsupported(reqCrit::ready, "BloodRequest.ready deferred");
            unsupported(reqCrit::approveRequest, "BloodRequest.approveRequest deferred");
            unsupported(() -> reqCrit.rejectRequest("reason"), "BloodRequest.rejectRequest deferred");
            unsupported(reqCrit::reopen, "BloodRequest.reopen deferred");
            unsupported(() -> reqCrit.fulfill(List.of(unit)), "BloodRequest.fulfill deferred");

            Inventory inv = f.service.ownInventory();
            unsupported(() -> inv.reserved("REQ999", f.service.today()), "Inventory.reserved deferred");
            unsupported(() -> inv.release("REQ999"), "Inventory.release deferred");
            unsupported(() -> inv.expire(f.service.today()), "Inventory.expire deferred");
            unsupported(() -> inv.checkLowStock(5, f.service.today()), "Inventory.checkLowStock deferred");

            AlertManager alerts = new AlertManager(10, 7);
            unsupported(() -> alerts.checkLowStock("HOSP001", inv, f.service.today()), "AlertManager.checkLowStock deferred");
            unsupported(() -> alerts.checkExpiry("HOSP001", inv, f.service.today()), "AlertManager.checkExpiry deferred");

            ReportGenerator reports = new ReportGenerator("Report", f.service.today());
            unsupported(() -> reports.generateInventoryReport("HOSP001", inv), "ReportGenerator.generateInventoryReport deferred");
            unsupported(() -> reports.exportReport("text", Path.of("test.txt")), "ReportGenerator.exportReport deferred");
        }
    }

    private static void authorizationAndValidation() throws Exception {
        try (Fixture f = new Fixture()) {
            f.as("hosp2");
            denied(() -> f.service.requestBlood("PAT001", 1, "LOW"), "Cross-hospital patient access denied");
            check(f.service.patients().isEmpty(), "Patient list scoped to hospital");
            Patient patient = new Patient("P2", "Name", 30, "Other", "1234567890", "Address, with comma", "p2", "password2", "O-", "Reason", "Doctor", 3, "HOSP002");
            f.service.createPatient(patient);
            denied(() -> f.service.createPatient(patient), "Duplicate IDs denied");
            check(f.service.patients().size() == 1, "Hospital creates its own patient");
            denied(() -> f.service.requestBlood("P2", -1, "LOW"), "Negative quantity denied");
            denied(() -> f.service.requestBlood("P2", 1, "NORMAL"), "Invalid urgency denied");
            BloodRequest r = f.service.requestBlood("P2", 1, "LOW");
            check(r.getPatientId().equals("P2"), "Blood request linked to created patient");
            f.as("patient");
            denied(f.service::ownInventory, "Patient cannot view inventory");
            denied(() -> f.service.requestBlood("PAT001", 1, "LOW"), "Patient cannot request blood");
            f.as("admin");
            f.service.registerFacility(new BloodBank("BB002", "Second Bank", "Address", "1234567890", "Manager"));
            f.service.registerStaff(new BloodBankAdmin("A2", "Read only", 30, "Other", "1234567890", "Address", "readadmin", "password2", "E2", "BB001", "READ_ONLY"));
            f.service.registerStaff(new BloodBankAdmin("A3", "Other bank", 30, "Other", "1234567890", "Address", "otheradmin", "password2", "E3", "BB002", "STANDARD"));
            check(f.service.login("readadmin", "password2"), "Read-only admin login");
            check(f.service.login("otheradmin", "password2"), "Second bank admin login");
            denied(() -> f.service.registerFacility(new BloodBank("BB003", "Third", "Address", "1234567890", "Manager")), "Standard admin cannot provision facility");
            f.reload();
            check(f.service.login("p2", "password2"), "New patient can log in after restart");
            check(f.service.currentUser().getAddress().contains(","), "Commas persist correctly");
            denied(() -> f.service.updateProfile("", 20, "Other", "1234567890", "Address"), "Empty name denied");
            denied(() -> f.service.updateProfile("Name", 121, "Other", "1234567890", "Address"), "Invalid age denied");
            denied(() -> f.service.updateProfile("Name", 20, "Other", "12", "Address"), "Invalid phone denied");
        }
        check(!Validation.validateDate("2026-02-30") && Validation.validateDate("2028-02-29"), "Strict date validation");
        denied(() -> new Donor("D", "Name", 22, "Other", "1234567890", "Address", "user", "password", "Z+", 13, 60), "Invalid blood group denied");
        denied(() -> new Donor("D", "Name", 22, "Other", "1234567890", "Address", "user", "password", "A+", Double.NaN, 60), "Nonfinite measurements denied");
    }

    private static void persistenceAndFailure() throws Exception {
        try (Fixture f = new Fixture()) {
            f.as("donor");
            Path blocker = f.directory.resolve("not-a-directory");
            Files.writeString(blocker, "blocked");
            BloodBankService broken = new BloodBankService(DemoData.create(), new FileManager(blocker), f.clock);
            broken.login("donor", "donor123");
            denied(() -> broken.donate("BB001", 1), "Write failure surfaced");
            check(broken.donations().isEmpty() && ((Donor) broken.currentUser()).getLastDonationDate() == null, "Write failure rolls back all memory changes");
            Files.writeString(f.files.getFilePath(), "corrupt snapshot");
            boolean failed = false;
            try {
                f.files.loadData();
            } catch (IOException ex) {
                failed = true;
            }
            check(failed && Files.readString(f.files.getFilePath()).equals("corrupt snapshot"), "Corrupt data never replaced with seed data");
            try (FileManager second = new FileManager(f.directory)) {
                boolean locked = false;
                try {
                    second.lock();
                } catch (IOException ex) {
                    locked = true;
                }
                check(locked, "Concurrent application blocked by file lock");
            }
        }
    }

    private static void consolePhase1() throws Exception {
        try (Fixture f = new Fixture()) {
            f.stock(2);
            f.service.logout();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            new Menu(f.service, new StringReader("bad\n1\ndonor\nwrong\n1\ndonor\ndonor123\n1\n5\n2\n"), new PrintStream(output), f.directory.resolve("reports")).displayMainMenu();
            String transcript = output.toString(java.nio.charset.StandardCharsets.UTF_8);
            check(transcript.contains("Enter a whole number") && transcript.contains("Invalid username or password") && transcript.contains("Donation history:"), "Console recovers from invalid input and displays donor profile");
            new Menu(f.service, new StringReader("1\n"), new PrintStream(output), f.directory.resolve("reports")).displayMainMenu();
            check(output.toString().contains("Input closed"), "EOF handled without crash");
        }
    }
}
