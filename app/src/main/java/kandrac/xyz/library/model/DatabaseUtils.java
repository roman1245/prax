package kandrac.xyz.library.model;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

import kandrac.xyz.library.model.obj.Author;
import kandrac.xyz.library.model.obj.Book;
import kandrac.xyz.library.model.obj.Publisher;

/**
 * Created by kandrac on 21/11/15.
 */
public final class DatabaseUtils {

    private DatabaseUtils() {
    }

    public static long saveBook(ContentResolver contentResolver, Book book) {

        long publisherId = savePublisher(contentResolver, book.publisher);

        long bookId = saveBookOnly(contentResolver, book, publisherId);

        for (Author author : book.authors) {
            long authorId = saveAuthor(contentResolver, author);
            saveBookAuthor(contentResolver, bookId, authorId);
        }

        return bookId;
    }

    public static long saveBookOnly(ContentResolver contentResolver, Book book, long publisherId) {

        ContentValues bookContentValues = new ContentValues();

        bookContentValues.put(Contract.Books.BOOK_TITLE, book.title);
        bookContentValues.put(Contract.Books.BOOK_SUBTITLE, book.subtitle);
        bookContentValues.put(Contract.Books.BOOK_IMAGE_FILE, book.imageFilePath);
        bookContentValues.put(Contract.Books.BOOK_DESCRIPTION, book.description);
        bookContentValues.put(Contract.Books.BOOK_IMAGE_URL, book.imageUrlPath);
        bookContentValues.put(Contract.Books.BOOK_BORROWED_TO, book.borrowedTo);
        bookContentValues.put(Contract.Books.BOOK_ISBN, book.isbn);
        bookContentValues.put(Contract.Books.BOOK_PUBLISHER_ID, publisherId);

        if (book.id > 0) {
            contentResolver.update(Contract.Books.buildBookUri(book.id), bookContentValues, null, null);
            return book.id;
        } else {
            Uri bookUri = contentResolver.insert(Contract.Books.CONTENT_URI, bookContentValues);
            return Contract.Books.getBookId(bookUri);
        }

    }

    public static long savePublisher(ContentResolver contentResolver, Publisher publisher) {

        ContentValues publisherContentValues = new ContentValues();
        publisherContentValues.put(Contract.Publishers.PUBLISHER_NAME, publisher.name);

        Uri publisherUri = contentResolver.insert(Contract.Publishers.CONTENT_URI, publisherContentValues);

        return Contract.Publishers.getPublisherId(publisherUri);
    }

    public static long saveAuthor(ContentResolver contentResolver, Author author) {

        ContentValues authorContentValues = new ContentValues();
        authorContentValues.put(Contract.Authors.AUTHOR_NAME, author.name);

        Uri authorUri = contentResolver.insert(Contract.Authors.CONTENT_URI, authorContentValues);

        return Contract.Authors.getAuthorId(authorUri);
    }

    public static void saveBookAuthor(ContentResolver contentResolver, long bookId, long authorId) {

        ContentValues bookAuthorContentValues = new ContentValues();
        bookAuthorContentValues.put(Contract.BookAuthors.BOOK_ID, bookId);
        bookAuthorContentValues.put(Contract.BookAuthors.AUTHOR_ID, authorId);

        contentResolver.insert(Contract.BookAuthors.CONTENT_URI, bookAuthorContentValues);
    }
}
