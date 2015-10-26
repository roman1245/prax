package kandrac.xyz.library.model;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

/**
 * Content provider for all database items.
 * Created by VizGhar on 9.8.2015.
 */
public class DatabaseProvider extends ContentProvider {

    private Database databaseHelper;

    private static final UriMatcher uriMatcher = buildUriMatcher();

    public static final int BOOKS = 100;
    public static final int BOOK_ID = 101;
    public static final int BOOK_ID_AUTHOR = 102;

    public static final int AUTHORS = 200;
    public static final int AUTHOR_ID = 201;
    public static final int AUTHOR_BOOKS = 202;

    public static final int BOOKS_AUTHORS = 300;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = Contract.CONTENT_AUTHORITY;

        uriMatcher.addURI(authority, "books", BOOKS);
        uriMatcher.addURI(authority, "books/#", BOOK_ID);
        uriMatcher.addURI(authority, "books/#/authors", BOOK_ID_AUTHOR);
        uriMatcher.addURI(authority, "authors", AUTHORS);
        uriMatcher.addURI(authority, "authors/#", AUTHOR_ID);
        uriMatcher.addURI(authority, "authors/#/books", AUTHOR_BOOKS);

        uriMatcher.addURI(authority, "books/authors", BOOKS_AUTHORS);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        final Context context = getContext();
        databaseHelper = new Database(context);
        return true;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case BOOKS:
                return Contract.Books.CONTENT_TYPE;
            case BOOK_ID:
                return Contract.Books.CONTENT_ITEM_TYPE;
            case AUTHORS:
                return Contract.Authors.CONTENT_TYPE;
            case AUTHOR_ID:
                return Contract.Authors.CONTENT_ITEM_TYPE;
            case AUTHOR_BOOKS:
                return Contract.Books.CONTENT_TYPE;
            case BOOKS_AUTHORS:
                return Contract.Books.CONTENT_TYPE;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)) {
            case BOOKS:
                qb.setTables(Database.Tables.BOOKS);
                sortOrder = sortOrder == null ? Contract.Books.DEFAULT_SORT : sortOrder;
                break;
            case BOOK_ID:
                qb.setTables(Database.Tables.BOOKS);
                qb.appendWhere(Contract.Books.BOOK_ID + "=" + Contract.Books.getBookId(uri));
                break;
            case BOOK_ID_AUTHOR:
                qb.setTables(Database.Tables.BOOKS_JOIN_AUTHORS_ID);
                qb.appendWhere(Database.Tables.BOOKS + "." + Contract.Books.BOOK_ID + "=" + Contract.Books.getBookId(uri));
                break;
            case AUTHORS:
                qb.setTables(Database.Tables.AUTHORS);
                sortOrder = sortOrder == null ? Contract.Authors.DEFAULT_SORT : sortOrder;
                break;
            case AUTHOR_ID:
                qb.setTables(Database.Tables.AUTHORS);
                qb.appendWhere(Contract.Authors.AUTHOR_ID + "=" + Contract.Authors.getAuthorId(uri));
                break;
            case BOOKS_AUTHORS:
                qb.setTables(Database.Tables.BOOKS_JOIN_AUTHORS_ID);
                sortOrder = sortOrder == null ? Contract.Books.DEFAULT_SORT : sortOrder;
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

    @SuppressWarnings("ConstantConditions")
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)) {
            case BOOKS: {
                long result = db.insertOrThrow(Database.Tables.BOOKS, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return Contract.Books.buildBookUri(result);
            }
            case AUTHORS: {
                long result = db.insertOrThrow(Database.Tables.AUTHORS, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return Contract.Authors.buildBlockUri(result);
            }
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int count;
        String id;
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)) {
            case BOOKS:
                count = db.delete(Database.Tables.BOOKS, selection, selectionArgs);
                break;
            case BOOK_ID:
                id = Contract.Books.getBookId(uri);
                count = db.delete(Database.Tables.BOOKS, Contract.Books.BOOK_ID + " = " + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            case AUTHORS:
                count = db.delete(Database.Tables.AUTHORS, selection, selectionArgs);
                break;
            case AUTHOR_ID:
                id = Contract.Authors.getAuthorId(uri);
                count = db.delete(Database.Tables.AUTHORS, Contract.Authors.AUTHOR_ID + " = " + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
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
        String id;
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)) {
            case BOOKS:
                count = db.update(Database.Tables.BOOKS, values, selection, selectionArgs);
                break;
            case BOOK_ID:
                id = Contract.Books.getBookId(uri);
                count = db.update(Database.Tables.BOOKS, values, Contract.Books.BOOK_ID + " = " + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            case AUTHORS:
                count = db.update(Database.Tables.AUTHORS, values, selection, selectionArgs);
                break;
            case AUTHOR_ID:
                id = Contract.Books.getBookId(uri);
                count = db.update(Database.Tables.AUTHORS, values, Contract.Authors.AUTHOR_ID + " = " + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
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
