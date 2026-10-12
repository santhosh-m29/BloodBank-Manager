package bloodbank.model;

import bloodbank.Main;
import bloodbank.utility.Validation;

import java.time.LocalDate;
import java.util.ArrayList;

public class BloodBankAdmin extends Staff<BloodBankAdmin> {
    public void approvePassedDonations() {
        ArrayList<BloodDonation> pendingDonations = new ArrayList<>();
        for (BloodDonation donation : Main.donations) {
            if ("PENDING_APPROVAL".equalsIgnoreCase(donation.getStatus())) {
                pendingDonations.add(donation);
            }
        }

        System.out.println("\n--- Passed Donations Waiting for Approval ---");
        if (pendingDonations.isEmpty()) {
            System.out.println("No passed donations are waiting for approval.");
            return;
        }
        for (int i = 0; i < pendingDonations.size(); i++) {
            BloodDonation donation = pendingDonations.get(i);
            System.out.println((i + 1) + ". " + donation.getTransactionId()
                    + " | Donor: " + donation.getDonorId()
                    + " | Group: " + donation.getBloodGroup()
                    + " | Units: " + donation.getQuantity());
        }
        System.out.print("Select donation: ");
        int index;
        try {
            index = Integer.parseInt(Main.sc.nextLine().trim()) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Invalid selection.");
            return;
        }
        if (index < 0 || index >= pendingDonations.size()) {
            System.out.println("Invalid selection.");
            return;
        }

        BloodDonation donation = pendingDonations.get(index);
        System.out.print("Approve this passed donation? (YES/NO): ");
        String answer = Main.sc.nextLine().trim();
        while (!"YES".equalsIgnoreCase(answer) && !"NO".equalsIgnoreCase(answer)) {
            System.out.println("Please enter YES or NO.");
            System.out.print("Approve this passed donation? (YES/NO): ");
            answer = Main.sc.nextLine().trim();
        }
        if ("NO".equalsIgnoreCase(answer)) {
            donation.setStatus("REJECTED");
            System.out.println("Donation rejected. No blood was added to central stock.");
        } else {
            LocalDate collectionDate = LocalDate.parse(donation.getTransactionDate());
            BloodUnit unit = new BloodUnit(donation.getBloodUnitId(), donation.getBloodGroup(),
                    donation.getQuantity(), donation.getTransactionDate(),
                    collectionDate.plusDays(42).toString(), "AVAILABLE");
            Main.inventory.addBloodUnit(unit);
            donation.setStatus("COMPLETED");
            System.out.println("Donation approved. Blood was added to central stock.");
        }
        Main.saveDonationsToFile();
        Main.saveBloodUnitsToFile();
    }

    private ArrayList<BloodRequest> getPendingRestockRequests() {
        ArrayList<BloodRequest> requests = new ArrayList<>();
        for (BloodRequest request : Main.requests) {
            if ("Pending".equalsIgnoreCase(request.getStatus())
                    && request.isHospitalRestockRequest()) {
                requests.add(request);
            }
        }
        return requests;
    }

    public void reviewHospitalRestockRequests() {
        System.out.println("\n--- Approve Hospital Stock Requests ---");
        ArrayList<BloodRequest> requests = getPendingRestockRequests();
        if (requests.isEmpty()) {
            System.out.println("No hospital stock requests need review.");
            return;
        }
        for (int i = 0; i < requests.size(); i++) {
            BloodRequest request = requests.get(i);
            System.out.println((i + 1) + ". ID: " + request.getTransactionId()
                    + " | Hospital: " + request.getHospitalId()
                    + " | Group: " + request.getBloodGroup() + " | Units: " + request.getUnitsRequested());
        }
        System.out.print("Select request (1-" + requests.size() + "): ");
        int index;
        try {
            index = Integer.parseInt(Main.sc.nextLine().trim()) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Invalid selection.");
            return;
        }
        if (index < 0 || index >= requests.size()) {
            System.out.println("Invalid selection.");
            return;
        }
        BloodRequest request = requests.get(index);
        System.out.print("Approve this hospital stock request? (YES/NO): ");
        String answer = Main.sc.nextLine().trim();
        while (!"YES".equalsIgnoreCase(answer) && !"NO".equalsIgnoreCase(answer)) {
            System.out.println("Please enter YES or NO.");
            System.out.print("Approve this hospital stock request? (YES/NO): ");
            answer = Main.sc.nextLine().trim();
        }
        if ("NO".equalsIgnoreCase(answer)) {
            request.rejectRequest();
            Main.saveRequestsToFile();
            return;
        }
        if (Main.inventory.getStockForGroup(request.getBloodGroup()) < request.getUnitsRequested()) {
            System.out.println("There is not enough central stock. The request remains pending.");
            return;
        }
        Hospital destination = Hospital.findById(request.getHospitalId());
        if (destination == null) {
            System.out.println("Error: Destination hospital not found. The request remains pending.");
            return;
        }
        request.approveRequest();
        BloodBank source = Main.bloodBanks.get(0);
        if (!source.transferBloodUnits(request, destination)) {
            request.setStatus("Pending");
            return;
        }
        Main.saveRequestsToFile();
        Main.saveTransfersToFile();
        Main.saveBloodUnitsToFile();
    }

    public void viewHospitalRestockHistory() {
        if (Main.hospitals.isEmpty()) {
            System.out.println("No hospitals are registered.");
            return;
        }
        for (int i = 0; i < Main.hospitals.size(); i++) {
            Hospital hospital = Main.hospitals.get(i);
            System.out.println((i + 1) + ". " + hospital.getOrganizationId() + " - " + hospital.getOrganizationName());
        }
        System.out.print("Select hospital: ");
        int selected;
        try {
            selected = Integer.parseInt(Main.sc.nextLine().trim()) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Invalid selection.");
            return;
        }
        if (selected < 0 || selected >= Main.hospitals.size()) {
            System.out.println("Invalid selection.");
            return;
        }
        Main.hospitals.get(selected).viewRestockHistory();
    }

    public void manageFacilities() {
        System.out.println("\n--- Manage Hospitals ---");
        System.out.println("1. View Registered Hospitals");
        System.out.println("2. Register New Hospital");
        System.out.print("Enter choice (1-2): ");
        int choice;
        try {
            choice = Integer.parseInt(Main.sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid choice.");
            return;
        }
        if (choice == 1) {
            for (Hospital hospital : Main.hospitals) hospital.displayOrganization();
        } else if (choice == 2) {
            String id = Main.nextId("HOSP");
            System.out.print("Hospital Name: ");
            String name = Main.sc.nextLine().trim();
            System.out.print("Address: ");
            String address = Main.sc.nextLine().trim();
            System.out.print("Contact Number: ");
            String contact = Main.sc.nextLine().trim();
            System.out.print("Type (Private/Public): ");
            String type = Main.sc.nextLine().trim();
            System.out.print("Emergency Contact: ");
            String emergencyContact = Main.sc.nextLine().trim();
            Main.hospitals.add(new Hospital(id, name, address, contact, type, emergencyContact, new Inventory()));
            Main.saveHospitalsToFile();
            System.out.println("Hospital registered successfully! ID: " + id);
        } else {
            System.out.println("Invalid choice.");
        }
    }

    public BloodBankAdmin(String personId, String name, int age, String gender, String phoneNumber, String address,
                          String username, String password, String role, String employeeId, String facilityId) {
        super(personId, name, age, gender, phoneNumber, address, username, password, role, employeeId, facilityId);
    }

    @Override
    public void updateDetails() {
        System.out.println("Updating details for Admin: " + name);
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
        
        System.out.println("Admin details updated successfully.");
    }

    @Override
    public void displayDetails() {
        System.out.println("----------------------------------------");
        System.out.println("Blood Bank Admin Details:");
        System.out.println("ID: " + personId);
        System.out.println("Name: " + name);
        System.out.println("Age: " + age);
        System.out.println("Gender: " + gender);
        System.out.println("Phone: " + phoneNumber);
        System.out.println("Address: " + address);
        System.out.println("Employee ID: " + employeeId);
        System.out.println("Facility ID: " + facilityId);
        System.out.println("----------------------------------------");
    }

}
