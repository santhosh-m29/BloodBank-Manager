package bloodbank.service;
import java.time.LocalDate;
import java.util.*;
import bloodbank.model.*;
import bloodbank.utility.Validation;
public final class AlertManager {
    private final int lowStockThreshold, expiryAlertDays;
    public AlertManager(int threshold, int days) {
        Validation.require(threshold >= 0 && days >= 0, "Alert settings cannot be negative.");
        lowStockThreshold = threshold;
        expiryAlertDays = days;
    }
    public List<String> checkLowStock(String facility, Inventory inventory, LocalDate today) {
        List<String> alerts = new ArrayList<>();
        for (String group : Validation.BLOOD_GROUPS) {
            int free = inventory.getStockForGroup(group, today);
            if (free < lowStockThreshold) alerts.add(generateAlert(facility + ": " + group + " has " + free + " unreserved units (threshold " + lowStockThreshold + ").", "LOW STOCK"));
        }
        return alerts;
    }
    public List<String> checkExpiry(String facility, Inventory inventory, LocalDate today) {
        List<String> alerts = new ArrayList<>();
        for (BloodUnit unit : inventory.getBloodUnits()) {
            if (unit.getStatus().equals("ISSUED") || unit.getStatus().equals("REJECTED")) continue;
            if (unit.isExpired(today)) alerts.add(generateAlert(facility + ": " + unit.getBloodUnitId() + " expired on " + unit.getExpiryDate(), "EXPIRED"));
            else if (!unit.getExpiryDate().isAfter(today.plusDays(expiryAlertDays))) alerts.add(generateAlert(facility + ": " + unit.getBloodUnitId() + " expires " + unit.getExpiryDate(), "EXPIRING"));
        }
        return alerts;
    }
    public String generateAlert(String message, String severity) {
        return severity + " | " + message;
    }
}
