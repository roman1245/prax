package kandrac.xyz.library.test;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;

import xyz.kandrac.library.model.Contract;
import xyz.kandrac.library.model.DatabaseProvider;
import xyz.kandrac.library.model.obj.Book;


/**
 * Created by kandrac on 21/11/15.
 */
public class DatabaseProviderBookTest extends ProviderTestCase2<DatabaseProvider> {

    public DatabaseProviderBookTest() {
        super(DatabaseProvider.class, "xyz.kandrac.Library");
    }

    // Helper methods for repetitive calls
    private Book generateBook(String title) {
        return new Book.Builder().setTitle(title).build();
    }

    private long insertBook(String title) {
        Book book = generateBook(title);

        Uri result = getProvider().insert(Contract.Books.CONTENT_URI, book.getContentValues());

        return (Contract.Books.getBookId(result));
    }

    private Book getBook(Cursor cursor) {
        return new Book(cursor);
    }

    public Cursor selectAllBooks() {
        return getProvider().query(
                Contract.Books.CONTENT_URI,
                null, null, null, null
        );
    }

    public Cursor selectBookById(long id) {
        return getProvider().query(
                Contract.Books.buildBookUri(id),
                null,
                null,
                null,
                null);
    }

    public Cursor selectBookByTitle(String name) {
        return getProvider().query(
                Contract.Books.CONTENT_URI,
                null,
                Contract.Books.BOOK_TITLE + " = ?",
                new String[]{name},
                null);
    }

    /**
     * PROVIDER INTERFACE TEST
     */
    public void testBookProviderInterface() {
        long test1 = Contract.Books.getBookId(Uri.parse("content://xyz.kandrac.Library/books/1"));
        String test2 = Contract.Books.buildBookUri(1).toString();
        String test3 = Contract.Books.buildBookWithAuthorUri(1).toString();

        assertEquals(1, test1);
        assertEquals("content://xyz.kandrac.Library/books/1", test2);
        assertEquals("content://xyz.kandrac.Library/books/1/authors", test3);
    }

    /**
     * INSERTION TEST
     * <p/>
     * Test whether insertion returns correct value for single insert
     */
    public void testInsertSingle() {
        assertTrue("Result of insertion cannot be -1", insertBook("Title") != -1);
    }

    /**
     * INSERTION TEST
     * <p/>
     * Test bulk insertion
     */
    public void testBulkInsertBook() {

        ContentValues[] values = new ContentValues[]{
                generateBook("Title1").getContentValues(),
                generateBook("Title2").getContentValues(),
                generateBook("Title3").getContentValues()
        };

        getProvider().bulkInsert(Contract.Books.CONTENT_URI, values);

        assertEquals("Title1", getBook(selectBookByTitle("Title1")).title);
        assertEquals("Title2", getBook(selectBookByTitle("Title2")).title);
        assertEquals("Title3", getBook(selectBookByTitle("Title3")).title);
    }

    /**
     * SELECTION TEST
     * <p/>
     * Test whether selection by PRIMARY KEY _id is working, where _id value is passed manually to where clause.
     */
    public void testSelectAllBooksById() {
        long resultId = insertBook("Title1");

        Cursor bookCursor = getProvider().query(
                Contract.Books.CONTENT_URI,
                null,
                Contract.Books.BOOK_ID + " = ?",
                new String[]{Long.toString(resultId)},
                null);

        assertNotNull(bookCursor);
        assertEquals(1, bookCursor.getCount());
        assertEquals("Title1", getBook(bookCursor).title);

        bookCursor.close();
    }

    /**
     * SELECTION TEST
     * <p/>
     * Test whether selection by UNIQUE book_name is working.
     */
    public void testSelectAllBooksByName() {
        insertBook("Title1");

        Cursor bookCursor = selectBookByTitle("Title1");

        assertNotNull(bookCursor);
        assertEquals(1, bookCursor.getCount());
        assertEquals("Title1", getBook(bookCursor).title);

        bookCursor.close();
    }

    /**
     * SELECTION TEST
     * <p/>
     * Test whether selection by PRIMARY KEY _id is working (correct implementation).
     */
    public void testSelectSingleBookById() {
        long resultId = insertBook("Title1");

        Cursor bookCursor = selectBookById(resultId);

        assertNotNull(bookCursor);
        assertEquals(1, bookCursor.getCount());
        assertEquals("Title1", getBook(bookCursor).title);

        bookCursor.close();
    }

    /**
     * SELECTION TEST
     * <p/>
     * Test whether selection by PRIMARY KEY _id is working (correct implementation). Multiple
     * values inserted in database.
     */
    public void testSelectSingleBookById2() {
        long resultId1 = insertBook("Title1");
        long resultId2 = insertBook("Title2");

        Cursor bookCursor1 = selectBookById(resultId1);
        Cursor bookCursor2 = selectBookById(resultId2);

        assertNotNull(bookCursor1);
        assertNotNull(bookCursor2);

        assertEquals("Title1", getBook(bookCursor1).title);
        assertEquals("Title2", getBook(bookCursor2).title);

        bookCursor1.close();
        bookCursor2.close();
    }
}
