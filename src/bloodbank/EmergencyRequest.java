package bloodbank;

public class EmergencyRequest extends BloodRequest {
    private String urgencyLevel; // "LOW", "NORMAL", "HIGH", "CRITICAL"
    protected String[] levels = {"LOW", "NORMAL", "HIGH", "CRITICAL"};
    private boolean isEmergency;

    public EmergencyRequest(String transactionId, String transactionDate, String status,
                            String patientId, String hospitalId, String bloodGroup, int unitsRequested,
                            String urgencyLevel) {
        super(transactionId, transactionDate, status, patientId, hospitalId, bloodGroup, unitsRequested, "EMERGENCY");
        this.urgencyLevel = urgencyLevel;
        this.isEmergency = true;
    }

    public String getUrgencyLevel() { return urgencyLevel; }
    public void setUrgencyLevel(String urgencyLevel) { this.urgencyLevel = urgencyLevel; }

    public String[] getLevels() { return levels; }
    public void setLevels(String[] levels) { this.levels = levels; }

    public boolean isEmergency() { return isEmergency; }
    public void setEmergency(boolean emergency) { isEmergency = emergency; }

    @Override
    public void displayTransaction() {
        System.out.println("----------------------------------------");
        System.out.println("EMERGENCY Blood Request Details:");
        System.out.println("Request ID (Txn ID): " + transactionId);
        System.out.println("Date: " + transactionDate);
        System.out.println("Status: " + status);
        System.out.println("Patient ID: " + getPatientId());
        System.out.println("Hospital ID: " + getHospitalId());
        System.out.println("Blood Group Required: " + getBloodGroup());
        System.out.println("Units Requested: " + getUnitsRequested() + " units");
        System.out.println("Emergency Status: YES");
        System.out.println("Emergency Urgency Level: " + urgencyLevel);
        System.out.println("----------------------------------------");
    }

    public void priorityDispatch() {
        System.out.println("[PRIORITY DISPATCH] Dispatching critical stock immediately for Emergency Request " + transactionId);
        this.status = "APPROVED";
    }

    public void overrideThreshold() {
        System.out.println("[OVERRIDE THRESHOLD] Safety thresholds overridden for emergency units dispatch on request " + transactionId);
    }
}
