package bloodbank;

public class BloodTransfer extends Transaction {
    private String sourceFacilityId;
    private String destinationFacilityId;
    private String bloodGroup;
    private int unitsTransferred;

    public BloodTransfer(String transactionId, String transactionDate, String status,
                         String sourceFacilityId, String destinationFacilityId, String bloodGroup, int unitsTransferred) {
        super(transactionId, transactionDate, status);
        this.sourceFacilityId = sourceFacilityId;
        this.destinationFacilityId = destinationFacilityId;
        this.bloodGroup = bloodGroup;
        this.unitsTransferred = unitsTransferred;
    }

    public String getSourceFacilityId() { return sourceFacilityId; }
    public void setSourceFacilityId(String sourceFacilityId) { this.sourceFacilityId = sourceFacilityId; }

    public String getDestinationFacilityId() { return destinationFacilityId; }
    public void setDestinationFacilityId(String destinationFacilityId) { this.destinationFacilityId = destinationFacilityId; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public int getUnitsTransferred() { return unitsTransferred; }
    public void setUnitsTransferred(int unitsTransferred) { this.unitsTransferred = unitsTransferred; }

    @Override
    public void displayTransaction() {
        System.out.println("----------------------------------------");
        System.out.println("Blood Transfer Details:");
        System.out.println("Transfer ID (Txn ID): " + transactionId);
        System.out.println("Date: " + transactionDate);
        System.out.println("Status: " + status);
        System.out.println("Source Facility ID: " + sourceFacilityId);
        System.out.println("Destination Facility ID: " + destinationFacilityId);
        System.out.println("Blood Group: " + bloodGroup);
        System.out.println("Units: " + unitsTransferred + " units");
        System.out.println("----------------------------------------");
    }

    public void transferUnits() {
        System.out.println("Transferring " + unitsTransferred + " units of " + bloodGroup + " from " + sourceFacilityId + " to " + destinationFacilityId);
        this.status = "Completed";
    }

    public void recordTransfer() {
        System.out.println("Transfer transaction " + transactionId + " recorded in dispatch registry.");
    }
}
