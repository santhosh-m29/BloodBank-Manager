package bloodbank.model;

import java.util.HashMap;

public class BloodRequest extends Transaction {
    private static final HashMap<String, Integer> PRIORITY = new HashMap<>();

    static {
        PRIORITY.put("CRITICAL", 1);
        PRIORITY.put("HIGH", 2);
        PRIORITY.put("NORMAL", 3);
        PRIORITY.put("MEDIUM", 3);
        PRIORITY.put("LOW", 4);
    }

    private String patientId;
    private String hospitalId;
    private String bloodGroup;
    private int unitsRequested;
    private String urgency; // LOW, NORMAL, HIGH, or CRITICAL

    public BloodRequest(String transactionId, String transactionDate, String status,
                        String patientId, String hospitalId, String bloodGroup, int unitsRequested) {
        this(transactionId, transactionDate, status, patientId, hospitalId,
                bloodGroup, unitsRequested, "NORMAL");
    }

    public BloodRequest(String transactionId, String transactionDate, String status,
                        String patientId, String hospitalId, String bloodGroup, int unitsRequested, String urgency) {
        super(transactionId, transactionDate, status);
        this.patientId = patientId;
        this.hospitalId = hospitalId;
        this.bloodGroup = bloodGroup;
        this.unitsRequested = unitsRequested;
        this.urgency = urgency;
    }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getHospitalId() { return hospitalId; }
    public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public int getUnitsRequested() { return unitsRequested; }
    public void setUnitsRequested(int unitsRequested) { this.unitsRequested = unitsRequested; }

    public String getUrgency() { return urgency; }
    public void setUrgency(String urgency) { this.urgency = urgency; }

    public boolean isHospitalRestockRequest() {
        return patientId == null || patientId.trim().isEmpty();
    }

    @Override
    public void displayTransaction() {
        System.out.println("----------------------------------------");
        System.out.println("Blood Request Details:");
        System.out.println("Request ID (Txn ID): " + transactionId);
        System.out.println("Date: " + transactionDate);
        System.out.println("Status: " + status);
        if (isHospitalRestockRequest()) {
            System.out.println("Request Type: Hospital stock restock");
        } else {
            System.out.println("Request Type: Patient blood request");
            System.out.println("Patient ID: " + patientId);
        }
        System.out.println("Hospital ID: " + hospitalId);
        System.out.println("Blood Group Required: " + bloodGroup);
        System.out.println("Units Requested: " + unitsRequested + " units");
        System.out.println("Urgency: " + urgency);
        System.out.println("----------------------------------------");
    }

    public void approveRequest() {
        this.status = "APPROVED";
        System.out.println("Request " + transactionId + " is APPROVED.");
    }

    public void rejectRequest() {
        this.status = "REJECTED";
        System.out.println("Request " + transactionId + " is REJECTED.");
    }

    public int getUrgencyRank() {
        if (urgency == null) return 4;
        Integer rank = PRIORITY.get(urgency.toUpperCase());
        return rank == null ? 4 : rank;
    }
}
