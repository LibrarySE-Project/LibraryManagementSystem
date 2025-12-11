package librarySE.core;

import java.math.BigDecimal;

import librarySE.utils.Config;
import librarySE.utils.ValidationUtils;

/**
 * Represents a compact audio disc (CD) in the library collection.
 * <p>
 * A CD is a {@link LibraryItem} that carries basic metadata
 * (title and artist) plus a monetary price. The core behaviors for:
 * <ul>
 *     <li>Price validation and storage</li>
 *     <li>Thread-safe borrowing and returning</li>
 *     <li>Tracking total vs. available copies</li>
 * </ul>
 * are implemented in the shared abstract base classes
 * (e.g. {@code AbstractLibraryItem} / {@code AbstractCopyTrackedItem}),
 * so this class focuses mainly on CD-specific fields and search behavior.
 * </p>
 *
 * <h3>Key Features</h3>
 * <ul>
 *   <li>Encapsulates CD metadata (title, artist, price).</li>
 *   <li>Supports keyword-based searching on both title and artist.</li>
 *   <li>Validates textual input using {@link ValidationUtils}.</li>
 *   <li>Uses configurable default pricing via {@link Config} when no
 *       explicit positive price is provided.</li>
 * </ul>
 *
 * <h3>Copy Management</h3>
 * <p>
 * The constructor accepts a {@code totalCopies} value that determines how many
 * physical copies of the CD exist in the library. The underlying abstract
 * base class maintains the <em>total</em> and <em>available</em> copy counts
 * and exposes them through {@link #getTotalCopies()} and
 * {@link #getAvailableCopies()}, which are also used in {@link #toString()}.
 * </p>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * CD cd = new CD("Greatest Hits", "Queen", BigDecimal.valueOf(39.99));
 * System.out.println(cd);
 * // [CD] Greatest Hits by Queen (Price:39.99, Available:1/1) — Available
 * }</pre>
 *
 * @author Malak
 */
public class CD extends AbstractLibraryItem {

    private static final long serialVersionUID = 1L;

    /** Human-readable title of this CD. */
    private String title;

    /** Artist or performer associated with this CD. */
    private String artist;

    /**
     * Internal constructor that initializes title/artist and delegates
     * copy-count initialization to the parent class.
     *
     * @param title       CD title (non-null, non-empty)
     * @param artist      CD artist (non-null, non-empty)
     * @param totalCopies total number of physical copies
     */
    private CD(String title, String artist, int totalCopies) {
        super(totalCopies);
        ValidationUtils.requireNonEmpty(title, "Title");
        ValidationUtils.requireNonEmpty(artist, "Artist");
        this.title = title.trim();
        this.artist = artist.trim();
    }

    /**
     * Creates a CD with a single copy and a "smart" price initialization:
     * either uses the given positive {@code price} or falls back to a
     * default value from configuration.
     *
     * @param title  CD title
     * @param artist CD artist
     * @param price  explicit price or {@code null}/non-positive to use defaults
     */
    public CD(String title, String artist, BigDecimal price) {
        this(title, artist, 1);
        initPrice(price);
    }

    /**
     * Creates a CD with an explicit number of total copies and "smart"
     * price initialization.
     *
     * @param title       CD title
     * @param artist      CD artist
     * @param price       explicit price or {@code null}/non-positive to use defaults
     * @param totalCopies total number of physical copies
     */
    public CD(String title, String artist, BigDecimal price, int totalCopies) {
        this(title, artist, totalCopies);
        initPrice(price);
    }

    /**
     * Initializes the price using the following strategy:
     * <ul>
     *   <li>If {@code price} is non-null and &gt; 0 → use it as-is.</li>
     *   <li>Otherwise → load {@code price.cd.default} from {@link Config},
     *       falling back to {@code 0.0} if absent.</li>
     * </ul>
     *
     * @param price explicit price or {@code null}/non-positive for defaults
     */
    private void initPrice(BigDecimal price) {
        if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
            setPrice(price);
        } else {
            double defaultPrice = Config.getDouble("price.cd.default", 0.0);
            setPrice(BigDecimal.valueOf(defaultPrice));
        }
    }

    /**
     * Returns the title of this CD.
     *
     * @return CD title
     */
    @Override
    public String getTitle() {
        return title;
    }

    /**
     * Returns the artist associated with this CD.
     *
     * @return artist name
     */
    public String getArtist() {
        return artist;
    }

    /**
     * Updates the CD title after basic validation.
     *
     * @param t new title string
     * @throws IllegalArgumentException if {@code t} is null or empty
     */
    public void setTitle(String t) {
        ValidationUtils.requireNonEmpty(t, "Title");
        this.title = t.trim();
    }

    /**
     * Updates the CD artist after basic validation.
     *
     * @param a new artist name
     * @throws IllegalArgumentException if {@code a} is null or empty
     */
    public void setArtist(String a) {
        ValidationUtils.requireNonEmpty(a, "Artist");
        this.artist = a.trim();
    }

    /**
     * Returns the material type for this item.
     *
     * @return {@link MaterialType#CD}
     */
    @Override
    public MaterialType getMaterialType() {
        return MaterialType.CD;
    }

    /**
     * Checks whether the given keyword appears in either the title or artist,
     * case-insensitively. Used by search features.
     *
     * @param keyword non-null, non-empty search term
     * @return {@code true} if the keyword is contained in title or artist
     */
    @Override
    public boolean matchesKeyword(String keyword) {
        ValidationUtils.requireNonEmpty(keyword, "Keyword");
        String k = keyword.toLowerCase();
        return (title + " " + artist).toLowerCase().contains(k);
    }

    /**
     * Returns a human-readable name used in messages or logs when referring
     * to this CD instance.
     *
     * @return display name for this item
     */
    @Override
    protected String getDisplayNameForMessages() {
        return title;
    }

    /**
     * Returns a formatted description of this CD, including metadata and
     * copy/availability information.
     */
    @Override
    public String toString() {
        return "[CD] %s by %s (Price:%s, Available:%d/%d) — %s".formatted(
                title,
                artist,
                getPrice().toPlainString(),
                getAvailableCopies(),
                getTotalCopies(),
                isAvailable() ? "Available" : "Fully borrowed"
        );
    }
}
