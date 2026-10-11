package bloodbank.model;

import bloodbank.Main;
import bloodbank.utility.Validation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BloodBankAdmin extends Staff {
    public void approvePassedDonations(Scanner scanner) {
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
        int index = readInt(scanner, "Select donation (1-" + pendingDonations.size() + "): ") - 1;
        if (index < 0 || index >= pendingDonations.size()) {
            System.out.println("Invalid selection.");
            return;
        }

        BloodDonation donation = pendingDonations.get(index);
        String answer = ask(scanner, "Approve this passed donation? (YES/NO): ");
        while (!"YES".equalsIgnoreCase(answer) && !"NO".equalsIgnoreCase(answer)) {
            System.out.println("Please enter YES or NO.");
            answer = ask(scanner, "Approve this passed donation? (YES/NO): ");
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

    private ArrayList<BloodRequest> getRequestsForReview(boolean restockRequests) {
        ArrayList<BloodRequest> requests = new ArrayList<>();
        for (BloodRequest request : Main.requests) {
            if ("Pending".equalsIgnoreCase(request.getStatus())
                    && request.isHospitalRestockRequest() == restockRequests) {
                requests.add(request);
            }
        }
        for (int i = 0; i < requests.size(); i++) {
            for (int j = i + 1; j < requests.size(); j++) {
                if (requests.get(j).getUrgencyRank() < requests.get(i).getUrgencyRank()) {
                    BloodRequest temporary = requests.get(i);
                    requests.set(i, requests.get(j));
                    requests.set(j, temporary);
                }
            }
        }
        return requests;
    }

    public boolean approveRequest(BloodRequest request) {
        Hospital hospital = null;
        for (Hospital item : Main.hospitals) {
            if (item.getOrganizationId().equals(request.getHospitalId())) {
                hospital = item;
                break;
            }
        }
        int localStock = hospital == null ? 0 : hospital.getInventory().getStockForGroup(request.getBloodGroup());
        int centralUnitsNeeded = request.isHospitalRestockRequest()
                ? request.getUnitsRequested()
                : Math.max(0, request.getUnitsRequested() - localStock);
        if (Main.inventory.getStockForGroup(request.getBloodGroup()) < centralUnitsNeeded) {
            return false;
        }
        request.approveRequest();
        return true;
    }

    public void rejectRequest(BloodRequest request) {
        request.rejectRequest();
    }

    public void reviewPatientRequests(Scanner scanner) {
        reviewRequests(scanner, false);
    }

    public void reviewHospitalRestockRequests(Scanner scanner) {
        reviewRequests(scanner, true);
    }

    private void reviewRequests(Scanner scanner, boolean restockRequests) {
        System.out.println(restockRequests
                ? "\n--- Approve/Reject Hospital Stock Requests ---"
                : "\n--- Approve/Reject Patient Blood Requests ---");
        List<BloodRequest> requests = getRequestsForReview(restockRequests);
        if (requests.isEmpty()) {
            System.out.println(restockRequests ? "No hospital stock requests need review." : "No patient requests need review.");
            return;
        }
        for (int i = 0; i < requests.size(); i++) {
            BloodRequest request = requests.get(i);
            System.out.println((i + 1) + ". ID: " + request.getTransactionId()
                    + " | Status: " + request.getStatus() + " | Urgency: " + request.getUrgency()
                    + " | Hospital: " + request.getHospitalId()
                    + (request.isHospitalRestockRequest() ? " | Request: Hospital stock" : " | Patient: " + request.getPatientId())
                    + " | Group: " + request.getBloodGroup() + " | Units: " + request.getUnitsRequested());
        }
        int index = readInt(scanner, "Select request (1-" + requests.size() + "): ") - 1;
        if (index < 0 || index >= requests.size()) {
            System.out.println("Invalid selection.");
            return;
        }
        BloodRequest request = requests.get(index);
        if ("Pending".equalsIgnoreCase(request.getStatus())) {
            String answer = ask(scanner, "Approve this request? (YES/NO): ");
            while (!"YES".equalsIgnoreCase(answer) && !"NO".equalsIgnoreCase(answer)) {
                System.out.println("Please enter YES or NO.");
                answer = ask(scanner, "Approve this request? (YES/NO): ");
            }
            if ("NO".equalsIgnoreCase(answer)) {
                rejectRequest(request);
                Main.saveRequestsToFile();
                return;
            }
            if (!approveRequest(request)) {
                System.out.println("There is not enough central stock. The request remains pending.");
                return;
            }
            Main.saveRequestsToFile();
        }
    }

    public void dispatchPatientTransfer(Scanner scanner) {
        dispatchTransfer(scanner, false);
    }

    public void dispatchHospitalStockTransfer(Scanner scanner) {
        dispatchTransfer(scanner, true);
    }

    private void dispatchTransfer(Scanner scanner, boolean restockTransfer) {
        System.out.println(restockTransfer
                ? "\n--- Dispatch Hospital Stock Transfer ---"
                : "\n--- Dispatch Patient Blood Transfer ---");
        ArrayList<BloodRequest> approvedRequests = new ArrayList<>();
        for (BloodRequest request : Main.requests) {
            if ("APPROVED".equalsIgnoreCase(request.getStatus())
                    && request.isHospitalRestockRequest() == restockTransfer) approvedRequests.add(request);
        }
        if (approvedRequests.isEmpty()) {
            System.out.println(restockTransfer
                    ? "No approved hospital stock requests are waiting for transfer."
                    : "No approved patient requests are waiting for transfer.");
            return;
        }
        for (int i = 0; i < approvedRequests.size(); i++) {
            BloodRequest request = approvedRequests.get(i);
            System.out.println((i + 1) + ". ID: " + request.getTransactionId()
                    + (restockTransfer ? " | Hospital stock restock" : " | Patient: " + request.getPatientId())
                    + " | Hospital: " + request.getHospitalId()
                    + " | Group: " + request.getBloodGroup() + " | Units: " + request.getUnitsRequested());
        }
        int index = readInt(scanner, "Select approved request to dispatch (1-" + approvedRequests.size() + "): ") - 1;
        if (index < 0 || index >= approvedRequests.size()) {
            System.out.println("Invalid selection.");
            return;
        }
        dispatchApprovedRequest(scanner, approvedRequests.get(index));
    }

    private void dispatchApprovedRequest(Scanner scanner, BloodRequest request) {
        String answer = ask(scanner, "Dispatch this approved request now? (YES/NO): ");
        while (!"YES".equalsIgnoreCase(answer) && !"NO".equalsIgnoreCase(answer)) {
            System.out.println("Please enter YES or NO.");
            answer = ask(scanner, "Dispatch this approved request now? (YES/NO): ");
        }
        if ("NO".equalsIgnoreCase(answer)) {
            System.out.println("Request remains approved for later dispatch.");
            return;
        }
        Hospital destination = Hospital.findById(request.getHospitalId());
        if (destination == null) {
            System.out.println("Error: Destination hospital not found.");
            return;
        }
        BloodBank source = Main.bloodBanks.isEmpty()
                ? new BloodBank("CENTRAL_BB", "Central Blood Bank", "", "", "", Main.inventory)
                : Main.bloodBanks.get(0);
        source.transferBloodUnits(request, destination);
        Main.saveRequestsToFile();
        Main.saveTransfersToFile();
        Main.saveBloodUnitsToFile();
    }

    public void viewHospitalRestockHistory(Scanner scanner) {
        if (Main.hospitals.isEmpty()) {
            System.out.println("No hospitals are registered.");
            return;
        }
        for (int i = 0; i < Main.hospitals.size(); i++) {
            Hospital hospital = Main.hospitals.get(i);
            System.out.println((i + 1) + ". " + hospital.getOrganizationId() + " - " + hospital.getOrganizationName());
        }
        int selected = readInt(scanner, "Select hospital: ") - 1;
        if (selected < 0 || selected >= Main.hospitals.size()) {
            System.out.println("Invalid selection.");
            return;
        }
        Main.hospitals.get(selected).viewRestockHistory();
    }

    public void manageFacilities(Scanner scanner) {
        System.out.println("\n--- Manage Hospitals ---");
        System.out.println("1. View Registered Hospitals");
        System.out.println("2. Register New Hospital");
        int choice = readInt(scanner, "Enter choice (1-2): ");
        if (choice == 1) {
            for (Hospital hospital : Main.hospitals) hospital.displayOrganization();
        } else if (choice == 2) {
            String id = Main.nextId("HOSP");
            String name = ask(scanner, "Hospital Name: ");
            String address = ask(scanner, "Address: ");
            String contact = ask(scanner, "Contact Number: ");
            String type = ask(scanner, "Type (Private/Public): ");
            String emergencyContact = ask(scanner, "Emergency Contact: ");
            Main.hospitals.add(new Hospital(id, name, address, contact, type, emergencyContact, new Inventory()));
            Main.saveHospitalsToFile();
            System.out.println("Hospital registered successfully! ID: " + id);
        } else {
            System.out.println("Invalid choice.");
        }
    }

    private String ask(Scanner scanner, String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private int readInt(Scanner scanner, String prompt) {
        while (true) {
            try { return Integer.parseInt(ask(scanner, prompt)); }
            catch (NumberFormatException e) { System.out.println("Invalid input. Please enter an integer."); }
        }
    }

    public BloodBankAdmin(String personId, String name, int age, String gender, String phoneNumber, String address,
                          String username, String password, String role, String employeeId, String facilityId) {
        super(personId, name, age, gender, phoneNumber, address, username, password, role, employeeId, facilityId);
    }

    @Override
    public void updateDetails() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Updating details for Admin: " + name);
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
