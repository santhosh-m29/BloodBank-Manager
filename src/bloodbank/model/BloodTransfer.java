package bloodbank.model;
import java.time.LocalDate;
import java.util.*;
import bloodbank.utility.Validation;
public final class BloodTransfer extends Transaction {
    private final String sourceFacilityId, destinationFacilityId, bloodGroup, requestId;
    private final int unitsTransferred;
    private final ArrayList<String> unitIds = new ArrayList<>();
    public BloodTransfer(String id, LocalDate date, String source, String destination, String group, int quantity, String request) {
        this(id, date, "PENDING", source, destination, group, quantity, request);
    }
    public BloodTransfer(String id, LocalDate date, String status, String source, String destination, String group, int quantity, String request) {
        super(id, date, status != null ? status : "PENDING");
        sourceFacilityId = Validation.id(source);
        destinationFacilityId = Validation.id(destination);
        bloodGroup = Validation.bloodGroup(group);
        unitsTransferred = Validation.positive(quantity);
        this.requestId = request != null && !request.isBlank() ? request : "REQ001";
    }
    public String getSourceFacilityId() {
        return sourceFacilityId;
    }
    public String getDestinationFacilityId() {
        return destinationFacilityId;
    }
    public String getBloodGroup() {
        return bloodGroup;
    }
    public int getUnitsTransferred() {
        return unitsTransferred;
    }
    public String getRequestId() {
        return requestId;
    }
    public List<String> getUnitIds() {
        return List.copyOf(unitIds);
    }
    public void transferUnits(Inventory source, Inventory destination, LocalDate today) {
        throw new UnsupportedOperationException("Blood transfer is planned for Phase 2 and is not part of the current 50% implementation.");
    }
    public void recordTransfer() {
        throw new UnsupportedOperationException("Blood transfer is planned for Phase 2 and is not part of the current 50% implementation.");
    }
    @Override public String toString() {
        return super.toString() + " | " + sourceFacilityId + " -> " + destinationFacilityId + " | " + bloodGroup + " | Qty: " + unitsTransferred + " | Request: " + requestId + " | Units: " + unitIds;
    }
}
