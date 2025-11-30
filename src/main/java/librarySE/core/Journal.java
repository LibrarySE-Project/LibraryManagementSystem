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
 * and price management functionality from {@link AbstractLibraryItem}.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 *   <li>Encapsulates metadata for journals (title, editor, issue number, price).</li>
 *   <li>Supports keyword-based search across all descriptive fields.</li>
 *   <li>Validated inputs via {@link ValidationUtils}.</li>
 *   <li>Compatible with fine calculation strategies (default: 21 days, 15 NIS/day).</li>
 * </ul>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * Journal j = new Journal("AI Research Monthly", "Dr. Smith", "Vol. 15", BigDecimal.valueOf(29.99));
 * System.out.println(j);
 * // [JOURNAL] AI Research Monthly [Vol. 15] edited by Dr. Smith (Price:29.99) — Available
 * }</pre>
 *
 * @author Malak
 * @see AbstractLibraryItem
 * @see MaterialType
 * @see ValidationUtils
 */
public class Journal extends AbstractLibraryItem {

    /** Serialization identifier for version consistency. */
    private static final long serialVersionUID = 1L;

    /** The title of the journal. */
    private String title;

    /** The editor of the journal. */
    private String editor;

    /** The issue number or volume identifier. */
    private String issueNumber;


   /**
    * Initializes a {@code Journal} instance with validated metadata (title, editor, and issue number).
    * <p>
    * This constructor is used internally by other constructors to ensure
    * consistent validation and initialization of core fields.
    * The default price is initialized to {@code 0.00}.
    * </p>
    *
    * @param title       the title of the journal (non-null and non-empty)
    * @param editor      the editor of the journal (non-null and non-empty)
    * @param issueNumber the issue number or volume identifier (non-null and non-empty)
    * @throws IllegalArgumentException if any argument is invalid
    * @implNote This constructor is private and should not be called directly.
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
    * Constructs a new {@code Journal} with validated metadata and a defined or automatically loaded price.
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
    * <p><b>Example:</b></p>
    * <pre>{@code
    * Journal j1 = new Journal("Nature", "Dr. Smith", "Vol. 42", BigDecimal.valueOf(29.99));
    * Journal j2 = new Journal("Nature", "Dr. Smith", "Vol. 42", BigDecimal.ZERO);
    * }</pre>
    *
    * @param title       the title of the journal (non-null and non-empty)
    * @param editor      the editor of the journal (non-null and non-empty)
    * @param issueNumber the issue number or volume identifier (non-null and non-empty)
    * @param price       the price of the journal; if {@code null} or zero, loads default from Config
    * @throws IllegalArgumentException if any argument is invalid
    */
   public Journal(String title, String editor, String issueNumber, BigDecimal price) {
       this(title, editor, issueNumber);
       if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
           setPrice(price); 
       } else {
           double defaultPrice =  Config.getDouble("price.journal.default", 0.0);
           setPrice(BigDecimal.valueOf(defaultPrice));
       }
   }


    /** {@inheritDoc} */
    @Override
    public String getTitle() {
        return title;
    }

    /** Returns the editor's name. */
    public String getEditor() {
        return editor;
    }

    /** Returns the issue number or volume identifier. */
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
     * @param keyword the keyword to search for (non-null)
     * @return {@code true} if keyword matches any field; otherwise {@code false}
     * @throws IllegalArgumentException if keyword is {@code null}
     */
    @Override
    public boolean matchesKeyword(String keyword) {
    	ValidationUtils.requireNonEmpty(keyword, "Keyword");
        String k = keyword.toLowerCase();
        return (title + " " + editor + " " + issueNumber).toLowerCase().contains(k);
    }


    /**
     * Returns a formatted string representation of this journal, including
     * title, issue number, editor, price, and availability.
     *
     * @return human-readable string describing this journal
     */
    @Override
    public String toString() {
        return "[JOURNAL] %s [%s] edited by %s (Price:%s) — %s".formatted(
                title,
                issueNumber,
                editor,
                getPrice().toPlainString(),
                isAvailable() ? "Available" : "Borrowed"
        );
    }
}
