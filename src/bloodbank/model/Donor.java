package bloodbank.model;

import bloodbank.Main;
import bloodbank.utility.Validation;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;

public class Donor extends Person {
    private String bloodGroup;
    private double haemoglobin;
    private double weight;
    private String lastDonationDate; // Format: YYYY-MM-DD
    private boolean eligible;

    public Donor(String personId, String name, int age, String gender, String phoneNumber, String address,
                 String username, String password, String role, String bloodGroup, double haemoglobin,
                 double weight, String lastDonationDate) {
        super(personId, name, age, gender, phoneNumber, address, username, password, role);
        this.bloodGroup = bloodGroup;
        this.haemoglobin = haemoglobin;
        this.weight = weight;
        this.lastDonationDate = lastDonationDate;
        this.eligible = checkEligibility();
    }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public double getHaemoglobin() { return haemoglobin; }
    public void setHaemoglobin(double haemoglobin) { this.haemoglobin = haemoglobin; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public String getLastDonationDate() { return lastDonationDate; }
    public void setLastDonationDate(String lastDonationDate) { this.lastDonationDate = lastDonationDate; }

    public boolean isEligible() {
        this.eligible = checkEligibility();
        return eligible;
    }
    public void setEligible(boolean eligible) { this.eligible = eligible; }

    @Override
    public void updateDetails() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Updating details for Donor: " + name);
        System.out.print("Enter New Name (Current: " + name + "): ");
        String newName = sc.nextLine().trim();
        if (!newName.isEmpty()) this.name = newName;

        System.out.print("Enter New Age (Current: " + age + "): ");
        String ageStr = sc.nextLine().trim();
        if (!ageStr.isEmpty()) {
            try {
                this.age = Integer.parseInt(ageStr);
            } catch (NumberFormatException ignored) {}
        }

        System.out.print("Enter New Gender (Current: " + gender + "): ");
        String newGender = sc.nextLine().trim();
        if (!newGender.isEmpty()) this.gender = newGender;

        System.out.print("Enter New Phone (Current: " + phoneNumber + "): ");
        String phone = sc.nextLine().trim();
        if (!phone.isEmpty() && Validation.validatePhoneNumber(phone)) {
            this.phoneNumber = phone;
        }

        System.out.print("Enter New Address (Current: " + address + "): ");
        String newAddr = sc.nextLine().trim();
        if (!newAddr.isEmpty()) this.address = newAddr;

        System.out.print("Enter New Haemoglobin level (Current: " + haemoglobin + "): ");
        String hbStr = sc.nextLine().trim();
        if (!hbStr.isEmpty()) {
            try {
                this.haemoglobin = Double.parseDouble(hbStr);
            } catch (NumberFormatException ignored) {}
        }

        System.out.print("Enter New Weight (Current: " + weight + "): ");
        String wStr = sc.nextLine().trim();
        if (!wStr.isEmpty()) {
            try {
                this.weight = Double.parseDouble(wStr);
            } catch (NumberFormatException ignored) {}
        }

        this.eligible = checkEligibility();
        System.out.println("Donor details updated. Eligibility status: " + (this.eligible ? "ELIGIBLE" : "INELIGIBLE"));
    }

    @Override
    public void displayDetails() {
        System.out.println("----------------------------------------");
        System.out.println("Donor Details:");
        System.out.println("ID: " + personId);
        System.out.println("Name: " + name);
        System.out.println("Age: " + age);
        System.out.println("Gender: " + gender);
        System.out.println("Phone: " + phoneNumber);
        System.out.println("Address: " + address);
        System.out.println("Blood Group: " + bloodGroup);
        System.out.println("Haemoglobin: " + haemoglobin + " g/dL");
        System.out.println("Weight: " + weight + " kg");
        System.out.println("Last Donation Date: " + (lastDonationDate == null || lastDonationDate.isEmpty() ? "None" : lastDonationDate));
        System.out.println("Eligible: " + (isEligible() ? "YES" : "NO"));
        System.out.println("----------------------------------------");
    }

    public boolean checkEligibility() {
        // Business Rules:
        // - Age: 18 to 65
        // - Weight: >= 50.0 kg
        // - Haemoglobin: >= 12.5 g/dL
        // - Blood Group: Must be valid
        // - Last Donation: Must be >= 90 days ago (or empty if first time)
        
        if (age < 18 || age > 65) return false;
        if (weight < 50.0) return false;
        if (haemoglobin < 12.5) return false;
        if (!Validation.validateBloodGroup(bloodGroup)) return false;
        
        if (lastDonationDate != null && !lastDonationDate.trim().isEmpty()) {
            try {
                LocalDate lastDate = LocalDate.parse(lastDonationDate.trim());
                LocalDate now = LocalDate.now();
                long days = ChronoUnit.DAYS.between(lastDate, now);
                if (days < 90) {
                    return false; // Not eligible if less than 90 days
                }
            } catch (DateTimeParseException e) {
                // If parsing fails, fall back to eligible assuming date is invalid/empty seed format
            }
        }
        
        return true;
    }

    public void viewDonationHistory() {
        System.out.println("Donation History for Donor: " + name + " (ID: " + personId + ")");
        boolean found = false;
        for (BloodDonation donation : Main.donations) {
            if (donation.getDonorId().equals(this.personId)) {
                donation.displayTransaction();
                found = true;
            }
        }
        if (!found) {
            System.out.println("No donation records found for this donor.");
        }
    }

    public BloodDonation donate(Scanner scanner) {
        System.out.println("\n--- Donate Blood ---");
        if (!isEligible()) {
            System.out.println("Donation cannot be submitted. Donor is not eligible.");
            System.out.println("Details: Weight: " + weight + "kg | Hb: " + haemoglobin
                    + "g/dL | Last donation: " + lastDonationDate);
            return null;
        }
        System.out.print("Enter Quantity collected (Units, e.g. 1 unit): ");
        int quantity;
        try {
            quantity = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Quantity must be a number.");
            return null;
        }
        if (quantity <= 0) {
            System.out.println("Quantity must be positive.");
            return null;
        }
        String result;
        do {
            System.out.print("Simulated lab test result (PASS/FAIL): ");
            result = scanner.nextLine().trim();
        } while (!"PASS".equalsIgnoreCase(result) && !"FAIL".equalsIgnoreCase(result));

        String today = LocalDate.now().toString();
        String transactionId = "DON_TXN_" + System.currentTimeMillis();
        String unitId = "UNIT_" + System.currentTimeMillis();
        BloodDonation donation = new BloodDonation(transactionId, today, "PENDING_TEST",
                personId, bloodGroup, quantity, unitId);
        donation.recordDonation();

        BloodUnit unit = new BloodUnit(unitId, bloodGroup, quantity, today,
                LocalDate.now().plusDays(42).toString(), "PENDING_TEST");
        if (unit.runLabTests("PASS".equalsIgnoreCase(result))) {
            unit.setStatus("AVAILABLE");
            donation.setStatus("Completed");
            Main.inventory.addBloodUnit(unit);
            System.out.println("Passed blood was added to the central blood bank stock.");
        } else {
            donation.setStatus("REJECTED");
            System.out.println("Failed blood was not added to stock.");
        }
        Main.donations.add(donation);
        lastDonationDate = today;
        eligible = checkEligibility();
        System.out.println("Donation saved. Donation ID: " + donation.getTransactionId()
                + " | Blood unit: " + donation.getBloodUnitId() + " | Status: " + donation.getStatus());
        Main.saveDonationsToFile();
        Main.saveBloodUnitsToFile();
        Main.saveDonorsToFile();
        return donation;
    }
}
