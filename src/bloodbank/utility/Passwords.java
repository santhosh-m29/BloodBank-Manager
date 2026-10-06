package bloodbank.utility;
import java.security.*;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
public final class Passwords {
    private Passwords() {
    }
    public static String hash(String password) {
        Validation.require(password != null && password.length() >= 8 && password.length() <= 128, "Password must contain 8 to 128 characters.");
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(derive(password, salt));
    }
    public static boolean matches(String password, String encoded) {
        if (password == null || password.length() > 128) return false;
        String[] parts = encoded.split(":");
        return MessageDigest.isEqual(Base64.getDecoder().decode(parts[1]), derive(password, Base64.getDecoder().decode(parts[0])));
    }
    private static byte[] derive(String password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 120000, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Password hashing unavailable.", ex);
        } finally {
            spec.clearPassword();
        }
    }
}
