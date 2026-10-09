package bloodbank.model;

import bloodbank.Main;

import java.util.ArrayList;

public class Hospital extends Organization {
    private String hospitalType;
    private String emergencyContact;
    private Inventory inventory;
    private ArrayList<HospitalStaff> staffs;

    public Hospital(String organizationId, String organizationName, String address, String contactNumber,
                    String hospitalType, String emergencyContact, Inventory inventory) {
        super(organizationId, organizationName, address, contactNumber);
        this.hospitalType = hospitalType;
        this.emergencyContact = emergencyContact;
        this.inventory = inventory;
        this.staffs = new ArrayList<>();
    }

    public String getHospitalType() { return hospitalType; }
    public void setHospitalType(String hospitalType) { this.hospitalType = hospitalType; }

    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }

    public Inventory getInventory() { return inventory; }
    public void setInventory(Inventory inventory) { this.inventory = inventory; }

    public ArrayList<HospitalStaff> getStaffs() { return staffs; }

    public static Hospital findById(String hospitalId) {
        for (Hospital hospital : Main.hospitals) {
            if (hospital.getOrganizationId().equalsIgnoreCase(hospitalId)) return hospital;
        }
        return null;
    }

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
        System.out.println("Number of Staff: " + staffs.size());
        System.out.println("----------------------------------------");
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
