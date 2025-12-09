package librarySE.utils;

import java.util.regex.Pattern;

/**
 * <p><b>ValidationUtils</b> is a utility class that centralizes all input
 * validation logic used throughout the Library Management System.</p>
 *
 * <p>The class provides static, thread-safe methods for validating:</p>
 * <ul>
 *     <li>Non-empty strings and object fields</li>
 *     <li>Email address correctness using a predefined regex pattern</li>
 *     <li>Password strength based on system security rules</li>
 * </ul>
 * @author Eman
 */
public final class ValidationUtils {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.(com|net|edu|org|ps)$"
    );

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{6,}$"
    );

    private ValidationUtils() {}

    /**
     * Validates that a given value is not {@code null}, and if it is a string,
     * ensures it is not empty or whitespace only.
     */
    public static void requireNonEmpty(Object value, String field) {
        if (value == null)
            throw new IllegalArgumentException(field + " must not be null.");

        if (value instanceof String str && str.trim().isBlank())
            throw new IllegalArgumentException(field + " must not be empty or blank.");
    }

    /**
     * Validates an email address according to the system's strict format rules.
     */
    public static void validateEmail(String email) {
        if (email == null)
            throw new IllegalArgumentException("Email cannot be null.");

        String trimmed = email.trim().toLowerCase();

        if (!EMAIL_PATTERN.matcher(trimmed).matches())
            throw new IllegalArgumentException(
                    "Invalid email format. Example: name@gmail.com or student@najah.edu"
            );
    }

    /**
     * Validates password strength based on the system's security requirements.
     */
    public static void validatePassword(String password) {
        if (password == null)
            throw new IllegalArgumentException("Password cannot be null.");

        String normalized = password.trim();

        if (!PASSWORD_PATTERN.matcher(normalized).matches())
            throw new IllegalArgumentException(
                "Password must be at least 6 characters long and include both letters and digits."
            );
    }
}
