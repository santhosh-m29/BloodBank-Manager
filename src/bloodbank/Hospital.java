package bloodbank;

import java.util.Scanner;

public class Hospital extends Organization {
    private String hospitalType;
    private String emergencyContact;
    private Inventory inventory;
    private HospitalStaff[] staffs;

    public Hospital(String organizationId, String organizationName, String address, String contactNumber,
                    String hospitalType, String emergencyContact, Inventory inventory) {
        super(organizationId, organizationName, address, contactNumber);
        this.hospitalType = hospitalType;
        this.emergencyContact = emergencyContact;
        this.inventory = inventory;
        this.staffs = new HospitalStaff[0];
    }

    public String getHospitalType() { return hospitalType; }
    public void setHospitalType(String hospitalType) { this.hospitalType = hospitalType; }

    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }

    public Inventory getInventory() { return inventory; }
    public void setInventory(Inventory inventory) { this.inventory = inventory; }

    public HospitalStaff[] getStaffs() { return staffs; }
    public void setStaffs(HospitalStaff[] staffs) { this.staffs = staffs; }

    @Override
    public void displayOrganization() {
        System.out.println("----------------------------------------");
        System.out.println("Hospital Facility Details:");
        System.out.println("ID: " + organizationId);
        System.out.println("Name: " + organizationName);
        System.out.println("Address: " + address);
        System.out.println("Contact Number: " + contactNumber);
        System.out.println("Type: " + hospitalType);
        System.out.println("Emergency Contact: " + emergencyContact);
        System.out.println("Inventory Total Stock: " + inventory.getTotalStock() + " units");
        System.out.println("Number of Staff: " + (staffs != null ? staffs.length : 0));
        System.out.println("----------------------------------------");
    }

    @Override
    public void updateOrganization() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Updating details for Hospital: " + organizationName);
        System.out.print("Enter New Name (Current: " + organizationName + "): ");
        String name = sc.nextLine().trim();
        if (!name.isEmpty()) this.organizationName = name;

        System.out.print("Enter New Address (Current: " + address + "): ");
        String addr = sc.nextLine().trim();
        if (!addr.isEmpty()) this.address = addr;

        System.out.print("Enter New Contact No (Current: " + contactNumber + "): ");
        String phone = sc.nextLine().trim();
        if (!phone.isEmpty() && Validation.validatePhoneNumberStatic(phone)) {
            this.contactNumber = phone;
        }

        System.out.print("Enter New Hospital Type (Current: " + hospitalType + "): ");
        String type = sc.nextLine().trim();
        if (!type.isEmpty()) this.hospitalType = type;

        System.out.print("Enter New Emergency Contact (Current: " + emergencyContact + "): ");
        String ec = sc.nextLine().trim();
        if (!ec.isEmpty() && Validation.validatePhoneNumberStatic(ec)) {
            this.emergencyContact = ec;
        }
        
        System.out.println("Hospital details updated.");
    }

    public void sendBloodRequest() {
        System.out.println("Hospital " + organizationName + " is sending a blood request.");
    }

    public void viewRequestHistory() {
        System.out.println("\n--- Request History for Hospital: " + organizationName + " ---");
        boolean found = false;
        for (BloodRequest req : Main.requests) {
            if (req.getHospitalId().equals(this.organizationId)) {
                req.displayTransaction();
                found = true;
            }
        }
        if (!found) {
            System.out.println("No blood requests submitted by this hospital yet.");
        }
    }

    public void viewInventory() {
        System.out.println("\n--- Local Stock for Hospital: " + organizationName + " ---");
        inventory.displayInventory();
    }
}
