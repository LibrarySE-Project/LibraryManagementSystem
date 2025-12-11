package librarySE.core;

import java.math.BigDecimal;

import librarySE.utils.Config;
import librarySE.utils.ValidationUtils;

/**
 * Represents a {@link LibraryItem} of type <b>Journal</b> (periodical or magazine)
 * within the library system.
 *
 * <p>
 * Each journal includes identifying and descriptive information such as a title,
 * editor, and issue number. Core responsibilities such as:
 * </p>
 * <ul>
 *     <li>Unique identity (UUID)</li>
 *     <li>Price management with validation</li>
 *     <li>Thread-safe borrowing and returning</li>
 *     <li>Tracking total and available copies</li>
 * </ul>
 * are inherited from {@link AbstractLibraryItem}, so this class focuses mainly
 * on journal-specific metadata and configuration.
 *
 * <h3>Key Features</h3>
 * <ul>
 *   <li>Encapsulates metadata for journals (title, editor, issue number).</li>
 *   <li>Supports keyword-based search across title, editor, and issue number.</li>
 *   <li>Validates all textual input via {@link ValidationUtils}.</li>
 *   <li>Uses “smart price” logic with configuration defaults via {@link Config}.</li>
 *   <li>Fully compatible with fine strategies (e.g., 21 days, 15 NIS/day) through the
 *       shared {@link LibraryItem} interface.</li>
 * </ul>
 *
 * <h3>Price Initialization</h3>
 * <p>
 * When constructing a journal:
 * </p>
 * <ul>
 *   <li>If a positive {@code price} is provided, it is used directly.</li>
 *   <li>If {@code price} is {@code null} or zero, a default is read from
 *       {@code Config} key {@code "price.journal.default"}.</li>
 *   <li>If no configuration is found, the price falls back to {@code 0.00}.</li>
 * </ul>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * Journal j = new Journal(
 *         "AI Research Monthly",
 *         "Dr. Smith",
 *         "Vol. 15",
 *         BigDecimal.valueOf(29.99),
 *         3
 * );
 *
 * System.out.println(j);
 * // [JOURNAL] AI Research Monthly [Vol. 15] edited by Dr. Smith
 * // (Price:29.99, Available:3/3) — Available
 * }</pre>
 *  @author Malak
 */
public class Journal extends AbstractLibraryItem {

    private static final long serialVersionUID = 1L;

    private String title;
    private String editor;
    private String issueNumber;

    /**
     * Internal constructor that validates metadata and delegates copy tracking
     * to {@link AbstractLibraryItem} via {@link AbstractLibraryItem#AbstractLibraryItem(int)}.
     *
     * @param title       journal title (non-null, non-empty)
     * @param editor      journal editor (non-null, non-empty)
     * @param issueNumber issue/volume identifier (non-null, non-empty)
     * @param totalCopies initial total copies (> 0)
     * @throws IllegalArgumentException if any argument is invalid
     */
    private Journal(String title, String editor, String issueNumber, int totalCopies) {
        super(totalCopies);
        ValidationUtils.requireNonEmpty(title, "Title");
        ValidationUtils.requireNonEmpty(editor, "Editor");
        ValidationUtils.requireNonEmpty(issueNumber, "Issue Number");
        this.title = title.trim();
        this.editor = editor.trim();
        this.issueNumber = issueNumber.trim();
    }

    /**
     * Constructs a new {@code Journal} with a single copy and smart price initialization.
     *
     * @param title       journal title (non-null, non-empty)
     * @param editor      journal editor (non-null, non-empty)
     * @param issueNumber issue/volume identifier (non-null, non-empty)
     * @param price       explicit price, or configuration/default if {@code null} or zero
     */
    public Journal(String title, String editor, String issueNumber, BigDecimal price) {
        this(title, editor, issueNumber, 1);
        initPrice(price);
    }

    /**
     * Constructs a new {@code Journal} with a specified number of total copies
     * and smart price initialization.
     *
     * @param title       journal title (non-null, non-empty)
     * @param editor      journal editor (non-null, non-empty)
     * @param issueNumber issue/volume identifier (non-null, non-empty)
     * @param price       explicit price, or configuration/default if {@code null} or zero
     * @param totalCopies total physical copies (> 0)
     */
    public Journal(String title, String editor, String issueNumber,
                   BigDecimal price, int totalCopies) {
        this(title, editor, issueNumber, totalCopies);
        initPrice(price);
    }

    /**
     * Initializes the price using the “smart price” policy:
     * <ul>
     *     <li>Uses the given positive {@code price} if provided.</li>
     *     <li>Otherwise, reads a default from {@code Config}.</li>
     * </ul>
     *
     * @param price optional explicit price
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
     * Returns the journal title.
     *
     * @return title string
     */
    @Override
    public String getTitle() {
        return title;
    }

    /**
     * Returns the editor’s name.
     *
     * @return editor name
     */
    public String getEditor() {
        return editor;
    }

    /**
     * Returns the issue or volume identifier.
     *
     * @return issue number string
     */
    public String getIssueNumber() {
        return issueNumber;
    }

    /**
     * Updates the title after validation.
     *
     * @param t new title (non-null, non-empty)
     */
    public void setTitle(String t) {
        ValidationUtils.requireNonEmpty(t, "Title");
        this.title = t.trim();
    }

    /**
     * Updates the editor after validation.
     *
     * @param e new editor name (non-null, non-empty)
     */
    public void setEditor(String e) {
        ValidationUtils.requireNonEmpty(e, "Editor");
        this.editor = e.trim();
    }

    /**
     * Updates the issue number after validation.
     *
     * @param i new issue identifier (non-null, non-empty)
     */
    public void setIssueNumber(String i) {
        ValidationUtils.requireNonEmpty(i, "Issue Number");
        this.issueNumber = i.trim();
    }

    /**
     * Returns the material type for this item.
     *
     * @return {@link MaterialType#JOURNAL}
     */
    @Override
    public MaterialType getMaterialType() {
        return MaterialType.JOURNAL;
    }

    /**
     * Performs a case-insensitive keyword search over title, editor, and issue number.
     *
     * @param keyword non-null, non-empty search term
     * @return {@code true} if the keyword appears in any descriptive field
     */
    @Override
    public boolean matchesKeyword(String keyword) {
        ValidationUtils.requireNonEmpty(keyword, "Keyword");
        String k = keyword.toLowerCase();
        return (title + " " + editor + " " + issueNumber).toLowerCase().contains(k);
    }

    /**
     * Human-friendly label used in error messages for this journal,
     * combining title and issue number.
     *
     * @return label like: {@code "AI Research Monthly [Vol. 15]"}
     */
    @Override
    protected String getDisplayNameForMessages() {
        return title + " [" + issueNumber + "]";
    }

    /**
     * Returns a formatted string representation of this journal, including:
     * <ul>
     *     <li>Title and issue number</li>
     *     <li>Editor</li>
     *     <li>Price</li>
     *     <li>Available/total copies (inherited counters)</li>
     *     <li>Availability status</li>
     * </ul>
     *
     * @return human-readable description of this journal
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
