package librarySE.core;

import java.math.BigDecimal;

import librarySE.utils.Config;
import librarySE.utils.ValidationUtils;

/**
 * Represents a {@link LibraryItem} of type <b>Journal</b> (periodical or magazine)
 * within the library system.
 * <p>
 * Each journal includes identifying and descriptive information such as
 * a title, editor, and issue number. It also inherits thread-safe borrowing
 * and price management functionality from {@link AbstractLibraryItem}, and
 * supports tracking multiple physical copies via total and available counts.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 *   <li>Encapsulates metadata for journals (title, editor, issue number, price).</li>
 *   <li>Supports keyword-based search across all descriptive fields.</li>
 *   <li>Validated inputs via {@link ValidationUtils}.</li>
 *   <li>Tracks multiple copies (total vs available) for circulation control.</li>
 *   <li>Compatible with fine calculation strategies (default: 21 days, 15 NIS/day).</li>
 * </ul>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * Journal j = new Journal("AI Research Monthly", "Dr. Smith", "Vol. 15",
 *                         BigDecimal.valueOf(29.99), 3);
 * System.out.println(j);
 * // [JOURNAL] AI Research Monthly [Vol. 15] edited by Dr. Smith
 * // (Price:29.99, Available:3/3) — Available
 * }</pre>
 *
 * @author Malak
 * @see AbstractLibraryItem
 * @see MaterialType
 * @see ValidationUtils
 */
public class Journal extends AbstractLibraryItem {

    private static final long serialVersionUID = 1L;

    /**
     * The title of the journal.
     */
    private String title;

    /**
     * The editor of the journal.
     */
    private String editor;

    /**
     * The issue number or volume identifier.
     */
    private String issueNumber;

    /**
     * Total number of physical copies of this journal owned by the library.
     */
    private int totalCopies;

    /**
     * Number of copies currently available for borrowing.
     */
    private int availableCopies;

    /**
     * Initializes a {@code Journal} instance with validated metadata (title, editor, and issue number).
     * <p>
     * This constructor is used internally by other constructors to ensure
     * consistent validation and initialization of core fields.
     * </p>
     *
     * @param title       the title of the journal (non-null and non-empty)
     * @param editor      the editor of the journal (non-null and non-empty)
     * @param issueNumber the issue number or volume identifier (non-null and non-empty)
     * @throws IllegalArgumentException if any argument is invalid
     */
    private Journal(String title, String editor, String issueNumber) {
        ValidationUtils.requireNonEmpty(title, "Title");
        ValidationUtils.requireNonEmpty(editor, "Editor");
        ValidationUtils.requireNonEmpty(issueNumber, "Issue Number");
        this.title = title.trim();
        this.editor = editor.trim();
        this.issueNumber = issueNumber.trim();
    }

    /**
     * Constructs a new {@code Journal} with validated metadata, a single copy,
     * and a defined or automatically loaded price.
     * <p>
     * Implements the <b>Smart Price Logic</b>:
     * <ul>
     *   <li>If a positive {@code price} is provided → it will be used directly.</li>
     *   <li>If {@code price} is {@code null} or zero → loads the default price from
     *       {@link Config} key {@code "price.journal.default"}.</li>
     *   <li>If no configuration value exists → defaults to {@code 0.00}.</li>
     * </ul>
     * </p>
     *
     * @param title       the title of the journal (non-null and non-empty)
     * @param editor      the editor of the journal (non-null and non-empty)
     * @param issueNumber the issue number or volume identifier (non-null and non-empty)
     * @param price       the price of the journal; if {@code null} or zero, loads default from Config
     * @throws IllegalArgumentException if any argument is invalid
     */
    public Journal(String title, String editor, String issueNumber, BigDecimal price) {
        this(title, editor, issueNumber);
        initCopies(1);
        initPrice(price);
    }

    /**
     * Constructs a new {@code Journal} with validated metadata, a specified number
     * of total copies, and a defined or automatically loaded price.
     *
     * @param title       the title of the journal (non-null and non-empty)
     * @param editor      the editor of the journal (non-null and non-empty)
     * @param issueNumber the issue number or volume identifier (non-null and non-empty)
     * @param price       the price of the journal; if {@code null} or zero, loads default from Config
     * @param totalCopies total number of physical copies (> 0)
     * @throws IllegalArgumentException if any argument is invalid or totalCopies is not positive
     */
    public Journal(String title, String editor, String issueNumber,
                   BigDecimal price, int totalCopies) {
        this(title, editor, issueNumber);
        initCopies(totalCopies);
        initPrice(price);
    }

    /**
     * Initializes the price using Smart Price Logic:
     * uses the provided positive price, or loads a default from configuration,
     * or falls back to zero.
     *
     * @param price user-specified price or {@code null}/zero to use defaults
     */
    private void initPrice(BigDecimal price) {
        if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
            setPrice(price);
        } else {
            double defaultPrice = Config.getDouble("price.journal.default", 0.0);
            setPrice(BigDecimal.valueOf(defaultPrice));
        }
    }

    /**
     * Initializes both total and available copies to the same positive value.
     *
     * @param copies initial total copies (> 0)
     * @throws IllegalArgumentException if {@code copies} is not positive
     */
    private void initCopies(int copies) {
        if (copies <= 0) {
            throw new IllegalArgumentException("Total copies must be > 0");
        }
        this.totalCopies = copies;
        this.availableCopies = copies;
    }

    /**
     * Returns the title of this journal.
     *
     * @return the title string
     */
    @Override
    public String getTitle() {
        return title;
    }

    /**
     * Returns the editor's name.
     *
     * @return editor name
     */
    public String getEditor() {
        return editor;
    }

    /**
     * Returns the issue number or volume identifier.
     *
     * @return issue identifier string
     */
    public String getIssueNumber() {
        return issueNumber;
    }

    /**
     * Updates the title after validation.
     *
     * @param t the new title (non-null and non-empty)
     * @throws IllegalArgumentException if invalid
     */
    public void setTitle(String t) {
        ValidationUtils.requireNonEmpty(t, "Title");
        this.title = t.trim();
    }

    /**
     * Updates the editor's name after validation.
     *
     * @param e the new editor (non-null and non-empty)
     * @throws IllegalArgumentException if invalid
     */
    public void setEditor(String e) {
        ValidationUtils.requireNonEmpty(e, "Editor");
        this.editor = e.trim();
    }

    /**
     * Updates the issue number after validation.
     *
     * @param i the new issue number (non-null and non-empty)
     * @throws IllegalArgumentException if invalid
     */
    public void setIssueNumber(String i) {
        ValidationUtils.requireNonEmpty(i, "Issue Number");
        this.issueNumber = i.trim();
    }

    /**
     * Returns the total number of physical copies of this journal.
     *
     * @return total copies
     */
    public synchronized int getTotalCopies() {
        return totalCopies;
    }

    /**
     * Returns the number of currently available copies of this journal.
     *
     * @return available copies
     */
    public synchronized int getAvailableCopies() {
        return availableCopies;
    }

    /**
     * Changes the total number of physical copies and adjusts the available
     * copies accordingly, ensuring the available count stays within [0, total].
     *
     * @param newTotal new total copies (> 0)
     * @throws IllegalArgumentException if {@code newTotal} is not positive
     */
    public synchronized void setTotalCopies(int newTotal) {
        if (newTotal <= 0) {
            throw new IllegalArgumentException("Total copies must be > 0");
        }
        int delta = newTotal - this.totalCopies;
        this.totalCopies = newTotal;
        this.availableCopies += delta;
        if (this.availableCopies > this.totalCopies) {
            this.availableCopies = this.totalCopies;
        }
        if (this.availableCopies < 0) {
            this.availableCopies = 0;
        }
    }

    /**
     * Returns the material type of this item.
     *
     * @return {@link MaterialType#JOURNAL}
     */
    @Override
    public MaterialType getMaterialType() {
        return MaterialType.JOURNAL;
    }

    /**
     * Determines if a given keyword matches any field (title, editor, issue).
     * <p>
     * Comparison is case-insensitive and designed for search functionality.
     * </p>
     *
     * @param keyword the keyword to search for (non-null and non-empty)
     * @return {@code true} if keyword matches any field; otherwise {@code false}
     * @throws IllegalArgumentException if keyword is {@code null} or empty
     */
    @Override
    public boolean matchesKeyword(String keyword) {
        ValidationUtils.requireNonEmpty(keyword, "Keyword");
        String k = keyword.toLowerCase();
        return (title + " " + editor + " " + issueNumber).toLowerCase().contains(k);
    }

    /**
     * Determines whether the journal has at least one available copy.
     * <p>
     * Journals may have multiple copies (e.g., multiple physical issues). This method
     * returns {@code true} only if there is at least one available copy of this
     * specific journal issue.
     * </p>
     *
     * @return {@code true} if at least one copy is available; {@code false} otherwise
     */
    @Override
    protected boolean isAvailableInternal() {
        return availableCopies > 0;
    }

    /**
     * Borrows exactly one copy of this journal issue.
     * <p>
     * If no available copies remain in the library, an {@link IllegalStateException}
     * is thrown to prevent the operation. Otherwise, the available copies count is
     * reduced by one.
     * </p>
     *
     * @return {@code true} if the borrow operation succeeds
     * @throws IllegalStateException if no copies of this journal issue are available
     */
    @Override
    protected boolean doBorrow() {
        if (availableCopies <= 0) {
            throw new IllegalStateException(
                    "No available copies of \"" + title + " [" + issueNumber + "]\" to borrow."
            );
        }
        availableCopies--;
        return true;
    }

    /**
     * Returns one borrowed copy of this journal issue to the library.
     * <p>
     * If all copies are already present (i.e., no outstanding borrowed copies),
     * an {@link IllegalStateException} is thrown. Otherwise, the available copies
     * counter is incremented by one.
     * </p>
     *
     * @return {@code true} if the return operation succeeds
     * @throws IllegalStateException if all copies of this journal issue are already returned
     */
    @Override
    protected boolean doReturn() {
        if (availableCopies >= totalCopies) {
            throw new IllegalStateException(
                    "All copies of \"" + title + " [" + issueNumber + "]\" are already in the library."
            );
        }
        availableCopies++;
        return true;
    }

    /**
     * Returns a formatted string representation of this journal, including
     * title, issue number, editor, price, copy counts, and availability.
     *
     * @return human-readable string describing this journal
     */
    @Override
    public String toString() {
        return "[JOURNAL] %s [%s] edited by %s (Price:%s, Available:%d/%d) — %s".formatted(
                title,
                issueNumber,
                editor,
                getPrice().toPlainString(),
                getAvailableCopies(),
                getTotalCopies(),
                isAvailable() ? "Available" : "Fully borrowed"
        );
    }
}
