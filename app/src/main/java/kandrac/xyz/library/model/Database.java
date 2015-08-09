package kandrac.xyz.library.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import kandrac.xyz.library.model.obj.Author;
import kandrac.xyz.library.model.obj.Book;
import kandrac.xyz.library.model.obj.Book2Author;
import kandrac.xyz.library.model.obj.Publisher;

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

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
        super.onConfigure(db);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Author.CREATE_TABLE);
        db.execSQL(Publisher.CREATE_TABLE);
        db.execSQL(Book.CREATE_TABLE);
        db.execSQL(Book2Author.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(Author.DROP_TABLE);
        db.execSQL(Publisher.DROP_TABLE);
        db.execSQL(Book.DROP_TABLE);
        db.execSQL(Book2Author.DROP_TABLE);
        onCreate(db);
    }
}
