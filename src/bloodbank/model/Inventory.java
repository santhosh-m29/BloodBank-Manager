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
        return available(group, null, today).stream().mapToInt(BloodUnit::getQuantity).sum();
    }
    public int getTotalStock(LocalDate today) {
        return bloodUnits.stream().filter(u -> u.usable(today)).mapToInt(BloodUnit::getQuantity).sum();
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
    public List<String> summaryRows(java.util.function.Function<String, String> donorLabel, LocalDate today) {
        record Batch(String donor, String group, LocalDate collected, LocalDate expires, String status) { }
        Map<Batch, Integer> quantities = new LinkedHashMap<>();
        for (BloodUnit unit : bloodUnits) {
            if ("ISSUED".equalsIgnoreCase(unit.getStatus())) continue;
            String status = unit.isExpired(today) && !unit.getStatus().equals("REJECTED") ? "EXPIRED" : unit.getStatus();
            Batch batch = new Batch(donorLabel.apply(unit.getDonationId()), unit.getBloodGroup(), unit.getCollectionDate(), unit.getExpiryDate(), status);
            quantities.merge(batch, unit.getQuantity(), Integer::sum);
        }
        return quantities.entrySet().stream().map(e -> e.getKey().donor() + " | " + e.getKey().group()
                + " | Qty: " + e.getValue() + " | Collected: " + e.getKey().collected()
                + " | Expires: " + e.getKey().expires() + " | " + e.getKey().status()).toList();
    }
    public void displayInventory() {
        summaryRows(id -> id.equals("OPENING_STOCK") ? "Opening stock" : "Donation: " + id, LocalDate.now()).forEach(System.out::println);
    }
}
