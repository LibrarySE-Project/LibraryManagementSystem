package librarySE.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilsTest {

    @Test
    void requireNonEmpty_throwsOnNull() {
        assertThrows(IllegalArgumentException.class, () ->
                ValidationUtils.requireNonEmpty(null, "Field")
        );
    }

    @Test
    void requireNonEmpty_throwsOnEmptyString() {
        assertThrows(IllegalArgumentException.class, () ->
                ValidationUtils.requireNonEmpty("   ", "Field")
        );
    }

    @Test
    void requireNonEmpty_acceptsValidString() {
        assertDoesNotThrow(() ->
                ValidationUtils.requireNonEmpty("Hello", "Field")
        );
    }

    @Test
    void validateEmail_throwsOnNull() {
        assertThrows(IllegalArgumentException.class, () ->
                ValidationUtils.validateEmail(null)
        );
    }

    @Test
    void validateEmail_throwsOnInvalidEmail() {
        assertThrows(IllegalArgumentException.class, () ->
                ValidationUtils.validateEmail("invalid@wrong.xyz")
        );
    }

    @Test
    void validateEmail_acceptsValidEmails() {
        assertDoesNotThrow(() ->
                ValidationUtils.validateEmail("test@najah.edu")
        );
    }

    @Test
    void validatePassword_throwsOnNull() {
        assertThrows(IllegalArgumentException.class, () ->
                ValidationUtils.validatePassword(null)
        );
    }

    @Test
    void validatePassword_throwsOnWeakPassword() {
        assertThrows(IllegalArgumentException.class, () ->
                ValidationUtils.validatePassword("abc")
        );
    }

    @Test
    void validatePassword_acceptsStrongPassword() {
        assertDoesNotThrow(() ->
                ValidationUtils.validatePassword("abc123")
        );
    }
}

