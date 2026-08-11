package bloodbank;

public class BloodTransfer extends Transaction {
    private String sourceBloodBankId;
    private String destinationBloodBankId;
    private String bloodGroup;
    private int unitsTransferred;

    public BloodTransfer(String transactionId, String transactionDate, String status,
                         String sourceBloodBankId, String destinationBloodBankId, String bloodGroup, int unitsTransferred) {
        super(transactionId, transactionDate, status);
        this.sourceBloodBankId = sourceBloodBankId;
        this.destinationBloodBankId = destinationBloodBankId;
        this.bloodGroup = bloodGroup;
        this.unitsTransferred = unitsTransferred;
    }

    public String getSourceBloodBankId() { return sourceBloodBankId; }
    public void setSourceBloodBankId(String sourceBloodBankId) { this.sourceBloodBankId = sourceBloodBankId; }

    public String getDestinationBloodBankId() { return destinationBloodBankId; }
    public void setDestinationBloodBankId(String destinationBloodBankId) { this.destinationBloodBankId = destinationBloodBankId; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public int getUnitsTransferred() { return unitsTransferred; }
    public void setUnitsTransferred(int unitsTransferred) { this.unitsTransferred = unitsTransferred; }

    @Override
    public void executeTransaction() {
        this.status = "Completed";
        System.out.println("Transfer executed successfully.");
    }

    @Override
    public void cancelTransaction() {
        this.status = "Cancelled";
        System.out.println("Transfer cancelled.");
    }

    @Override
    public void displayTransaction() {
        System.out.println("----------------------------------------");
        System.out.println("Blood Transfer Details:");
        System.out.println("Transfer ID (Txn ID): " + transactionId);
        System.out.println("Date: " + transactionDate);
        System.out.println("Status: " + status);
        System.out.println("Source Blood Bank ID: " + sourceBloodBankId);
        System.out.println("Destination Blood Bank ID: " + destinationBloodBankId);
        System.out.println("Blood Group: " + bloodGroup);
        System.out.println("Units: " + unitsTransferred + " units");
        System.out.println("----------------------------------------");
    }

    public void transferUnits() {
        executeTransaction();
    }

    public void recordTransfer() {
        System.out.println("Recording transfer details...");
    }
}
