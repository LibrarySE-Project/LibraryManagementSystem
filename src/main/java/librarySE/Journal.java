package librarySE;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a {@link LibraryItem} of type Journal (magazine or periodical) in the library system.
 * <p>
 * Each {@code Journal} contains identifying and descriptive information such as
 * a unique ID, title, editor, and issue number. It inherits shared functionality
 * like borrowing, returning, and availability control from {@link AbstractLibraryItem}.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 *     <li>Encapsulates core metadata for journals (title, editor, issue number).</li>
 *     <li>Automatically assigns a unique {@link UUID} to each journal instance.</li>
 *     <li>Provides keyword-based search support across all descriptive fields.</li>
 *     <li>Integrates seamlessly with {@link BorrowManager} and fine management via {@link FineContext}.</li>
 * </ul>
 *
 * <p>
 * This class adheres to the <b>Single Responsibility Principle (SRP)</b> —
 * it focuses solely on representing a journal item and its identifying data,
 * while borrowing and fine-handling logic are delegated to
 * {@link AbstractLibraryItem} and {@link BorrowManager}.
 * </p>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * Journal j = new Journal("AI Research", "Dr. Smith", "Vol. 15, Issue 2");
 * System.out.println(j.getTitle());       // "AI Research"
 * System.out.println(j.getEditor());      // "Dr. Smith"
 * System.out.println(j.getIssueNumber()); // "Vol. 15, Issue 2"
 * j.borrow();
 * System.out.println(j.isAvailable());    // false
 * }</pre>
 *
 * @see LibraryItem
 * @see AbstractLibraryItem
 * @see MaterialType
 * @see BorrowManager
 * @see FineContext
 * @see JournalFineStrategy
 * 
 * @author Eman
 */
public class Journal extends AbstractLibraryItem {

    /** Unique identifier for this journal (auto-generated). */
    private final UUID id = UUID.randomUUID();

    /** The title of the journal. */
    private String title;

    /** The editor of the journal. */
    private String editor;

    /** The issue number or volume identifier for the journal. */
    private String issueNumber;

    /**
     * Constructs a new {@code Journal} instance with the specified details.
     *
     * @param title       the title of the journal; must not be {@code null} or empty
     * @param editor      the editor's name; must not be {@code null} or empty
     * @param issueNumber the issue or volume number; must not be {@code null} or empty
     * @throws IllegalArgumentException if any argument is {@code null} or empty
     */
    public Journal(String title, String editor, String issueNumber) {
        validateNonEmpty(title, "Title");
        validateNonEmpty(editor, "Editor");
        validateNonEmpty(issueNumber, "Issue Number");
        this.title = title.trim();
        this.editor = editor.trim();
        this.issueNumber = issueNumber.trim();
    }

    /**
     * Returns the title of this journal.
     *
     * @return the title as a {@link String}
     */
    @Override
    public String getTitle() { return title; }

    /**
     * Returns the editor of this journal.
     *
     * @return the editor's name as a {@link String}
     */
    public String getEditor() { return editor; }

    /**
     * Returns the issue number or volume identifier.
     *
     * @return the issue number as a {@link String}
     */
    public String getIssueNumber() { return issueNumber; }

    /**
     * Updates the title of this journal.
     *
     * @param title the new title; must not be {@code null} or empty
     * @throws IllegalArgumentException if {@code title} is {@code null} or empty
     */
    public void setTitle(String title) {
        validateNonEmpty(title, "Title");
        this.title = title.trim();
    }

    /**
     * Updates the editor's name of this journal.
     *
     * @param editor the new editor name; must not be {@code null} or empty
     * @throws IllegalArgumentException if {@code editor} is {@code null} or empty
     */
    public void setEditor(String editor) {
        validateNonEmpty(editor, "Editor");
        this.editor = editor.trim();
    }

    /**
     * Updates the issue number of this journal.
     *
     * @param issueNumber the new issue number; must not be {@code null} or empty
     * @throws IllegalArgumentException if {@code issueNumber} is {@code null} or empty
     */
    public void setIssueNumber(String issueNumber) {
        validateNonEmpty(issueNumber, "Issue Number");
        this.issueNumber = issueNumber.trim();
    }

    /**
     * Returns the material type of this item.
     *
     * @return {@link MaterialType#JOURNAL}
     */
    @Override
    public MaterialType getMaterialType() { return MaterialType.JOURNAL; }

    /**
     * Checks whether this journal matches a given keyword.
     * <p>
     * A match occurs if the keyword (case-insensitive) appears in the title,
     * editor, or issue number.
     * </p>
     *
     * @param keyword the keyword to search for; must not be {@code null}
     * @return {@code true} if this journal matches the keyword; {@code false} otherwise
     * @throws IllegalArgumentException if {@code keyword} is {@code null}
     */
    @Override
    public boolean matchesKeyword(String keyword) {
        if (keyword == null)
            throw new IllegalArgumentException("Keyword cannot be null.");
        String lower = keyword.toLowerCase();
        return String.join(" ", title, editor, issueNumber).toLowerCase().contains(lower);
    }

    /**
     * Compares this journal to another object for equality.
     * <p>
     * Two journals are considered equal if they have the same unique identifier.
     * </p>
     *
     * @param obj the object to compare with
     * @return {@code true} if the other object is a {@code Journal} with the same ID
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof Journal j && id.equals(j.id);
    }

    /**
     * Returns a hash code value for this journal based on its unique ID.
     *
     * @return the hash code for this journal
     */
    @Override
    public int hashCode() { return Objects.hash(id); }

    /**
     * Returns a formatted string representation of this journal,
     * including its title, issue number, editor, and availability status.
     *
     * @return a human-readable string describing this journal
     */
    @Override
    public String toString() {
        return String.format("[JOURNAL] %s [%s] edited by %s — %s",
                title, issueNumber, editor, isAvailable() ? "Available" : "Borrowed");
    }
}
