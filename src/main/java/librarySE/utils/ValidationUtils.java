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
 *
 * <p>This ensures all modules (Login, UserManager, AdminPanel, etc.) rely on
 * a single, consistent validation mechanism.</p>
 *
 * <p><b>Design notes:</b></p>
 * <ul>
 *     <li>The class is {@code final} and has a private constructor → cannot be instantiated.</li>
 *     <li>All methods are {@code static} → convenient for global use.</li>
 *     <li>The validators throw {@link IllegalArgumentException} when invalid.</li>
 * </ul>
 *
 * @author Eman
 * @version 1.1
 */
public final class ValidationUtils {

    /**
     * Regular expression for validating email addresses.
     * <p>
     * Accepted domains:
     * <ul>
     *     <li>.com</li>
     *     <li>.net</li>
     *     <li>.edu</li>
     *     <li>.org</li>
     *     <li>.ps</li>
     * </ul>
     * <p>
     * This keeps the validation strict but still suitable for academic emails
     * (e.g., student@najah.edu).
     * </p>
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.(com|net|edu|org|ps)$"
    );

    /**
     * Regular expression enforcing password security rules:
     * <ul>
     *     <li>Minimum length: 6 characters</li>
     *     <li>Contains at least one letter</li>
     *     <li>Contains at least one digit</li>
     *     <li>Allows optional symbols: @ $ ! % * # ? &</li>
     * </ul>
     * <p>
     * The rule matches the project requirement:
     * <i>"Password must include both letters and digits."</i>
     * </p>
     */
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{6,}$"
    );

    /** Private constructor — prevents instantiation. */
    private ValidationUtils() {}


    // ========================================================================
    //  requireNonEmpty
    // ========================================================================

    /**
     * Validates that a given value is not {@code null}, and if it is a string,
     * ensures it is not empty or whitespace only.
     *
     * <p>Typical use cases: validating username, title, item fields, etc.</p>
     *
     * @param value the value to validate (String or any object)
     * @param field the logical field name for error messages
     *
     * @throws IllegalArgumentException if:
     *     <ul>
     *         <li>{@code value} is {@code null}</li>
     *         <li>{@code value} is a blank string</li>
     *     </ul>
     */
    public static void requireNonEmpty(Object value, String field) {
        if (value == null)
            throw new IllegalArgumentException(field + " must not be null.");

        if (value instanceof String str && str.trim().isBlank())
            throw new IllegalArgumentException(field + " must not be empty or blank.");
    }


    // ========================================================================
    //  validateEmail
    // ========================================================================

    /**
     * Validates an email address according to the system's strict format rules.
     *
     * <p>The validation ensures the email:</p>
     * <ul>
     *     <li>Is not null or blank</li>
     *     <li>Matches the predefined {@link #EMAIL_PATTERN}</li>
     * </ul>
     *
     * <p>Examples of valid emails:</p>
     * <ul>
     *     <li>user@gmail.com</li>
     *     <li>student@najah.edu</li>
     *     <li>admin@organization.org</li>
     * </ul>
     *
     * @param email the email string to validate
     * @throws IllegalArgumentException if the email is null or invalid
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


    // ========================================================================
    //  validatePassword
    // ========================================================================

    /**
     * Validates password strength based on the system's security requirements.
     *
     * <p>Rules enforced by {@link #PASSWORD_PATTERN}:</p>
     * <ul>
     *     <li>Minimum length: 6 characters</li>
     *     <li>At least one letter</li>
     *     <li>At least one digit</li>
     *     <li>May include certain special symbols</li>
     * </ul>
     *
     * <p>Examples of valid passwords:</p>
     * <ul>
     *     <li>{@code pass123}</li>
     *     <li>{@code A1b2c3}</li>
     *     <li>{@code adm123456A9}</li>
     * </ul>
     *
     * <p>If validation fails, the method throws an exception with a clear, uniform message.</p>
     *
     * @param password the password string to validate
     *
     * @throws IllegalArgumentException if:
     *     <ul>
     *         <li>Password is null</li>
     *         <li>Password does not satisfy strength rules</li>
     *     </ul>
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
