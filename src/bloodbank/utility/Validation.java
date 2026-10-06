package bloodbank.utility;
import java.time.LocalDate;
import java.util.List;
public final class Validation {
    public static final List<String> BLOOD_GROUPS = List.of("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");
    private Validation() {
    }
    public static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
    public static String text(String value, String label) {
        require(value != null && !value.isBlank() && value.length() <= 500 && !value.contains("\n") && !value.contains("\r"), label + " is required (maximum 500 characters, one line).");
        return value.trim();
    }
    public static String id(String value) {
        require(value != null && value.matches("[A-Za-z0-9_-]{1,100}"), "Invalid ID or username.");
        return value;
    }
    public static boolean validateBloodGroup(String value) {
        return BLOOD_GROUPS.contains(value);
    }
    public static boolean validatePhoneNumber(String value) {
        return value != null && value.matches("[0-9]{10}");
    }
    public static boolean validateAge(int value) {
        return value >= 0 && value <= 120;
    }
    public static boolean validateDate(String value) {
        try {
            LocalDate.parse(value);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }
    public static String bloodGroup(String value) {
        require(validateBloodGroup(value), "Invalid blood group.");
        return value;
    }
    public static int positive(int value) {
        require(value > 0 && value <= 10000, "Quantity must be between 1 and 10000.");
        return value;
    }
    public static double positive(double value) {
        require(Double.isFinite(value) && value > 0, "Measurement must be finite and positive.");
        return value;
    }
}
