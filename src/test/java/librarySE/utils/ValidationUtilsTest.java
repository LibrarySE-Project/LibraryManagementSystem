package librarySE.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilsTest {

    // =========================================================================
    // requireNonEmpty tests
    // =========================================================================

    @Test
    void requireNonEmpty_throwsWhenNullNonString() {
        assertThrows(IllegalArgumentException.class,
                () -> ValidationUtils.requireNonEmpty(null, "Field"));
    }

    @Test
    void requireNonEmpty_acceptsNullStringObject() {
        // value == null BUT instanceof String is false → SHOULD throw
        assertThrows(IllegalArgumentException.class,
                () -> ValidationUtils.requireNonEmpty(null, "Field"));
    }

    @Test
    void requireNonEmpty_acceptsValidString() {
        assertDoesNotThrow(() ->
                ValidationUtils.requireNonEmpty("Hello", "Field"));
    }

    @Test
    void requireNonEmpty_throwsOnEmptyString() {
        assertThrows(IllegalArgumentException.class,
                () -> ValidationUtils.requireNonEmpty("", "Field"));
    }

    @Test
    void requireNonEmpty_throwsOnBlankSpaces() {
        assertThrows(IllegalArgumentException.class,
                () -> ValidationUtils.requireNonEmpty("   ", "Field"));
    }

    @Test
    void requireNonEmpty_throwsOnTabsAndNewlines() {
        assertThrows(IllegalArgumentException.class,
                () -> ValidationUtils.requireNonEmpty(" \n\t ", "Field"));
    }

    @Test
    void requireNonEmpty_acceptsNonStringObject() {
        assertDoesNotThrow(() ->
                ValidationUtils.requireNonEmpty(123, "Age"));
    }

    // =========================================================================
    // validateEmail tests
    // =========================================================================

    @Test
    void validateEmail_throwsOnNull() {
        assertThrows(IllegalArgumentException.class,
                () -> ValidationUtils.validateEmail(null));
    }

    @Test
    void validateEmail_throwsOnEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> ValidationUtils.validateEmail(" "));
    }

    @Test
    void validateEmail_throwsOnMissingAt() {
        assertThrows(IllegalArgumentException.class,
                () -> ValidationUtils.validateEmail("testgmail.com"));
    }

    @Test
    void validateEmail_throwsOnInvalidExtension() {
        assertThrows(IllegalArgumentException.class,
                () -> ValidationUtils.validateEmail("user@domain.xyz"));
    }

    @Test
    void validateEmail_acceptsValidDomains() {
        assertDoesNotThrow(() -> ValidationUtils.validateEmail("user@test.com"));
        assertDoesNotThrow(() -> ValidationUtils.validateEmail("user@test.net"));
        assertDoesNotThrow(() -> ValidationUtils.validateEmail("user@test.edu"));
        assertDoesNotThrow(() -> ValidationUtils.validateEmail("user@test.org"));
        assertDoesNotThrow(() -> ValidationUtils.validateEmail("user@najah.ps"));
    }

    @Test
    void validateEmail_trimsBeforeValidation() {
        assertDoesNotThrow(() ->
                ValidationUtils.validateEmail("   user@najah.edu   "));
    }

    // =========================================================================
    // validatePassword tests
    // =========================================================================

    @Test
    void validatePassword_throwsOnNull() {
        assertThrows(IllegalArgumentException.class,
                () -> ValidationUtils.validatePassword(null));
    }

    @Test
    void validatePassword_throwsWhenTooShort() {
        assertThrows(IllegalArgumentException.class,
                () -> ValidationUtils.validatePassword("a1"));
    }

    @Test
    void validatePassword_throwsWhenMissingDigit() {
        assertThrows(IllegalArgumentException.class,
                () -> ValidationUtils.validatePassword("abcdef"));
    }

    @Test
    void validatePassword_throwsWhenMissingLetter() {
        assertThrows(IllegalArgumentException.class,
                () -> ValidationUtils.validatePassword("123456"));
    }

    @Test
    void validatePassword_acceptsStrongPassword() {
        assertDoesNotThrow(() ->
                ValidationUtils.validatePassword("abc123"));
    }

    @Test
    void validatePassword_acceptsSpecialCharacters() {
        assertDoesNotThrow(() ->
                ValidationUtils.validatePassword("Abc12@"));
    }
    @Test
    void requireNonEmpty_throwsWhenNullValue() {
        assertThrows(IllegalArgumentException.class, () ->
                ValidationUtils.requireNonEmpty(null, "Field")
        );
    }


}
