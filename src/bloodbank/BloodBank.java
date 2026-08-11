package bloodbank;

public class BloodBank extends Organization {
    private String managerName;
    private int totalBloodUnits;

    public BloodBank(String organizationId, String organizationName, String address, String contactNumber,
                     String managerName, int totalBloodUnits) {
        super(organizationId, organizationName, address, contactNumber);
        this.managerName = managerName;
        this.totalBloodUnits = totalBloodUnits;
    }

    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }

    public int getTotalBloodUnits() { return totalBloodUnits; }
    public void setTotalBloodUnits(int totalBloodUnits) { this.totalBloodUnits = totalBloodUnits; }

    @Override
    public void displayOrganization() {
        System.out.println("----------------------------------------");
        System.out.println("Blood Bank Details:");
        System.out.println("ID: " + organizationId);
        System.out.println("Name: " + organizationName);
        System.out.println("Address: " + address);
        System.out.println("Contact No: " + contactNumber);
        System.out.println("Manager Name: " + managerName);
        System.out.println("Total Stock: " + totalBloodUnits + " units");
        System.out.println("----------------------------------------");
    }

    @Override
    public void updateOrganization(String organizationName, String address, String contactNumber) {
        this.organizationName = organizationName;
        this.address = address;
        this.contactNumber = contactNumber;
    }

    public void updateManager(String managerName) {
        this.managerName = managerName;
    }

    public void addBloodUnit() {
        System.out.println("Adding blood units to inventory...");
    }

    public void removeBloodUnit() {
        System.out.println("Removing blood units from inventory...");
    }

    public void transferBlood() {
        System.out.println("Transferring blood units...");
    }

    public void viewInventory() {
        System.out.println("Displaying inventory details for blood bank: " + organizationName);
    }
}
