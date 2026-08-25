package bloodbank;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

public class AlertManager {
    private int lowStockThreshold;
    private int expiryAlertDays;

    public AlertManager(int lowStockThreshold, int expiryAlertDays) {
        this.lowStockThreshold = lowStockThreshold;
        this.expiryAlertDays = expiryAlertDays;
    }

    public AlertManager() {
        this.lowStockThreshold = 10;
        this.expiryAlertDays = 7;
    }

    public int getLowStockThreshold() { return lowStockThreshold; }
    public void setLowStockThreshold(int lowStockThreshold) { this.lowStockThreshold = lowStockThreshold; }

    public int getExpiryAlertDays() { return expiryAlertDays; }
    public void setExpiryAlertDays(int expiryAlertDays) { this.expiryAlertDays = expiryAlertDays; }

    public void checkLowStock() {
        String[] groups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        boolean alertGenerated = false;

        System.out.println("\n[ALERT MONITOR] Running stock check (Threshold: " + lowStockThreshold + " units)...");
        
        // 1. Check Central Blood Bank Stock
        for (String g : groups) {
            int bbStock = Main.inventory.getStockForGroup(g);
            if (bbStock < lowStockThreshold) {
                System.out.println("  >> [BB ALERT] Central Blood Bank is LOW on group " + g + " (" + bbStock + " units available)");
                alertGenerated = true;
            }
        }

        // 2. Check Hospital Stocks
        for (Hospital h : Main.hospitals) {
            for (String g : groups) {
                int hospStock = h.getInventory().getStockForGroup(g);
                if (hospStock < lowStockThreshold) {
                    System.out.println("  >> [HOSP ALERT] " + h.getOrganizationName() + " is LOW on group " + g + " (" + hospStock + " units available)");
                    alertGenerated = true;
                }
            }
        }

        if (!alertGenerated) {
            System.out.println("  >> All facilities have adequate stock levels.");
        }
    }

    public void checkExpiry() {
        LocalDate now = LocalDate.now();
        boolean alertGenerated = false;
        
        System.out.println("\n[ALERT MONITOR] Running unit expiry check (Alert window: " + expiryAlertDays + " days)...");
        
        // 1. Check Central Blood Bank Units
        for (BloodUnit bu : Main.inventory.getBloodUnits()) {
            if (bu.isExpired()) {
                System.out.println("  >> [BB CRITICAL] Expired Unit " + bu.getBloodUnitId() + " (" + bu.getBloodGroup() + ") in Blood Bank! Expiry: " + bu.getExpiryDate());
                alertGenerated = true;
            } else {
                try {
                    LocalDate exp = LocalDate.parse(bu.getExpiryDate().trim());
                    long days = ChronoUnit.DAYS.between(now, exp);
                    if (days <= expiryAlertDays) {
                        System.out.println("  >> [BB WARNING] Unit " + bu.getBloodUnitId() + " (" + bu.getBloodGroup() + ") expires in " + days + " days (Date: " + bu.getExpiryDate() + ")");
                        alertGenerated = true;
                    }
                } catch (DateTimeParseException ignored) {}
            }
        }

        // 2. Check Hospitals' Units
        for (Hospital h : Main.hospitals) {
            for (BloodUnit bu : h.getInventory().getBloodUnits()) {
                if (bu.isExpired()) {
                    System.out.println("  >> [HOSP CRITICAL] Expired Unit " + bu.getBloodUnitId() + " (" + bu.getBloodGroup() + ") in " + h.getOrganizationName() + "! Expiry: " + bu.getExpiryDate());
                    alertGenerated = true;
                } else {
                    try {
                        LocalDate exp = LocalDate.parse(bu.getExpiryDate().trim());
                        long days = ChronoUnit.DAYS.between(now, exp);
                        if (days <= expiryAlertDays) {
                            System.out.println("  >> [HOSP WARNING] Unit " + bu.getBloodUnitId() + " (" + bu.getBloodGroup() + ") in " + h.getOrganizationName() + " expires in " + days + " days (Date: " + bu.getExpiryDate() + ")");
                            alertGenerated = true;
                        }
                    } catch (DateTimeParseException ignored) {}
                }
            }
        }

        if (!alertGenerated) {
            System.out.println("  >> No expired or near-expiry blood units found.");
        }
    }

    public void generateAlert() {
        System.out.println("[ALERT] Critical system notifications checked.");
    }
}
