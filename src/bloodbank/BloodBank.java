package bloodbank;

import java.util.Scanner;

public class BloodBank extends Organization {
    private String managerName;
    private Inventory inventory;

    public BloodBank(String organizationId, String organizationName, String address, String contactNumber,
                     String managerName, Inventory inventory) {
        super(organizationId, organizationName, address, contactNumber);
        this.managerName = managerName;
        this.inventory = inventory;
    }

    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }

    public Inventory getInventory() { return inventory; }
    public void setInventory(Inventory inventory) { this.inventory = inventory; }

    @Override
    public void displayOrganization() {
        System.out.println("----------------------------------------");
        System.out.println("Blood Bank Facility Details:");
        System.out.println("ID: " + organizationId);
        System.out.println("Name: " + organizationName);
        System.out.println("Address: " + address);
        System.out.println("Contact Number: " + contactNumber);
        System.out.println("Manager Name: " + managerName);
        System.out.println("Inventory Total Stock: " + inventory.getTotalStock() + " units");
        System.out.println("----------------------------------------");
    }

    @Override
    public void updateOrganization() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Updating details for Blood Bank: " + organizationName);
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

        System.out.print("Enter New Manager Name (Current: " + managerName + "): ");
        String mgr = sc.nextLine().trim();
        if (!mgr.isEmpty()) this.managerName = mgr;
        
        System.out.println("Blood bank details updated.");
    }

    public void addBloodUnit() {
        // Concrete method from diagram
        System.out.println("System Admin or Donation Flow adds blood units directly via Inventory.");
    }

    public void removeBloodUnit() {
        // Concrete method from diagram
        System.out.println("Removing blood unit from inventory.");
    }

    public void transferBlood() {
        // Concrete method from diagram
        System.out.println("Initiating transfer of blood units.");
    }

    public void viewInventory() {
        System.out.println("\n--- Inventory of Blood Bank: " + organizationName + " ---");
        inventory.displayInventory();
    }
}
