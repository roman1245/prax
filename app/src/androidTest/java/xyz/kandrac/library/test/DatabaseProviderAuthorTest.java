package xyz.kandrac.library.test;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;

import xyz.kandrac.library.model.Contract;
import xyz.kandrac.library.model.DatabaseProvider;
import xyz.kandrac.library.model.obj.Author;


/**
 * Basic Tests for database operations
 * <p>
 * Created by kandrac on 19/11/15.
 */
public class DatabaseProviderAuthorTest extends ProviderTestCase2<DatabaseProvider> {

    public DatabaseProviderAuthorTest() {
        super(DatabaseProvider.class, "xyz.kandrac.Library");
    }

    // Helper methods for repetitive calls
    private Author generateAuthor(String name) {
        return new Author.Builder().setName(name).build();
    }

    private long insertAuthor(String name) {
        Author author = generateAuthor(name);

        Uri result = getProvider().insert(Contract.Authors.CONTENT_URI, author.getContentValues());

        return Contract.Authors.getAuthorId(result);
    }

    private String getAuthorName(Cursor cursor) {
        Author author = new Author(cursor);

        return author.name;
    }

    public void testUriAuthor() {
        long test1 = Contract.Authors.getAuthorId(Uri.parse("content://xyz.kandrac.Library/authors/1"));
        String test2 = Contract.Authors.buildAuthorUri(1).toString();
        String test3 = Contract.Authors.buildBooksUri(1).toString();

        assertEquals(1, test1);
        assertEquals("content://xyz.kandrac.Library/authors/1", test2);
        assertEquals("content://xyz.kandrac.Library/authors/1/books", test3);
    }

    public Cursor selectAllAuthors() {
        return getProvider().query(
                Contract.Authors.CONTENT_URI,
                null, null, null, null
        );
    }

    public Cursor selectAuthorById(long id) {
        return getProvider().query(
                Contract.Authors.buildAuthorUri(id),
                null,
                null,
                null,
                null);
    }

    public Cursor selectAuthorByName(String name) {
        return getProvider().query(
                Contract.Authors.CONTENT_URI,
                null,
                Contract.Authors.AUTHOR_NAME + " = ?",
                new String[]{name},
                null);
    }

    /**
     * INSERTION TEST
     * <p>
     * Test whether insertion returns correct value for single insert
     */
    public void testInsertSingle() {
        assertTrue("Result of insertion cannot be -1", insertAuthor("John") != -1);
    }

    /**
     * INSERTION TEST
     * <p>
     * Test whether insertion returns correct value for insert with conflict. Expected result:
     * Single unchanged author.
     */
    public void testInsertConflict() {

        long resultId1 = insertAuthor("John");
        long resultId2 = insertAuthor("John");

        Cursor authorCursor = selectAuthorByName("John");

        assertNotNull(authorCursor);
        assertEquals(1, authorCursor.getCount());
        assertEquals("John", getAuthorName(authorCursor));
        assertEquals("When inserting and conflict occurs we should return id of row in conflict", resultId1, resultId2);

        authorCursor.close();
    }

    /**
     * INSERTION TEST
     * <p>
     * Test bulk insertion
     */
    public void testBulkInsertAuthor() {

        ContentValues[] values = new ContentValues[]{
                generateAuthor("John").getContentValues(),
                generateAuthor("Mary").getContentValues(),
                generateAuthor("Susan").getContentValues()
        };

        getProvider().bulkInsert(Contract.Authors.CONTENT_URI, values);

        assertEquals("John", getAuthorName(selectAuthorByName("John")));
        assertEquals("Mary", getAuthorName(selectAuthorByName("Mary")));
        assertEquals("Susan", getAuthorName(selectAuthorByName("Susan")));
    }

    /**
     * INSERTION TEST
     * <p>
     * Test bulk insertion with conflicting names
     */
    public void testBulkInsertAuthorConflict() {

        ContentValues[] values = new ContentValues[]{
                generateAuthor("John").getContentValues(),
                generateAuthor("John").getContentValues(),
                generateAuthor("John").getContentValues()
        };

        getProvider().bulkInsert(Contract.Authors.CONTENT_URI, values);

        Cursor cursor = selectAllAuthors();
        assertEquals(1, cursor.getCount());
        assertEquals("John", getAuthorName(selectAuthorByName("John")));
    }

    /**
     * SELECTION TEST
     * <p>
     * Test whether selection by PRIMARY KEY _id is working, where _id value is passed manually to where clause.
     */
    public void testSelectAllAuthorsById() {
        long resultId = insertAuthor("John");

        Cursor authorCursor = getProvider().query(
                Contract.Authors.CONTENT_URI,
                null,
                Contract.Authors.AUTHOR_ID + " = ?",
                new String[]{Long.toString(resultId)},
                null);

        assertNotNull(authorCursor);
        assertEquals(1, authorCursor.getCount());
        assertEquals("John", getAuthorName(authorCursor));

        authorCursor.close();
    }

    /**
     * SELECTION TEST
     * <p>
     * Test whether selection by UNIQUE author_name is working.
     */
    public void testSelectAllAuthorsByName() {
        insertAuthor("John");

        Cursor authorCursor = selectAuthorByName("John");

        assertNotNull(authorCursor);
        assertEquals(1, authorCursor.getCount());
        assertEquals("John", getAuthorName(authorCursor));

        authorCursor.close();
    }

    /**
     * SELECTION TEST
     * <p>
     * Test whether selection by PRIMARY KEY _id is working (correct implementation).
     */
    public void testSelectSingleAuthorById() {
        long resultId = insertAuthor("John");

        Cursor authorCursor = selectAuthorById(resultId);

        assertNotNull(authorCursor);
        assertEquals(1, authorCursor.getCount());
        assertEquals("John", getAuthorName(authorCursor));

        authorCursor.close();
    }

    /**
     * SELECTION TEST
     * <p>
     * Test whether selection by PRIMARY KEY _id is working (correct implementation). Multiple
     * values inserted in database.
     */
    public void testSelectSingleAuthorById2() {
        long resultId1 = insertAuthor("John");
        long resultId2 = insertAuthor("Mary");

        Cursor authorCursor1 = selectAuthorById(resultId1);
        Cursor authorCursor2 = selectAuthorById(resultId2);

        assertNotNull(authorCursor1);
        assertNotNull(authorCursor2);

        assertEquals(getAuthorName(authorCursor1), "John");
        assertEquals(getAuthorName(authorCursor2), "Mary");

        authorCursor1.close();
        authorCursor2.close();
    }
}
