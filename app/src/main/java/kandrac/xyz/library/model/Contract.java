package kandrac.xyz.library.model;

import android.content.ContentValues;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by kandrac on 26/10/15.
 */
public class Contract {

    private Contract() {

    }

    // Database Columns
    interface BooksColumns {
        String BOOK_ID = BaseColumns._ID;
        String BOOK_TITLE = "book_title";
        String BOOK_SUBTITLE = "book_subtitle";
        String BOOK_ISBN = "book_isbn";
        String BOOK_DESCRIPTION = "book_description";
        String BOOK_IMAGE_FILE = "book_image_file";
        String BOOK_PUBLISHER_ID = "book_publisher_id";
        String BOOK_IMAGE_URL = "book_image_url";
        String BOOK_AUTHORS_READ = "book_authors_readable";
        String BOOK_BORROWED = "book_borrowed";
    }

    interface AuthorsColumns {
        String AUTHOR_ID = BaseColumns._ID;
        String AUTHOR_NAME = "author_name";
    }

    interface PublishersColumns {
        String PUBLISHER_ID = BaseColumns._ID;
        String PUBLISHER_NAME = "publisher_name";
    }

    interface BookAuthorsColumns {
        String BOOK_ID = "book_id";
        String AUTHOR_ID = "author_id";
    }

    interface BorrowInfoColumns {
        String BORROW_ID = BaseColumns._ID;
        String BORROW_BOOK_ID = "borrow_book_id";
        String BORROW_TO = "borrow_to";
        String BORROW_MAIL = "borrow_mail";
        String BORROW_PHONE = "borrow_phone";
        String BORROW_NAME = "borrow_name";
        String BORROW_DATE_BORROWED = "date_borrowed";
        String BORROW_DATE_RETURNED = "date_returned";
    }

    // URI Paths
    public static final String PATH_BOOKS = "books";
    public static final String PATH_AUTHORS = "authors";
    public static final String PATH_PUBLISHERS = "publishers";
    public static final String PATH_BOOKS_AUTHORS = "books/authors";
    public static final String PATH_BORROW_INFO = "borrowinfo";

    // Base URI specification (authority and its URI representation)
    public static final String CONTENT_AUTHORITY = "xyz.kandrac.Library";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final Uri BOOKS_AUTHORS_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_BOOKS).appendPath(PATH_AUTHORS).build();
    public static final Uri BOOKS_BORROW_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_BOOKS).appendPath(PATH_BORROW_INFO).build();

    /**
     *
     */
    public static class Books implements BooksColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_BOOKS).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.books";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.books";


        /**
         * Default "ORDER BY" clause.
         */
        public static final String DEFAULT_SORT = BOOK_TITLE + " ASC";


        /**
         * Build {@link Uri} for requested {@link #BOOK_ID}.
         */
        public static Uri buildBookUri(long bookId) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(bookId)).build();
        }

        /**
         * Build {@link Uri} for requested {@link #BOOK_ID}.
         */
        public static Uri buildBookWithAuthorUri(long bookId) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(bookId)).appendPath(PATH_AUTHORS).build();
        }

        /**
         * Read {@link #BOOK_ID} from {@link Books} {@link Uri}.
         */
        public static long getBookId(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(1));
        }

        public static Uri buildBorrowInfoUri(long bookId) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(bookId)).appendPath(PATH_BORROW_INFO).build();
        }
    }

    /**
     *
     */
    public static class Authors implements AuthorsColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_AUTHORS).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.authors";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.authors";


        /**
         * Default "ORDER BY" clause.
         */
        public static final String DEFAULT_SORT = AUTHOR_NAME + " ASC";


        /**
         * Build {@link Uri} for requested {@link #AUTHOR_ID}.
         */
        public static Uri buildAuthorUri(long authorId) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(authorId)).build();
        }

        /**
         * Build {@link Uri} that references any {@link Books} associated
         * with the requested {@link #AUTHOR_ID}.
         */
        public static Uri buildBooksUri(long authorId) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(authorId)).appendPath(PATH_BOOKS).build();
        }

        /**
         * Read {@link #AUTHOR_ID} from {@link Authors} {@link Uri}.
         */
        public static long getAuthorId(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(1));
        }
    }

    /**
     *
     */
    public static class Publishers implements PublishersColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PUBLISHERS).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.publishers";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.publishers";


        /**
         * Default "ORDER BY" clause.
         */
        public static final String DEFAULT_SORT = PUBLISHER_NAME + " ASC";


        /**
         * Build {@link Uri} for requested {@link #PUBLISHER_ID}.
         */
        public static Uri buildPublisherUri(long publisherId) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(publisherId)).build();
        }

        /**
         * Build {@link Uri} that references any {@link Books} associated
         * with the requested {@link #PUBLISHER_ID}.
         */
        public static Uri buildBooksUri(long publisherId) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(publisherId)).appendPath(PATH_BOOKS).build();
        }

        /**
         * Read {@link #PUBLISHER_ID} from {@link Authors} {@link Uri}.
         */
        public static long getPublisherId(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(1));
        }
    }

    public static class BookAuthors implements BookAuthorsColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_BOOKS).appendPath(PATH_AUTHORS).build();

        public static ContentValues generateContentValues(long bookId, long authorId) {
            ContentValues cv = new ContentValues();
            cv.put(BOOK_ID, bookId);
            cv.put(AUTHOR_ID, authorId);
            return cv;
        }
    }

    public static class BorrowInfo implements BorrowInfoColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_BORROW_INFO).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.borrow";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.borrow";

        /**
         * Default "ORDER BY" clause.
         */
        public static final String DEFAULT_SORT = BORROW_DATE_BORROWED + " ASC";

        public static Uri buildUri(long borrowId) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(borrowId)).build();
        }

        public static long getBookId(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(1));
        }
    }
}
