package bloodbank.model;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;
import bloodbank.utility.Validation;
public final class Inventory implements Serializable {
    private static final long serialVersionUID = 1L;
    private final ArrayList<BloodUnit> bloodUnits = new ArrayList<>();
    public List<BloodUnit> getBloodUnits() {
        return List.copyOf(bloodUnits);
    }
    public void addBloodUnit(BloodUnit unit) {
        Validation.require(unit != null && bloodUnits.stream().noneMatch(u -> u.getBloodUnitId().equals(unit.getBloodUnitId())), "Duplicate or missing blood unit.");
        bloodUnits.add(unit);
    }
    public boolean removeBloodUnit(String id) {
        return bloodUnits.removeIf(u -> u.getBloodUnitId().equals(id));
    }
    public List<BloodUnit> searchBloodGroup(String group, LocalDate today) {
        Validation.bloodGroup(group);
        return bloodUnits.stream().filter(u -> u.getBloodGroup().equals(group) && u.usable(today)).sorted(Comparator.comparing(BloodUnit::getExpiryDate).thenComparing(BloodUnit::getBloodUnitId)).toList();
    }
    public List<BloodUnit> available(String group, String request, LocalDate today) {
        return searchBloodGroup(group, today).stream().filter(u -> u.getReservedFor() == null || u.getReservedFor().equals(request)).toList();
    }
    public List<BloodUnit> reserved(String request, LocalDate today) {
        throw new UnsupportedOperationException("Unit reservation is planned for Phase 2 and is not part of the current 50% implementation.");
    }
    public int getStockForGroup(String group, LocalDate today) {
        return available(group, null, today).size();
    }
    public int getTotalStock(LocalDate today) {
        return (int) bloodUnits.stream().filter(u -> u.usable(today)).count();
    }
    public boolean checkLowStock(int threshold, LocalDate today) {
        throw new UnsupportedOperationException("Low-stock alerts are planned for Phase 2 and is not part of the current 50% implementation.");
    }
    public void release(String request) {
        throw new UnsupportedOperationException("Reservation release is planned for Phase 2 and is not part of the current 50% implementation.");
    }
    public void expire(LocalDate today) {
        throw new UnsupportedOperationException("Advanced expiry handling is planned for Phase 2 and is not part of the current 50% implementation.");
    }
    public void displayInventory() {
        bloodUnits.forEach(BloodUnit::displayBloodUnit);
    }
}
