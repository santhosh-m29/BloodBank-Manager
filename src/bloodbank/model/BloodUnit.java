package bloodbank.model;
import java.time.LocalDate;
import bloodbank.utility.Validation;
/** One uniquely traceable physical unit; donations containing N units create N records. */
public final class BloodUnit {
    private final String bloodUnitId, bloodGroup, donationId;
    private int quantity = 1;
    private final LocalDate collectionDate, expiryDate;
    private String status = "PENDING_TEST";
    private String reservedFor;
    private String issuedTo;
    private String labResult = "PENDING";
    private boolean openingStock;
    public BloodUnit(String id, String group, String donationId, LocalDate collected, LocalDate expires) {
        bloodUnitId = Validation.id(id);
        bloodGroup = Validation.bloodGroup(group);
        this.donationId = Validation.id(donationId);
        Validation.require(collected != null && expires != null && expires.isAfter(collected), "Expiry must follow collection date.");
        collectionDate = collected;
        expiryDate = expires;
    }
    public BloodUnit(String id, String group, int quantity, LocalDate collected, LocalDate expires, String status, String donationId) {
        bloodUnitId = Validation.id(id);
        bloodGroup = Validation.bloodGroup(group);
        this.quantity = Validation.positive(quantity);
        this.donationId = donationId != null && !donationId.isBlank() ? donationId : id;
        this.collectionDate = collected;
        this.expiryDate = expires;
        this.status = status != null ? status : "AVAILABLE";
        this.labResult = "AVAILABLE".equalsIgnoreCase(this.status) ? "PASS" : "PENDING";
    }
    /** Explicit demonstration opening stock, not attributed to a fictional donor. */
    public static BloodUnit openingStock(String id, String group, LocalDate date) {
        BloodUnit unit = new BloodUnit(id, group, "OPENING_STOCK", date, date.plusDays(42));
        unit.openingStock = true;
        unit.status = "AVAILABLE";
        unit.labResult = "PASS";
        return unit;
    }
    public boolean isOpeningStock() { return openingStock; }
    public String getBloodUnitId() {
        return bloodUnitId;
    }
    public String getBloodGroup() {
        return bloodGroup;
    }
    public String getDonationId() {
        return donationId;
    }
    public int getQuantity() {
        return quantity;
    }
    public LocalDate getCollectionDate() {
        return collectionDate;
    }
    public LocalDate getExpiryDate() {
        return expiryDate;
    }
    public String getStatus() {
        return status;
    }
    public String getReservedFor() {
        return reservedFor;
    }
    public String getIssuedTo() {
        return issuedTo;
    }
    public String getLabResult() {
        return labResult;
    }
    public boolean isExpired(LocalDate today) {
        return !expiryDate.isAfter(today);
    }
    public boolean isExpired(String date) {
        return isExpired(LocalDate.parse(date));
    }
    public boolean usable(LocalDate today) {
        return !isExpired(today) && (status.equals("AVAILABLE") || status.equals("TRANSFERRED"));
    }
    public void expire(LocalDate today) {
        throw new UnsupportedOperationException("Advanced expiry handling is planned for Phase 2 and is not part of the current 50% implementation.");
    }
    public boolean runLabTests(Donor donor, LocalDate today) {
        Validation.require(status.equals("PENDING_TEST"), "Unit has already been tested.");
        Validation.require(donor != null && today != null, "Donor and test date are required.");
        boolean passed = donor.isEligible(today) && donor.getBloodGroup().equals(bloodGroup) && !isExpired(today);
        labResult = passed ? "PASS" : "FAIL";
        status = passed ? "TEST_PASSED" : "REJECTED";
        return passed;
    }

    public void register(LocalDate today) {
        Validation.require((status.equals("TEST_PASSED") || status.equals("AVAILABLE")) && !isExpired(today), "Only unexpired, passed units can be registered.");
        status = "AVAILABLE";
    }
    public void reserve(String request, LocalDate today) {
        throw new UnsupportedOperationException("Unit reservation is planned for Phase 2 and is not part of the current 50% implementation.");
    }
    public void release(String request) {
        throw new UnsupportedOperationException("Unit reservation release is planned for Phase 2 and is not part of the current 50% implementation.");
    }
    public void transfer(String request, LocalDate today) {
        throw new UnsupportedOperationException("Unit transfer is planned for Phase 2 and is not part of the current 50% implementation.");
    }
    public void issue(String request, String patient, LocalDate today) {
        Validation.require(usable(today) && (reservedFor == null || request.equals(reservedFor)), "Unit is not available for issue.");
        status = "ISSUED";
        issuedTo = Validation.id(patient);
        reservedFor = null;
    }
    public BloodUnit takeForIssue(int amount, String request, String patient, LocalDate today) {
        Validation.require(amount > 0 && amount <= quantity && usable(today), "Invalid issue quantity.");
        if (amount == quantity) { issue(request, patient, today); return this; }
        BloodUnit portion = new BloodUnit(bloodUnitId + "_" + request, bloodGroup, amount, collectionDate, expiryDate, status, donationId);
        portion.openingStock = openingStock;
        portion.issue(request, patient, today);
        quantity -= amount;
        return portion;
    }
    public void restoreIssuedTo(String patient) { issuedTo = patient == null || patient.isBlank() ? null : Validation.id(patient); }

    @Override public String toString() {
        return bloodUnitId + " | " + bloodGroup + " | Qty: " + quantity + " | Collected: " + collectionDate
                + " | Expires: " + expiryDate + " | " + status;
    }
    public void displayBloodUnit() {
        System.out.println(this);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BloodUnit bloodUnit = (BloodUnit) o;
        return java.util.Objects.equals(bloodUnitId, bloodUnit.bloodUnitId);
    }
    @Override
    public int hashCode() {
        return java.util.Objects.hash(bloodUnitId);
    }
}
