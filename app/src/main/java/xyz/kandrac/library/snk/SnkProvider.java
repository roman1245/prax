package xyz.kandrac.library.snk;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


/**
 * Created by kandrac on 20/01/16.
 */
public class SnkProvider extends ContentProvider {

    private SnkDatabase databaseHelper;

    private static final UriMatcher uriMatcher = buildUriMatcher();

    // Everything from books (SELECT, INSERT, UPDATE, DELETE)
    public static final int BOOKS = 100;
    public static final int BOOKS_ISBN = 101;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = SnkContract.CONTENT_AUTHORITY;

        uriMatcher.addURI(authority, "books", BOOKS);
        uriMatcher.addURI(authority, "books/*", BOOKS_ISBN);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        final Context context = getContext();
        databaseHelper = new SnkDatabase(context);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)) {
            case BOOKS:
                qb.setTables("books");
                sortOrder = sortOrder == null ? SnkContract.Books.DEFAULT_SORT : sortOrder;
                break;
            case BOOKS_ISBN:
                qb.setTables("books");
                qb.appendWhere(SnkContract.Books.BOOK_ISBN + "=" + SnkContract.Books.getBookIsbn(uri));
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        Cursor cursor = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        Context context = getContext();
        if (context != null) {
            cursor.setNotificationUri(context.getContentResolver(), uri);
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {

        switch (uriMatcher.match(uri)) {
            case BOOKS:
                return SnkContract.Books.CONTENT_TYPE;
            case BOOKS_ISBN:
                return SnkContract.Books.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("Insertion not enabled into SNK database");
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Deletion not enabled from SNK database");
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Updating not enabled in SNK database");
    }
}
