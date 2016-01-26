package xyz.kandrac.library;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

import xyz.kandrac.library.model.Contract;
import xyz.kandrac.library.utils.LogUtils;

/**
 * Receiver that generates notifications about borrowed books
 * <p/>
 * Created by kandrac on 14/12/15.
 */
public class NotificationReceiver extends BroadcastReceiver {

    public static final String TAG = NotificationReceiver.class.getName();
    public static final String PREFERENCE_LAST_NOTIFICATION = "pref_last_notification";

    @Override
    public void onReceive(Context context, Intent intent) {
        long bookId = Contract.Books.getBookId(intent.getData());
        LogUtils.d(TAG, "Show notification request received for book " + bookId);
        showNotification(context, bookId);
    }

    private static void showNotification(Context context, long bookId) {
        Intent notificationIntent = new Intent(context, BookDetailActivity.class);
        notificationIntent.putExtra(BookDetailActivity.EXTRA_BOOK_ID, bookId);

        Notification notification = new Notification.Builder(context)
                .setSmallIcon(R.drawable.ic_book_white)
                .setContentText(context.getString(R.string.notification_book_borrowed_reminder_message))
                .setContentTitle(context.getString(R.string.notification_book_borrowed_reminder_title))
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(context, 0, notificationIntent, 0))
                .build();

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        manager.notify((int) bookId, notification);

        PreferenceManager.getDefaultSharedPreferences(context).edit().putLong(PREFERENCE_LAST_NOTIFICATION, System.currentTimeMillis()).apply();
    }

    public static void prepareNotification(Context context, long timeToNotify, long bookId) {
        if (timeToNotify > System.currentTimeMillis()) {
            LogUtils.d(TAG, "Delaying notification to " + timeToNotify);
            AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            manager.set(AlarmManager.RTC, timeToNotify, getBookPendingIntent(context, bookId));
        } else {
            long lastNotification = PreferenceManager.getDefaultSharedPreferences(context).getLong(PREFERENCE_LAST_NOTIFICATION, 0);
            if (timeToNotify > lastNotification) {
                LogUtils.d(TAG, "Showing notification right away");
                showNotification(context, bookId);
            } else {
                LogUtils.d(TAG, "Notification was already shown once");
            }
        }
    }

    public static void cancelNotification(Context context, long bookId) {
        LogUtils.d(TAG, "Cancelling notification for book " + bookId);
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
