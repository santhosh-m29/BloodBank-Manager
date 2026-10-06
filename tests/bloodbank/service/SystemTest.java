package bloodbank.service;
import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import bloodbank.model.*;
import bloodbank.ui.Menu;
import bloodbank.utility.*;
/** Dependency-free regression suite. Every fixture uses a unique temporary directory. */
public final class SystemTest {
    private static int checks;
    private static void check(boolean value, String message) {
        checks++;
        if (!value) throw new AssertionError(message);
    }
    private static void denied(Runnable action, String message) {
        boolean failed = false;
        try {
            action.run();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            failed = true;
        }
        check(failed, message);
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
    private static final class Fixture implements AutoCloseable {
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
        endToEndAndPersistence();
        priorityAndReservations();
        authorizationAndValidation();
        expiryAndFailure();
        reportsAndConsole();
        System.out.println("PASS: " + checks + " regression assertions across six workflow groups.");
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
    private static void endToEndAndPersistence() throws Exception {
        try (Fixture f = new Fixture()) {
            f.stock(22);
            BloodBankService s = f.service;
            f.as("hosp1");
            BloodRequest initial = s.requestBlood("PAT001", 2, "MEDIUM");
            s.requestFromBank(initial.getTransactionId(), "BB001");
            f.as("admin");
            s.approveRequest(initial.getTransactionId());
            BloodTransfer first = s.dispatchTransfer(initial.getTransactionId());
            check(first.getUnitsTransferred() == 2, "Initial hospital transfer");
            // Set up the PRD's 20 central + 2 local scenario by releasing the first request in the fixture snapshot.
            SystemState setup = f.files.loadData();
            setup.requests.remove(initial.getTransactionId());
            setup.transfers.clear();
            setup.facilities.get("HOSP001").getInventory().release(initial.getTransactionId());
            f.files.saveData(setup);
            f.reload();
            s = f.service;
            f.as("hosp1");
            BloodRequest r = s.requestBlood("PAT001", 5, "HIGH");
            String id = r.getTransactionId();
            check(s.shortage(id) == 3, "5 required minus 2 local equals 3 shortage");
            s.requestFromBank(id, "BB001");
            f.as("admin");
            s.approveRequest(id);
            check(s.ownInventory().getStockForGroup("A+", s.today()) == 17, "Approval reserves only three units");
            BloodTransfer transfer = s.dispatchTransfer(id);
            check(transfer.getUnitsTransferred() == 3 && transfer.getUnitIds().size() == 3, "Transfer records actual shortage and IDs");
            check(s.ownInventory().getTotalStock(s.today()) == 17, "Central inventory 20 to 17");
            BloodBankService active = s;
            denied(() -> active.dispatchTransfer(id), "Duplicate transfer rejected");
            f.as("hosp1");
            check(s.ownInventory().getTotalStock(s.today()) == 5, "Hospital inventory 2 to 5");
            check(s.requests().get(0).getStatus().equals("READY"), "Transfer does not fulfill patient request");
            f.reload();
            s = f.service;
            f.as("hosp1");
            s.issueBlood(id);
            check(s.ownInventory().getTotalStock(s.today()) == 0, "Hospital inventory 5 to 0");
            check(s.requests().get(0).getStatus().equals("FULFILLED"), "Request fulfilled only at issue");
            check(s.requests().get(0).getIssuedUnitIds().size() == 5 && s.ownInventory().getBloodUnits().stream().allMatch(u -> u.getStatus().equals("ISSUED") && u.getIssuedTo().equals("PAT001")), "Issued records retained");
            BloodBankService issuedService = s;
            denied(() -> issuedService.issueBlood(id), "Double issue rejected");
            f.reload();
            f.as("patient");
            check(f.service.requests().get(0).getStatus().equals("FULFILLED"), "Patient sees persisted completion");
            f.as("admin");
            check(f.service.transfers().size() == 1 && f.service.donations().size() == 1, "Donation and transfer history persisted");
            check(f.service.ownInventory().getTotalStock(f.service.today()) == 17, "Restart preserves central stock without reseeding");
        }
        try (Fixture f = new Fixture()) {
            f.stock(4);
            f.as("hosp1");
            BloodRequest r = f.service.requestBlood("PAT001", 2, "LOW");
            f.service.requestFromBank(r.getTransactionId(), "BB001");
            f.as("admin");
            f.service.approveRequest(r.getTransactionId());
            f.service.dispatchTransfer(r.getTransactionId());
            SystemState setup = f.files.loadData();
            setup.requests.remove(r.getTransactionId());
            setup.transfers.clear();
            setup.facilities.get("HOSP001").getInventory().release(r.getTransactionId());
            f.files.saveData(setup);
            f.reload();
            f.as("hosp1");
            BloodRequest local = f.service.requestBlood("PAT001", 2, "LOW");
            check(local.getStatus().equals("READY"), "Sufficient local stock requires no bank approval");
            f.service.issueBlood(local.getTransactionId());
            check(f.service.transfers().isEmpty(), "Local issue creates no transfer");
        }
    }
    private static void priorityAndReservations() throws Exception {
        try (Fixture f = new Fixture()) {
            f.stock(6);
            f.as("hosp1");
            BloodRequest low = f.service.requestBlood("PAT001", 2, "LOW"), high = f.service.requestBlood("PAT001", 2, "HIGH"), critical = f.service.requestBlood("PAT001", 2, "CRITICAL");
            for (BloodRequest r : List.of(low, high, critical)) f.service.requestFromBank(r.getTransactionId(), "BB001");
            f.as("admin");
            check(f.service.requests().get(0).getUrgency().equals("CRITICAL"), "Requests sorted critical before high before low");
            denied(() -> f.service.approveRequest(low.getTransactionId()), "Lower priority cannot take stock first");
            f.service.approveRequest(critical.getTransactionId());
            f.service.approveRequest(high.getTransactionId());
            f.service.approveRequest(low.getTransactionId());
            check(f.service.ownInventory().getStockForGroup("A+", f.service.today()) == 0, "Reservations avoid overcommit");
            f.service.rejectRequest(high.getTransactionId(), "No longer required");
            check(f.service.ownInventory().getStockForGroup("A+", f.service.today()) == 2, "Rejection releases bank reservations");
            denied(() -> f.service.dispatchTransfer(high.getTransactionId()), "Rejected request cannot dispatch");
            f.as("hosp1");
            BloodRequest tooLarge = f.service.requestBlood("PAT001", 10, "CRITICAL");
            f.service.requestFromBank(tooLarge.getTransactionId(), "BB001");
            f.as("admin");
            denied(() -> f.service.approveRequest(tooLarge.getTransactionId()), "Critical urgency never bypasses availability");
            check(f.service.ownInventory().getStockForGroup("A+", f.service.today()) == 2, "Failed approval leaves stock unchanged");
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
            f.service.requestFromBank(r.getTransactionId(), "BB001");
            f.as("hosp1");
            denied(() -> f.service.issueBlood(r.getTransactionId()), "Other hospital cannot issue request");
            denied(() -> f.service.approveRequest(r.getTransactionId()), "Hospital cannot approve central request");
            denied(() -> f.service.shortage(r.getTransactionId()), "Other hospital cannot read request shortage");
            f.as("patient");
            denied(f.service::ownInventory, "Patient cannot view inventory");
            denied(() -> f.service.requestBlood("PAT001", 1, "LOW"), "Patient cannot request blood");
            f.as("admin");
            f.service.registerFacility(new BloodBank("BB002", "Second Bank", "Address", "1234567890", "Manager"));
            f.service.registerStaff(new BloodBankAdmin("A2", "Read only", 30, "Other", "1234567890", "Address", "readadmin", "password2", "E2", "BB001", "READ_ONLY"));
            f.service.registerStaff(new BloodBankAdmin("A3", "Other bank", 30, "Other", "1234567890", "Address", "otheradmin", "password2", "E3", "BB002", "STANDARD"));
            check(f.service.login("readadmin", "password2"), "Read-only admin login");
            denied(() -> f.service.rejectRequest(r.getTransactionId(), "reason"), "Read-only admin cannot mutate");
            check(f.service.login("otheradmin", "password2"), "Second bank admin login");
            denied(() -> f.service.rejectRequest(r.getTransactionId(), "reason"), "Admin scoped to bank");
            check(f.service.requests().isEmpty(), "Bank request visibility scoped");
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
    private static void expiryAndFailure() throws Exception {
        try (Fixture f = new Fixture()) {
            f.stock(3);
            f.as("hosp1");
            BloodRequest r = f.service.requestBlood("PAT001", 2, "LOW");
            f.service.requestFromBank(r.getTransactionId(), "BB001");
            f.as("admin");
            f.service.approveRequest(r.getTransactionId());
            f.clock.advance(42);
            check(f.service.ownInventory().getTotalStock(f.service.today()) == 0, "Expiry date itself is unusable");
            denied(() -> f.service.dispatchTransfer(r.getTransactionId()), "Expired reserved units cannot transfer");
            f.service.rejectRequest(r.getTransactionId(), "Expired");
            check(f.service.ownInventory().getBloodUnits().stream().allMatch(u -> u.getStatus().equals("EXPIRED")), "Expired status saved on next mutation");
        }
        try (Fixture f = new Fixture()) {
            f.stock(2);
            f.as("hosp1");
            BloodRequest r = f.service.requestBlood("PAT001", 2, "LOW");
            f.service.requestFromBank(r.getTransactionId(), "BB001");
            f.as("admin");
            f.service.approveRequest(r.getTransactionId());
            f.service.dispatchTransfer(r.getTransactionId());
            f.clock.advance(42);
            f.as("hosp1");
            denied(() -> f.service.issueBlood(r.getTransactionId()), "Expired hospital units cannot issue");
            f.service.requestFromBank(r.getTransactionId(), "BB001");
            check(f.service.shortage(r.getTransactionId()) == 2, "Expired local reservations can be restocked");
        }
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
    private static void reportsAndConsole() throws Exception {
        try (Fixture f = new Fixture()) {
            f.stock(2);
            f.clock.advance(38);
            Inventory inv = f.service.ownInventory();
            AlertManager alerts = new AlertManager(10, 7);
            check(alerts.checkLowStock("BB001", inv, f.service.today()).size() == 8, "Low stock covers all groups including zero");
            check(alerts.checkExpiry("BB001", inv, f.service.today()).size() == 2, "Near-expiry alerts");
            ReportGenerator reports = new ReportGenerator("Test report", f.service.today());
            String content = reports.generateInventoryReport("BB001", inv) + reports.generateDonationReport(f.service.donations());
            Path exported = reports.exportReport(content, f.directory.resolve("reports/report.txt"));
            check(Files.readString(exported).contains("REGISTERED") && content.contains("Unreserved"), "Report export includes status and counts");
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
