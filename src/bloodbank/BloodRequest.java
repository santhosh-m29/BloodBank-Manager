package bloodbank;

public class BloodRequest extends Transaction {
    private String recipientId;
    private String hospitalId; // Can be empty or "N/A" if recipient requested directly
    private String bloodGroup;
    private int unitsRequested;

    public BloodRequest(String transactionId, String transactionDate, String status,
                        String recipientId, String hospitalId, String bloodGroup, int unitsRequested) {
        super(transactionId, transactionDate, status);
        this.recipientId = recipientId;
        this.hospitalId = hospitalId;
        this.bloodGroup = bloodGroup;
        this.unitsRequested = unitsRequested;
    }

    public String getRecipientId() { return recipientId; }
    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }

    public String getHospitalId() { return hospitalId; }
    public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public int getUnitsRequested() { return unitsRequested; }
    public void setUnitsRequested(int unitsRequested) { this.unitsRequested = unitsRequested; }

    @Override
    public void executeTransaction() {
        this.status = "Approved";
        System.out.println("Blood request executed/approved.");
    }

    @Override
    public void cancelTransaction() {
        this.status = "Cancelled";
        System.out.println("Blood request cancelled.");
    }

    @Override
    public void displayTransaction() {
        System.out.println("----------------------------------------");
        System.out.println("Blood Request Details:");
        System.out.println("Request ID (Txn ID): " + transactionId);
        System.out.println("Date: " + transactionDate);
        System.out.println("Status: " + status);
        System.out.println("Recipient ID: " + recipientId);
        System.out.println("Hospital ID: " + (hospitalId.isEmpty() ? "N/A" : hospitalId));
        System.out.println("Blood Group Required: " + bloodGroup);
        System.out.println("Units Requested: " + unitsRequested + " units");
        System.out.println("----------------------------------------");
    }

    public void processRequest() {
        System.out.println("Processing blood request...");
    }

    public boolean checkAvailability() {
        // Business logic will delegate to inventory
        return true;
    }

    public void approveRequest() {
        this.status = "Approved";
        System.out.println("Request " + transactionId + " Approved.");
    }

    public void rejectRequest() {
        this.status = "Rejected";
        System.out.println("Request " + transactionId + " Rejected.");
    }
}
