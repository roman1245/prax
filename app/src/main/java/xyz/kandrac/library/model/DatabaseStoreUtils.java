package xyz.kandrac.library.model;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

import xyz.kandrac.library.model.obj.Author;
import xyz.kandrac.library.model.obj.Book;
import xyz.kandrac.library.model.obj.Publisher;


/**
 * Database utils related to storing data into database.
 * <p/>
 * Created by kandrac on 21/11/15.
 */
public final class DatabaseStoreUtils {

    private DatabaseStoreUtils() {
    }

    /**
     * Save {@link xyz.kandrac.library.model.obj.Book} into database via provided {@link ContentResolver}. Also store every detail
     * about book such as {@link Publisher} and all its {@link Author}s.
     *
     * @param contentResolver for database
     * @param book            to store
     * @return book ID
     */
    public static long saveBook(ContentResolver contentResolver, Book book) {

        if (book.id > 0) {
            deleteBookAuthor(contentResolver, book.id);
        }

        long publisherId = savePublisher(contentResolver, book.publisher);

        long bookId = saveBookOnly(contentResolver, book, publisherId);

        if (book.authors != null && book.authors.length > 0) {
            for (Author author : book.authors) {
                long authorId = saveAuthor(contentResolver, author);
                saveBookAuthor(contentResolver, bookId, authorId);
            }
        } else {
            Author author = new Author.Builder().setName("").build();
            long authorId = saveAuthor(contentResolver, author);
            saveBookAuthor(contentResolver, bookId, authorId);
        }

        return bookId;
    }

    /**
     * Save {@link Book} into database via provided {@link ContentResolver}. This method doesn't
     * store any additional data, just book itself.
     *
     * @param contentResolver for database
     * @param book            to store
     * @param publisherId     of existing publisher
     * @return book ID
     */
    public static long saveBookOnly(ContentResolver contentResolver, Book book, long publisherId) {

        ContentValues bookContentValues = new ContentValues();

        bookContentValues.put(Contract.Books.BOOK_TITLE, book.title);
        bookContentValues.put(Contract.Books.BOOK_SUBTITLE, book.subtitle);
        bookContentValues.put(Contract.Books.BOOK_IMAGE_FILE, book.imageFilePath);
        bookContentValues.put(Contract.Books.BOOK_DESCRIPTION, book.description);
        bookContentValues.put(Contract.Books.BOOK_IMAGE_URL, book.imageUrlPath);
        bookContentValues.put(Contract.Books.BOOK_ISBN, book.isbn);
        bookContentValues.put(Contract.Books.BOOK_AUTHORS_READ, book.authorsReadable);
        bookContentValues.put(Contract.Books.BOOK_PUBLISHER_ID, publisherId);
        bookContentValues.put(Contract.Books.BOOK_PUBLISHER_READ, book.publisherReadable);
        bookContentValues.put(Contract.Books.BOOK_WISH_LIST, book.wish);

        if (book.id > 0) {
            contentResolver.update(Contract.Books.buildBookUri(book.id), bookContentValues, null, null);
            return book.id;
        } else {
            Uri bookUri = contentResolver.insert(Contract.Books.CONTENT_URI, bookContentValues);
            return Contract.Books.getBookId(bookUri);
        }

    }

    /**
     * Save {@link Publisher} into database via provided {@link ContentResolver}.
     *
     * @param contentResolver for database
     * @param publisher       to store
     * @return publisher ID
     */
    public static long savePublisher(ContentResolver contentResolver, Publisher publisher) {

        ContentValues publisherContentValues = new ContentValues();
        publisherContentValues.put(Contract.Publishers.PUBLISHER_NAME, publisher.name);

        Uri publisherUri = contentResolver.insert(Contract.Publishers.CONTENT_URI, publisherContentValues);

        return Contract.Publishers.getPublisherId(publisherUri);
    }

    /**
     * Save {@link Author} into database via provided {@link ContentResolver}.
     *
     * @param contentResolver for database
     * @param author          to store
     * @return author ID
     */
    public static long saveAuthor(ContentResolver contentResolver, Author author) {

        ContentValues authorContentValues = new ContentValues();
        authorContentValues.put(Contract.Authors.AUTHOR_NAME, author.name);

        Uri authorUri = contentResolver.insert(Contract.Authors.CONTENT_URI, authorContentValues);

        return Contract.Authors.getAuthorId(authorUri);
    }

    /**
     * Save connection between {@link Book} and {@link Author}
     *
     * @param contentResolver for database
     * @param bookId          for connection
     * @param authorId        for connection
     */
    public static void saveBookAuthor(ContentResolver contentResolver, long bookId, long authorId) {

        ContentValues bookAuthorContentValues = new ContentValues();
        bookAuthorContentValues.put(Contract.BookAuthors.BOOK_ID, bookId);
        bookAuthorContentValues.put(Contract.BookAuthors.AUTHOR_ID, authorId);

        contentResolver.insert(Contract.BookAuthors.CONTENT_URI, bookAuthorContentValues);
    }

    /**
     * Remove all book-author connections for given {@link Book} based on its ID.
     *
     * @param contentResolver for database
     * @param bookId          to remove references to
     * @return number of rows deleted
     */
    public static long deleteBookAuthor(ContentResolver contentResolver, long bookId) {
        return contentResolver.delete(Contract.BOOKS_AUTHORS_URI, Contract.BookAuthors.BOOK_ID + " = ?", new String[]{Long.toString(bookId)});
    }
}
