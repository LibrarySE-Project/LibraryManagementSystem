package librarySE;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a journal (magazine or periodical) in the library system.
 * <p>
 * Each Journal has a unique identifier, a title, an editor, an issue number, and an availability status.
 * Implements {@link LibraryItem} to support borrowing and returning functionality.
 * </p>
 * 
 * <p>
 * Thread-safe operations are provided for checking availability, borrowing, and returning the journal.
 * </p>
 * 
 * @author Eman
 * @see LibraryItem
 * @see MaterialType
 */
public class Journal implements LibraryItem {

    /** Unique identifier for the journal (UUID). */
    private final UUID id = UUID.randomUUID();

    /** The title of the journal. */
    private String title;

    /** The editor of the journal. */
    private String editor;

    /** The issue number of the journal. */
    private String issueNumber;

    /** Availability status of the journal; true if available for borrowing. */
    private boolean available = true;

    /**
     * Constructs a new {@code Journal} with the given title, editor, and issue number.
     *
     * @param title the title of the journal; must not be null or empty
     * @param editor the editor of the journal; must not be null or empty
     * @param issueNumber the issue number; must not be null or empty
     * @throws IllegalArgumentException if title, editor, or issueNumber is null or empty
     */
    public Journal(String title, String editor, String issueNumber) {
        validateNonEmpty(title, "Title");
        validateNonEmpty(editor, "Editor");
        validateNonEmpty(issueNumber, "IssueNumber");
        this.title = title.trim();
        this.editor = editor.trim();
        this.issueNumber = issueNumber.trim();
    }

    /** Sets a new title for the journal. */
    public void setTitle(String title) { 
        validateNonEmpty(title, "Title"); 
        this.title = title.trim(); 
    }

    /** Sets a new editor for the journal. */
    public void setEditor(String editor) {
        validateNonEmpty(editor, "Editor");
        this.editor = editor.trim();
    }

    /** Sets a new issue number for the journal. */
    public void setIssueNumber(String issueNumber) { 
        validateNonEmpty(issueNumber, "IssueNumber"); 
        this.issueNumber = issueNumber.trim(); 
    }

    /** Returns the title of the journal. */
    public String getTitle() { return title; }

    /** Returns the editor of the journal. */
    public String getEditor() { return editor; }

    /** Returns the issue number of the journal. */
    public String getIssueNumber() { return issueNumber; }

    /** Checks if the journal is available for borrowing. */
    @Override
    public boolean isAvailable() { synchronized(this) { return available; } }

    /** Marks the journal as borrowed if it is available. */
    @Override
    public boolean borrow() { synchronized(this) { if(!available) return false; available=false; return true; } }

    /** Marks the journal as returned if it was borrowed. */
    @Override
    public boolean returnItem() { synchronized(this) { if(available) return false; available=true; return true; } }

    /** Returns the material type of this item. */
    @Override
    public MaterialType getMaterialType() { return MaterialType.JOURNAL; }

    /** Checks equality based on unique ID. */
    @Override
    public boolean equals(Object o) { return this==o || (o instanceof Journal j && id.equals(j.id)); }

    /** Returns hash code based on unique ID. */
    @Override
    public int hashCode() { return Objects.hash(id); }

    /** Returns a string representation of the journal. */
    @Override
    public String toString() {
        return title + " [" + issueNumber + "] Edited by: " + editor + (available ? " [AVAILABLE]" : " [BORROWED]");
    }

    /** Validates that a string is not null or empty. */
    private void validateNonEmpty(String value, String fieldName) {
        if(value==null || value.trim().isEmpty())
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
    }

    /**
     * Checks if the journal matches the given keyword.
     * <p>
     * A journal matches if the keyword is found (case-insensitive) in the title,
     * editor, or issue number.
     * </p>
     *
     * @param keyword the keyword to search for; must not be null
     * @return {@code true} if the journal matches the keyword, {@code false} otherwise
     * @throws IllegalArgumentException if {@code keyword} is null
     */
    @Override
    public boolean matchesKeyword(String keyword) {
        if (keyword == null) {
            throw new IllegalArgumentException("Keyword cannot be null");
        }
        String lower = keyword.toLowerCase();
        return title.toLowerCase().contains(lower)
            || editor.toLowerCase().contains(lower)
            || issueNumber.toLowerCase().contains(lower);
    }
}

