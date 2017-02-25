package xyz.kandrac.library.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import xyz.kandrac.library.R;

/**
 * SQLite database representation for this application
 * Created by VizGhar on 9.8.2015.
 */
public class Database extends SQLiteOpenHelper {


    private static final String DATABASE_NAME = "library.db";
    private static final int DATABASE_VERSION = 11;

    private Context context;

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    public interface Tables {
        String BOOKS = "books";
        String AUTHORS = "authors";
        String PUBLISHERS = "publishers";
        String BORROW_INFO = "borrow_info";
        String BORROW_ME = "borrow_me";
        String LIBRARIES = "libraries";
        String FEEDBACK = "feedback";
        String GENRES = "genres";

        // m-n connections
        String BOOKS_AUTHORS = "book_author";

        // everything from connected authors and books
        String BOOKS_JOIN_AUTHORS = "books LEFT JOIN book_author ON books._id = book_author.book_id LEFT JOIN authors ON authors._id = book_author.author_id";
        String BOOKS_JOIN_PUBLISHERS = "books JOIN publishers ON books.book_publisher_id = publishers._id";
        String BOOKS_JOIN_GENRES = "books JOIN genres ON books.book_genre = genres._id";
        String BOOKS_JOIN_LIBRARIES = "books JOIN libraries ON books.book_library_id = libraries._id";
        String BOOKS_JOIN_BORROW = "books LEFT JOIN borrow_info ON borrow_info.borrow_book_id = books._id";

        String JOIN_PUBLISHERS = "JOIN publishers ON books.book_publisher_id = publishers._id";
        String JOIN_LIBRARIES = "JOIN libraries ON books.book_library_id = libraries._id";
        String JOIN_BORROW = "LEFT JOIN borrow_info ON borrow_info.borrow_book_id = books._id";
        String JOIN_BORROWED_TO_ME = "LEFT JOIN borrow_me ON borrow_me.borrow_me_book_id = books._id";

    }

    interface References {
        String AUTHORS_ID = "REFERENCES " + Tables.AUTHORS + "(" + Contract.Authors.AUTHOR_ID + ")";
        String BOOKS_ID = "REFERENCES " + Tables.BOOKS + "(" + Contract.Books.BOOK_ID + ")";
        String PUBLISHERS_ID = "REFERENCES " + Tables.PUBLISHERS + "(" + Contract.Publishers.PUBLISHER_ID + ")";
        String LIBRARY_ID = "REFERENCES " + Tables.LIBRARIES + "(" + Contract.Libraries.LIBRARY_ID + ")";
        String GENRE_ID = "REFERENCES " + Tables.GENRES + "(" + Contract.GenresColumns.GENRE_ID+ ")";
    }

    private static final String BOOKS_CREATE_TABLE =
            "CREATE TABLE " + Tables.BOOKS + " (" +
                    Contract.Books.BOOK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Contract.Books.BOOK_REFERENCE + " TEXT," +
                    Contract.Books.BOOK_TITLE + " TEXT NOT NULL," +
                    Contract.Books.BOOK_SUBTITLE + " TEXT," +
                    Contract.Books.BOOK_DESCRIPTION + " TEXT," +
                    Contract.Books.BOOK_ISBN + " TEXT," +
                    Contract.Books.BOOK_IMAGE_FILE + " TEXT," +
                    Contract.Books.BOOK_IMAGE_URL + " TEXT," +
                    Contract.Books.BOOK_BORROWED + " BOOLEAN DEFAULT 0," +
                    Contract.Books.BOOK_PUBLISHED + " INTEGER," +
                    Contract.Books.BOOK_BORROWED_TO_ME + " BOOLEAN DEFAULT 0," +
                    Contract.Books.BOOK_WISH_LIST + " BOOLEAN DEFAULT 0," +
                    Contract.Books.BOOK_UPDATED_AT + " INTEGER," +
                    Contract.Books.BOOK_PROGRESS + " INTEGER," +
                    Contract.Books.BOOK_MY_SCORE + " INTEGER," +
                    Contract.Books.BOOK_QUOTE + " TEXT," +
                    Contract.Books.BOOK_NOTES + " TEXT," +
                    Contract.Books.BOOK_GENRE_ID + " INTEGER " + References.GENRE_ID + " ON DELETE CASCADE, "+
                    Contract.Books.BOOK_LIBRARY_ID + " INTEGER " + References.LIBRARY_ID + " ON DELETE CASCADE, " +
                    Contract.Books.BOOK_PUBLISHER_ID + " INTEGER " + References.PUBLISHERS_ID + " ON DELETE CASCADE)";

    private static final String AUTHORS_CREATE_TABLE =
            "CREATE TABLE " + Tables.AUTHORS + " (" +
                    Contract.Authors.AUTHOR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Contract.Authors.AUTHOR_NAME + " TEXT NOT NULL," +
                    "UNIQUE (" + Contract.Authors.AUTHOR_NAME + ") ON CONFLICT REPLACE)";

    private static final String BOOKS_AUTHORS_CREATE_TABLE =
            "CREATE TABLE " + Tables.BOOKS_AUTHORS + "(" +
                    Contract.BookAuthorsColumns.BOOK_ID + " INTEGER " + References.BOOKS_ID + " ON DELETE CASCADE, " +
                    Contract.BookAuthorsColumns.AUTHOR_ID + " INTEGER " + References.AUTHORS_ID + " ON DELETE CASCADE, " +
                    "PRIMARY KEY (" + Contract.BookAuthorsColumns.BOOK_ID + " , " + Contract.BookAuthorsColumns.AUTHOR_ID + "))";

    private static final String PUBLISHERS_CREATE_TABLE =
            "CREATE TABLE " + Tables.PUBLISHERS + " (" +
                    Contract.Publishers.PUBLISHER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Contract.Publishers.PUBLISHER_NAME + " TEXT NOT NULL," +
                    "UNIQUE (" + Contract.Publishers.PUBLISHER_NAME + ") ON CONFLICT REPLACE)";

    private static final String LIBRARIES_CREATE_TABLE =
            "CREATE TABLE " + Tables.LIBRARIES + " (" +
                    Contract.Libraries.LIBRARY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Contract.Libraries.LIBRARY_NAME + " TEXT NOT NULL," +
                    "UNIQUE (" + Contract.Libraries.LIBRARY_NAME + ") ON CONFLICT REPLACE)";

    private static final String BORROW_INFO_CREATE_TABLE =
            "CREATE TABLE " + Tables.BORROW_INFO + " (" +
                    Contract.BorrowInfoColumns.BORROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Contract.BorrowInfoColumns.BORROW_BOOK_ID + " INTEGER " + References.BOOKS_ID + " ON DELETE CASCADE, " +
                    Contract.BorrowInfoColumns.BORROW_TO + " TEXT, " +
                    Contract.BorrowInfoColumns.BORROW_DATE_BORROWED + " INTEGER, " +
                    Contract.BorrowInfoColumns.BORROW_NEXT_NOTIFICATION + " INTEGER, " +
                    Contract.BorrowInfoColumns.BORROW_DATE_RETURNED + " INTEGER DEFAULT 0, " +
                    Contract.BorrowInfoColumns.BORROW_MAIL + " TEXT, " +
                    Contract.BorrowInfoColumns.BORROW_NAME + " TEXT, " +
                    Contract.BorrowInfoColumns.BORROW_PHONE + " TEXT)";

    private static final String FEEDBACK_CREATE_TABLE =
            "CREATE TABLE " + Tables.FEEDBACK + " (" +
                    Contract.FeedbackColumns.FEEDBACK_REFERENCE + " TEXT PRIMARY KEY," +
                    Contract.FeedbackColumns.FEEDBACK_VALUE + " INTEGER)";

    private static final String BORROW_ME_CREATE_TABLE =
            "CREATE TABLE " + Tables.BORROW_ME + " (" +
                    Contract.BorrowMeInfoColumns.BORROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Contract.BorrowMeInfoColumns.BORROW_BOOK_ID + " INTEGER " + References.BOOKS_ID + " ON DELETE CASCADE, " +
                    Contract.BorrowMeInfoColumns.BORROW_DATE_BORROWED + " INTEGER, " +
                    Contract.BorrowMeInfoColumns.BORROW_NAME + " TEXT)";

    private static final String GENRES_CREATE_TABLE =
            "CREATE TABLE " + Tables.GENRES + " (" +
                    Contract.GenresColumns.GENRE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    Contract.GenresColumns.GENRE_NAME + " TEXT NOT NULL," +
                    "UNIQUE (" + Contract.GenresColumns.GENRE_NAME + ") ON CONFLICT REPLACE)";

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
        super.onConfigure(db);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(AUTHORS_CREATE_TABLE);
        db.execSQL(PUBLISHERS_CREATE_TABLE);
        db.execSQL(LIBRARIES_CREATE_TABLE);
        db.execSQL(BOOKS_CREATE_TABLE);
        db.execSQL(BOOKS_AUTHORS_CREATE_TABLE);
        db.execSQL(BORROW_INFO_CREATE_TABLE);
        db.execSQL(BORROW_ME_CREATE_TABLE);
        db.execSQL(FEEDBACK_CREATE_TABLE);
        db.execSQL(GENRES_CREATE_TABLE);
        genresInit(context, db);

        // clear context after create
        context = null;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 1:
                db.execSQL(LIBRARIES_CREATE_TABLE);
                db.execSQL("INSERT INTO " + Tables.LIBRARIES + " (" + Contract.Libraries.LIBRARY_NAME + ") VALUES ('')");
                db.execSQL("ALTER TABLE " + Tables.BOOKS + " ADD " + Contract.Books.BOOK_LIBRARY_ID + " INTEGER " + References.LIBRARY_ID + " ON DELETE CASCADE");
                db.execSQL("UPDATE " + Tables.BOOKS + " SET " + Contract.Books.BOOK_LIBRARY_ID + " = 1");
            case 2:
                db.execSQL("ALTER TABLE " + Tables.BORROW_INFO + " ADD " + Contract.BorrowInfo.BORROW_NEXT_NOTIFICATION + " INTEGER");
            case 3:
                db.execSQL(BORROW_ME_CREATE_TABLE);
                db.execSQL("ALTER TABLE " + Tables.BOOKS + " ADD " + Contract.Books.BOOK_BORROWED_TO_ME + " BOOLEAN DEFAULT 0");
            case 4:
                db.execSQL("ALTER TABLE " + Tables.BOOKS + " ADD " + Contract.Books.BOOK_PUBLISHED + " INTEGER");
            case 5:
                db.execSQL("ALTER TABLE " + Tables.BOOKS + " ADD " + Contract.Books.BOOK_REFERENCE + " TEXT");
            case 6:
                db.execSQL("ALTER TABLE " + Tables.BOOKS + " ADD " + Contract.Books.BOOK_UPDATED_AT + " INTEGER");
                db.execSQL("UPDATE " + Tables.BOOKS + " SET " + Contract.Books.BOOK_UPDATED_AT + " = " + System.currentTimeMillis());
            case 7:
                db.execSQL("DELETE FROM " + Tables.BORROW_INFO + " WHERE " + Contract.BorrowInfo.BORROW_DATE_RETURNED + " != 0");
            case 8:
                db.execSQL("ALTER TABLE " + Tables.BOOKS + " ADD " + Contract.Books.BOOK_PROGRESS + " INTEGER DEFAULT 0");
                db.execSQL("ALTER TABLE " + Tables.BOOKS + " ADD " + Contract.Books.BOOK_MY_SCORE + " INTEGER DEFAULT 0");
                db.execSQL("ALTER TABLE " + Tables.BOOKS + " ADD " + Contract.Books.BOOK_QUOTE + " TEXT");
                db.execSQL("ALTER TABLE " + Tables.BOOKS + " ADD " + Contract.Books.BOOK_NOTES + " TEXT");
            case 9:
                db.execSQL(FEEDBACK_CREATE_TABLE);
            case 10:
                db.execSQL(GENRES_CREATE_TABLE);
                genresInit(context, db);
                db.execSQL("ALTER TABLE " + Tables.BOOKS + " ADD " + Contract.Books.BOOK_GENRE_ID + " INTEGER DEFAULT -1 "+ References.GENRE_ID + " ON DELETE CASCADE");
        }

        // clear context after upgrade
        context = null;
    }

    private void genresInit(Context context, SQLiteDatabase db) {

        String[] genres = context.getResources().getStringArray(R.array.book_genres);

        for (String genre : genres) {
            ContentValues cv = new ContentValues();
            cv.put(Contract.GenresColumns.GENRE_NAME, genre);
            db.insert(Tables.GENRES, null, cv);
        }
    }
}
