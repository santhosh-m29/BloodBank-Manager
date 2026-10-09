package bloodbank.model;

import bloodbank.Main;
import bloodbank.utility.Validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BloodBankAdmin extends Staff {
    public ArrayList<BloodRequest> getRequestsForReview() {
        ArrayList<BloodRequest> requests = new ArrayList<>();
        for (BloodRequest request : Main.requests) {
            if ("Pending".equalsIgnoreCase(request.getStatus())
                    || "APPROVED".equalsIgnoreCase(request.getStatus())) {
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
        int localStock = hospital == null ? 0
                : hospital.getInventory().getStockForGroup(request.getBloodGroup());
        int shortage = Math.max(0, request.getUnitsRequested() - localStock);
        if (Main.inventory.getStockForGroup(request.getBloodGroup()) < shortage) {
            return false;
        }
        request.approveRequest();
        return true;
    }

    public void rejectRequest(BloodRequest request) {
        request.rejectRequest();
    }

    public void reviewRequestsAndDispatch(Scanner scanner) {
        System.out.println("\n--- Approve/Reject Requests / Dispatch Transfer ---");
        List<BloodRequest> requests = getRequestsForReview();
        if (requests.isEmpty()) {
            System.out.println("No requests need admin action.");
            return;
        }
        for (int i = 0; i < requests.size(); i++) {
            BloodRequest request = requests.get(i);
            System.out.println((i + 1) + ". ID: " + request.getTransactionId()
                    + " | Status: " + request.getStatus() + " | Urgency: " + request.getUrgency()
                    + " | Hospital: " + request.getHospitalId() + " | Patient: " + request.getPatientId()
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
        dispatchTransfer(scanner, request);
    }

    public void dispatchTransfer(Scanner scanner, BloodRequest request) {
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

    public void removeBloodUnit(Scanner scanner) {
        System.out.println("\n--- Remove Blood Unit ---");
        String unitId = ask(scanner, "Enter Blood Unit ID to remove: ");
        boolean removed = Main.inventory.removeBloodUnit(unitId);
        if (!removed) {
            for (Hospital hospital : Main.hospitals) {
                if (hospital.getInventory().removeBloodUnit(unitId)) {
                    removed = true;
                    break;
                }
            }
        }
        if (removed) {
            Main.saveBloodUnitsToFile();
            System.out.println("Blood unit removed successfully.");
        } else {
            System.out.println("Unit ID not found.");
        }
    }

    public void manageFacilities(Scanner scanner) {
        System.out.println("\n--- Facility Registry ---");
        System.out.println("1. View Registered Blood Banks");
        System.out.println("2. View Registered Hospitals");
        System.out.println("3. Register New Hospital");
        System.out.println("4. Register New Blood Bank");
        int choice = readInt(scanner, "Enter choice (1-4): ");
        if (choice == 1) {
            for (BloodBank bank : Main.bloodBanks) bank.displayOrganization();
        } else if (choice == 2) {
            for (Hospital hospital : Main.hospitals) hospital.displayOrganization();
        } else if (choice == 3) {
            String id = "HOSP_" + System.currentTimeMillis();
            String name = ask(scanner, "Hospital Name: ");
            String address = ask(scanner, "Address: ");
            String contact = ask(scanner, "Contact Number: ");
            String type = ask(scanner, "Type (Private/Public): ");
            String emergencyContact = ask(scanner, "Emergency Contact: ");
            Main.hospitals.add(new Hospital(id, name, address, contact, type, emergencyContact, new Inventory()));
            Main.saveHospitalsToFile();
            System.out.println("Hospital registered successfully! ID: " + id);
        } else if (choice == 4) {
            String id = "BB_" + System.currentTimeMillis();
            String name = ask(scanner, "Blood Bank Name: ");
            String address = ask(scanner, "Address: ");
            String contact = ask(scanner, "Contact Number: ");
            String manager = ask(scanner, "Manager Name: ");
            Main.bloodBanks.add(new BloodBank(id, name, address, contact, manager, Main.inventory));
            Main.saveBloodBanksToFile();
            System.out.println("Blood Bank registered successfully! ID: " + id);
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
