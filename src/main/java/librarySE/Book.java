package librarySE;


import java.util.Objects;


public class Book {
    private final String isbn;
    private final String title;
    private final String author;
    private boolean available = true;

    public Book(String isbn, String title, String author) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
    }

    // getters
    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public boolean isAvailable () { return available; }
    public void setAvailable (boolean b) { this.available = b; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Book)) return false;
        if (this == o) return true;
        Book b = (Book) o;
        return isbn.equals(b.isbn);
    }

    @Override
    public int hashCode() { return Objects.hash(isbn); }

    @Override
    public String toString() {
    	return String.format("%s — %s (ISBN: %s) %s", title, author, isbn, available ? " [AVAILABLE]" : " [BORROWED]");
    }
}
