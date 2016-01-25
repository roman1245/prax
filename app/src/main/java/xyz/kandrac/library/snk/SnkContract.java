package xyz.kandrac.library.snk;

import android.net.Uri;
import android.provider.BaseColumns;

import xyz.kandrac.library.BuildConfig;

/**
 * Created by kandrac on 20/01/16.
 */
public final class SnkContract {

    private SnkContract() {
    }


    // Database Columns
    interface BooksColumns {
        String BOOK_ID = BaseColumns._ID;
        String BOOK_TITLE = "title";
        String BOOK_AUTHORS = "authors";
        String BOOK_ISBN = "isbn";
        String BOOK_PUBLISHER = "publisher";
    }

    public static final String PATH_BOOKS = "books";

    // Base URI specification (authority and its URI representation)
    public static final String CONTENT_AUTHORITY = BuildConfig.SNK_DATABASE_AUTHORITY;
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static class Books implements BooksColumns {

        public static final String DEFAULT_SORT = BOOK_TITLE + " ASC";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_BOOKS).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.books";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.books";

        public static String getBookIsbn(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static Uri getBookIsbnUri(String isbn) {
            return CONTENT_URI.buildUpon().appendPath(isbn).build();
        }
    }
}
