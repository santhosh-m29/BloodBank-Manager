package bloodbank;

public class BloodDonation extends Transaction {
    private String donorId;
    private String bloodGroup;
    private int quantity;

    public BloodDonation(String transactionId, String transactionDate, String status,
                         String donorId, String bloodGroup, int quantity) {
        super(transactionId, transactionDate, status);
        this.donorId = donorId;
        this.bloodGroup = bloodGroup;
        this.quantity = quantity;
    }

    public String getDonorId() { return donorId; }
    public void setDonorId(String donorId) { this.donorId = donorId; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    @Override
    public void executeTransaction() {
        this.status = "Completed";
        System.out.println("Donation recorded successfully and status set to Completed.");
    }

    @Override
    public void cancelTransaction() {
        this.status = "Cancelled";
        System.out.println("Donation transaction cancelled.");
    }

    @Override
    public void displayTransaction() {
        System.out.println("----------------------------------------");
        System.out.println("Blood Donation Details:");
        System.out.println("Donation ID (Txn ID): " + transactionId);
        System.out.println("Date: " + transactionDate);
        System.out.println("Status: " + status);
        System.out.println("Donor ID: " + donorId);
        System.out.println("Blood Group: " + bloodGroup);
        System.out.println("Quantity: " + quantity + " units");
        System.out.println("----------------------------------------");
    }

    public void recordDonation() {
        executeTransaction();
    }

    public void updateInventory() {
        System.out.println("Updating inventory with donated blood unit...");
    }
}
