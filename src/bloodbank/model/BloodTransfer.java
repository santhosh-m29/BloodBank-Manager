package bloodbank.model;
import java.time.LocalDate;
import java.util.*;
import bloodbank.utility.Validation;
public final class BloodTransfer extends Transaction {
    private static final long serialVersionUID = 1L;
    private final String sourceFacilityId, destinationFacilityId, bloodGroup, requestId;
    private final int unitsTransferred;
    private final ArrayList<String> unitIds = new ArrayList<>();
    public BloodTransfer(String id, LocalDate date, String source, String destination, String group, int quantity, String request) {
        super(id, date, "PENDING");
        sourceFacilityId = Validation.id(source);
        destinationFacilityId = Validation.id(destination);
        bloodGroup = Validation.bloodGroup(group);
        unitsTransferred = Validation.positive(quantity);
        requestId = Validation.id(request);
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
        Validation.require(status.equals("PENDING") && source != destination, "Invalid or duplicate transfer.");
        List<BloodUnit> units = source.reserved(requestId, today);
        Validation.require(units.size() == unitsTransferred && units.stream().allMatch(u -> u.getBloodGroup().equals(bloodGroup)), "Reserved transfer stock is insufficient; reject and recreate the request if stock expired.");
        Validation.require(units.stream().noneMatch(u -> destination.getBloodUnits().stream().anyMatch(d -> d.getBloodUnitId().equals(u.getBloodUnitId()))), "Duplicate destination unit.");
        for (BloodUnit unit : units) {
            unit.transfer(requestId, today);
            source.removeBloodUnit(unit.getBloodUnitId());
            destination.addBloodUnit(unit);
            unitIds.add(unit.getBloodUnitId());
        }
        recordTransfer();
    }
    public void recordTransfer() {
        Validation.require(status.equals("PENDING") && unitIds.size() == unitsTransferred, "Transfer must move all units before recording.");
        status = "DISPATCHED";
    }
    @Override public String toString() {
        return super.toString() + " | " + sourceFacilityId + " -> " + destinationFacilityId + " | " + bloodGroup + " | Qty: " + unitsTransferred + " | Request: " + requestId + " | Units: " + unitIds;
    }
}
