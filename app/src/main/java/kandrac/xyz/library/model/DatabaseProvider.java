package kandrac.xyz.library.model;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import kandrac.xyz.library.model.obj.Author;
import kandrac.xyz.library.model.obj.Book;

/**
 * Content provider for all database items.
 * Created by VizGhar on 9.8.2015.
 */
public class DatabaseProvider extends ContentProvider {

    public static final String PROVIDER_NAME = "xyz.kandrac.Library";
    public static final String URL = "content://" + PROVIDER_NAME;
    public static final Uri CONTENT_URI = Uri.parse(URL);

    public static final int BOOKS = 1;
    public static final int BOOK_ID = 2;
    public static final int AUTHORS = 3;
    public static final int AUTHOR_ID = 4;

    @IntDef({BOOKS, AUTHORS})
    @Retention(RetentionPolicy.SOURCE)
    @interface UriType {
    }

    @IntDef({BOOK_ID, AUTHOR_ID})
    @Retention(RetentionPolicy.SOURCE)
    @interface UriTypeId {
    }

    static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, Book.TABLE_NAME, BOOKS);
        uriMatcher.addURI(PROVIDER_NAME, Book.TABLE_NAME + "/#", BOOK_ID);
        uriMatcher.addURI(PROVIDER_NAME, Author.TABLE_NAME, AUTHORS);
        uriMatcher.addURI(PROVIDER_NAME, Author.TABLE_NAME + "/#", AUTHOR_ID);
    }

    private SQLiteDatabase db;

    public static Uri getUri(@UriType int type) {
        switch (type) {
            case BOOKS:
                return Uri.parse(URL + "/" + Book.TABLE_NAME);
            case AUTHORS:
                return Uri.parse(URL + "/" + Author.TABLE_NAME);
        }
        throw new IllegalArgumentException("Unknown Type" + type);
    }

    public static Uri getUriWithId(@UriTypeId int type, long id) {
        switch (type) {
            case BOOK_ID:
                return Uri.parse(URL + "/" + Book.TABLE_NAME + "/" + id);
            case AUTHOR_ID:
                return Uri.parse(URL + "/" + Author.TABLE_NAME + "/" + id);
        }
        throw new IllegalArgumentException("Unknown Type" + type);
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        Database dbHelper = new Database(context);

        /**
         * Create a write able database which will trigger its
         * creation if it doesn't already exist.
         */
        db = dbHelper.getWritableDatabase();
        return db != null;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (uriMatcher.match(uri)) {
            case BOOKS:
                qb.setTables(Book.TABLE_NAME);
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = Book.COLUMN_TITLE;
                }
                break;
            case BOOK_ID:
                qb.setTables(Book.TABLE_NAME);
                qb.appendWhere(Book.COLUMN_ID + "=" + uri.getPathSegments().get(1));
                break;
            case AUTHORS:
                qb.setTables(Author.TABLE_NAME);
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = Author.COLUMN_NAME;
                }
                break;
            case AUTHOR_ID:
                qb.setTables(Author.TABLE_NAME);
//                qb.appendWhere(Author.COLUMN_ID + "=" + uri.getPathSegments().get(1));
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

    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case BOOKS:
                return "vnd.android.cursor.dir/vnd.books";
            case BOOK_ID:
                return "vnd.android.cursor.item/vnd.books";
            case AUTHORS:
                return "vnd.android.cursor.dir/vnd.authors";
            case AUTHOR_ID:
                return "vnd.android.cursor.item/vnd.authors";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        long rowID;
        switch (uriMatcher.match(uri)) {
            case BOOKS:
                rowID = db.insert(Book.TABLE_NAME, "", values);
                break;
            case AUTHORS:
                rowID = db.insert(Author.TABLE_NAME, "", values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            Context context = getContext();
            if (context != null) {
                context.getContentResolver().notifyChange(uri, null);
            }
            return _uri;
        }
        throw new SQLException("Failed to add a record into " + uri);
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int count;
        String id;

        switch (uriMatcher.match(uri)) {
            case BOOKS:
                count = db.delete(Book.TABLE_NAME, selection, selectionArgs);
                break;
            case BOOK_ID:
                id = uri.getPathSegments().get(1);
                count = db.delete(Book.TABLE_NAME, Book.COLUMN_ID + " = " + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            case AUTHORS:
                count = db.delete(Author.TABLE_NAME, selection, selectionArgs);
                break;
            case AUTHOR_ID:
                id = uri.getPathSegments().get(1);
                count = 0;
//                count = db.delete(Author.TABLE_NAME, Author.COLUMN_ID + " = " + id +
//                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        Context context = getContext();
        if (context != null) {
            context.getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count;
        switch (uriMatcher.match(uri)) {
            case BOOKS:
                count = db.update(Book.TABLE_NAME, values, selection, selectionArgs);
                break;
            case BOOK_ID:
                count = db.update(Book.TABLE_NAME, values, Book.COLUMN_ID + " = " + uri.getPathSegments().get(1) +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            case AUTHORS:
                count = db.update(Author.TABLE_NAME, values, selection, selectionArgs);
                break;
            case AUTHOR_ID:
                count = 0;
//                count = db.update(Author.TABLE_NAME, values, Author.COLUMN_ID + " = " + uri.getPathSegments().get(1) +
//                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        Context context = getContext();
        if (context != null) {
            context.getContentResolver().notifyChange(uri, null);
        }
        return count;
    }
}
