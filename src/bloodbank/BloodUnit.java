package bloodbank;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class BloodUnit {
    private String bloodUnitId;
    private String bloodGroup;
    private int quantity;
    private String collectionDate; // Format: YYYY-MM-DD
    private String expiryDate;     // Format: YYYY-MM-DD
    private String status;         // "COLLECTED", "PENDING_TEST", "TEST_PASSED", "TEST_FAILED", "AVAILABLE", "RESERVED", "ISSUED", "TRANSFERRED", "EXPIRED", "DISCARDED"

    public BloodUnit(String bloodUnitId, String bloodGroup, int quantity, String collectionDate, String expiryDate) {
        this.bloodUnitId = bloodUnitId;
        this.bloodGroup = bloodGroup;
        this.quantity = quantity;
        this.collectionDate = collectionDate;
        this.expiryDate = expiryDate;
        this.status = "PENDING_TEST"; // Initial state
    }

    public BloodUnit(String bloodUnitId, String bloodGroup, int quantity, String collectionDate, String expiryDate, String status) {
        this.bloodUnitId = bloodUnitId;
        this.bloodGroup = bloodGroup;
        this.quantity = quantity;
        this.collectionDate = collectionDate;
        this.expiryDate = expiryDate;
        this.status = status;
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

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean runLabTests() {
        System.out.println("\n[LAB TEST] Initiating screening for infectious pathogens (HIV, HBV, HCV, Syphilis) on Unit: " + bloodUnitId);
        // Deterministic test result: units with "FAIL" in ID or a 0 quantity will fail, others pass
        boolean passed = !bloodUnitId.toUpperCase().contains("FAIL") && quantity > 0;
        if (passed) {
            this.status = "AVAILABLE";
            System.out.println("[LAB TEST RESULT] PASS - Unit " + bloodUnitId + " is safe. Status set to AVAILABLE (usable stock).");
        } else {
            this.status = "TEST_FAILED";
            System.out.println("[LAB TEST RESULT] FAIL - Pathogen detected in Unit " + bloodUnitId + ". Status set to TEST_FAILED.");
        }
        return passed;
    }

    public boolean isExpired() {
        if ("EXPIRED".equalsIgnoreCase(status) || "DISCARDED".equalsIgnoreCase(status)) {
            return true;
        }
        if (expiryDate == null || expiryDate.trim().isEmpty()) {
            return false;
        }
        try {
            LocalDate exp = LocalDate.parse(expiryDate.trim());
            LocalDate now = LocalDate.now();
            if (exp.isBefore(now)) {
                this.status = "EXPIRED"; // Auto-update status
                return true;
            }
            return false;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public void updateQuantity() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter new quantity for blood unit (Current: " + quantity + "): ");
        String input = sc.nextLine().trim();
        if (!input.isEmpty()) {
            try {
                int qty = Integer.parseInt(input);
                if (qty >= 0) {
                    this.quantity = qty;
                    System.out.println("Quantity updated to " + qty);
                } else {
                    System.out.println("Quantity must be positive.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid integer.");
            }
        }
    }

    // Overload for direct programmatic updates
    public void updateQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void displayBloodUnit() {
        System.out.println("Blood Unit ID: " + bloodUnitId + " | Group: " + bloodGroup + 
                           " | Qty: " + quantity + " | Collected: " + collectionDate + 
                           " | Expiry: " + expiryDate + " | Status: " + status + 
                           " | Expired: " + (isExpired() ? "YES" : "NO"));
    }
}
