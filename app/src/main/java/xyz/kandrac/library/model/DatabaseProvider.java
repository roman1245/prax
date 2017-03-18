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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import javax.inject.Inject;

import xyz.kandrac.library.LibraryApplication;
import xyz.kandrac.library.model.firebase.References;
import xyz.kandrac.library.utils.LogUtils;
import xyz.kandrac.library.utils.MediaUtils;
import xyz.kandrac.library.utils.SharedPreferencesManager;

import static xyz.kandrac.library.model.firebase.FirebaseBook.KEY_AUTHORS;
import static xyz.kandrac.library.model.firebase.FirebaseBook.KEY_BORROW_ME_NAME;
import static xyz.kandrac.library.model.firebase.FirebaseBook.KEY_BORROW_ME_WHEN;
import static xyz.kandrac.library.model.firebase.FirebaseBook.KEY_LIBRARY;
import static xyz.kandrac.library.model.firebase.FirebaseBook.KEY_PUBLISHER;

/**
 * Content provider for all database items.
 * Created by VizGhar on 9.8.2015.
 */
public class DatabaseProvider extends ContentProvider {

    private static final String LOG_TAG = DatabaseProvider.class.getName();

    private Database databaseHelper;
    private FirebaseAuth mFirebaseAuth;

    private static final UriMatcher uriMatcher = buildUriMatcher();

    // Everything from books (SELECT, INSERT, UPDATE, DELETE)
    public static final int BOOKS = 100;
    // Book by ID (SELECT, UPDATE, DELETE)
    public static final int BOOK_ID = 101;
    // Books by author id (SELECT)
    public static final int BOOK_BY_AUTHOR = 102;
    public static final int BOOK_BY_PUBLISHER = 103;
    public static final int BOOKS_BY_LIBRARY = 104;
    public static final int BOOKS_BY_ISBN = 105;
    private static final int BOOKS_BY_REFERENCE = 106;

    // Everything from authors (SELECT, INSERT, UPDATE, DELETE)
    public static final int AUTHORS = 200;
    // Author by id (SELECT, UPDATE, DELETE)
    public static final int AUTHOR_ID = 201;
    // Authors of book (SELECT)
    public static final int AUTHOR_BY_BOOK = 202;

    public static final int PUBLISHERS = 300;
    public static final int PUBLISHER_ID = 301;
    public static final int PUBLISHER_BY_BOOK = 302;
    public static final int PUBLISHER_BY_BOOK_REFERENCE = 303;
    public static final int LIBRARY_BY_BOOK_REFERENCE = 304;
    public static final int AUTHORS_BY_BOOK_REFERENCE = 305;
    public static final int BORROW_ME_INFO_BY_BOOK_REFERENCE = 306;

    public static final int BOOKS_AUTHORS = 400;

    public static final int BORROW_INFO = 500;
    public static final int BORROW_INFO_BY_BOOK = 501;
    public static final int BOOKS_BORROW = 502;
    public static final int BORROW_INFO_ID = 503;

    public static final int BORROW_ME_INFO_BY_BOOK = 504;
    public static final int BORROW_ME_INFO_ID = 505;
    public static final int BORROW_ME_INFO = 506;

    public static final int LIBRARIES = 600;
    public static final int LIBRARY_ID = 601;
    public static final int LIBRARY_BY_BOOK = 602;

    public static final int FEEDBACK = 700;
    public static final int FEEDBACK_REFERENCE = 701;

    public static final int GENRES = 800;
    public static final int GENRES_USED = 801;
    public static final int GENRE_ID = 802;
    public static final int GENRE_BY_BOOK = 803;

    public static final int SPECIAL_TABLE = 900;

    @Inject
    public SharedPreferencesManager manager;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = Contract.CONTENT_AUTHORITY;

        uriMatcher.addURI(authority, "books", BOOKS);
        uriMatcher.addURI(authority, "books/#", BOOK_ID);
        uriMatcher.addURI(authority, "books/isbn/*", BOOKS_BY_ISBN);
        uriMatcher.addURI(authority, "books/ref/*", BOOKS_BY_REFERENCE);
        uriMatcher.addURI(authority, "books/ref/*/publishers", PUBLISHER_BY_BOOK_REFERENCE);
        uriMatcher.addURI(authority, "books/ref/*/libraries", LIBRARY_BY_BOOK_REFERENCE);
        uriMatcher.addURI(authority, "books/ref/*/authors", AUTHORS_BY_BOOK_REFERENCE);
        uriMatcher.addURI(authority, "books/ref/*/borrow_me_info", BORROW_ME_INFO_BY_BOOK_REFERENCE);
        uriMatcher.addURI(authority, "books/#/authors", AUTHOR_BY_BOOK);
        uriMatcher.addURI(authority, "books/#/borrow_info", BORROW_INFO_BY_BOOK);
        uriMatcher.addURI(authority, "books/#/borrow_me_info", BORROW_ME_INFO_BY_BOOK);
        uriMatcher.addURI(authority, "books/#/publishers", PUBLISHER_BY_BOOK);
        uriMatcher.addURI(authority, "books/#/libraries", LIBRARY_BY_BOOK);
        uriMatcher.addURI(authority, "books/#/genres", GENRE_BY_BOOK);

        uriMatcher.addURI(authority, "authors", AUTHORS);
        uriMatcher.addURI(authority, "authors/#", AUTHOR_ID);
        uriMatcher.addURI(authority, "authors/#/books", BOOK_BY_AUTHOR);

        uriMatcher.addURI(authority, "publishers", PUBLISHERS);
        uriMatcher.addURI(authority, "publishers/#", PUBLISHER_ID);
        uriMatcher.addURI(authority, "publishers/#/books", BOOK_BY_PUBLISHER);

        uriMatcher.addURI(authority, "books/authors", BOOKS_AUTHORS);
        uriMatcher.addURI(authority, "borrow_info", BORROW_INFO);
        uriMatcher.addURI(authority, "borrow_info/#", BORROW_INFO_ID);

        uriMatcher.addURI(authority, "books/borrow_info", BOOKS_BORROW);
        uriMatcher.addURI(authority, "borrow_me_info/#", BORROW_ME_INFO_ID);
        uriMatcher.addURI(authority, "borrow_me_info", BORROW_ME_INFO);

        uriMatcher.addURI(authority, "libraries", LIBRARIES);
        uriMatcher.addURI(authority, "libraries/#", LIBRARY_ID);
        uriMatcher.addURI(authority, "libraries/#/books", BOOKS_BY_LIBRARY);

        uriMatcher.addURI(authority, "feedback", FEEDBACK);
        uriMatcher.addURI(authority, "feedback/*", FEEDBACK_REFERENCE);

        uriMatcher.addURI(authority, "genres", GENRES);
        uriMatcher.addURI(authority, "genres/used", GENRES_USED);
        uriMatcher.addURI(authority, "genres/*", GENRE_ID);

        uriMatcher.addURI(authority, "special/table", SPECIAL_TABLE);
        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        final Context context = getContext();
        LibraryApplication.getAppComponent(context).inject(this);
        databaseHelper = new Database(context);
        mFirebaseAuth = FirebaseAuth.getInstance();
        return true;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case BOOKS:
                return Contract.Books.CONTENT_TYPE;
            case BOOK_ID:
                return Contract.Books.CONTENT_ITEM_TYPE;
            case BOOKS_BY_ISBN:
                return Contract.Books.CONTENT_TYPE;
            case BOOKS_BY_REFERENCE:
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
            case FEEDBACK:
                return Contract.Feedback.CONTENT_TYPE;
            case FEEDBACK_REFERENCE:
                return Contract.Feedback.CONTENT_ITEM_TYPE;
            case GENRES:
                return Contract.Genres.CONTENT_TYPE;
            case GENRE_ID:
                return Contract.Genres.CONTENT_ITEM_TYPE;
            case SPECIAL_TABLE:
                return Contract.Special.CONTENT_TYPE;
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
                selection = Contract.Books.BOOK_ID + "=?";
                selectionArgs = new String[]{Long.toString(Contract.Books.getBookId(uri))};
                break;
            case BOOKS_BY_ISBN:
                qb.setTables(Database.Tables.BOOKS);
                selection = Contract.Books.BOOK_ISBN + "=?";
                selectionArgs = new String[]{Contract.Books.getBookIsbn(uri)};
                break;
            case BOOKS_BY_REFERENCE:
                qb.setTables(Database.Tables.BOOKS);
                selection = Contract.Books.BOOK_REFERENCE + "=?";
                selectionArgs = new String[]{Contract.Books.getBookReference(uri)};
                break;
            case BOOK_BY_AUTHOR:
                qb.setTables(Database.Tables.BOOKS_JOIN_AUTHORS);
                selection = Database.Tables.AUTHORS + "." + Contract.Authors.AUTHOR_ID + "=?";
                selectionArgs = new String[]{Long.toString(Contract.Authors.getAuthorId(uri))};
                break;
            case AUTHORS:
                qb.setTables(Database.Tables.AUTHORS);
                sortOrder = sortOrder == null ? Contract.Authors.DEFAULT_SORT : sortOrder;
                break;
            case AUTHOR_ID:
                qb.setTables(Database.Tables.AUTHORS);
                selection = Contract.Authors.AUTHOR_ID + "=?";
                selectionArgs = new String[]{Long.toString(Contract.Authors.getAuthorId(uri))};
                break;
            case AUTHOR_BY_BOOK:
                qb.setTables(Database.Tables.BOOKS_JOIN_AUTHORS);
                selection = Database.Tables.BOOKS + "." + Contract.Books.BOOK_ID + "=?";
                selectionArgs = new String[]{Long.toString(Contract.Books.getBookId(uri))};
                break;
            case PUBLISHERS:
                qb.setTables(Database.Tables.PUBLISHERS);
                sortOrder = sortOrder == null ? Contract.Publishers.DEFAULT_SORT : sortOrder;
                break;
            case PUBLISHER_BY_BOOK:
                qb.setTables(Database.Tables.BOOKS_JOIN_PUBLISHERS);
                selection = Database.Tables.BOOKS + "." + Contract.Books.BOOK_ID + "=?";
                selectionArgs = new String[]{Long.toString(Contract.Books.getBookId(uri))};
                sortOrder = sortOrder == null ? Contract.Publishers.DEFAULT_SORT : sortOrder;
                break;
            case PUBLISHER_ID:
                qb.setTables(Database.Tables.PUBLISHERS);
                selection = Contract.Publishers.PUBLISHER_ID + "=?";
                selectionArgs = new String[]{Long.toString(Contract.Publishers.getPublisherId(uri))};
                break;
            case BOOKS_AUTHORS:
                qb.setTables(Database.Tables.BOOKS_JOIN_AUTHORS);
                qb.setDistinct(true);
                group = Database.Tables.BOOKS + "." + Contract.Books.BOOK_ID;
                sortOrder = sortOrder == null ? Contract.Books.DEFAULT_SORT : sortOrder;
                break;
            case BOOK_BY_PUBLISHER:
                qb.setTables(Database.Tables.BOOKS_JOIN_PUBLISHERS);
                selection = Database.Tables.PUBLISHERS + "." + Contract.Publishers.PUBLISHER_ID + "=?";
                selectionArgs = new String[]{Long.toString(Contract.Publishers.getPublisherId(uri))};
                break;
            case BORROW_INFO:
                qb.setTables(Database.Tables.BORROW_INFO);
                sortOrder = sortOrder == null ? Contract.BorrowInfo.DEFAULT_SORT : sortOrder;
                break;
            case BORROW_INFO_BY_BOOK:
                qb.setTables(Database.Tables.BORROW_INFO);
                selection = Contract.BorrowInfo.BORROW_BOOK_ID + "=?";
                selectionArgs = new String[]{Long.toString(Contract.Books.getBookId(uri))};
                sortOrder = sortOrder == null ? Contract.BorrowInfo.DEFAULT_SORT : sortOrder;
                break;
            case BORROW_ME_INFO_BY_BOOK:
                qb.setTables(Database.Tables.BORROW_ME);
                selection = Contract.BorrowMeInfo.BORROW_BOOK_ID + "=?";
                selectionArgs = new String[]{Long.toString(Contract.BorrowMeInfo.getBookId(uri))};
                sortOrder = sortOrder == null ? Contract.BorrowMeInfo.DEFAULT_SORT : sortOrder;
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
                selection = Contract.Libraries.LIBRARY_ID + "=?";
                selectionArgs = new String[]{Long.toString(Contract.Libraries.getLibraryId(uri))};
                break;
            case BOOKS_BY_LIBRARY:
                qb.setTables(Database.Tables.BOOKS_JOIN_LIBRARIES);
                selection = Database.Tables.LIBRARIES + "." + Contract.Libraries.LIBRARY_ID + "=?";
                selectionArgs = new String[]{Long.toString(Contract.Libraries.getLibraryId(uri))};
                break;
            case LIBRARY_BY_BOOK:
                qb.setTables(Database.Tables.BOOKS_JOIN_LIBRARIES);
                selection = Database.Tables.BOOKS + "." + Contract.Books.BOOK_ID + "=?";
                selectionArgs = new String[]{Long.toString(Contract.Books.getBookId(uri))};
                sortOrder = sortOrder == null ? Contract.Libraries.DEFAULT_SORT : sortOrder;
                break;
            case FEEDBACK_REFERENCE:
                qb.setTables(Database.Tables.FEEDBACK);
                selection = Contract.FeedbackColumns.FEEDBACK_REFERENCE + "=?";
                selectionArgs = new String[]{Contract.Feedback.getReference(uri)};
                break;
            case GENRES:
                qb.setTables(Database.Tables.GENRES);
                sortOrder = sortOrder == null ? Contract.Genres.GENRE_NAME : sortOrder;
                break;
            case GENRES_USED:
                qb.setTables(Database.Tables.BOOKS_JOIN_GENRES);
                projection = new String[]{Database.Tables.GENRES + "." + Contract.Genres.GENRE_ID, Contract.Genres.GENRE_NAME};
                sortOrder = sortOrder == null ? Contract.Genres.GENRE_NAME : sortOrder;
                group = Database.Tables.GENRES + "." + Contract.Genres.GENRE_ID;
                qb.setDistinct(true);
                break;
            case GENRE_ID:
                qb.setTables(Database.Tables.GENRES);
                selection = Contract.GenresColumns.GENRE_ID + "=?";
                selectionArgs = new String[]{Long.toString(Contract.Genres.getId(uri))};
                break;
            case GENRE_BY_BOOK:
                qb.setTables(Database.Tables.BOOKS_JOIN_GENRES);
                selection = Database.Tables.BOOKS + "." + Contract.Books.BOOK_ID + "=?";
                selectionArgs = new String[]{Long.toString(Contract.Genres.getBookId(uri))};
                break;
            case SPECIAL_TABLE:
                projection = new String[]{
                        Contract.Books.FULL_BOOK_ID,
                        Contract.Books.BOOK_REFERENCE,
                        Contract.Books.BOOK_TITLE,
                        Contract.Books.BOOK_ISBN,
                        Contract.Books.BOOK_DESCRIPTION,
                        Contract.Books.BOOK_WISH_LIST,
                        Contract.Books.BOOK_PUBLISHED,
                        Contract.Books.BOOK_SUBTITLE,
                        Contract.Books.BOOK_UPDATED_AT,
                        Contract.BorrowInfo.BORROW_DATE_BORROWED,
                        Contract.BorrowInfo.BORROW_NAME,
                        Contract.BorrowInfo.BORROW_NEXT_NOTIFICATION,
                        Contract.BorrowMeInfo.BORROW_DATE_BORROWED,
                        Contract.BorrowMeInfo.BORROW_NAME,
                        Contract.Libraries.LIBRARY_NAME,
                        "group_concat(" + Contract.Authors.AUTHOR_NAME + ", \",\") AS " + Contract.Authors.AUTHOR_NAME,
                        Contract.Publishers.PUBLISHER_NAME};

                group = Contract.Books.FULL_BOOK_ID;
                qb.setTables(Database.Tables.BOOKS_JOIN_AUTHORS + " " + Database.Tables.JOIN_PUBLISHERS + " " + Database.Tables.JOIN_LIBRARIES + " " + Database.Tables.JOIN_BORROW + " " + Database.Tables.JOIN_BORROWED_TO_ME);
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

                if (mFirebaseAuth.getCurrentUser() != null && manager.getBooleanPreference(SharedPreferencesManager.KEY_PREF_DRIVER_BOUGHT)) {
                    String uid = mFirebaseAuth.getCurrentUser().getUid();

                    String reference = FirebaseDatabase.getInstance().getReference()
                            .child(References.USERS_REFERENCE).child(uid)
                            .child(References.BOOKS_REFERENCE).push().getKey();

                    for (String key : values.keySet()) {
                        FirebaseDatabase.getInstance().getReference()
                                .child(References.USERS_REFERENCE).child(uid)
                                .child(References.BOOKS_REFERENCE).child(reference)
                                .child(key).setValue(values.get(key));
                    }

                    values.put(Contract.BooksColumns.BOOK_REFERENCE, reference);

                    db.insert(Database.Tables.BOOKS, null, values);
                    return Contract.Books.buildBookFirebaseUri(reference);
                } else {
                    long result = db.insert(Database.Tables.BOOKS, null, values);
                    return Contract.Books.buildBookUri(result);
                }
            }
            case AUTHORS: {
                long result = insertOrIgnore(db, values, Database.Tables.AUTHORS, Contract.Authors.AUTHOR_NAME);
                getContext().getContentResolver().notifyChange(uri, null);
                return Contract.Authors.buildAuthorUri(result);
            }
            case AUTHOR_BY_BOOK: {
                long bookId = Contract.Books.getBookId(uri);
                ContentValues foo = new ContentValues();

                for (int i = 0; i < values.size(); i++) {
                    foo.put(Contract.Authors.AUTHOR_NAME, values.getAsString(Integer.toString(i)));
                    long result = insertOrIgnore(db, foo, Database.Tables.AUTHORS, Contract.Authors.AUTHOR_NAME);
                    ContentValues cv = Contract.BookAuthors.generateContentValues(bookId, result);
                    db.insert(Database.Tables.BOOKS_AUTHORS, null, cv);
                }

                getContext().getContentResolver().notifyChange(uri, null);
                return uri;
            }
            case AUTHORS_BY_BOOK_REFERENCE: {
                String reference = Contract.Books.getBookReference(uri);
                StringBuilder sb = new StringBuilder();

                for (int i = 0; i < values.size(); i++) {
                    String authorName = values.getAsString(Integer.toString(i));
                    ContentValues foo = new ContentValues();
                    foo.put(Contract.Authors.AUTHOR_NAME, authorName);
                    long authorId = insertOrIgnore(db, foo, Database.Tables.AUTHORS, Contract.Authors.AUTHOR_NAME);
                    ContentValues cv = Contract.BookAuthors.generateContentValues(getBookId(db, reference), authorId);
                    db.insert(Database.Tables.BOOKS_AUTHORS, null, cv);

                    if (i != 0) {
                        sb.append(",");
                    }
                    sb.append(authorName);
                }

                if (mFirebaseAuth.getCurrentUser() != null && manager.getBooleanPreference(SharedPreferencesManager.KEY_PREF_DRIVER_BOUGHT)) {
                    String uid = mFirebaseAuth.getCurrentUser().getUid();

                    FirebaseDatabase.getInstance().getReference()
                            .child(References.USERS_REFERENCE).child(uid)
                            .child(References.BOOKS_REFERENCE).child(reference)
                            .child(KEY_AUTHORS).setValue(sb.toString());
                }

                return uri;
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
            case PUBLISHER_BY_BOOK_REFERENCE: {
                String reference = Contract.Books.getBookReference(uri);

                if (mFirebaseAuth.getCurrentUser() != null && manager.getBooleanPreference(SharedPreferencesManager.KEY_PREF_DRIVER_BOUGHT)) {
                    String uid = mFirebaseAuth.getCurrentUser().getUid();

                    FirebaseDatabase.getInstance().getReference()
                            .child(References.USERS_REFERENCE).child(uid)
                            .child(References.BOOKS_REFERENCE).child(reference)
                            .child(KEY_PUBLISHER).setValue(values.get(Contract.Publishers.PUBLISHER_NAME));
                }
                long result = insertOrIgnore(db, values, Database.Tables.PUBLISHERS, Contract.Publishers.PUBLISHER_NAME);
                getContext().getContentResolver().notifyChange(uri, null);
                return Contract.Publishers.buildPublisherUri(result);
            }
            case PUBLISHER_BY_BOOK: {
                long bookId = Contract.Books.getBookId(uri);
                long result = insertOrIgnore(db, values, Database.Tables.PUBLISHERS, Contract.Publishers.PUBLISHER_NAME);
                ContentValues cv = new ContentValues();
                cv.put(Contract.Books.BOOK_PUBLISHER_ID, result);
                update(Contract.Books.buildBookUri(bookId), cv, null, null);
                getContext().getContentResolver().notifyChange(uri, null);
                return Contract.Publishers.buildPublisherUri(result);
            }
            case PUBLISHERS: {
                long result = insertOrIgnore(db, values, Database.Tables.PUBLISHERS, Contract.Publishers.PUBLISHER_NAME);
                getContext().getContentResolver().notifyChange(uri, null);
                return Contract.Publishers.buildPublisherUri(result);
            }
            case LIBRARY_BY_BOOK: {
                long bookId = Contract.Books.getBookId(uri);
                long result = insertOrIgnore(db, values, Database.Tables.LIBRARIES, Contract.Libraries.LIBRARY_NAME);
                ContentValues cv = new ContentValues();
                cv.put(Contract.Books.BOOK_LIBRARY_ID, result);
                update(Contract.Books.buildBookUri(bookId), cv, null, null);
                getContext().getContentResolver().notifyChange(uri, null);
                return Contract.Libraries.buildLibraryUri(result);
            }
            case LIBRARY_BY_BOOK_REFERENCE: {
                String reference = Contract.Books.getBookReference(uri);

                if (mFirebaseAuth.getCurrentUser() != null && manager.getBooleanPreference(SharedPreferencesManager.KEY_PREF_DRIVER_BOUGHT)) {
                    String uid = mFirebaseAuth.getCurrentUser().getUid();

                    FirebaseDatabase.getInstance().getReference()
                            .child(References.USERS_REFERENCE).child(uid)
                            .child(References.BOOKS_REFERENCE).child(reference)
                            .child(KEY_LIBRARY).setValue(values.get(Contract.Libraries.LIBRARY_NAME));
                }
                long result = insertOrIgnore(db, values, Database.Tables.LIBRARIES, Contract.Libraries.LIBRARY_NAME);
                getContext().getContentResolver().notifyChange(uri, null);
                return Contract.Libraries.buildLibraryUri(result);
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
            case BORROW_ME_INFO_BY_BOOK_REFERENCE: {
                String reference = Contract.Books.getBookReference(uri);

                if (mFirebaseAuth.getCurrentUser() != null && manager.getBooleanPreference(SharedPreferencesManager.KEY_PREF_DRIVER_BOUGHT)) {
                    String uid = mFirebaseAuth.getCurrentUser().getUid();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                            .child(References.USERS_REFERENCE).child(uid)
                            .child(References.BOOKS_REFERENCE).child(reference);

                    ref.child(KEY_BORROW_ME_NAME).setValue(values.get(Contract.BorrowMeInfoColumns.BORROW_NAME));
                    ref.child(KEY_BORROW_ME_WHEN).setValue(values.get(Contract.BorrowMeInfoColumns.BORROW_DATE_BORROWED));
                }

                values.put(Contract.BorrowMeInfo.BORROW_BOOK_ID, getBookId(db, reference));
                long result = db.insert(Database.Tables.BORROW_ME, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return Contract.BorrowMeInfo.buildUri(result);
            }
            case BORROW_ME_INFO_BY_BOOK: {
                long bookId = Contract.Books.getBookId(uri);
                values.put(Contract.BorrowMeInfo.BORROW_BOOK_ID, bookId);

                long result = db.insert(Database.Tables.BORROW_ME, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return Contract.BorrowMeInfo.buildUri(result);
            }
            case BORROW_INFO: {
                long result = db.insert(Database.Tables.BORROW_INFO, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return Contract.BorrowInfo.buildUri(result);
            }
            case BORROW_ME_INFO: {
                long result = db.insert(Database.Tables.BORROW_ME, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return Contract.BorrowMeInfo.buildUri(result);
            }
            case FEEDBACK: {
                db.insert(Database.Tables.FEEDBACK, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                getContext().getContentResolver().notifyChange(Contract.Feedback.CONTENT_URI, null);
                return Contract.Feedback.buildUri((String) values.get(Contract.Feedback.FEEDBACK_REFERENCE));
            }
            case GENRES: {
                long result = insertOrIgnore(db, values, Database.Tables.GENRES, Contract.GenresColumns.GENRE_NAME);
                getContext().getContentResolver().notifyChange(uri, null);
                return Contract.Genres.buildUri(result);
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
     * @param db           database to insert to
     * @param values       values to insert
     * @param table        table to insert to
     * @param uniqueColumn unique column name
     * @return _id column value
     */
    @SuppressWarnings("unused")
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
     * @param db           database to insert to
     * @param values       values to insert
     * @param table        table to insert to
     * @param uniqueColumn unique column name
     * @return _id column value
     */
    private long selectId(SQLiteDatabase db, ContentValues values, String table, String uniqueColumn) {
        String selectStatement = "SELECT " + BaseColumns._ID +
                " FROM " + table +
                " WHERE " + uniqueColumn + " = '" + values.getAsString(uniqueColumn).replace("'", "''") + "'";

        LogUtils.d(LOG_TAG, selectStatement);

        return db.compileStatement(selectStatement).simpleQueryForLong();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)) {
            case BOOK_ID: {
                long id = Contract.Books.getBookId(uri);

                Cursor bookCursor = db.query(Database.Tables.BOOKS, new String[]{Contract.Books.BOOK_REFERENCE, Contract.Books.BOOK_IMAGE_FILE}, Contract.Books.BOOK_ID + " = ?", new String[]{Long.toString(id)}, null, null, null);

                if (bookCursor.moveToFirst()) {
                    // clear cloud data first
                    if (mFirebaseAuth.getCurrentUser() != null) {

                        String firebaseId = bookCursor.getString(bookCursor.getColumnIndex(Contract.Books.BOOK_REFERENCE));

                        if (!TextUtils.isEmpty(firebaseId)) {
                            FirebaseDatabase.getInstance().getReference()
                                    .child(References.USERS_REFERENCE).child(mFirebaseAuth.getCurrentUser().getUid())
                                    .child(References.BOOKS_REFERENCE).child(firebaseId)
                                    .removeValue();
                        }
                    }

                    String imagePath = bookCursor.getString(bookCursor.getColumnIndex(Contract.Books.BOOK_IMAGE_FILE));
                    MediaUtils.delete(getContext(), imagePath);

                    bookCursor.close();
                }

                count = db.delete(Database.Tables.BOOKS, Contract.Books.BOOK_ID + " = " + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                count += db.delete(Database.Tables.BORROW_INFO, Contract.BorrowInfo.BORROW_BOOK_ID + " = ?", new String[]{Long.toString(id)});
                count += db.delete(Database.Tables.BORROW_ME, Contract.BorrowMeInfo.BORROW_BOOK_ID + " = ?", new String[]{Long.toString(id)});
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
            case AUTHOR_BY_BOOK: {
                long id = Contract.Books.getBookId(uri);
                count += db.delete(Database.Tables.BOOKS_AUTHORS, Contract.BookAuthorsColumns.BOOK_ID + " = ?", new String[]{Long.toString(id)});
                getContext().getContentResolver().notifyChange(Contract.Books.CONTENT_URI, null);
                getContext().getContentResolver().notifyChange(Contract.Books.buildBookUri(id), null);
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
            case BORROW_INFO_ID: {
                long id = Contract.BorrowInfo.getBorrowInfoId(uri);
                count = db.delete(Database.Tables.BORROW_INFO, Contract.BorrowInfo.BORROW_ID + " = ?", new String[]{Long.toString(id)});
                break;
            }
            case BORROW_ME_INFO_ID: {
                long id = Contract.Libraries.getLibraryId(uri);
                count = db.delete(Database.Tables.BORROW_ME, Contract.BorrowMeInfo.BORROW_ID + " = ?", new String[]{Long.toString(id)});
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
                values.put(Contract.Books.BOOK_UPDATED_AT, System.currentTimeMillis());
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
            case BORROW_INFO: {
                count = db.update(Database.Tables.BORROW_INFO, values, selection, selectionArgs);
                break;
            }
            case BORROW_INFO_BY_BOOK: {
                long id = Contract.Books.getBookId(uri);
                count = db.update(Database.Tables.BORROW_INFO, values, Contract.BorrowInfo.BORROW_BOOK_ID + " = ? ", new String[]{Long.toString(id)});
                break;
            }
            case BORROW_INFO_ID: {
                long id = Contract.BorrowInfo.getBorrowInfoId(uri);
                count = db.update(Database.Tables.BORROW_INFO, values, Contract.BorrowInfo.BORROW_ID + " = ? ", new String[]{Long.toString(id)});
                break;
            }
            case BORROW_ME_INFO: {
                count = db.update(Database.Tables.BORROW_ME, values, selection, selectionArgs);
                break;
            }
            case BORROW_ME_INFO_ID: {
                long id = Contract.BorrowMeInfo.getBookId(uri);
                count = db.update(Database.Tables.BORROW_ME, values, Contract.BorrowMeInfo.BORROW_ID + " = ? ", new String[]{Long.toString(id)});
                break;
            }
            case BOOKS_BY_REFERENCE: {
                String reference = Contract.Books.getBookReference(uri);
                count = db.update(Database.Tables.BOOKS, values, Contract.Books.BOOK_REFERENCE + " = ? ", new String[]{reference});

                if (mFirebaseAuth.getCurrentUser() != null) {
                    String uid = mFirebaseAuth.getCurrentUser().getUid();

                    for (String key : values.keySet()) {
                        FirebaseDatabase.getInstance().getReference()
                                .child(References.USERS_REFERENCE).child(uid)
                                .child(References.BOOKS_REFERENCE).child(reference)
                                .child(key).setValue(values.get(key));
                    }
                }
                getContext().getContentResolver().notifyChange(Contract.Books.CONTENT_URI, null);
                getContext().getContentResolver().notifyChange(Contract.BOOKS_AUTHORS_URI, null);
                break;
            }
            case FEEDBACK_REFERENCE: {
                String reference = Contract.Feedback.getReference(uri);
                count = db.update(Database.Tables.FEEDBACK, values, Contract.Feedback.FEEDBACK_REFERENCE + " = ? ", new String[]{reference});
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

    private long getBookId(SQLiteDatabase db, String reference) {
        Cursor c = db.query(Database.Tables.BOOKS, new String[]{Contract.BooksColumns.BOOK_ID}, Contract.BooksColumns.BOOK_REFERENCE + " = ?", new String[]{reference}, null, null, null);
        if (c != null && c.moveToFirst()) {
            long result = c.getLong(c.getColumnIndex(Contract.BooksColumns.BOOK_ID));
            c.close();
            return result;
        } else {
            return -1;
        }
    }
}
