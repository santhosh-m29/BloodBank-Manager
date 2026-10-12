package bloodbank.service;

import bloodbank.Main;
import bloodbank.model.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

public class AlertManager {
    private int expiryAlertDays;

    public AlertManager(int expiryAlertDays) {
        this.expiryAlertDays = expiryAlertDays;
    }

    public void displayHospitalStock() {
        String[] groups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        System.out.println("\n--- Available Stock at Each Hospital ---");
        System.out.printf("%-28s %-12s %-16s%n", "Hospital", "Blood Group", "Available Units");
        System.out.println("------------------------------------------------------------");
        for (Hospital h : Main.hospitals) {
            for (String g : groups) {
                int available = h.getInventory().getStockForGroup(g);
                System.out.printf("%-28s %-12s %-16d%n", h.getOrganizationName(), g, available);
            }
        }
        System.out.println("------------------------------------------------------------");
    }

    public void checkExpiry() {
        LocalDate now = LocalDate.now();
        boolean alertGenerated = false;

        // Check Hospitals' Units
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
}
