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

        String BOOKS_JOIN_AUTHORS_ID = "books "
                + "LEFT OUTER JOIN authors ON books.book_author_id=authors._id";
    }

    interface References {
        String AUTHORS_ID = "REFERENCES " + Tables.AUTHORS + "(" + Contract.Authors.AUTHOR_ID + ")";
    }


    public static final String BOOKS_CREATE_TABLE =
            "CREATE TABLE " + Tables.BOOKS + " (" +
                    Contract.Books.BOOK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Contract.Books.BOOK_TITLE + " TEXT NOT NULL," +
                    Contract.Books.BOOK_DESCRIPTION + " TEXT," +
                    Contract.Books.BOOK_ISBN + " TEXT," +
                    Contract.Books.BOOK_IMAGE_FILE + " TEXT," +
                    Contract.Books.BOOK_AUTHOR_ID + " INTEGER " + References.AUTHORS_ID + ")";

    public static final String AUTHORS_CREATE_TABLE =
            "CREATE TABLE " + Tables.AUTHORS + " (" +
                    Contract.Authors.AUTHOR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Contract.Authors.AUTHOR_NAME + " TEXT NOT NULL," +
                    "UNIQUE (" + Contract.Authors.AUTHOR_NAME + ") ON CONFLICT REPLACE)";

    public static final String BOOKS_DROP_TABLE = "DROP TABLE IF EXISTS " + Tables.BOOKS;
    public static final String AUTHORS_DROP_TABLE = "DROP TABLE IF EXISTS " + Tables.AUTHORS;

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
        super.onConfigure(db);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(AUTHORS_CREATE_TABLE);
        db.execSQL(BOOKS_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(BOOKS_DROP_TABLE);
        db.execSQL(AUTHORS_DROP_TABLE);
        onCreate(db);
    }
}
