package bloodbank.ui;

import java.io.*;
import java.util.*;
import bloodbank.model.*;
import bloodbank.service.*;
import bloodbank.utility.Validation;

/** Direct role selection for the single-facility Phase-1 demonstration. */
public final class Menu {
    private final BloodBankService service;
    private final BufferedReader input;
    private final PrintStream out;
    private int choice;

    public Menu(BloodBankService service, Reader input, PrintStream out) {
        this.service = service;
        this.input = new BufferedReader(input);
        this.out = out;
    }
    private String read(String prompt) throws IOException {
        out.print(prompt + ": ");
        String value = input.readLine();
        if (value == null) throw new EOFException();
        return value.trim();
    }
    private int number(String prompt) throws IOException {
        try { return Integer.parseInt(read(prompt)); }
        catch (NumberFormatException ex) { throw new IllegalArgumentException("Enter a whole number."); }
    }
    private double decimal(String prompt) throws IOException {
        try { return Double.parseDouble(read(prompt)); }
        catch (NumberFormatException ex) { throw new IllegalArgumentException("Enter a decimal number."); }
    }
    private void show(Object value) { out.println(value); }
    private void list(Collection<?> values) {
        if (values.isEmpty()) show("No records.");
        else values.forEach(this::show);
    }
    public void displayMainMenu() throws IOException {
        show("BLOOD BANK MANAGEMENT SYSTEM");
        try {
            while (true) {
                try {
                    show("\nChoose a role\n1. Blood Bank Admin\n2. Patient\n3. Donor\n4. Hospital Staff\n5. Exit");
                    int role = number("Choice");
                    if (role == 5) return;
                    Validation.require(role >= 1 && role <= 4, "Invalid role choice.");
                    service.selectRole(List.of("BLOOD_BANK", "PATIENT", "DONOR", "HOSPITAL").get(role - 1));
                    if (role == 2) {
                        Patient patient = choosePatient(service.patientChoices());
                        if (patient == null) { service.clearRole(); continue; }
                        service.selectPatient(patient.getPersonId());
                    }
                    show("Selected: " + service.currentUser().getName());
                    dashboard();
                } catch (IllegalArgumentException | IllegalStateException ex) { show("Error: " + ex.getMessage()); }
            }
        } catch (EOFException ex) { show("Input closed. All completed operations are saved."); }
        finally { service.clearRole(); }
    }
    private void dashboard() throws IOException {
        while (true) {
            try {
                switch (service.currentUser().getRole()) {
                    case "DONOR" -> donorMenu();
                    case "PATIENT" -> patientMenu();
                    case "HOSPITAL" -> staffMenu();
                    case "BLOOD_BANK" -> adminMenu();
                    default -> throw new IllegalStateException("Unknown role.");
                }
                if (choice == 0) { service.clearRole(); return; }
            } catch (IllegalArgumentException | IllegalStateException ex) { show("Error: " + ex.getMessage()); }
        }
    }
    public void donorMenu() throws IOException {
        show("\nDONOR\n1. View Profile\n2. Update Profile\n3. Donate Blood\n4. Logout");
        choice = number("Choice");
        switch (choice) {
            case 1 -> show(((Donor) service.currentUser()).viewProfile(service.donations(), service.today()));
            case 2 -> updateProfile();
            case 3 -> {
                BloodDonation donation = service.donate(number("Units"));
                show(donation);
                show(donation.getStatus().equals("TEST_PASSED") ? "Awaiting admin approval." : "Test failed. Donation rejected.");
            }
            case 4 -> choice = 0;
            default -> show("Invalid menu choice.");
        }
    }
    public void patientMenu() throws IOException {
        show("\nPATIENT\n1. View Profile\n2. Update Profile\n3. View Request History\n4. Logout");
        choice = number("Choice");
        switch (choice) {
            case 1 -> show(service.currentUser().getDetails());
            case 2 -> updateProfile();
            case 3 -> list(service.requests());
            case 4 -> choice = 0;
            default -> show("Invalid menu choice.");
        }
    }
    public void staffMenu() throws IOException {
        show("\nHOSPITAL STAFF\n1. View Patients\n2. Create Patient\n3. Create Blood Request\n4. View Inventory\n5. Issue Blood\n6. View Request History\n7. Logout");
        choice = number("Choice");
        switch (choice) {
            case 1 -> service.patients().forEach(p -> show(p.getDetails()));
            case 2 -> {
                Patient p = service.createPatient(read("Name"), number("Age"), read("Gender"), read("Phone (10 digits)"), read("Address"),
                        read("Blood group").toUpperCase(Locale.ROOT), read("Disease/reason"), read("Doctor"), number("Units required"));
                show("Patient created: " + p.getDetails());
            }
            case 3 -> {
                List<Patient> waiting = service.patientsAwaitingBlood();
                if (waiting.isEmpty()) { show("No patients awaiting blood. All existing patients are completed."); return; }
                Patient p = choosePatient(waiting);
                if (p == null) return;
                show("Requesting " + p.getUnitsRequired() + " units of " + p.getBloodGroup() + " for " + p.getName());
                String urgency = read("Urgency (CRITICAL/HIGH/MEDIUM/LOW)").toUpperCase(Locale.ROOT);
                BloodRequest request = service.requestBlood(p.getPersonId(), urgency);
                show(request);
                show(request.checkAvailability(service.ownInventory(), service.today()) ? "Stock available. Choose Issue Blood to complete this request." : "Insufficient hospital stock. Request remains pending.");
            }
            case 4 -> list(service.inventoryRows());
            case 5 -> {
                BloodRequest request = chooseRequest(service.requests().stream().filter(r -> r.getStatus().equals("PENDING")).toList());
                if (request == null) return;
                service.issueBlood(request.getTransactionId());
                show(request.getTransactionId() + " COMPLETED. Issued " + request.getUnitsRequested() + " units to " + request.getPatientId() + ".");
            }
            case 6 -> list(service.requests());
            case 7 -> choice = 0;
            default -> show("Invalid menu choice.");
        }
    }
    private Patient choosePatient(List<Patient> patients) throws IOException {
        if (patients.isEmpty()) { show("No patients. Hospital staff must create a patient first."); return null; }
        for (int i = 0; i < patients.size(); i++) show((i + 1) + ". " + patients.get(i).getDetails());
        int index = number("Choose patient number") - 1;
        Validation.require(index >= 0 && index < patients.size(), "Invalid patient selection.");
        return patients.get(index);
    }
    private BloodRequest chooseRequest(List<BloodRequest> requests) throws IOException {
        if (requests.isEmpty()) { show("No requests available."); return null; }
        for (int i = 0; i < requests.size(); i++) show((i + 1) + ". " + requests.get(i));
        int index = number("Choose request number") - 1;
        Validation.require(index >= 0 && index < requests.size(), "Invalid request selection.");
        return requests.get(index);
    }
    public void hospitalMenu() throws IOException { staffMenu(); }
    public void adminMenu() throws IOException {
        show("\nBLOOD BANK ADMIN\n1. View Donations\n2. Approve Passed Donation\n3. View Blood Bank Inventory\n4. View My Audit History\n5. Logout");
        choice = number("Choice");
        switch (choice) {
            case 1 -> {
                List<BloodDonation> donations = service.donations();
                if (donations.isEmpty()) { show("No donations."); }
                else donations.forEach(d -> show(d + (d.getStatus().equals("TEST_PASSED") ? "  << NEEDS APPROVAL >>" : "  [No approval needed]")));
            }
            case 2 -> {
                var passed = service.donations().stream().filter(d -> d.getStatus().equals("TEST_PASSED")).toList();
                if (passed.isEmpty()) {
                    show("No passed donations awaiting approval. Create a new donation first.");
                    return;
                }
                list(passed);
                service.registerDonation(read("Donation ID to approve"));
                show("Donation approved and registered into usable inventory.");
            }
            case 3 -> list(service.inventoryRows());
            case 4 -> list(service.audit());
            case 5 -> choice = 0;
            default -> show("Invalid menu choice.");
        }
    }
    private void updateProfile() throws IOException {
        Person person = service.currentUser();
        show(person.getDetails());
        service.updateProfile(read("Name"), number("Age"), read("Gender"), read("Phone (10 digits)"), read("Address"));
        if (person instanceof Donor && read("Update haemoglobin and weight? (yes/no)").equalsIgnoreCase("yes")) {
            service.updateMeasurements(decimal("Haemoglobin"), decimal("Weight (kg)"));
        }
        show("Profile saved.");
    }
}
