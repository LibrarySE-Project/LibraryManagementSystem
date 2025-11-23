package librarySE.core;

import java.math.BigDecimal;

import librarySE.utils.Config;
import librarySE.utils.ValidationUtils;

/**
 * Represents a {@link LibraryItem} of type <b>CD</b> (audio disc) in the library system.
 * <p>
 * Each CD has a title, an artist, and an optional price. It inherits core behavior
 * such as borrowing, returning, and price management from {@link AbstractLibraryItem},
 * ensuring consistent and thread-safe behavior across all materials.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 *   <li>Encapsulates CD metadata (title, artist, price).</li>
 *   <li>Supports keyword-based searching on both title and artist fields.</li>
 *   <li>Validates all input fields using {@link ValidationUtils}.</li>
 *   <li>Compatible with fine strategies (7-day period, 20 NIS/day by default).</li>
 * </ul>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * CD cd = new CD("Greatest Hits", "Queen", BigDecimal.valueOf(39.99));
 * System.out.println(cd);
 * // [CD] Greatest Hits by Queen (Price:39.99) — Available
 * }</pre>
 *
 * @author Malak
 * @see AbstractLibraryItem
 * @see MaterialType
 * @see ValidationUtils
 */
public class CD extends AbstractLibraryItem {

    /** Serialization identifier for version consistency. */
    private static final long serialVersionUID = 1L;

    /** The title of the CD. */
    private String title;

    /** The artist or performer of the CD. */
    private String artist;

    /**
     * Initializes a {@code CD} instance with validated metadata (title and artist).
     * <p>
     * This constructor is used internally by other constructors to ensure
     * consistent validation and initialization of core fields.
     * The default price is initialized to {@code 0.00}.
     * </p>
     *
     * @param title  the CD title (non-null and non-empty)
     * @param artist the CD artist (non-null and non-empty)
     * @throws IllegalArgumentException if any parameter is invalid
     * @implNote This constructor is private and should not be called directly.
     */
    private CD(String title, String artist) {
        ValidationUtils.requireNonEmpty(title, "Title");
        ValidationUtils.requireNonEmpty(artist, "Artist");
        this.title = title.trim();
        this.artist = artist.trim();
    }

    /**
     * Constructs a new {@code CD} with validated metadata and a defined or automatically loaded price.
     * <p>
     * Implements the <b>Smart Price Logic</b>:
     * <ul>
     *   <li>If a positive {@code price} is provided → it will be used directly.</li>
     *   <li>If {@code price} is {@code null} or zero → loads the default price from
     *       {@link Config} key {@code "price.cd.default"}.</li>
     *   <li>If no configuration value exists → defaults to {@code 0.00}.</li>
     * </ul>
     * </p>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * CD cd1 = new CD("Thriller", "Michael Jackson", BigDecimal.valueOf(39.99));
     * CD cd2 = new CD("Thriller", "Michael Jackson", BigDecimal.ZERO);
     * }</pre>
     *
     * @param title  the CD title (non-null and non-empty)
     * @param artist the CD artist (non-null and non-empty)
     * @param price  the CD price; if {@code null} or zero, loads default from Config
     * @throws IllegalArgumentException if any field is invalid
     */
    public CD(String title, String artist, BigDecimal price) {
        this(title, artist);
        if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
            setPrice(price); 
        } else {
            double defaultPrice =  Config.getDouble("price.cd.default", 0.0);
            setPrice(BigDecimal.valueOf(defaultPrice));
        }
    }
    /** {@inheritDoc} */
    @Override
    public String getTitle() {
        return title;
    }

    /** Returns the artist or performer of this CD. */
    public String getArtist() {
        return artist;
    }

    /**
     * Updates the CD title after validation.
     *
     * @param t the new title (non-null and non-empty)
     * @throws IllegalArgumentException if invalid
     */
    public void setTitle(String t) {
        ValidationUtils.requireNonEmpty(t, "Title");
        this.title = t.trim();
    }

    /**
     * Updates the artist name after validation.
     *
     * @param a the new artist (non-null and non-empty)
     * @throws IllegalArgumentException if invalid
     */
    public void setArtist(String a) {
        ValidationUtils.requireNonEmpty(a, "Artist");
        this.artist = a.trim();
    }

    /**
     * Returns the material type of this item.
     *
     * @return {@link MaterialType#CD}
     */
    @Override
    public MaterialType getMaterialType() {
        return MaterialType.CD;
    }

    /**
     * Checks whether the given keyword matches either the CD title or artist.
     * <p>
     * The match is case-insensitive and used for searching the library catalog.
     * </p>
     *
     * @param keyword the keyword to search for (non-null)
     * @return {@code true} if the keyword appears in title or artist
     * @throws IllegalArgumentException if keyword is {@code null}
     */
    @Override
    public boolean matchesKeyword(String keyword) {
    	ValidationUtils.requireNonEmpty(keyword, "Keyword");
        String k = keyword.toLowerCase();
        return (title + " " + artist).toLowerCase().contains(k);
    }


    /**
     * Returns a human-readable description of this CD, including title,
     * artist, price, and availability state.
     *
     * @return formatted string representation of this CD
     */
    @Override
    public String toString() {
        return "[CD] %s by %s (Price:%s) — %s".formatted(
                title,
                artist,
                getPrice().toPlainString(),
                isAvailable() ? "Available" : "Borrowed"
        );
    }
}
