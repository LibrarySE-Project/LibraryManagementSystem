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
 * 
 */
public class CD extends AbstractLibraryItem {

    private static final long serialVersionUID = 1L;

    /**
     * The title of this CD.
     */
    private String title;

    /**
     * The artist or performer of this CD.
     */
    private String artist;

    /**
     * The total number of physical copies owned by the library.
     */
    private int totalCopies;

    /**
     * The number of currently available copies (not borrowed).
     */
    private int availableCopies;

    /**
     * Creates a CD with validated title and artist fields.
     *
     * @param title  CD title, non-empty
     * @param artist CD artist, non-empty
     */
    private CD(String title, String artist) {
        ValidationUtils.requireNonEmpty(title, "Title");
        ValidationUtils.requireNonEmpty(artist, "Artist");
        this.title = title.trim();
        this.artist = artist.trim();
    }

    /**
     * Creates a CD with one default total copy.
     *
     * @param title  CD title
     * @param artist CD artist
     * @param price  CD price, or default configuration if null/zero
     */
    public CD(String title, String artist, BigDecimal price) {
        this(title, artist);
        initCopies(1);
        initPrice(price);
    }

    /**
     * Creates a CD with specified total copies.
     *
     * @param title       CD title
     * @param artist      CD artist
     * @param price       CD price
     * @param totalCopies total number of physical copies
     */
    public CD(String title, String artist, BigDecimal price, int totalCopies) {
        this(title, artist);
        initCopies(totalCopies);
        initPrice(price);
    }

    /**
     * Initializes the CD price using smart price logic.
     *
     * @param price user-specified price or default configuration
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
     * Initializes totalCopies and availableCopies.
     *
     * @param copies initial total copies (>0)
     */
    private void initCopies(int copies) {
        if (copies <= 0) throw new IllegalArgumentException("Total copies must be > 0");
        this.totalCopies = copies;
        this.availableCopies = copies;
    }

    /**
     * Returns the CD title.
     *
     * @return CD title
     */
    @Override
    public String getTitle() {
        return title;
    }

    /**
     * Returns the CD artist.
     *
     * @return artist name
     */
    public String getArtist() {
        return artist;
    }

    /**
     * Updates the CD title after validation.
     *
     * @param t new title
     */
    public void setTitle(String t) {
        ValidationUtils.requireNonEmpty(t, "Title");
        this.title = t.trim();
    }

    /**
     * Updates the CD artist after validation.
     *
     * @param a new artist
     */
    public void setArtist(String a) {
        ValidationUtils.requireNonEmpty(a, "Artist");
        this.artist = a.trim();
    }

    /**
     * Returns the total number of physical copies.
     *
     * @return total copies
     */
    public synchronized int getTotalCopies() {
        return totalCopies;
    }

    /**
     * Returns number of available copies.
     *
     * @return available copies
     */
    public synchronized int getAvailableCopies() {
        return availableCopies;
    }

    /**
     * Changes the total number of physical copies and adjusts the available
     * count accordingly.
     *
     * @param newTotal new total copies (>0)
     */
    public synchronized void setTotalCopies(int newTotal) {
        if (newTotal <= 0) throw new IllegalArgumentException("Total copies must be > 0");
        int delta = newTotal - this.totalCopies;
        this.totalCopies = newTotal;
        this.availableCopies += delta;
        if (availableCopies > totalCopies) availableCopies = totalCopies;
        if (availableCopies < 0) availableCopies = 0;
    }

    /**
     * Returns CD as a material type.
     *
     * @return MaterialType.CD
     */
    @Override
    public MaterialType getMaterialType() {
        return MaterialType.CD;
    }

    /**
     * Checks if keyword matches title or artist.
     *
     * @param keyword search keyword (non-null)
     * @return true if keyword found
     */
    @Override
    public boolean matchesKeyword(String keyword) {
        ValidationUtils.requireNonEmpty(keyword, "Keyword");
        String k = keyword.toLowerCase();
        return (title + " " + artist).toLowerCase().contains(k);
    }

    /**
     * Determines whether the item has at least one available copy to borrow.
     * <p>
     * This method is called internally by the abstract borrowing mechanism in
     * {@link AbstractLibraryItem}. Classes that maintain multiple copies (such as
     * books, journals, or CDs) override this method to define the item’s real
     * availability status based on the remaining copies.
     * </p>
     *
     * @return {@code true} if there is at least one available copy, otherwise {@code false}
     */
    @Override
    protected boolean isAvailableInternal() {
        return availableCopies > 0;
    }

    /**
     * Attempts to borrow a single copy of the item.
     * <p>
     * If no copies are currently available, this method throws an exception and the
     * borrowing operation fails. Otherwise, the internal available copy count is
     * decreased by one.
     * </p>
     *
     * @return {@code true} if the borrowing operation succeeds
     * @throws IllegalStateException if there are no available copies to borrow
     */
    @Override
    protected boolean doBorrow() {
        if (availableCopies <= 0) {
            throw new IllegalStateException(
                    "No available copies of \"" + title + "\" to borrow."
            );
        }
        availableCopies--;
        return true;
    }

    /**
     * Returns one borrowed copy back to the library.
     * <p>
     * If all copies are already present in the library (no outstanding loans),
     * the method throws an exception and the return operation is rejected.
     * Otherwise, the internal available copy count is increased by one.
     * </p>
     *
     * @return {@code true} if the return operation succeeds
     * @throws IllegalStateException if all copies are already returned
     */
    @Override
    protected boolean doReturn() {
        if (availableCopies >= totalCopies) {
            throw new IllegalStateException(
                    "All copies of \"" + title + "\" are already in the library."
            );
        }
        availableCopies++;
        return true;
    }



    /**
     * Returns a formatted string representation of this CD showing
     * metadata and availability.
     *
     * @return formatted description
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
