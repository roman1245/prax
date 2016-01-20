package xyz.kandrac.library.snk;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by kandrac on 20/01/16.
 */
public class SnkDatabase extends SQLiteOpenHelper {

    /**
     * In assets and real database name
     */
    public static final String DB_NAME = "snk.db";

    public SnkDatabase(Context context) {
        super(context, DB_NAME, null, 1);
        openDatabase(context);
    }

    /**
     * Opening the database from assets if not previously opened
     *
     * @param context of database
     */
    public void openDatabase(Context context) {

        // destination database file
        File dbFile = context.getDatabasePath(DB_NAME);

        if (!dbFile.exists()) {
            try {
                if ((dbFile.getParentFile().mkdirs() || dbFile.getParentFile().isDirectory()) && dbFile.createNewFile()) {
                    try {
                        copyDatabase(context, dbFile);
                    } catch (IOException e) {
                        throw new RuntimeException("Error while copying database", e);
                    }
                } else {
                    throw new RuntimeException("Error creating database file");
                }
            } catch (IOException ex) {
                throw new RuntimeException("Error creating database file", ex);
            }
        }

        SQLiteDatabase.openDatabase(dbFile.getPath(), null, SQLiteDatabase.OPEN_READONLY);
    }

    private void copyDatabase(Context context, File dbFile) throws IOException {
        InputStream is = context.getAssets().open(DB_NAME);
        OutputStream os = new FileOutputStream(dbFile);

        byte[] buffer = new byte[1024];
        while (is.read(buffer) > 0) {
            os.write(buffer);
        }

        os.flush();
        os.close();
        is.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
