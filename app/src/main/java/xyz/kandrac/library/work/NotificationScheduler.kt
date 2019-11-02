package xyz.kandrac.library.work

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import xyz.kandrac.library.NotificationReceiver
import xyz.kandrac.library.model.Contract

class NotificationScheduler(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    override fun doWork(): Result {

        applicationContext.contentResolver.query(
                Contract.BorrowInfo.CONTENT_URI,
                arrayOf(Contract.BorrowInfo.BORROW_NEXT_NOTIFICATION, Contract.BorrowInfo.BORROW_BOOK_ID),
                Contract.BorrowInfo.BORROW_DATE_RETURNED + " = 0", null, null)
                .use { cursor ->

                    if (cursor != null && cursor.moveToFirst()) {

                        do {
                            val time = cursor.getLong(cursor.getColumnIndex(Contract.BorrowInfo.BORROW_NEXT_NOTIFICATION))
                            val bookId = cursor.getLong(cursor.getColumnIndex(Contract.BorrowInfo.BORROW_BOOK_ID))
                            NotificationReceiver.prepareNotification(applicationContext, time, bookId)
                        } while (cursor.moveToNext())
                        cursor.close()
                    }

                }

        return Result.success()
    }
}
