package bloodbank.model;

import bloodbank.Main;
import bloodbank.utility.Validation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    public Patient createPatient() {
        System.out.println("\n--- Register Patient at " + facilityId + " ---");
        System.out.print("Enter Full Name: ");
        String name = Main.sc.nextLine().trim();
        System.out.print("Enter Age: ");
        int age;
        try {
            age = Integer.parseInt(Main.sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid age. Patient was not registered.");
            return null;
        }
        System.out.print("Enter Gender: ");
        String gender = Main.sc.nextLine().trim();
        System.out.print("Enter Phone Number: ");
        String phone = Main.sc.nextLine().trim();
        while (!Validation.validatePhoneNumber(phone)) {
            System.out.println("Invalid phone (must be 10 digits).");
            System.out.print("Enter Phone Number: ");
            phone = Main.sc.nextLine().trim();
        }
        System.out.print("Enter Address: ");
        String address = Main.sc.nextLine().trim();
        System.out.print("Choose Username: ");
        String username = Main.sc.nextLine().trim();
        while (Main.loginManager.isUsernameTaken(username)) {
            System.out.println("Username is already taken. Try another.");
            System.out.print("Choose Username: ");
            username = Main.sc.nextLine().trim();
        }
        System.out.print("Choose Password: ");
        String password = Main.sc.nextLine().trim();
        System.out.print("Blood Group Required (e.g. B+, AB-): ");
        String bloodGroup = Main.sc.nextLine().trim();
        while (!Validation.validateBloodGroup(bloodGroup)) {
            System.out.println("Invalid blood group.");
            System.out.print("Blood Group: ");
            bloodGroup = Main.sc.nextLine().trim();
        }
        System.out.print("Disease/Reason for Request: ");
        String disease = Main.sc.nextLine().trim();
        System.out.print("Referring Doctor: ");
        String doctor = Main.sc.nextLine().trim();
        System.out.print("Units Required: ");
        int units;
        try {
            units = Integer.parseInt(Main.sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid number of units. Patient was not registered.");
            return null;
        }

        String id = Main.nextId("PAT");
        Patient patient = new Patient(id, name, age, gender, phone, address, username, password,
                "PATIENT", bloodGroup, disease, doctor, units, facilityId);
        Main.patients.add(patient);
        Main.savePatientsToFile();
        System.out.println("Patient registered at " + facilityId + ". Patient ID: " + id);
        return patient;
    }

    public BloodRequest requestBlood() {
        System.out.println("\n--- Create Blood Request ---");
        System.out.println("Patients registered at this hospital:");
        ArrayList<Patient> hospitalPatients = new ArrayList<>();
        for (Patient patient : Main.patients) {
            if (patient.getHospitalId().equals(facilityId) && !hasCompletedRequest(patient)) {
                hospitalPatients.add(patient);
                System.out.println(patient.getPersonId() + " - " + patient.getName()
                        + " - Blood Group Required: " + patient.getBloodGroup()
                        + " - Units Required: " + patient.getUnitsRequired());
            }
        }
        if (hospitalPatients.isEmpty()) {
            System.out.println("No patients without a completed blood request are available at this hospital.");
            return null;
        }
        System.out.print("Enter Patient ID: ");
        String patientId = Main.sc.nextLine().trim();
        Patient selected = null;
        for (Patient patient : hospitalPatients) {
            if (patient.getPersonId().equals(patientId)) selected = patient;
        }
        if (selected == null) {
            System.out.println("Error: Patient not found.");
            return null;
        }

        System.out.println("Blood group required: " + selected.getBloodGroup());
        int quantity = selected.getUnitsRequired();
        System.out.println("Units requested: " + quantity);
        if (quantity <= 0) {
            System.out.println("Quantity must be positive.");
            return null;
        }
        System.out.println("Urgency: 1. LOW  2. NORMAL  3. HIGH  4. CRITICAL");
        System.out.print("Choose urgency (1-4): ");
        int urgencyChoice;
        try {
            urgencyChoice = Integer.parseInt(Main.sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid urgency. Request was not created.");
            return null;
        }
        while (urgencyChoice < 1 || urgencyChoice > 4) {
            System.out.println("Choose a number from 1 to 4.");
            System.out.print("Choose urgency (1-4): ");
            try {
                urgencyChoice = Integer.parseInt(Main.sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid urgency. Request was not created.");
                return null;
            }
        }
        String urgency = "NORMAL";
        if (urgencyChoice == 1) urgency = "LOW";
        else if (urgencyChoice == 3) urgency = "HIGH";
        else if (urgencyChoice == 4) urgency = "CRITICAL";

        BloodRequest request = new BloodRequest(Main.nextId("REQ"),
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
                    + " units. Submit a separate hospital stock request to refill this blood group.");
        }
        Main.saveRequestsToFile();
        System.out.println("Blood Request submitted. Request ID: " + request.getTransactionId()
                + " | Status: " + request.getStatus());
        return request;
    }

    private boolean hasCompletedRequest(Patient patient) {
        for (BloodRequest request : Main.requests) {
            if (request.getPatientId().equals(patient.getPersonId())
                    && "COMPLETED".equalsIgnoreCase(request.getStatus())) return true;
        }
        return false;
    }

    public void issueBlood() {
        Hospital hospital = Hospital.findById(facilityId);
        if (hospital == null) return;
        System.out.println("\n--- Issue Blood to Patient ---");
        System.out.println("Select ready blood request for hospital " + facilityId + ":");
        List<BloodRequest> readyRequests = new ArrayList<>();
        for (BloodRequest request : Main.requests) {
            if (request.getHospitalId().equals(facilityId)
                    && !request.isHospitalRestockRequest()
                    && "READY".equalsIgnoreCase(request.getStatus())) {
                readyRequests.add(request);
            }
        }
        if (readyRequests.isEmpty()) {
            System.out.println("No ready requests found for this hospital.");
            return;
        }
        for (int i = 0; i < readyRequests.size(); i++) {
            for (int j = i + 1; j < readyRequests.size(); j++) {
                if (readyRequests.get(j).getUrgencyRank() < readyRequests.get(i).getUrgencyRank()) {
                    BloodRequest temporary = readyRequests.get(i);
                    readyRequests.set(i, readyRequests.get(j));
                    readyRequests.set(j, temporary);
                }
            }
        }
        for (int i = 0; i < readyRequests.size(); i++) {
            BloodRequest request = readyRequests.get(i);
            System.out.println((i + 1) + ". Request " + request.getTransactionId()
                    + " (Patient ID: " + request.getPatientId() + ") - Group: "
                    + request.getBloodGroup() + " | Units: " + request.getUnitsRequested()
                    + " | Priority: " + request.getUrgency());
        }
        System.out.print("Select Request (1-" + readyRequests.size() + "): ");
        int index;
        try {
            index = Integer.parseInt(Main.sc.nextLine().trim()) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Invalid selection.");
            return;
        }
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

    public BloodRequest requestRestock() {
        System.out.println("\n--- Request Restock from Blood Bank ---");
        System.out.print("Blood Group to Restock: ");
        String bloodGroup = Main.sc.nextLine().trim();
        while (!Validation.validateBloodGroup(bloodGroup)) {
            System.out.println("Invalid blood group.");
            System.out.print("Blood Group to Restock: ");
            bloodGroup = Main.sc.nextLine().trim();
        }
        System.out.print("Units to Request: ");
        int quantity;
        try {
            quantity = Integer.parseInt(Main.sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid quantity. Request was not created.");
            return null;
        }
        if (quantity <= 0) {
            System.out.println("Units must be positive.");
            return null;
        }
        BloodRequest request = new BloodRequest(Main.nextId("REQ"),
                LocalDate.now().toString(), "Pending", "", facilityId,
                bloodGroup, quantity, "NORMAL");
        Main.requests.add(request);
        Main.saveRequestsToFile();
        System.out.println("Hospital stock request sent to central Blood Bank. ID: " + request.getTransactionId());
        return request;
    }

    @Override
    public void updateDetails() {
        System.out.println("Updating details for Hospital Staff: " + name);
        System.out.print("Enter New Name (Current: " + name + "): ");
        String newName = Main.sc.nextLine().trim();
        if (!newName.isEmpty()) this.name = newName;

        System.out.print("Enter New Age (Current: " + age + "): ");
        String ageStr = Main.sc.nextLine().trim();
        if (!ageStr.isEmpty()) {
            try {
                this.age = Integer.parseInt(ageStr);
            } catch (NumberFormatException ignored) {}
        }

        System.out.print("Enter New Gender (Current: " + gender + "): ");
        String newGender = Main.sc.nextLine().trim();
        if (!newGender.isEmpty()) this.gender = newGender;

        System.out.print("Enter New Phone (Current: " + phoneNumber + "): ");
        String phone = Main.sc.nextLine().trim();
        if (!phone.isEmpty() && Validation.validatePhoneNumber(phone)) {
            this.phoneNumber = phone;
        }

        System.out.print("Enter New Address (Current: " + address + "): ");
        String newAddr = Main.sc.nextLine().trim();
        if (!newAddr.isEmpty()) this.address = newAddr;

        System.out.print("Enter New Department (Current: " + department + "): ");
        String newDept = Main.sc.nextLine().trim();
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
