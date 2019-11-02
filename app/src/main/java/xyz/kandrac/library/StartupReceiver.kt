package xyz.kandrac.library

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import xyz.kandrac.library.work.NotificationScheduler

/**
 * Created by kandrac on 26/01/16.
 */
class StartupReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {

            val uploadWorkRequest = OneTimeWorkRequestBuilder<NotificationScheduler>()
                    .build()

            WorkManager.getInstance(context).enqueue(uploadWorkRequest)

        }
    }
}
