package bloodbank;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class BloodUnit {
    private String bloodUnitId;
    private String bloodGroup;
    private int quantity;
    private String collectionDate; // Format: YYYY-MM-DD
    private String expiryDate;     // Format: YYYY-MM-DD

    public BloodUnit(String bloodUnitId, String bloodGroup, int quantity, String collectionDate, String expiryDate) {
        this.bloodUnitId = bloodUnitId;
        this.bloodGroup = bloodGroup;
        this.quantity = quantity;
        this.collectionDate = collectionDate;
        this.expiryDate = expiryDate;
    }

    public String getBloodUnitId() { return bloodUnitId; }
    public void setBloodUnitId(String bloodUnitId) { this.bloodUnitId = bloodUnitId; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getCollectionDate() { return collectionDate; }
    public void setCollectionDate(String collectionDate) { this.collectionDate = collectionDate; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    public boolean isExpired() {
        if (expiryDate == null || expiryDate.isEmpty()) {
            return false;
        }
        try {
            LocalDate exp = LocalDate.parse(expiryDate);
            LocalDate now = LocalDate.now();
            return exp.isBefore(now);
        } catch (DateTimeParseException e) {
            return false; // Return false on parsing error as fallback
        }
    }

    public void updateQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void displayBloodUnit() {
        System.out.println("Blood Unit ID: " + bloodUnitId + " | Group: " + bloodGroup + 
                           " | Qty: " + quantity + " | Collected: " + collectionDate + 
                           " | Expiry: " + expiryDate + " | Expired: " + (isExpired() ? "YES" : "NO"));
    }
}
