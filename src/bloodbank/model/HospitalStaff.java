package bloodbank.model;

import bloodbank.Main;
import bloodbank.utility.Validation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class HospitalStaff extends Staff {
    private String department;

    public HospitalStaff(String personId, String name, int age, String gender, String phoneNumber, String address,
                         String username, String password, String role, String employeeId, String facilityId,
                         String department) {
        super(personId, name, age, gender, phoneNumber, address, username, password, role, employeeId, facilityId);
        this.department = department;
    }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public Patient createPatient(Scanner scanner) {
        System.out.println("\n--- Register Patient at " + facilityId + " ---");
        String name = ask(scanner, "Enter Full Name: ");
        int age = askInt(scanner, "Enter Age: ");
        String gender = ask(scanner, "Enter Gender: ");
        String phone = ask(scanner, "Enter Phone Number: ");
        while (!Validation.validatePhoneNumber(phone)) {
            System.out.println("Invalid phone (must be 10 digits).");
            phone = ask(scanner, "Enter Phone Number: ");
        }
        String address = ask(scanner, "Enter Address: ");
        String username = ask(scanner, "Choose Username: ");
        while (Main.loginManager.isUsernameTaken(username)) {
            System.out.println("Username is already taken. Try another.");
            username = ask(scanner, "Choose Username: ");
        }
        String password = ask(scanner, "Choose Password: ");
        String bloodGroup = ask(scanner, "Blood Group Required (e.g. B+, AB-): ");
        while (!Validation.validateBloodGroup(bloodGroup)) {
            System.out.println("Invalid blood group.");
            bloodGroup = ask(scanner, "Blood Group: ");
        }
        String disease = ask(scanner, "Disease/Reason for Request: ");
        String doctor = ask(scanner, "Referring Doctor: ");
        int units = askInt(scanner, "Units Required: ");

        String id = "PAT_" + System.currentTimeMillis();
        Patient patient = new Patient(id, name, age, gender, phone, address, username, password,
                "PATIENT", bloodGroup, disease, doctor, units, facilityId);
        Main.patients.add(patient);
        Main.savePatientsToFile();
        System.out.println("Patient registered at " + facilityId + ". Patient ID: " + id);
        return patient;
    }

    public BloodRequest requestBlood(Scanner scanner) {
        System.out.println("\n--- Create Blood Request ---");
        System.out.println("Patients registered at this hospital:");
        ArrayList<Patient> hospitalPatients = new ArrayList<>();
        for (Patient patient : Main.patients) {
            if (patient.getHospitalId().equals(facilityId)) {
                hospitalPatients.add(patient);
                System.out.println(patient.getPersonId() + " - " + patient.getName()
                        + " - Blood Group: " + patient.getBloodGroup());
            }
        }
        if (hospitalPatients.isEmpty()) {
            System.out.println("No patients are registered here yet. Register a patient first.");
            return null;
        }
        String patientId = ask(scanner, "Enter Patient ID: ");
        Patient selected = null;
        for (Patient patient : hospitalPatients) {
            if (patient.getPersonId().equals(patientId)) selected = patient;
        }
        if (selected == null) {
            System.out.println("Error: Patient not found.");
            return null;
        }

        System.out.println("Blood group required: " + selected.getBloodGroup());
        int quantity = askInt(scanner, "Enter Units Required: ");
        if (quantity <= 0) {
            System.out.println("Quantity must be positive.");
            return null;
        }
        System.out.println("Urgency: 1. LOW  2. NORMAL  3. HIGH  4. CRITICAL");
        int urgencyChoice = askInt(scanner, "Choose urgency (1-4): ");
        while (urgencyChoice < 1 || urgencyChoice > 4) {
            System.out.println("Choose a number from 1 to 4.");
            urgencyChoice = askInt(scanner, "Choose urgency (1-4): ");
        }
        String urgency = "NORMAL";
        if (urgencyChoice == 1) urgency = "LOW";
        else if (urgencyChoice == 3) urgency = "HIGH";
        else if (urgencyChoice == 4) urgency = "CRITICAL";

        BloodRequest request = new BloodRequest("REQ_" + System.currentTimeMillis(),
                LocalDate.now().toString(), "Pending", selected.getPersonId(), facilityId,
                selected.getBloodGroup(), quantity, urgency);
        Hospital hospital = Hospital.findById(facilityId);
        int localStock = hospital == null ? 0
                : hospital.getInventory().getStockForGroup(selected.getBloodGroup());
        if (localStock >= quantity) request.setStatus("READY");
        Main.requests.add(request);
        if ("READY".equalsIgnoreCase(request.getStatus())) {
            System.out.println("Local inventory has enough stock. The request is ready to issue.");
        } else {
            System.out.println("Local stock: " + localStock
                    + " units. The request needs blood bank approval for the shortage.");
        }
        Main.saveRequestsToFile();
        System.out.println("Blood Request submitted. Request ID: " + request.getTransactionId()
                + " | Status: " + request.getStatus());
        return request;
    }

    public void issueBlood(Scanner scanner) {
        Hospital hospital = Hospital.findById(facilityId);
        if (hospital == null) return;
        System.out.println("\n--- Issue Blood to Patient ---");
        System.out.println("Select ready blood request for hospital " + facilityId + ":");
        List<BloodRequest> readyRequests = new ArrayList<>();
        for (BloodRequest request : Main.requests) {
            if (request.getHospitalId().equals(facilityId)
                    && "READY".equalsIgnoreCase(request.getStatus())) {
                readyRequests.add(request);
            }
        }
        if (readyRequests.isEmpty()) {
            System.out.println("No ready requests found for this hospital.");
            return;
        }
        for (int i = 0; i < readyRequests.size(); i++) {
            BloodRequest request = readyRequests.get(i);
            System.out.println((i + 1) + ". Request " + request.getTransactionId()
                    + " (Patient ID: " + request.getPatientId() + ") - Group: "
                    + request.getBloodGroup() + " | Units: " + request.getUnitsRequested());
        }
        int index = askInt(scanner, "Select Request (1-" + readyRequests.size() + "): ") - 1;
        if (index < 0 || index >= readyRequests.size()) {
            System.out.println("Invalid selection.");
            return;
        }
        BloodRequest request = readyRequests.get(index);
        int available = hospital.getInventory().getStockForGroup(request.getBloodGroup());
        if (available < request.getUnitsRequested()) {
            int stock = hospital.getInventory().getStockForGroup(request.getBloodGroup());
            System.out.println("Error: Insufficient local inventory of " + request.getBloodGroup()
                    + ". Local stock: " + stock + " units. Requested: " + request.getUnitsRequested());
            return;
        }
        int remaining = request.getUnitsRequested();
        BloodUnit[] units = hospital.getInventory().searchBloodGroup(request.getBloodGroup());
        for (BloodUnit unit : units) {
            if (remaining <= 0) break;
            if (unit.getQuantity() <= remaining) {
                remaining -= unit.getQuantity();
                unit.setStatus("ISSUED");
            } else {
                unit.updateQuantity(unit.getQuantity() - remaining);
                remaining = 0;
            }
        }
        request.setStatus("COMPLETED");
        System.out.println("Successfully issued " + request.getUnitsRequested() + " units of "
                + request.getBloodGroup() + " to Patient " + request.getPatientId());
        Main.saveRequestsToFile();
        Main.saveBloodUnitsToFile();
    }

    public BloodRequest requestRestock(Scanner scanner) {
        System.out.println("\n--- Request Restock from Blood Bank ---");
        String bloodGroup = ask(scanner, "Blood Group to Restock: ");
        while (!Validation.validateBloodGroup(bloodGroup)) {
            System.out.println("Invalid blood group.");
            bloodGroup = ask(scanner, "Blood Group to Restock: ");
        }
        int quantity = askInt(scanner, "Units to Request: ");
        if (quantity <= 0) {
            System.out.println("Units must be positive.");
            return null;
        }
        BloodRequest request = new BloodRequest("REQ_RESTOCK_" + System.currentTimeMillis(),
                LocalDate.now().toString(), "Pending", "HOSPITAL", facilityId,
                bloodGroup, quantity, "NORMAL");
        Main.requests.add(request);
        Main.saveRequestsToFile();
        System.out.println("Restock request sent to central Blood Bank. ID: " + request.getTransactionId());
        return request;
    }

    private String ask(Scanner scanner, String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private int askInt(Scanner scanner, String prompt) {
        while (true) {
            try {
                return Integer.parseInt(ask(scanner, prompt));
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter an integer.");
            }
        }
    }

    @Override
    public void updateDetails() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Updating details for Hospital Staff: " + name);
        System.out.print("Enter New Name (Current: " + name + "): ");
        String newName = sc.nextLine().trim();
        if (!newName.isEmpty()) this.name = newName;

        System.out.print("Enter New Age (Current: " + age + "): ");
        String ageStr = sc.nextLine().trim();
        if (!ageStr.isEmpty()) {
            try {
                this.age = Integer.parseInt(ageStr);
            } catch (NumberFormatException ignored) {}
        }

        System.out.print("Enter New Gender (Current: " + gender + "): ");
        String newGender = sc.nextLine().trim();
        if (!newGender.isEmpty()) this.gender = newGender;

        System.out.print("Enter New Phone (Current: " + phoneNumber + "): ");
        String phone = sc.nextLine().trim();
        if (!phone.isEmpty() && Validation.validatePhoneNumber(phone)) {
            this.phoneNumber = phone;
        }

        System.out.print("Enter New Address (Current: " + address + "): ");
        String newAddr = sc.nextLine().trim();
        if (!newAddr.isEmpty()) this.address = newAddr;

        System.out.print("Enter New Department (Current: " + department + "): ");
        String newDept = sc.nextLine().trim();
        if (!newDept.isEmpty()) this.department = newDept;
        
        System.out.println("Hospital staff details updated successfully.");
    }

    @Override
    public void displayDetails() {
        System.out.println("----------------------------------------");
        System.out.println("Hospital Staff Details:");
        System.out.println("ID: " + personId);
        System.out.println("Name: " + name);
        System.out.println("Age: " + age);
        System.out.println("Gender: " + gender);
        System.out.println("Phone: " + phoneNumber);
        System.out.println("Address: " + address);
        System.out.println("Employee ID: " + employeeId);
        System.out.println("Hospital ID (Facility): " + facilityId);
        System.out.println("Department: " + department);
        System.out.println("----------------------------------------");
    }

}
