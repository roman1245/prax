package xyz.kandrac.library;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;

import xyz.kandrac.library.model.Contract;
import xyz.kandrac.library.model.Database;
import xyz.kandrac.library.utils.LogUtils;

/**
 * Created by Jan Kandrac on 5.6.2016.
 */
public class InitService extends IntentService {

    public static final String ACTION = "take_action";
    public static final String TAG = InitService.class.getName();

    public static final int ACTION_UNDEFINED = 0;
    public static final int ACTION_CLEAR_DATABASE = 1;

    public InitService() {
        super(TAG);
    }

    public static void start(Context context, int action) {
        Intent intent = new Intent(context, InitService.class);
        intent.putExtra(ACTION, action);
        context.startService(intent);
    }

    public static void start(Context context) {
        context.startService(new Intent(context, InitService.class));
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int action = intent.getIntExtra(ACTION, ACTION_UNDEFINED);

        switch (action) {
            case ACTION_UNDEFINED:

                // standard setting values loading
                PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

                // remove old database if present
                deleteDatabase("snk.db");

                // no break
            case ACTION_CLEAR_DATABASE:
                // remove authors and publishers without books
                try {
                    Database db = new Database(this);
                    SQLiteDatabase dbHelper = db.getWritableDatabase();
                    dbHelper.execSQL("DELETE FROM authors WHERE _id IN (SELECT _id FROM authors LEFT JOIN book_author ON _id = author_id WHERE author_id IS NULL)");
                    dbHelper.execSQL("DELETE FROM publishers WHERE _id IN (SELECT publishers._id FROM publishers LEFT JOIN books ON publishers._id = book_publisher_id WHERE book_publisher_id IS NULL)");
                    dbHelper.execSQL("DELETE FROM libraries WHERE _id IN (SELECT libraries._id FROM libraries LEFT JOIN books ON libraries._id = book_library_id WHERE book_library_id IS NULL);");
                    dbHelper.close();

                    getContentResolver().notifyChange(Contract.Authors.CONTENT_URI, null);
                    getContentResolver().notifyChange(Contract.Publishers.CONTENT_URI, null);
                    getContentResolver().notifyChange(Contract.Libraries.CONTENT_URI, null);
                } catch (SQLException exception) {
                    LogUtils.w(TAG, "cannot delete from database", exception);
                }
        }
    }
}
