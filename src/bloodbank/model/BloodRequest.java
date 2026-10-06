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
        throw new UnsupportedOperationException("Request routing and shortage transfer are planned for Phase 2 and is not part of the current 50% implementation.");
    }
    public void ready() {
        throw new UnsupportedOperationException("Request readiness transitions are planned for Phase 2 and is not part of the current 50% implementation.");
    }
    public void approveRequest() {
        throw new UnsupportedOperationException("Request approval is planned for Phase 2 and is not part of the current 50% implementation.");
    }
    public void rejectRequest(String reason) {
        throw new UnsupportedOperationException("Request rejection is planned for Phase 2 and is not part of the current 50% implementation.");
    }
    public void reopen() {
        throw new UnsupportedOperationException("Request reopening is planned for Phase 2 and is not part of the current 50% implementation.");
    }
    public void fulfill(List<BloodUnit> units) {
        throw new UnsupportedOperationException("Request fulfillment is planned for Phase 2 and is not part of the current 50% implementation.");
    }
    @Override public String toString() {
        return super.toString() + " | Patient: " + patientId + " | Hospital: " + hospitalId + " | " + bloodGroup + " | Qty: " + unitsRequested + " | " + urgency + " | Bank: " + bankId + " | " + reason + " | Issued: " + issuedUnitIds;
    }
}
