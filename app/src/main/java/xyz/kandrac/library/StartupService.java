package xyz.kandrac.library;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import xyz.kandrac.library.model.Contract;
import xyz.kandrac.library.utils.LogUtils;

/**
 * Created by kandrac on 26/01/16.
 */
public class StartupService extends IntentService {

    private static final String TAG = StartupService.class.getName();

    public StartupService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d(TAG, "Handling intent in Startup Service");

        Cursor cursor = getContentResolver().query(
                Contract.BorrowInfo.CONTENT_URI,
                new String[]{Contract.BorrowInfo.BORROW_NEXT_NOTIFICATION, Contract.BorrowInfo.BORROW_BOOK_ID},
                Contract.BorrowInfo.BORROW_DATE_RETURNED + " = 0",
                null,
                null);

        if (cursor != null && cursor.moveToFirst()) {

            LogUtils.d(TAG, "Setting notification for " + cursor.getCount() + " items");

            do {
                long time = cursor.getLong(cursor.getColumnIndex(Contract.BorrowInfo.BORROW_NEXT_NOTIFICATION));
                long bookId = cursor.getLong(cursor.getColumnIndex(Contract.BorrowInfo.BORROW_BOOK_ID));
                NotificationReceiver.prepareNotification(this, time, bookId);
            } while (cursor.moveToNext());
            cursor.close();
        }
    }
}
