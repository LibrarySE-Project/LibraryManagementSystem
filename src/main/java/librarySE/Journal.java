package librarySE;

import java.util.Objects;

/**
 * Represents a Journal item in the library.
 * <p>
 * A Journal has a title and an issue number, and can be borrowed or returned.
 * Availability is tracked internally. Implements the {@link LibraryItem} interface.
 * </p>
 * 
 * @author Malak
 */
public class Journal implements LibraryItem {

    /** The title of the journal */
    private String title;

    /** The issue number of the journal */
    private String issueNumber;

    /** Indicates if the journal is currently available for borrowing */
    private boolean available = true;

    /**
     * Constructs a Journal with the given title and issue number.
     *
     * @param title the title of the journal; must not be null or empty
     * @param issueNumber the issue number; must not be null or empty
     * @throws IllegalArgumentException if title or issueNumber is null/empty
     */
    public Journal(String title, String issueNumber) {
        if (title == null || title.trim().isEmpty()) throw new IllegalArgumentException("Title cannot be empty");
        if (issueNumber == null || issueNumber.trim().isEmpty()) throw new IllegalArgumentException("Issue number cannot be empty");
        this.title = title.trim();
        this.issueNumber = issueNumber.trim();
    }

    /** Sets a new title for the journal */
    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty())
            throw new IllegalArgumentException("Title cannot be null or empty");
        this.title = title.trim();
    }

    /** Sets a new issue number for the journal */
    public void setIssueNumber(String issueNumber) {
        if (issueNumber == null || issueNumber.trim().isEmpty())
            throw new IllegalArgumentException("Issue number cannot be null or empty");
        this.issueNumber = issueNumber.trim();
    }

    /** Returns the title of the journal */
    public String getTitle() { return title; }

    /** Returns the issue number of the journal */
    public String getIssueNumber() { return issueNumber; }

    /** Checks if the journal is available for borrowing */
    @Override
    public boolean isAvailable() {
        synchronized(this) { return available; }
    }
    /**
     * Attempts to borrow the journal in a thread-safe manner.
     * <p>
     * If the journal is currently available, it will be marked as borrowed and
     * this method returns {@code true}. If it is already borrowed (not available),
     * the method returns {@code false} without changing the state.
     * </p>
     *
     * @return {@code true} if the journal was successfully borrowed,
     *         {@code false} if the journal was already borrowed (not available)
     */
    @Override
    public boolean borrow() {
        synchronized(this) {
            if (!available) return false;
            available = false;
            return true;
        }
    }

    /**
     * Returns the journal to the library in a thread-safe manner.
     * <p>
     * If the journal was borrowed, it will be marked as available and
     * this method returns {@code true}. If it was already available,
     * the method returns {@code false} without changing the state.
     * </p>
     *
     * @return {@code true} if the journal was successfully returned,
     *         {@code false} if the journal was already available
     */
    @Override
    public boolean returnItem() {
        synchronized(this) {
            if (available) return false;
            available = true;
            return true;
        }
    }


    /** Returns the material type of this item */
    @Override
    public MaterialType getMaterialType() {
        return MaterialType.JOURNAL;
    }

    /** Compares two Journals for equality based on title and issue number */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Journal)) return false;
        Journal j = (Journal) o;
        return title.equalsIgnoreCase(j.title) && issueNumber.equalsIgnoreCase(j.issueNumber);
    }

    /** Returns the hash code for the journal (based on title and issue number, case-insensitive) */
    @Override
    public int hashCode() {
        return Objects.hash(title.toLowerCase(), issueNumber.toLowerCase());
    }

    /** Returns a string representation of the journal with availability status */
    @Override
    public String toString() {
        return title + " [" + issueNumber + "]" + (available ? " [AVAILABLE]" : " [BORROWED]");
    }
}
