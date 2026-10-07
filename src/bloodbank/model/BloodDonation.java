package bloodbank.model;
import java.time.LocalDate;
import java.util.*;
import bloodbank.utility.Validation;
public final class BloodDonation extends Transaction {
    private final String donorId, bloodGroup, bankId;
    private final int quantity;
    private final ArrayList<BloodUnit> units = new ArrayList<>();
    private String labNote = "", testedBy = "", registeredBy = "";
    public BloodDonation(String id, LocalDate date, String donor, String group, int quantity, String bank) {
        this(id, date, "PENDING_TEST", donor, group, quantity, bank);
    }
    public BloodDonation(String id, LocalDate date, String status, String donor, String group, int quantity, String bank) {
        super(id, date, status != null ? status : "PENDING_TEST");
        donorId = Validation.id(donor);
        bloodGroup = Validation.bloodGroup(group);
        this.quantity = Validation.positive(quantity);
        this.bankId = bank != null && !bank.isBlank() ? bank : "BB001";
        String unitStatus = "TEST_PASSED".equals(status) || "REGISTERED".equals(status) ? status : "PENDING_TEST";
        for (int i = 1; i <= quantity; i++) {
            units.add(new BloodUnit(id + "_" + i, group, 1, date, date.plusDays(42), unitStatus, id));
        }
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
    public void test(Donor donor, LocalDate today) {
        Validation.require(status.equals("PENDING_TEST"), "Donation already tested.");
        Validation.require(donor.getPersonId().equals(donorId), "Donation donor mismatch.");
        boolean passed = true;
        for (BloodUnit unit : units) passed = unit.runLabTests(donor, today) && passed;
        labNote = (passed ? "PASS" : "FAIL") + " | Age=" + donor.getAge() + " (18-65)"
                + " | Weight=" + donor.getWeight() + " (>=50 kg)"
                + " | Haemoglobin=" + donor.getHaemoglobin() + " (>=12.5)"
                + " | Previous donation=" + (donor.getLastDonationDate() == null ? "None" : donor.getLastDonationDate()) + " (interval >=90 days)"
                + " | Blood group=" + donor.getBloodGroup();
        testedBy = "AUTOMATIC";
        status = passed ? "TEST_PASSED" : "REJECTED";
    }

    public void updateInventory(Inventory inventory, LocalDate today, String actor) {
        Validation.require(status.equals("TEST_PASSED"), "Donation must pass testing before registration.");
        for (BloodUnit donationUnit : units) {
            BloodUnit invUnit = inventory.getBloodUnits().stream()
                    .filter(u -> u.getBloodUnitId().equalsIgnoreCase(donationUnit.getBloodUnitId()))
                    .findFirst()
                    .orElse(null);
            Validation.require(invUnit != null && !invUnit.isExpired(today), "Missing or expired donation units.");
            invUnit.register(today);
            if (donationUnit != invUnit) donationUnit.register(today);
        }
        registeredBy = actor;
        status = "REGISTERED";
    }
    @Override public String toString() {
        String values = labNote.replaceFirst("^(PASS|FAIL) \\| ", "");
        return super.toString() + " | Donor: " + donorId + " | " + bloodGroup + " | Qty: " + quantity
                + (values.isBlank() ? "" : " | " + values);
    }
}
