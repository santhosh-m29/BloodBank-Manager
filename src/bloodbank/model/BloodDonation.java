package bloodbank.model;

public class BloodDonation extends Transaction {
    private String donorId;
    private String bloodGroup;
    private int quantity;
    private String bloodUnitId;

    public BloodDonation(String transactionId, String transactionDate, String status,
                         String donorId, String bloodGroup, int quantity, String bloodUnitId) {
        super(transactionId, transactionDate, status);
        this.donorId = donorId;
        this.bloodGroup = bloodGroup;
        this.quantity = quantity;
        this.bloodUnitId = bloodUnitId;
    }

    public String getDonorId() { return donorId; }
    public void setDonorId(String donorId) { this.donorId = donorId; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getBloodUnitId() { return bloodUnitId; }
    public void setBloodUnitId(String bloodUnitId) { this.bloodUnitId = bloodUnitId; }

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
        if (bloodUnitId != null && !bloodUnitId.isEmpty()) {
            System.out.println("Blood Unit ID: " + bloodUnitId);
        }
        System.out.println("----------------------------------------");
    }

    public void recordDonation() {
        this.status = "PENDING_TEST";
        System.out.println("Donation " + transactionId + " submitted by Donor: " + donorId + ". Starting lab screening now.");
    }
}
