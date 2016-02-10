package xyz.kandrac.library.model;

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

/**
 * Content provider for all database items.
 * Created by VizGhar on 9.8.2015.
 */
public class DatabaseProvider extends ContentProvider {

    private Database databaseHelper;

    private static final UriMatcher uriMatcher = buildUriMatcher();

    // Everything from books (SELECT, INSERT, UPDATE, DELETE)
    public static final int BOOKS = 100;
    // Book by ID (SELECT, UPDATE, DELETE)
    public static final int BOOK_ID = 101;
    // Books by author id (SELECT)
    public static final int BOOK_BY_AUTHOR = 102;
    public static final int BOOK_BY_PUBLISHER = 103;
    public static final int BOOKS_BY_LIBRARY = 104;

    // Everything from authors (SELECT, INSERT, UPDATE, DELETE)
    public static final int AUTHORS = 200;
    // Author by id (SELECT, UPDATE, DELETE)
    public static final int AUTHOR_ID = 201;
    // Authors of book (SELECT)
    public static final int AUTHOR_BY_BOOK = 202;

    public static final int PUBLISHERS = 300;
    public static final int PUBLISHER_ID = 301;
    public static final int PUBLISHER_BY_BOOK = 302;

    public static final int BOOKS_AUTHORS = 400;

    public static final int BORROW_INFO = 500;
    public static final int BORROW_INFO_ID = 503;
    public static final int BORROW_INFO_BY_BOOK = 501;

    public static final int BOOKS_BORROW = 502;

    public static final int LIBRARIES = 600;
    public static final int LIBRARY_ID = 601;
    public static final int LIBRARY_BY_BOOK = 602;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = Contract.CONTENT_AUTHORITY;

        uriMatcher.addURI(authority, "books", BOOKS);
        uriMatcher.addURI(authority, "books/#", BOOK_ID);
        uriMatcher.addURI(authority, "books/#/authors", AUTHOR_BY_BOOK);
        uriMatcher.addURI(authority, "books/#/borrowinfo", BORROW_INFO_BY_BOOK);
        uriMatcher.addURI(authority, "books/#/publishers", PUBLISHER_BY_BOOK);
        uriMatcher.addURI(authority, "books/#/libraries", LIBRARY_BY_BOOK);

        uriMatcher.addURI(authority, "authors", AUTHORS);
        uriMatcher.addURI(authority, "authors/#", AUTHOR_ID);
        uriMatcher.addURI(authority, "authors/#/books", BOOK_BY_AUTHOR);
        uriMatcher.addURI(authority, "publishers", PUBLISHERS);
        uriMatcher.addURI(authority, "publishers/#", PUBLISHER_ID);
        uriMatcher.addURI(authority, "publishers/#/books", BOOK_BY_PUBLISHER);

        uriMatcher.addURI(authority, "books/authors", BOOKS_AUTHORS);
        uriMatcher.addURI(authority, "borrowinfo", BORROW_INFO);
        uriMatcher.addURI(authority, "borrowinfo/#", BORROW_INFO_ID);

        uriMatcher.addURI(authority, "books/borrowinfo", BOOKS_BORROW);

        uriMatcher.addURI(authority, "libraries", LIBRARIES);
        uriMatcher.addURI(authority, "libraries/#", LIBRARY_ID);
        uriMatcher.addURI(authority, "libraries/#/books", BOOKS_BY_LIBRARY);
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
            case PUBLISHER_BY_BOOK:
                return Contract.Publishers.CONTENT_ITEM_TYPE;
            case PUBLISHER_ID:
                return Contract.Publishers.CONTENT_ITEM_TYPE;
            case BOOKS_AUTHORS:
                return Contract.Books.CONTENT_TYPE;
            case BOOK_BY_PUBLISHER:
                return Contract.Books.CONTENT_TYPE;
            case BORROW_INFO:
                return Contract.BorrowInfo.CONTENT_TYPE;
            case BORROW_INFO_BY_BOOK:
                return Contract.BorrowInfo.CONTENT_TYPE;
            case BOOKS_BORROW:
                return Contract.Books.CONTENT_TYPE;
            case LIBRARIES:
                return Contract.Libraries.CONTENT_TYPE;
            case LIBRARY_ID:
                return Contract.Libraries.CONTENT_ITEM_TYPE;
            case BOOKS_BY_LIBRARY:
                return Contract.Books.CONTENT_TYPE;
            case LIBRARY_BY_BOOK:
                return Contract.Libraries.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        String group = null;

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
            case PUBLISHER_BY_BOOK:
                qb.setTables(Database.Tables.BOOKS_JOIN_PUBLISHERS);
                qb.appendWhere(Database.Tables.BOOKS + "." + Contract.Books.BOOK_ID + "=" + Contract.Books.getBookId(uri));
                sortOrder = sortOrder == null ? Contract.Publishers.DEFAULT_SORT : sortOrder;
                break;
            case PUBLISHER_ID:
                qb.setTables(Database.Tables.PUBLISHERS);
                qb.appendWhere(Contract.Publishers.PUBLISHER_ID + "=" + Contract.Publishers.getPublisherId(uri));
                break;
            case BOOKS_AUTHORS:
                qb.setTables(Database.Tables.BOOKS_JOIN_AUTHORS);
                qb.setDistinct(true);
                group = Database.Tables.BOOKS + "." + Contract.Books.BOOK_ID;
                sortOrder = sortOrder == null ? Contract.Books.DEFAULT_SORT : sortOrder;
                break;
            case BOOK_BY_PUBLISHER:
                qb.setTables(Database.Tables.BOOKS_JOIN_PUBLISHERS);
                qb.appendWhere(Database.Tables.PUBLISHERS + "." + Contract.Publishers.PUBLISHER_ID + "=" + Contract.Publishers.getPublisherId(uri));
                break;
            case BORROW_INFO:
                qb.setTables(Database.Tables.BORROW_INFO);
                sortOrder = sortOrder == null ? Contract.BorrowInfo.DEFAULT_SORT : sortOrder;
                break;
            case BORROW_INFO_BY_BOOK:
                qb.setTables(Database.Tables.BORROW_INFO);
                qb.appendWhere(Contract.BorrowInfo.BORROW_BOOK_ID + "=" + Contract.BorrowInfo.getBookId(uri));
                sortOrder = sortOrder == null ? Contract.BorrowInfo.DEFAULT_SORT : sortOrder;
                break;
            case BOOKS_BORROW:
                qb.setTables(Database.Tables.BOOKS_JOIN_BORROW);
                qb.setDistinct(true);
                sortOrder = sortOrder == null ? Contract.Books.DEFAULT_SORT : sortOrder;
                break;
            case LIBRARIES:
                qb.setTables(Database.Tables.LIBRARIES);
                sortOrder = sortOrder == null ? Contract.Libraries.DEFAULT_SORT : sortOrder;
                break;
            case LIBRARY_ID:
                qb.setTables(Database.Tables.LIBRARIES);
                qb.appendWhere(Contract.Libraries.LIBRARY_ID + "=" + Contract.Libraries.getLibraryId(uri));
                break;
            case BOOKS_BY_LIBRARY:
                qb.setTables(Database.Tables.BOOKS_JOIN_LIBRARIES);
                qb.appendWhere(Database.Tables.LIBRARIES + "." + Contract.Libraries.LIBRARY_ID + "=" + Contract.Libraries.getLibraryId(uri));
                break;
            case LIBRARY_BY_BOOK:
                qb.setTables(Database.Tables.BOOKS_JOIN_LIBRARIES);
                qb.appendWhere(Database.Tables.BOOKS + "." + Contract.Books.BOOK_ID + "=" + Contract.Books.getBookId(uri));
                sortOrder = sortOrder == null ? Contract.Libraries.DEFAULT_SORT : sortOrder;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        Cursor cursor = qb.query(db, projection, selection, selectionArgs, group, null, sortOrder);

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
            case AUTHOR_BY_BOOK: {
                long bookId = Contract.Books.getBookId(uri);

                // insert author
                long result = insertOrIgnore(db, values, Database.Tables.AUTHORS, Contract.Authors.AUTHOR_NAME);

                // insert author book connection
                ContentValues cv = Contract.BookAuthors.generateContentValues(bookId, result);

                db.insert(Database.Tables.BOOKS_AUTHORS, null, cv);

                getContext().getContentResolver().notifyChange(uri, null);
                return Contract.Authors.buildAuthorUri(result);
            }
            case BOOK_BY_AUTHOR: {
                long authorId = Contract.Authors.getAuthorId(uri);

                // insert author
                long result = insertOrIgnore(db, values, Database.Tables.AUTHORS, Contract.Authors.AUTHOR_NAME);

                // insert author book connection
                ContentValues cv = Contract.BookAuthors.generateContentValues(result, authorId);

                db.insert(Database.Tables.BOOKS_AUTHORS, null, cv);

                getContext().getContentResolver().notifyChange(uri, null);
                return Contract.Authors.buildAuthorUri(result);
            }
            case PUBLISHERS: {
                long result = insertOrIgnore(db, values, Database.Tables.PUBLISHERS, Contract.Publishers.PUBLISHER_NAME);
                getContext().getContentResolver().notifyChange(uri, null);
                return Contract.Publishers.buildPublisherUri(result);
            }
            case LIBRARIES: {
                long result = insertOrIgnore(db, values, Database.Tables.LIBRARIES, Contract.Libraries.LIBRARY_NAME);
                getContext().getContentResolver().notifyChange(uri, null);
                return Contract.Libraries.buildLibraryUri(result);
            }
            case BOOKS_AUTHORS: {
                db.insert(Database.Tables.BOOKS_AUTHORS, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return uri;
            }
            case BORROW_INFO_BY_BOOK: {
                long bookId = Contract.Books.getBookId(uri);
                values.put(Contract.BorrowInfo.BORROW_BOOK_ID, bookId);

                long result = db.insert(Database.Tables.BORROW_INFO, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return Contract.BorrowInfo.buildUri(result);
            }
            case BORROW_INFO: {
                long result = db.insert(Database.Tables.BORROW_INFO, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return Contract.BorrowInfo.buildUri(result);
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
        long id = db.insertWithOnConflict(table, null, values, SQLiteDatabase.CONFLICT_IGNORE);
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
        return db.compileStatement(selectStatement).simpleQueryForLong();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)) {
            case BOOKS: {
                count = db.delete(Database.Tables.BOOKS, selection, selectionArgs);
                break;
            }
            case BOOK_ID: {
                long id = Contract.Books.getBookId(uri);
                count = db.delete(Database.Tables.BOOKS, Contract.Books.BOOK_ID + " = " + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                getContext().getContentResolver().notifyChange(Contract.Books.CONTENT_URI, null);
                getContext().getContentResolver().notifyChange(Contract.BOOKS_AUTHORS_URI, null);
                break;
            }
            case AUTHORS: {
                count = db.delete(Database.Tables.AUTHORS, selection, selectionArgs);
                break;
            }
            case AUTHOR_ID: {
                long id = Contract.Authors.getAuthorId(uri);

                count += db.delete(
                        Database.Tables.BOOKS,
                        Contract.Books.BOOK_ID + " IN (SELECT "
                                + Contract.BookAuthors.BOOK_ID + " FROM " + Database.Tables.BOOKS_AUTHORS +
                                " WHERE " + Contract.BookAuthors.AUTHOR_ID + " = ?)",
                        new String[]{Long.toString(id)});

                getContext().getContentResolver().notifyChange(Contract.Books.CONTENT_URI, null);

                count += db.delete(Database.Tables.BOOKS_AUTHORS, Contract.BookAuthors.AUTHOR_ID + " = ?", new String[]{Long.toString(id)});
                count += db.delete(Database.Tables.AUTHORS, Contract.Authors.AUTHOR_ID + " = ?", new String[]{Long.toString(id)});

                break;
            }
            case PUBLISHERS: {
                count = db.delete(Database.Tables.PUBLISHERS, selection, selectionArgs);
                break;
            }
            case PUBLISHER_ID: {
                long id = Contract.Libraries.getLibraryId(uri);

                count += db.delete(
                        Database.Tables.BOOKS,
                        Contract.Books.BOOK_PUBLISHER_ID + "  = ?",
                        new String[]{Long.toString(id)});

                getContext().getContentResolver().notifyChange(Contract.Books.CONTENT_URI, null);

                count += db.delete(Database.Tables.PUBLISHERS, Contract.Publishers.PUBLISHER_ID + " = ?", new String[]{Long.toString(id)});
                break;
            }
            case LIBRARIES: {
                count = db.delete(Database.Tables.LIBRARIES, selection, selectionArgs);
                break;
            }
            case LIBRARY_ID: {
                long id = Contract.Libraries.getLibraryId(uri);

                count += db.delete(
                        Database.Tables.BOOKS,
                        Contract.Books.BOOK_LIBRARY_ID + "  = ?",
                        new String[]{Long.toString(id)});

                getContext().getContentResolver().notifyChange(Contract.Books.CONTENT_URI, null);

                count += db.delete(Database.Tables.LIBRARIES, Contract.Libraries.LIBRARY_ID + " = ?", new String[]{Long.toString(id)});

                break;
            }
            case BOOKS_AUTHORS: {
                count = db.delete(Database.Tables.BOOKS_AUTHORS, selection, selectionArgs);
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count;
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)) {
            case BOOKS: {
                count = db.update(Database.Tables.BOOKS, values, selection, selectionArgs);
                break;
            }
            case BOOK_ID: {
                long id = Contract.Books.getBookId(uri);
                count = db.update(Database.Tables.BOOKS, values, Contract.Books.BOOK_ID + " = ?", new String[]{Long.toString(id)});
                getContext().getContentResolver().notifyChange(Contract.Books.CONTENT_URI, null);
                getContext().getContentResolver().notifyChange(Contract.BOOKS_AUTHORS_URI, null);
                break;
            }
            case AUTHORS: {
                count = db.update(Database.Tables.AUTHORS, values, selection, selectionArgs);
                break;
            }
            case AUTHOR_ID: {
                long id = Contract.Authors.getAuthorId(uri);
                count = db.update(Database.Tables.AUTHORS, values, Contract.Authors.AUTHOR_ID + " = " + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            }
            case PUBLISHERS: {
                count = db.update(Database.Tables.PUBLISHERS, values, selection, selectionArgs);
                break;
            }
            case PUBLISHER_ID: {
                long id = Contract.Publishers.getPublisherId(uri);
                count = db.update(Database.Tables.PUBLISHERS, values, Contract.Publishers.PUBLISHER_ID + " = " + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            }
            case BORROW_INFO_ID: {
                long id = Contract.BorrowInfo.getBookId(uri);
                count = db.update(Database.Tables.BORROW_INFO, values, Contract.BorrowInfo.BORROW_ID + " = ? ", new String[]{Long.toString(id)});
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        int result = 0;
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)) {
            case BOOKS: {
                db.beginTransaction();
                for (ContentValues contentValues : values) {
                    db.insert(Database.Tables.BOOKS, null, contentValues);
                    result++;
                }
                db.setTransactionSuccessful();
                db.endTransaction();
                getContext().getContentResolver().notifyChange(uri, null);
                break;
            }
            case AUTHORS: {
                db.beginTransaction();
                for (ContentValues contentValues : values) {
                    db.insert(Database.Tables.AUTHORS, null, contentValues);
                    result++;
                }
                db.setTransactionSuccessful();
                db.endTransaction();
                getContext().getContentResolver().notifyChange(uri, null);
                break;
            }
            case PUBLISHERS: {
                db.beginTransaction();
                for (ContentValues contentValues : values) {
                    db.insert(Database.Tables.PUBLISHERS, null, contentValues);
                    result++;
                }
                db.setTransactionSuccessful();
                db.endTransaction();
                getContext().getContentResolver().notifyChange(uri, null);
                break;
            }
            case LIBRARIES: {
                db.beginTransaction();
                for (ContentValues contentValues : values) {
                    db.insert(Database.Tables.LIBRARIES, null, contentValues);
                    result++;
                }
                db.setTransactionSuccessful();
                db.endTransaction();
                getContext().getContentResolver().notifyChange(uri, null);
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        return result;
    }
}
