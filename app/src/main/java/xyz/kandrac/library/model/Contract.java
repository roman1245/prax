package xyz.kandrac.library.model;

import android.content.ContentValues;
import android.net.Uri;
import android.provider.BaseColumns;

import xyz.kandrac.library.BuildConfig;

/**
 * Contracts linked to {@link Database} that should be easily used via {@link DatabaseProvider}.
 * In order to access data from database please use URI's from these classes:
 * <ul>
 * <li>{@link Books}</li>
 * <li>{@link Authors}</li>
 * <li>{@link Publishers}</li>
 * <li>{@link BorrowInfo}</li>
 * </ul>
 * <p/>
 * Created by kandrac on 26/10/15.
 */
@SuppressWarnings("ALL")
public final class Contract {

    private Contract() {
    }

    // Database Columns
    public interface BooksColumns {
        String BOOK_ID = BaseColumns._ID;
        String BOOK_TITLE = "book_title";
        String BOOK_SUBTITLE = "book_subtitle";
        String BOOK_ISBN = "book_isbn";
        String BOOK_DESCRIPTION = "book_description";
        String BOOK_IMAGE_FILE = "book_image_file";
        String BOOK_PUBLISHER_ID = "book_publisher_id";
        String BOOK_LIBRARY_ID = "book_library_id";
        String BOOK_IMAGE_URL = "book_image_url";
        String BOOK_BORROWED = "book_borrowed";
        String BOOK_BORROWED_TO_ME = "book_borrowed_to_me";
        String BOOK_WISH_LIST = "book_wish";
        String BOOK_UPDATED_AT = "book_updated_at";
        String BOOK_PUBLISHED = "book_published";
        String BOOK_REFERENCE = "book_reference";
        String BOOK_PROGRESS = "book_progress";
        String BOOK_MY_SCORE = "book_my_score";
        String BOOK_QUOTE = "book_quote";
        String BOOK_NOTES = "book_notes";
    }

    interface AuthorsColumns {
        String AUTHOR_ID = BaseColumns._ID;
        String AUTHOR_NAME = "author_name";
    }

    interface PublishersColumns {
        String PUBLISHER_ID = BaseColumns._ID;
        String PUBLISHER_NAME = "publisher_name";
    }

    interface LibrariesColumns {
        String LIBRARY_ID = BaseColumns._ID;
        String LIBRARY_NAME = "library_name";
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
        String BORROW_NEXT_NOTIFICATION = "borrow_next_notify";
    }

    interface FeedbackColumns {
        String FEEDBACK_REFERENCE = "feedback_reference";
        String FEEDBACK_VALUE = "feedback_value";
    }

    interface GenresColumns {
        String GENRE_ID = BaseColumns._ID;
        String GENRE_NAME = "genre_name";
    }

    public interface BorrowMeInfoColumns {
        String BORROW_ID = BaseColumns._ID;
        String BORROW_BOOK_ID = "borrow_me_book_id";
        String BORROW_NAME = "borrow_me_name";
        String BORROW_DATE_BORROWED = "borrow_me_date_borrowed";
    }

    // Aliases
    // Base URI specification (authority and its URI representation)
    public static final String CONTENT_AUTHORITY = BuildConfig.DATABASE_AUTHORITY;

    public interface ConcatAliases {
        String AUTHORS_CONCAT_ALIAS = "concat";

    }

    // URI Paths
    public static final String PATH_BOOKS = "books";
    public static final String PATH_AUTHORS = "authors";
    public static final String PATH_PUBLISHERS = "publishers";
    public static final String PATH_LIBRARIES = "libraries";
    public static final String PATH_FEEDBACK = "feedback";

    public static final String PATH_BORROW_INFO = "borrow_info";
    public static final String PATH_BORROW_ME_INFO = "borrow_me_info";
    public static final String PATH_REFERENCE = "ref";
    public static final String PATH_ISBN = "isbn";
    public static final String PATH_GENRES = "genres";

    public static final String PATH_SPECIAL = "special";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final Uri BOOKS_AUTHORS_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_BOOKS).appendPath(PATH_AUTHORS).build();

    /**
     * Details about Book contract can be obtained from:
     * <ul>
     * <li>Table name: {@link xyz.kandrac.library.model.Database.Tables#BOOKS}</li>
     * <li>Table columns: {@link xyz.kandrac.library.model.Contract.BooksColumns}</li>
     * <li>Requests for {@link android.content.ContentProvider} from:
     * <ul>
     * <li>{@link #CONTENT_URI} or</li>
     * <li>methods from this class</li>
     * </ul>
     * </li>
     * </ul>
     * To see more detailed info about requests please see {@link DatabaseProvider} constants
     */
    public static class Books implements BooksColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_BOOKS).build();
        public static String FULL_BOOK_ID = Database.Tables.BOOKS + "." + BOOK_ID;

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
        public static Uri buildBookUri(String bookId) {
            return CONTENT_URI.buildUpon().appendPath(bookId).build();
        }

        /**
         * Build {@link Uri} for requested {@link #BOOK_ID}.
         */
        public static Uri buildBookFirebaseUri(String firebaseReference) {
            return CONTENT_URI.buildUpon().appendPath(PATH_REFERENCE).appendPath(firebaseReference).build();
        }

        public static Uri buildBookIsbnUri(String isbn) {
            return CONTENT_URI.buildUpon().appendPath(PATH_ISBN).appendPath(isbn).build();
        }

        public static Uri buildBookPublisherUri(long bookId) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(bookId)).appendPath(PATH_PUBLISHERS).build();
        }

        public static Uri buildBookPublisherFirebaseUri(String reference) {
            return CONTENT_URI.buildUpon().appendPath(PATH_REFERENCE).appendPath(reference).appendPath(PATH_PUBLISHERS).build();
        }

        public static Uri buildBookLibraryUri(long bookId) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(bookId)).appendPath(PATH_LIBRARIES).build();
        }

        public static Uri buildBookLibraryFirebaseUri(String reference) {
            return CONTENT_URI.buildUpon().appendPath(PATH_REFERENCE).appendPath(reference).appendPath(PATH_LIBRARIES).build();
        }

        /**
         * Build {@link Uri} for requested {@link #BOOK_ID}.
         */
        public static Uri buildBookWithAuthorUri(long bookId) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(bookId)).appendPath(PATH_AUTHORS).build();
        }

        private static Uri buildBookWithAuthorFirebaseUri(String reference) {
            return CONTENT_URI.buildUpon().appendPath(PATH_REFERENCE).appendPath(reference).appendPath(PATH_AUTHORS).build();
        }

        private static Uri buildBorrowedToMeInfoFirebaseUri(String reference) {
            return CONTENT_URI.buildUpon().appendPath(PATH_REFERENCE).appendPath(reference).appendPath(PATH_BORROW_ME_INFO).build();
        }

        /**
         * Read {@link #BOOK_ID} from {@link Books} {@link Uri}.
         */
        public static long getBookId(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(1));
        }

        /**
         * Read {@link #BOOK_ID} from {@link Books} {@link Uri}.
         */
        public static String getBookIsbn(Uri uri) {
            return uri.getPathSegments().get(2);
        }

        public static String getBookReference(Uri uri) {
            return uri.getPathSegments().get(2);
        }

        public static Uri buildBorrowInfoUri(long bookId) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(bookId)).appendPath(PATH_BORROW_INFO).build();
        }

        public static Uri buildBorrowedToMeInfoUri(long bookId) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(bookId)).appendPath(PATH_BORROW_ME_INFO).build();
        }

        public static Uri buildBookPublisherUri(Uri bookUri) {
            if (isReferenceUri(bookUri)) {
                return buildBookPublisherFirebaseUri(getBookReference(bookUri));
            } else {
                return buildBookPublisherUri(getBookId(bookUri));
            }
        }

        public static Uri buildBookLibraryUri(Uri bookUri) {
            if (isReferenceUri(bookUri)) {
                return buildBookLibraryFirebaseUri(getBookReference(bookUri));
            } else {
                return buildBookLibraryUri(getBookId(bookUri));
            }
        }

        public static Uri buildBookAuthorUri(Uri bookUri) {
            if (isReferenceUri(bookUri)) {
                return buildBookWithAuthorFirebaseUri(getBookReference(bookUri));
            } else {
                return buildBookWithAuthorUri(getBookId(bookUri));
            }
        }

        public static Uri buildBorrowedToMeInfoUri(Uri bookUri) {
            if (isReferenceUri(bookUri)) {
                return buildBorrowedToMeInfoFirebaseUri(getBookReference(bookUri));
            } else {
                return buildBorrowedToMeInfoUri(getBookId(bookUri));
            }
        }

        private static boolean isReferenceUri(Uri bookUri) {
            return bookUri.getPathSegments().size() == 3;
        }
    }

    /**
     * Details about Author contract can be obtained from:
     * <ul>
     * <li>Table name: {@link xyz.kandrac.library.model.Database.Tables#AUTHORS}</li>
     * <li>Table columns: {@link xyz.kandrac.library.model.Contract.AuthorsColumns}</li>
     * <li>Requests for {@link android.content.ContentProvider} from:
     * <ul>
     * <li>{@link #CONTENT_URI} or</li>
     * <li>methods from this class</li>
     * </ul>
     * </li>
     * </ul>
     * To see more detailed info about requests please see {@link DatabaseProvider} constants
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
     * Details about Publisher contract can be obtained from:
     * <ul>
     * <li>Table name: {@link xyz.kandrac.library.model.Database.Tables#PUBLISHERS}</li>
     * <li>Table columns: {@link xyz.kandrac.library.model.Contract.PublishersColumns}</li>
     * <li>Requests for {@link android.content.ContentProvider} from:
     * <ul>
     * <li>{@link #CONTENT_URI} or</li>
     * <li>methods from this class</li>
     * </ul>
     * </li>
     * </ul>
     * To see more detailed info about requests please see {@link DatabaseProvider} constants
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

    /**
     * Details about Libraries contract can be obtained from:
     * <ul>
     * <li>Table name: {@link xyz.kandrac.library.model.Database.Tables#LIBRARIES}</li>
     * <li>Table columns: {@link xyz.kandrac.library.model.Contract.LibrariesColumns}</li>
     * <li>Requests for {@link android.content.ContentProvider} from:
     * <ul>
     * <li>{@link #CONTENT_URI} or</li>
     * <li>methods from this class</li>
     * </ul>
     * </li>
     * </ul>
     * To see more detailed info about requests please see {@link DatabaseProvider} constants
     */
    public static class Libraries implements LibrariesColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_LIBRARIES).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.libraries";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.libraries";


        /**
         * Default "ORDER BY" clause.
         */
        public static final String DEFAULT_SORT = LIBRARY_NAME + " ASC";


        /**
         * Build {@link Uri} for requested {@link #LIBRARY_ID}.
         */
        public static Uri buildLibraryUri(long libraryId) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(libraryId)).build();
        }

        /**
         * Build {@link Uri} that references any {@link Books} associated
         * with the requested {@link #LIBRARY_ID}.
         */
        public static Uri buildBooksUri(long publisherId) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(publisherId)).appendPath(PATH_BOOKS).build();
        }

        /**
         * Read {@link #LIBRARY_ID} from {@link Libraries} {@link Uri}.
         */
        public static long getLibraryId(Uri uri) {
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

    /**
     * Details about Book Borrow Info contract can be obtained from:
     * <ul>
     * <li>Table name: {@link xyz.kandrac.library.model.Database.Tables#BORROW_INFO}</li>
     * <li>Table columns: {@link xyz.kandrac.library.model.Contract.BorrowInfoColumns}</li>
     * <li>Requests for {@link android.content.ContentProvider} from:
     * <ul>
     * <li>{@link #CONTENT_URI} or</li>
     * <li>methods from this class</li>
     * </ul>
     * </li>
     * </ul>
     * To see more detailed info about requests please see {@link DatabaseProvider} constants
     */
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

        public static long getBorrowInfoId(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(1));
        }
    }

    /**
     * Details about Book Borrow Me contract can be obtained from:
     * <ul>
     * <li>Table name: {@link xyz.kandrac.library.model.Database.Tables#BORROW_ME}</li>
     * <li>Table columns: {@link xyz.kandrac.library.model.Contract.BorrowMeInfoColumns}</li>
     * <li>Requests for {@link android.content.ContentProvider} from:
     * <ul>
     * <li>{@link #CONTENT_URI} or</li>
     * <li>methods from this class</li>
     * </ul>
     * </li>
     * </ul>
     * To see more detailed info about requests please see {@link DatabaseProvider} constants
     */
    public static class BorrowMeInfo implements BorrowMeInfoColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_BORROW_ME_INFO).build();

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

    public static class Feedback implements FeedbackColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_FEEDBACK).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.feedback";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.feedback";

        public static Uri buildUri(String reference) {
            return CONTENT_URI.buildUpon().appendPath(reference).build();
        }

        public static String getReference(Uri uri) {
            return uri.getPathSegments().get(1);
        }

    }

    public static class Genres implements GenresColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_GENRES).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.genres";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.genres";

        public static Uri buildUri(long id) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(id)).build();
        }

        public static String getId(Uri uri) {
            return uri.getPathSegments().get(1);
        }

    }

    public static class Special {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_SPECIAL).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.special";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.special";

        public static final Uri TABLE_URI = CONTENT_URI.buildUpon().appendPath("table").build();
    }
}
