package bloodbank.model;
import java.time.LocalDate;
import java.util.*;
import bloodbank.utility.Validation;
public final class BloodRequest extends Transaction {
    private static final long serialVersionUID = 1L;
    private static final HashMap<String, Integer> PRIORITY = new HashMap<>(Map.of("CRITICAL", 1, "HIGH", 2, "MEDIUM", 3, "LOW", 4));
    private final String patientId, hospitalId, bloodGroup, urgency;
    private final int unitsRequested;
    private String bankId, reason = "";
    private final ArrayList<String> issuedUnitIds = new ArrayList<>();
    public BloodRequest(String id, LocalDate date, String patient, String hospital, String group, int quantity, String urgency) {
        super(id, date, "PENDING");
        patientId = Validation.id(patient);
        hospitalId = Validation.id(hospital);
        bloodGroup = Validation.bloodGroup(group);
        unitsRequested = Validation.positive(quantity);
        Validation.require(PRIORITY.containsKey(urgency), "Urgency must be CRITICAL, HIGH, MEDIUM or LOW.");
        this.urgency = urgency;
    }
    public String getPatientId() {
        return patientId;
    }
    public String getHospitalId() {
        return hospitalId;
    }
    public String getBloodGroup() {
        return bloodGroup;
    }
    public String getUrgency() {
        return urgency;
    }
    public int getUnitsRequested() {
        return unitsRequested;
    }
    public int priorityDispatch() {
        return PRIORITY.get(urgency);
    }
    public String getBankId() {
        return bankId;
    }
    public String getReason() {
        return reason;
    }
    public List<String> getIssuedUnitIds() {
        return List.copyOf(issuedUnitIds);
    }
    public boolean checkAvailability(Inventory inventory, LocalDate today) {
        return inventory.available(bloodGroup, getTransactionId(), today).size() >= unitsRequested;
    }
    public void route(String bank) {
        Validation.require(status.equals("PENDING"), "Only pending requests can be routed.");
        bankId = Validation.id(bank);
    }
    public void ready() {
        Validation.require(status.equals("PENDING") || status.equals("APPROVED"), "Request cannot become ready.");
        status = "READY";
    }
    public void approveRequest() {
        Validation.require(status.equals("PENDING"), "Only pending requests can be approved.");
        status = "APPROVED";
    }
    public void rejectRequest(String reason) {
        Validation.require(status.equals("PENDING") || status.equals("APPROVED"), "Request cannot be rejected in this state.");
        this.reason = Validation.text(reason, "Rejection reason");
        status = "REJECTED";
    }
    public void reopen() {
        Validation.require(status.equals("READY") || status.equals("PENDING"), "Only pending or ready requests can be rechecked.");
        status = "PENDING";
        bankId = null;
    }
    public void fulfill(List<BloodUnit> units) {
        Validation.require(status.equals("READY") && units.size() == unitsRequested, "Request is not ready to issue.");
        units.forEach(u -> issuedUnitIds.add(u.getBloodUnitId()));
        status = "FULFILLED";
    }
    @Override public String toString() {
        return super.toString() + " | Patient: " + patientId + " | Hospital: " + hospitalId + " | " + bloodGroup + " | Qty: " + unitsRequested + " | " + urgency + " | Bank: " + bankId + " | " + reason + " | Issued: " + issuedUnitIds;
    }
}
