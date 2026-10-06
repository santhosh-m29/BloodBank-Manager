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
        throw new UnsupportedOperationException("Low-stock alert system is planned for Phase 2 and is not part of the current 50% implementation.");
    }
    public List<String> checkExpiry(String facility, Inventory inventory, LocalDate today) {
        throw new UnsupportedOperationException("Near-expiry alert system is planned for Phase 2 and is not part of the current 50% implementation.");
    }
    public String generateAlert(String message, String severity) {
        return severity + " | " + message;
    }
}
