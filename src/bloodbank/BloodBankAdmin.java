package bloodbank;

import java.util.Scanner;

public class BloodBankAdmin extends Staff {
    private String adminLevel; // "SuperAdmin", "StandardAdmin", etc.

    public BloodBankAdmin(String personId, String name, int age, String gender, String phoneNumber, String address,
                          String username, String password, String role, String employeeId, String facilityId,
                          String adminLevel) {
        super(personId, name, age, gender, phoneNumber, address, username, password, role, employeeId, facilityId);
        this.adminLevel = adminLevel;
    }

    public String getAdminLevel() { return adminLevel; }
    public void setAdminLevel(String adminLevel) { this.adminLevel = adminLevel; }

    @Override
    public String getDetails() {
        return "Admin [ID: " + personId + ", Name: " + name + ", Level: " + adminLevel + ", Facility: " + facilityId + "]";
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
        if (!phone.isEmpty() && Validation.validatePhoneNumberStatic(phone)) {
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
        System.out.println("Admin Level: " + adminLevel);
        System.out.println("----------------------------------------");
    }

    @Override
    public void displayStaffDetails() {
        System.out.println("Staff: " + name + " | Employee ID: " + employeeId + " | Facility: " + facilityId + " | Admin Level: " + adminLevel);
    }

    public void registerDonation() {
        // Will be called and processed inside Menu/Workflows
        System.out.println("Registering a new donation...");
    }

    public boolean initiateLabTest() {
        // Will initiate a test flow
        System.out.println("Initiating lab test on blood unit...");
        return true;
    }

    public void approveRequest() {
        System.out.println("Approving blood request...");
    }

    public void dispatchTransfer() {
        System.out.println("Dispatching blood transfer to hospital...");
    }
}
