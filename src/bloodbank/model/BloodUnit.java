package bloodbank.model;
import java.io.Serializable;
import java.time.LocalDate;
import bloodbank.utility.Validation;
/** One uniquely traceable physical unit; donations containing N units create N records. */
public final class BloodUnit implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String bloodUnitId, bloodGroup, donationId;
    private final int quantity = 1;
    private final LocalDate collectionDate, expiryDate;
    private String status = "PENDING_TEST";
    private String reservedFor;
    private String issuedTo;
    private String labResult = "PENDING";
    public BloodUnit(String id, String group, String donationId, LocalDate collected, LocalDate expires) {
        bloodUnitId = Validation.id(id);
        bloodGroup = Validation.bloodGroup(group);
        this.donationId = Validation.id(donationId);
        Validation.require(collected != null && expires != null && expires.isAfter(collected), "Expiry must follow collection date.");
        collectionDate = collected;
        expiryDate = expires;
    }
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
        if (isExpired(today) && !status.equals("ISSUED") && !status.equals("REJECTED")) {
            status = "EXPIRED";
            reservedFor = null;
        }
    }
    public boolean runLabTests(boolean passed) {
        Validation.require(status.equals("PENDING_TEST"), "Unit has already been tested.");
        labResult = passed ? "PASS" : "FAIL";
        status = passed ? "TEST_PASSED" : "REJECTED";
        return passed;
    }
    public void register(LocalDate today) {
        Validation.require(status.equals("TEST_PASSED") && !isExpired(today), "Only unexpired, passed units can be registered.");
        status = "AVAILABLE";
    }
    public void reserve(String request, LocalDate today) {
        Validation.require(usable(today) && (reservedFor == null || reservedFor.equals(request)), "Unit is unavailable or reserved.");
        reservedFor = Validation.id(request);
    }
    public void release(String request) {
        if (request.equals(reservedFor)) reservedFor = null;
    }
    public void transfer(String request, LocalDate today) {
        Validation.require(usable(today) && request.equals(reservedFor), "Transfer unit is unavailable.");
        status = "TRANSFERRED";
    }
    public void issue(String request, String patient, LocalDate today) {
        Validation.require(usable(today) && request.equals(reservedFor), "Issue unit is unavailable.");
        status = "ISSUED";
        issuedTo = patient;
        reservedFor = null;
    }
    @Override public String toString() {
        return bloodUnitId + " | " + bloodGroup + " | 1 | " + collectionDate + " | " + expiryDate + " | " + status + " | Lab: " + labResult + " | Reserved: " + reservedFor + " | Patient: " + issuedTo;
    }
    public void displayBloodUnit() {
        System.out.println(this);
    }
}
