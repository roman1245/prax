package kandrac.xyz.library.model;

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
        String BOOK_ISBN = "book_isbn";
        String BOOK_DESCRIPTION = "book_description";
        String BOOK_IMAGE_FILE = "book_image_file";
        String BOOK_AUTHOR_ID = "book_author_id";
        String BOOK_PUBLISHER_ID = "book_publisher_id";
    }

    interface AuthorsColumns {
        String AUTHOR_ID = BaseColumns._ID;
        String AUTHOR_NAME = "author_name";
    }

    interface PublishersColumns {
        String PUBLISHER_ID = BaseColumns._ID;
        String PUBLISHER_NAME = "publisher_name";
    }

    // URI Paths
    public static final String PATH_BOOKS = "books";
    public static final String PATH_AUTHORS = "authors";
    public static final String PATH_PUBLISHERS = "publishers";

    // Base URI specification (authority and its URI representation)
    public static final String CONTENT_AUTHORITY = "xyz.kandrac.Library";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final Uri BOOKS_AUTHORS_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_BOOKS).appendPath(PATH_AUTHORS).build();

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
        public static String getBookId(Uri uri) {
            return uri.getPathSegments().get(1);
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
        public static Uri buildBooksUri(String authorId) {
            return CONTENT_URI.buildUpon().appendPath(authorId).appendPath(PATH_BOOKS).build();
        }

        /**
         * Read {@link #AUTHOR_ID} from {@link Authors} {@link Uri}.
         */
        public static String getAuthorId(Uri uri) {
            return uri.getPathSegments().get(1);
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
        public static Uri buildBooksUri(String authorId) {
            return CONTENT_URI.buildUpon().appendPath(authorId).appendPath(PATH_BOOKS).build();
        }

        /**
         * Read {@link #PUBLISHER_ID} from {@link Authors} {@link Uri}.
         */
        public static String getPublisherId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }
}