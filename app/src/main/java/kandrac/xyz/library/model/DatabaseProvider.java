package kandrac.xyz.library.model;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

/**
 * Content provider for all database items.
 * Created by VizGhar on 9.8.2015.
 */
public class DatabaseProvider extends ContentProvider {

    private static final String TAG = DatabaseProvider.class.getName();
    private Database databaseHelper;

    private static final UriMatcher uriMatcher = buildUriMatcher();

    // Everything from books (SELECT, INSERT, UPDATE, DELETE)
    public static final int BOOKS = 100;
    // Book by ID (SELECT, UPDATE, DELETE)
    public static final int BOOK_ID = 101;
    // Books by author id (SELECT)
    public static final int BOOK_BY_AUTHOR = 102;

    // Everything from authors (SELECT, INSERT, UPDATE, DELETE)
    public static final int AUTHORS = 200;
    // Author by id (SELECT, UPDATE, DELETE)
    public static final int AUTHOR_ID = 201;
    // Authors of book (SELECT)
    public static final int AUTHOR_BY_BOOK = 202;

    public static final int PUBLISHERS = 300;
    public static final int PUBLISHER_ID = 301;

    public static final int BOOKS_AUTHORS = 400;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = Contract.CONTENT_AUTHORITY;

        uriMatcher.addURI(authority, "books", BOOKS);
        uriMatcher.addURI(authority, "books/#", BOOK_ID);
        uriMatcher.addURI(authority, "books/#/authors", AUTHOR_BY_BOOK);
        uriMatcher.addURI(authority, "authors", AUTHORS);
        uriMatcher.addURI(authority, "authors/#", AUTHOR_ID);
        uriMatcher.addURI(authority, "authors/#/books", BOOK_BY_AUTHOR);
        uriMatcher.addURI(authority, "publishers", PUBLISHERS);
        uriMatcher.addURI(authority, "publishers/#", PUBLISHER_ID);

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
            case AUTHOR_BY_BOOK:
                return Contract.Books.CONTENT_TYPE;
            case PUBLISHERS:
                return Contract.Publishers.CONTENT_TYPE;
            case PUBLISHER_ID:
                return Contract.Publishers.CONTENT_ITEM_TYPE;
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
            case BOOK_BY_AUTHOR:
                qb.setTables(Database.Tables.BOOKS_JOIN_AUTHORS);
                qb.appendWhere(Database.Tables.AUTHORS + "." + Contract.Authors.AUTHOR_ID + "=" + Contract.Authors.getAuthorId(uri));
                break;
            case AUTHORS:
                qb.setTables(Database.Tables.AUTHORS);
                sortOrder = sortOrder == null ? Contract.Authors.DEFAULT_SORT : sortOrder;
                break;
            case AUTHOR_ID:
                qb.setTables(Database.Tables.AUTHORS);
                qb.appendWhere(Contract.Authors.AUTHOR_ID + "=" + Contract.Authors.getAuthorId(uri));
                break;
            case AUTHOR_BY_BOOK:
                qb.setTables(Database.Tables.BOOKS_JOIN_AUTHORS);
                qb.appendWhere(Database.Tables.BOOKS + "." + Contract.Books.BOOK_ID + "=" + Contract.Books.getBookId(uri));
                break;
            case PUBLISHERS:
                qb.setTables(Database.Tables.PUBLISHERS);
                sortOrder = sortOrder == null ? Contract.Publishers.DEFAULT_SORT : sortOrder;
                break;
            case PUBLISHER_ID:
                qb.setTables(Database.Tables.PUBLISHERS);
                qb.appendWhere(Contract.Publishers.PUBLISHER_ID + "=" + Contract.Publishers.getPublisherId(uri));
                break;
            case BOOKS_AUTHORS:
                qb.setTables(Database.Tables.BOOKS_JOIN_AUTHORS);
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
                long result = insertOrIgnore(db, values, Database.Tables.AUTHORS, Contract.Authors.AUTHOR_NAME);
                getContext().getContentResolver().notifyChange(uri, null);
                return Contract.Authors.buildAuthorUri(result);
            }
            case PUBLISHERS: {
                long result = insertOrIgnore(db, values, Database.Tables.PUBLISHERS, Contract.Publishers.PUBLISHER_NAME);
                getContext().getContentResolver().notifyChange(uri, null);
                return Contract.Publishers.buildPublisherUri(result);
            }
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    /**
     * Insert or ignore {@link ContentValues} to database. While inserting you are able to know any
     * unique column value except of {@link BaseColumns#_ID}, therefor we are updating based on
     * {@code uniqueColumn} name. In this case updating is completely ignored, but select request is required
     * in order to get the missing BaseColumns._ID value.
     *
     * @param db           database to insert to
     * @param values       values to insert
     * @param table        table to insert to
     * @param uniqueColumn unique column name
     * @return _id column value
     */
    private long insertOrIgnore(SQLiteDatabase db, ContentValues values, String table, String uniqueColumn) {
        long id = db.insert(table, null, values);
        return (id == -1) ? selectId(db, values, table, uniqueColumn) : id;
    }

    /**
     * Insert or update {@link ContentValues} to database. While inserting you are able to know any
     * unique column value except of {@link BaseColumns#_ID}, therefor we are updating based on
     * {@code uniqueColumn} name.
     *
     * @param db           database to insert to
     * @param values       values to insert
     * @param table        table to insert to
     * @param uniqueColumn unique column name
     * @return _id column value
     */
    private long insertOrUpdate(SQLiteDatabase db, ContentValues values, String table, String uniqueColumn) {
        long id = db.insert(table, null, values);
        if (id == -1) {
            id = selectId(db, values, table, uniqueColumn);
            db.update(table, values, uniqueColumn + " = ?", new String[]{values.getAsString(uniqueColumn)});
        }
        return id;
    }

    /**
     * Select {@link BaseColumns#_ID} from given table based on other {@code uniqueColumn}
     *
     * @param db           database to insert to
     * @param values       values to insert
     * @param table        table to insert to
     * @param uniqueColumn unique column name
     * @return _id column value
     */
    private long selectId(SQLiteDatabase db, ContentValues values, String table, String uniqueColumn) {
        String selectStatement = "SELECT " + BaseColumns._ID + " FROM " + table + " WHERE " + uniqueColumn + " = '" + values.getAsString(uniqueColumn) + "'";
        Log.d(TAG, "Select: " + selectStatement);
        return db.compileStatement(selectStatement).simpleQueryForLong();
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
            case PUBLISHERS:
                count = db.delete(Database.Tables.PUBLISHERS, selection, selectionArgs);
                break;
            case PUBLISHER_ID:
                id = Contract.Publishers.getPublisherId(uri);
                count = db.delete(Database.Tables.PUBLISHERS, Contract.Publishers.PUBLISHER_ID + " = " + id +
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
                id = Contract.Authors.getAuthorId(uri);
                count = db.update(Database.Tables.AUTHORS, values, Contract.Authors.AUTHOR_ID + " = " + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            case PUBLISHERS:
                count = db.update(Database.Tables.PUBLISHERS, values, selection, selectionArgs);
                break;
            case PUBLISHER_ID:
                id = Contract.Publishers.getPublisherId(uri);
                count = db.update(Database.Tables.PUBLISHERS, values, Contract.Publishers.PUBLISHER_ID + " = " + id +
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
