package bloodbank.utility;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Validation {
    private static final String[] BLOOD_GROUPS = {
            "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
    };

    public static boolean validateBloodGroup(String group) {
        if (group == null) return false;
        for (String bloodGroup : BLOOD_GROUPS) {
            if (bloodGroup.equalsIgnoreCase(group.trim())) return true;
        }
        return false;
    }

    public static void checkBloodGroup(String group) throws InvalidBloodGroupException {
        if (!validateBloodGroup(group)) {
            throw new InvalidBloodGroupException("Invalid blood group. Enter a valid group such as A+ or O-.");
        }
    }

    public static boolean validatePhoneNumber(String phone) {
        if (phone == null) return false;
        return phone.matches("\\d{10}"); // Must be exactly 10 digits
    }

    public static boolean validateAge(int age, int min, int max) {
        return age >= min && age <= max;
    }

    public static boolean validateDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return false;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate.parse(dateStr, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

}
