package bloodbank.model;
import java.time.LocalDate;
import java.util.*;
import bloodbank.utility.Validation;
public final class BloodDonation extends Transaction {
    private static final long serialVersionUID = 1L;
    private final String donorId, bloodGroup, bankId;
    private final int quantity;
    private final ArrayList<BloodUnit> units = new ArrayList<>();
    private String labNote = "", testedBy = "", registeredBy = "";
    public BloodDonation(String id, LocalDate date, String donor, String group, int quantity, String bank) {
        super(id, date, "PENDING_TEST");
        donorId = Validation.id(donor);
        bloodGroup = Validation.bloodGroup(group);
        this.quantity = Validation.positive(quantity);
        bankId = Validation.id(bank);
        for (int i = 1; i <= quantity; i++) units.add(new BloodUnit(id + "_" + i, group, id, date, date.plusDays(42)));
    }
    public String getDonorId() {
        return donorId;
    }
    public String getBloodGroup() {
        return bloodGroup;
    }
    public int getQuantity() {
        return quantity;
    }
    public String getBankId() {
        return bankId;
    }
    public List<BloodUnit> getUnits() {
        return List.copyOf(units);
    }
    public String getLabNote() {
        return labNote;
    }
    public void recordDonation(Inventory inventory) {
        Validation.require(units.stream().noneMatch(u -> inventory.getBloodUnits().stream().anyMatch(v -> v.getBloodUnitId().equals(u.getBloodUnitId()))), "Donation already recorded.");
        units.forEach(inventory::addBloodUnit);
    }
    public void test(boolean passed, String note, String actor) {
        Validation.require(status.equals("PENDING_TEST"), "Donation already tested.");
        Validation.text(note, "Lab reference/result note");
        units.forEach(u -> u.runLabTests(passed));
        labNote = note;
        testedBy = actor;
        status = passed ? "TEST_PASSED" : "REJECTED";
    }
    public void updateInventory(Inventory inventory, LocalDate today, String actor) {
        Validation.require(status.equals("TEST_PASSED"), "Donation must pass testing before registration.");
        Validation.require(units.stream().allMatch(u -> inventory.getBloodUnits().contains(u) && !u.isExpired(today)), "Missing or expired donation units.");
        units.forEach(u -> u.register(today));
        registeredBy = actor;
        status = "REGISTERED";
    }
    @Override public String toString() {
        return super.toString() + " | Donor: " + donorId + " | " + bloodGroup + " | Qty: " + quantity + " | Bank: " + bankId + " | Lab: " + labNote + " | Tested by: " + testedBy + " | Registered by: " + registeredBy;
    }
}
