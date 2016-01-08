package xyz.kandrac.library;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import java.util.concurrent.TimeUnit;

import xyz.kandrac.library.model.Contract;
import xyz.kandrac.library.utils.DateUtils;

/**
 * Receiver that generates notifications about borrowed books
 * <p/>
 * Created by kandrac on 14/12/15.
 */
public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        long bookId = Contract.Books.getBookId(intent.getData());

        Cursor bookCursor = context.getContentResolver().query(Contract.Books.buildBookUri(bookId), new String[]{Contract.Books.BOOK_TITLE}, null, null, null);

        if (bookCursor != null && bookCursor.getCount() > 0 && bookCursor.moveToFirst()) {

            String title = bookCursor.getString(bookCursor.getColumnIndex(Contract.Books.BOOK_TITLE));

            if (bookCursor.getInt(bookCursor.getColumnIndex(Contract.Books.BOOK_BORROWED)) == 0) {
                bookCursor.close();
                return;
            }

            Intent notificationIntent = new Intent(context, BookDetailActivity.class);
            notificationIntent.putExtra(BookDetailActivity.EXTRA_BOOK_ID, bookId);

            // TODO: notification for multiple books? modify setNumber and message content
            Notification notification = new Notification.Builder(context)
                    .setSmallIcon(R.drawable.ic_author)
                    .setNumber(1)
                    .setContentText(context.getString(R.string.notification_book_borrowed_reminder_message, title))
                    .setContentTitle(context.getString(R.string.notification_book_borrowed_reminder_title))
                    .setPriority(Notification.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setContentIntent(PendingIntent.getActivity(context, 0, notificationIntent, 0))
                    .build();

            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            manager.notify(1, notification);
        } else if (bookCursor != null) {
            bookCursor.close();
        }
    }

    public static void prepareNotification(Context context, int numberOfDays, long bookId) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Long timeToNotify =
                BuildConfig.DEBUG
                        ? System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(numberOfDays)
                        : DateUtils.getTodayHourTime(18) + TimeUnit.DAYS.toMillis(numberOfDays);

        manager.set(AlarmManager.RTC, timeToNotify, getBookPendingIntent(context, bookId));
    }

    public static void cancelNotification(Context context, long bookId) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.cancel(getBookPendingIntent(context, bookId));
    }

    private static Intent getBookIntent(Context context, long bookId) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setData(Contract.Books.buildBookUri(bookId));
        return intent;
    }

    private static PendingIntent getBookPendingIntent(Context context, long bookId) {
        return PendingIntent.getBroadcast(context, 0, getBookIntent(context, bookId), 0);
    }
}
