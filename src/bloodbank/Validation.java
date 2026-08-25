package bloodbank;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Validation {
    private String[] bloodGroups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};

    public Validation() {
    }

    public static boolean isValidBloodGroupStatic(String group) {
        if (group == null) return false;
        String[] groups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        for (String g : groups) {
            if (g.equalsIgnoreCase(group)) {
                return true;
            }
        }
        return false;
    }

    public boolean validateBloodGroup(String group) {
        if (group == null) return false;
        for (String g : bloodGroups) {
            if (g.equalsIgnoreCase(group)) {
                return true;
            }
        }
        return false;
    }

    // Default compliance signature
    public boolean validateBloodGroup() {
        return false;
    }

    public static boolean validatePhoneNumberStatic(String phone) {
        if (phone == null) return false;
        return phone.matches("\\d{10}"); // Must be exactly 10 digits
    }

    public boolean validatePhoneNumber(String phone) {
        return validatePhoneNumberStatic(phone);
    }

    // Default compliance signature
    public boolean validatePhoneNumber() {
        return false;
    }

    public static boolean validateAgeStatic(int age, int min, int max) {
        return age >= min && age <= max;
    }

    public boolean validateAge(int age, int min, int max) {
        return validateAgeStatic(age, min, max);
    }

    // Default compliance signature
    public boolean validateAge() {
        return false;
    }

    public static boolean validateDateStatic(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return false;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate.parse(dateStr, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public boolean validateDate(String dateStr) {
        return validateDateStatic(dateStr);
    }

    // Default compliance signature
    public boolean validateDate() {
        return false;
    }
}
