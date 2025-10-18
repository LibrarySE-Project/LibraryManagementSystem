/**
 * 
 */
package librarySE;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 
 */
class BookTest {
	private static Book b1;
	private static Book b2;
	private static Book b3;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		 b1 = new Book("193", "Java Basics", "John Doe");
	     b2 = new Book("193", "Java Basics", "John Doe");

	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterAll
	static void tearDownAfterClass() throws Exception {
		b1 = b2 = b3 = null;
	}

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
	     b3 = new Book("457", "Python Intro", "Alice");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterEach
	void tearDown() throws Exception {
		b3 = null;
	}

	
	@Test
	void testGetters() {
		// work at b3
		String expectedIsbn="457";
		String expectedTitle="Python Intro";
		String expectedAuthor="Alice";
		assertEquals(expectedIsbn,b3.getIsbn());
		assertEquals(expectedTitle,b3.getTitle());
		assertEquals(expectedAuthor,b3.getAuthor());
	}
	
	@Test
	void testEquals() {
		// work at b1 & b2 & b3
		Integer obj = 21 ; 
		boolean expectedOut = true;
		assertEquals(expectedOut,b1.equals(b1)); // 2 obj is equal
		assertEquals(expectedOut,b1.equals(b2)); // 2 obj is equal
		assertNotEquals(expectedOut,b1.equals(b3)); // 2 obj is not equal -> 2 obj with type Book
		assertNotEquals(expectedOut,b1.equals(obj));// 2 obj is not equal -> one obj with type Book, other with type Integer 
		assertNotEquals(expectedOut,b1.equals(null));// one obj of type Book with null
	}
	@Test
	void testAvailable() {
		// work at b3
		boolean expectedOut = true;
		assertEquals(expectedOut,b3.isAvailable()); 
		b3.setAvailable(false);
		assertNotEquals(expectedOut,b3.isAvailable()); 	
	}
	
	@Test
	void testPrint() {
		// work at b3
		b3.setAvailable(false);
		String expectedOut = "Python Intro — Alice (ISBN: 457)  [BORROWED]";
		assertEquals(expectedOut,b3.toString()); 
		b3.setAvailable(true);
		expectedOut = "Python Intro — Alice (ISBN: 457)  [AVAILABLE]";
		assertEquals(expectedOut,b3.toString()); 
	}
	
	@Test
	void testHashCode() {
		// work at b1 & b2
		assertEquals(b1.hashCode(),b2.hashCode()); 
		assertNotEquals(b1.hashCode(),b3.hashCode()); 
	}

}
