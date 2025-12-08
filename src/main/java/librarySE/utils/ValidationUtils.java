package librarySE.utils;

import java.util.regex.Pattern;

/**
 * Utility class for validating user inputs across the library system.
 * <p>
 * Provides centralized validation methods for checking string emptiness,
 * email format, password strength, and general input consistency.
 * </p>
 *
 * <p>All methods are static and thread-safe.</p>
 *
 * @author Eman
 * 
 */
public final class ValidationUtils {

    /** Regular expression pattern for validating email addresses. */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.(com|net|edu|org|ps)$"
    );

    /** Regex for simple password strength (at least one letter and one digit). */
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{6,}$"
    );

    /** Private constructor to prevent instantiation. */
    private ValidationUtils() {}


    /**
     * Ensures that a value is not {@code null}, and if it's a string,
     * ensures it is not empty after trimming.
     *
     * @param value the value to validate
     * @param field the field name for error messages
     * @throws IllegalArgumentException if {@code value} is {@code null} or empty
     */
    public static void requireNonEmpty(Object value, String field) {
        if (value == null)
            throw new IllegalArgumentException(field + " must not be null.");

        if (value instanceof String str && str.trim().isBlank())
            throw new IllegalArgumentException(field + " must not be empty or blank.");
    }


    /**
     * Validates an email address against a fixed pattern.
     * Accepts .com, .net, .edu, .org, .ps domains.
     *
     * @param email the email address to validate
     * @throws IllegalArgumentException if invalid
     */
    public static void validateEmail(String email) {
        if (email == null)
            throw new IllegalArgumentException("Email cannot be null.");

        String trimmed = email.trim().toLowerCase();

        if (!EMAIL_PATTERN.matcher(trimmed).matches())
            throw new IllegalArgumentException(
                    "Invalid email format. Example: name@gmail.com or user@najah.edu"
            );
    }

    /**
     * Validates password strength and minimum security requirements.
     * <p>
     * Rules:
     * <ul>
     *   <li>At least 6 characters long</li>
     *   <li>Contains at least one letter and one digit</li>
     * </ul>
     * </p>
     *
     * @param password the password string to validate
     * @throws IllegalArgumentException if the password is weak or invalid
     */
    public static void validatePassword(String password) {
        if (password == null)
            throw new IllegalArgumentException("Password cannot be null.");

        if (!PASSWORD_PATTERN.matcher(password).matches())
            throw new IllegalArgumentException(
                "Password must be at least 6 characters long and include both letters and digits."
            );
    }
}
