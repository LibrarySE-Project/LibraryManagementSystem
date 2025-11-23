package librarySE.core;



import java.math.BigDecimal;
import java.util.UUID;

/**
 * Represents a general library item that can be borrowed and searched.
 * <p>
 * This interface combines the behavior of {@link Borrowable} and {@link Searchable},
 * and adds core identifying and descriptive attributes common to all library materials,
 * such as books, CDs, or journals.
 * </p>
 *
 * <p>
 * Every {@code LibraryItem} should have:
 * <ul>
 *   <li>A unique {@link UUID} identifier</li>
 *   <li>A descriptive title</li>
 *   <li>A {@link MaterialType} that specifies its category (BOOK, CD, JOURNAL, etc.)</li>
 *   <li>A price that represents the item’s value or replacement cost</li>
 * </ul>
 * </p>
 *
 * @author Eman
 * @see Borrowable
 * @see Searchable
 * @see MaterialType
 */
public interface LibraryItem extends Borrowable, Searchable {

    /**
     * Returns the unique identifier of this library item.
     *
     * @return a non-null {@link UUID} representing the unique ID of this item
     */
    UUID getId();

    /**
     * Returns the title of this library item.
     *
     * @return the title of the item; never {@code null} or empty
     */
    String getTitle();

    /**
     * Returns the material type of this item (e.g., BOOK, CD, JOURNAL).
     *
     * @return the {@link MaterialType} representing the item's category
     */
    MaterialType getMaterialType();

    /**
     * Returns the price of this item.
     * <p>
     * The price may represent the cost of replacement, market value, or
     * reference price for fines and fees.
     * </p>
     *
     * @return the item's price as a {@code double}
     */
    BigDecimal getPrice();

    /**
     * Sets the price of this item.
     *
     * @param price the new price to assign; must be non-negative
     */
    void setPrice(BigDecimal price);
}
