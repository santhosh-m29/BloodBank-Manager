package bloodbank.ui;
import java.io.*;
import java.nio.file.Path;
import java.util.*;
import bloodbank.model.*;
import bloodbank.service.*;
import bloodbank.utility.Validation;
/** One input reader for the entire application; domain classes never read console input. */
public final class Menu {
    private final BloodBankService service;
    private final BufferedReader input;
    private final PrintStream out;
    private final Path reports;
    private int choice;
    public Menu(BloodBankService service, Reader input, PrintStream out, Path reports) {
        this.service = service;
        this.input = new BufferedReader(input);
        this.out = out;
        this.reports = reports;
    }
    private String read(String prompt) throws IOException {
        out.print(prompt + ": ");
        String value = input.readLine();
        if (value == null) throw new EOFException();
        return value.trim();
    }
    private String password(String prompt) throws IOException {
        if (System.console() != null) {
            char[] value = System.console().readPassword("%s: ", prompt);
            if (value == null) throw new EOFException();
            String result = new String(value);
            Arrays.fill(value, '\0');
            return result;
        }
        return read(prompt);
    }
    private int number(String prompt) throws IOException {
        try {
            return Integer.parseInt(read(prompt));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Enter a whole number.");
        }
    }
    private double decimal(String prompt) throws IOException {
        try {
            return Double.parseDouble(read(prompt));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Enter a valid decimal number.");
        }
    }
    private void show(Object value) {
        out.println(value);
    }
    private void list(Collection<?> values) {
        if (values.isEmpty()) show("No records.");
        else values.forEach(this::show);
    }
    public void displayMainMenu() throws IOException {
        show("BLOOD BANK MANAGEMENT SYSTEM");
        try {
            while (true) {
                try {
                    show("\n1. Login\n2. Exit");
                    choice = number("Choice");
                    if (choice == 2) return;
                    Validation.require(choice == 1, "Invalid menu choice.");
                    if (!service.login(read("Username"), password("Password"))) {
                        show("Invalid username or password.");
                        continue;
                    }
                    show("Welcome, " + service.currentUser().getName() + " [" + service.currentUser().getRole() + "]");
                    dashboard();
                } catch (IllegalArgumentException | IllegalStateException ex) {
                    show("Error: " + ex.getMessage());
                }
            }
        } catch (EOFException ex) {
            show("\nInput closed. All completed operations are saved.");
        } finally {
            service.logout();
        }
    }
    private void dashboard() throws IOException {
        while (true) {
            try {
                String role = service.currentUser().getRole();
                switch (role) {
                    case "DONOR" -> donorMenu();
                    case "PATIENT" -> patientMenu();
                    case "HOSPITAL" -> staffMenu();
                    case "BLOOD_BANK" -> adminMenu();
                    default -> throw new IllegalStateException("Unknown role.");
                }
                if (choice == 0) {
                    service.logout();
                    return;
                }
            } catch (IllegalArgumentException | IllegalStateException ex) {
                show("Error: " + ex.getMessage());
            } catch (IOException ex) {
                if (ex instanceof EOFException) throw ex;
                show("File operation failed: " + ex.getMessage());
            }
        }
    }
    public void donorMenu() throws IOException {
        show("\nDONOR\n1. View Profile\n2. Update Profile\n3. Donate Blood\n4. Change Password\n5. Logout");
        choice = number("Choice");
        switch (choice) {
            case 1 -> show(((Donor) service.currentUser()).viewProfile(service.donations(), service.today()));
            case 2 -> updateProfile();
            case 3 -> {
                show(service.facilities());
                show(service.donate(read("Blood bank ID"), number("Units collected")));
            }
            case 4 -> changePassword();
            case 5 -> choice = 0;
            default -> show("Invalid menu choice.");
        }
    }
    public void patientMenu() throws IOException {
        show("\nPATIENT\n1. View Profile\n2. Update Profile\n3. View Request Status\n4. View Request History\n5. Change Password\n6. Logout");
        choice = number("Choice");
        switch (choice) {
            case 1 -> show(service.currentUser().getDetails());
            case 2 -> updateProfile();
            case 3 -> show(((Patient) service.currentUser()).viewRequestStatus(read("Request ID"), service.requests()));
            case 4 -> list(service.requests());
            case 5 -> changePassword();
            case 6 -> choice = 0;
            default -> show("Invalid menu choice.");
        }
    }
    public void staffMenu() throws IOException {
        show("\nHOSPITAL\n1. Create Patient\n2. View Patients\n3. Create Blood Request\n4. Create Emergency/Urgent Request\n5. Check Local Inventory\n6. View Inventory\n7. Issue Blood\n8. Request Blood from Blood Bank / Recheck Stock\n9. View Request History\n10. View Transfer History\n11. Change Password\n12. Logout\n13. View Alerts\n14. Export Reports");
        choice = number("Choice");
        switch (choice) {
            case 1 -> createPatient();
            case 2 -> service.patients().forEach(p -> show(p.getDetails()));
            case 3, 4 -> {
                String patient = read("Patient ID");
                int qty = number("Units required");
                String urgency = read(choice == 4 ? "Urgency (CRITICAL/HIGH)" : "Urgency (CRITICAL/HIGH/MEDIUM/LOW)").toUpperCase(Locale.ROOT);
                if (choice == 4) Validation.require(Set.of("CRITICAL", "HIGH").contains(urgency), "Urgent requests must be HIGH or CRITICAL.");
                BloodRequest r = service.requestBlood(patient, qty, urgency);
                show(r);
                show("Reserved locally: " + (qty - service.shortage(r.getTransactionId())) + "; shortage: " + service.shortage(r.getTransactionId()));
            }
            case 5 -> {
                String group = read("Blood group").toUpperCase(Locale.ROOT);
                show("Unreserved usable units: " + service.ownInventory().getStockForGroup(group, service.today()));
            }
            case 6 -> list(service.ownInventory().getBloodUnits());
            case 7 -> {
                list(service.requests());
                service.issueBlood(read("Request ID"));
                show("Blood issued. Request fulfilled.");
            }
            case 8 -> {
                list(service.requests());
                show(service.facilities());
                service.requestFromBank(read("Request ID"), read("Blood bank ID"));
                show("Local stock checked; any shortage sent to the selected blood bank.");
            }
            case 9 -> list(service.requests());
            case 10 -> list(service.transfers());
            case 11 -> changePassword();
            case 12 -> choice = 0;
            case 13 -> alerts();
            case 14 -> reports();
            default -> show("Invalid menu choice.");
        }
    }
    public void hospitalMenu() throws IOException {
        staffMenu();
    }
    public void adminMenu() throws IOException {
        show("\nBLOOD BANK\n1. View Pending Donations\n2. Initiate Lab Test\n3. Register Approved Donation\n4. View Blood Bank Inventory\n5. View Hospital Requests (priority order)\n6. Approve Request\n7. Reject Request\n8. Dispatch Blood Transfer\n9. View Transfer History\n10. View Alerts\n11. Generate Reports\n12. Change Password\n13. Logout\n14. Register Donor\n15. Register Facility / Staff\n16. View My Audit History");
        choice = number("Choice");
        switch (choice) {
            case 1 -> list(service.donations().stream().filter(d -> !d.getStatus().equals("REGISTERED") && !d.getStatus().equals("REJECTED")).toList());
            case 2 -> {
                list(service.donations());
                String id = read("Donation ID");
                String result = read("Recorded laboratory result (PASS/FAIL)").toUpperCase(Locale.ROOT);
                Validation.require(result.equals("PASS") || result.equals("FAIL"), "Enter PASS or FAIL.");
                service.labTest(id, result.equals("PASS"), read("Laboratory reference/result note"));
                show("Lab result recorded. Passed donations require registration.");
            }
            case 3 -> {
                list(service.donations());
                service.registerDonation(read("Donation ID"));
                show("Donation registered into usable stock.");
            }
            case 4 -> list(service.ownInventory().getBloodUnits());
            case 5 -> list(service.requests());
            case 6 -> {
                list(service.requests());
                service.approveRequest(read("Request ID"));
                show("Request approved; central units reserved.");
            }
            case 7 -> {
                list(service.requests());
                service.rejectRequest(read("Request ID"), read("Reason"));
                show("Request rejected; reservations released.");
            }
            case 8 -> {
                list(service.requests());
                show(service.dispatchTransfer(read("Request ID")));
            }
            case 9 -> list(service.transfers());
            case 10 -> alerts();
            case 11 -> reports();
            case 12 -> changePassword();
            case 13 -> choice = 0;
            case 14 -> createDonor();
            case 15 -> provision();
            case 16 -> list(service.audit());
            default -> show("Invalid menu choice.");
        }
    }
    private void updateProfile() throws IOException {
        Person p = service.currentUser();
        show(p.getDetails());
        service.updateProfile(read("Name"), number("Age"), read("Gender"), read("Phone (10 digits)"), read("Address"));
        show("Profile saved.");
        if (p instanceof Donor && read("Update haemoglobin and weight? (yes/no)").equalsIgnoreCase("yes")) {
            service.updateMeasurements(decimal("Haemoglobin"), decimal("Weight (kg)"));
            show("Measurements saved.");
        }
    }
    private void changePassword() throws IOException {
        service.changePassword(password("Current password"), password("New password (8-128 characters)"));
        show("Password changed.");
    }
    private record Profile(String name, int age, String gender, String phone, String address, String username, String password) {
    }
    private Profile profile() throws IOException {
        return new Profile(read("Full name"), number("Age"), read("Gender"), read("Phone (10 digits)"), read("Address"), read("Username"), password("Initial password (8-128 characters)"));
    }
    private void createPatient() throws IOException {
        Profile p = profile();
        String id = BloodBankService.newId("PAT");
        Patient patient = new Patient(id, p.name, p.age, p.gender, p.phone, p.address, p.username, p.password, read("Blood group").toUpperCase(Locale.ROOT), read("Disease/reason"), read("Doctor"), number("Units required"), ((HospitalStaff) service.currentUser()).getFacilityId());
        service.createPatient(patient);
        show("Patient created: " + id);
    }
    private void createDonor() throws IOException {
        Profile p = profile();
        String id = BloodBankService.newId("DNR");
        service.registerDonor(new Donor(id, p.name, p.age, p.gender, p.phone, p.address, p.username, p.password, read("Blood group").toUpperCase(Locale.ROOT), decimal("Haemoglobin"), decimal("Weight (kg)")));
        show("Donor created: " + id);
    }
    private void provision() throws IOException {
        show("1. Hospital\n2. Blood Bank\n3. Hospital Staff\n4. Blood Bank Administrator");
        int type = number("Type");
        Validation.require(type >= 1 && type <= 4, "Invalid type.");
        if (type <= 2) {
            String id = read("Facility ID"), name = read("Name"), address = read("Address"), phone = read("Phone (10 digits)");
            service.registerFacility(type == 1 ? new Hospital(id, name, address, phone, read("Hospital type"), read("Emergency contact")) : new BloodBank(id, name, address, phone, read("Manager")));
        } else {
            Profile p = profile();
            show(service.facilities());
            String id = BloodBankService.newId("STF"), employee = read("Employee ID"), facility = read("Facility ID");
            service.registerStaff(type == 3 ? new HospitalStaff(id, p.name, p.age, p.gender, p.phone, p.address, p.username, p.password, employee, facility, read("Department")) : new BloodBankAdmin(id, p.name, p.age, p.gender, p.phone, p.address, p.username, p.password, employee, facility, read("Level (SUPER/STANDARD/READ_ONLY)").toUpperCase(Locale.ROOT)));
        }
        show("Registration saved.");
    }
    private void alerts() {
        String facility = ((Staff) service.currentUser()).getFacilityId();
        AlertManager manager = new AlertManager(10, 7);
        Inventory inventory = service.ownInventory();
        list(manager.checkLowStock(facility, inventory, service.today()));
        list(manager.checkExpiry(facility, inventory, service.today()));
    }
    private void reports() throws IOException {
        String facility = ((Staff) service.currentUser()).getFacilityId();
        ReportGenerator generator = new ReportGenerator("Blood Bank Management - " + facility, service.today());
        String report = generator.generateInventoryReport(facility, service.ownInventory()) + "\n" + generator.generateDonationReport(service.donations()) + "\n" + generator.generateRequestReport(service.requests()) + "\n" + generator.generateTransferReport(service.transfers());
        show(report);
        show("Exported: " + generator.exportReport(report, reports.resolve(BloodBankService.newId(facility) + ".txt")).toAbsolutePath());
    }
}
