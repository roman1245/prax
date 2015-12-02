package kandrac.xyz.library.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLite database representation for this application
 * Created by VizGhar on 9.8.2015.
 */
public class Database extends SQLiteOpenHelper {


    public static final String DATABASE_NAME = "library.db";
    public static final int DATABASE_VERSION = 1;

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public interface Tables {
        String BOOKS = "books";
        String AUTHORS = "authors";
        String PUBLISHERS = "publishers";
        String BORROW_INFO = "borrow_info";

        // m-n connections
        String BOOKS_AUTHORS = "book_author";

        // everything from connected authors and books
        String BOOKS_JOIN_AUTHORS = "books JOIN book_author ON books._id = book_author.book_id JOIN authors ON authors._id = book_author.author_id";
        String BOOKS_JOIN_PUBLISHERS = "books JOIN publishers ON books.book_publisher_id = publishers._id";
        String BOOKS_JOIN_BORROW = "books JOIN borrow_info ON borrow_info.borrow_book_id = books._id";
    }

    interface References {
        String AUTHORS_ID = "REFERENCES " + Tables.AUTHORS + "(" + Contract.Authors.AUTHOR_ID + ")";
        String BOOKS_ID = "REFERENCES " + Tables.BOOKS + "(" + Contract.Books.BOOK_ID + ")";
        String PUBLISHERS_ID = "REFERENCES " + Tables.PUBLISHERS + "(" + Contract.Publishers.PUBLISHER_ID + ")";
    }

    public static final String BOOKS_CREATE_TABLE =
            "CREATE TABLE " + Tables.BOOKS + " (" +
                    Contract.Books.BOOK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Contract.Books.BOOK_TITLE + " TEXT NOT NULL," +
                    Contract.Books.BOOK_SUBTITLE + " TEXT," +
                    Contract.Books.BOOK_DESCRIPTION + " TEXT," +
                    Contract.Books.BOOK_ISBN + " TEXT," +
                    Contract.Books.BOOK_IMAGE_FILE + " TEXT," +
                    Contract.Books.BOOK_IMAGE_URL + " TEXT," +
                    Contract.Books.BOOK_AUTHORS_READ + " TEXT," +
                    Contract.Books.BOOK_PUBLISHER_ID + " INTEGER " + References.PUBLISHERS_ID + ")";

    public static final String AUTHORS_CREATE_TABLE =
            "CREATE TABLE " + Tables.AUTHORS + " (" +
                    Contract.Authors.AUTHOR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Contract.Authors.AUTHOR_NAME + " TEXT NOT NULL," +
                    "UNIQUE (" + Contract.Authors.AUTHOR_NAME + ") ON CONFLICT REPLACE)";

    public static final String BOOKS_AUTHORS_CREATE_TABLE =
            "CREATE TABLE " + Tables.BOOKS_AUTHORS + "(" +
                    Contract.BookAuthorsColumns.BOOK_ID + " INTEGER " + References.BOOKS_ID + " ON DELETE CASCADE, " +
                    Contract.BookAuthorsColumns.AUTHOR_ID + " INTEGER " + References.AUTHORS_ID + " ON DELETE CASCADE, " +
                    "PRIMARY KEY (" + Contract.BookAuthorsColumns.BOOK_ID + " , " + Contract.BookAuthorsColumns.AUTHOR_ID + "))";

    public static final String PUBLISHERS_CREATE_TABLE =
            "CREATE TABLE " + Tables.PUBLISHERS + " (" +
                    Contract.Publishers.PUBLISHER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Contract.Publishers.PUBLISHER_NAME + " TEXT NOT NULL," +
                    "UNIQUE (" + Contract.Publishers.PUBLISHER_NAME + ") ON CONFLICT REPLACE)";

    public static final String BORROW_INFO_CREATE_TABLE =
            "CREATE TABLE " + Tables.BORROW_INFO + " (" +
                    Contract.BorrowInfoColumns.BORROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Contract.BorrowInfoColumns.BORROW_BOOK_ID + " INTEGER " + References.BOOKS_ID + " ON DELETE CASCADE, " +
                    Contract.BorrowInfoColumns.BORROW_TO + " TEXT, " +
                    Contract.BorrowInfoColumns.BORROW_DATE_BORROWED + " INTEGER, " +
                    Contract.BorrowInfoColumns.BORROW_DATE_RETURNED + " INTEGER DEFAULT 0, " +
                    Contract.BorrowInfoColumns.BORROW_MAIL + " TEXT, " +
                    Contract.BorrowInfoColumns.BORROW_NAME + " TEXT, " +
                    Contract.BorrowInfoColumns.BORROW_PHONE + " TEXT)";

    public static final String BOOKS_DROP_TABLE = "DROP TABLE IF EXISTS " + Tables.BOOKS;
    public static final String AUTHORS_DROP_TABLE = "DROP TABLE IF EXISTS " + Tables.AUTHORS;
    public static final String PUBLISHERS_DROP_TABLE = "DROP TABLE IF EXISTS " + Tables.PUBLISHERS;
    public static final String BOOKS_AUTHORS_DROP_TABLE = "DROP TABLE IF EXISTS " + Tables.BOOKS_AUTHORS;
    public static final String BORROW_INFO_DROP_TABLE = "DROP TABLE IF EXISTS " + Tables.BORROW_INFO;

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
        super.onConfigure(db);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(AUTHORS_CREATE_TABLE);
        db.execSQL(PUBLISHERS_CREATE_TABLE);
        db.execSQL(BOOKS_CREATE_TABLE);
        db.execSQL(BOOKS_AUTHORS_CREATE_TABLE);
        db.execSQL(BORROW_INFO_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(BORROW_INFO_DROP_TABLE);
        db.execSQL(BOOKS_AUTHORS_DROP_TABLE);
        db.execSQL(BOOKS_DROP_TABLE);
        db.execSQL(PUBLISHERS_DROP_TABLE);
        db.execSQL(AUTHORS_DROP_TABLE);
        onCreate(db);
    }
}
