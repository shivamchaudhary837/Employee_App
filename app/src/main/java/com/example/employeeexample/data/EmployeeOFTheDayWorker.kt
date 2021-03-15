package com.example.employeeexample.data

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.employeeexample.R
import com.example.employeeexample.ui.MainActivity


class EmployeeOfTheDayWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {
    private val CHANNEL_ID = "1000"

    override fun doWork(): Result {
        // runs on Background thread

        if (runAttemptCount > 5) {
            return Result.failure()
        }
        try {
            val name = getEmployeeOfTheDay()
            showNotification(name)
        }
        catch (e: Exception) {
            return Result.retry()
        }

        return Result.success()
    }

    private fun getEmployeeOfTheDay(): String{
        // get name and age of employee of the day from API
        // we are not implementing a real API instead having a hard coded name
        // ideally an employer id wil be more accurate
        return "Sara Jones"

    }

    private fun showNotification(name: String){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }

        val intent = Intent(applicationContext, MainActivity::class.java)

        val uniqueInt = (System.currentTimeMillis() and 0xfffffff).toInt()
        val pendingIntent: PendingIntent = PendingIntent.getActivity(applicationContext,
            uniqueInt, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(applicationContext.getString(R.string.employee_of_the_day))
            .setContentText(applicationContext.getString(R.string.employee_of_the_day_name, name))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(applicationContext)) {
            notify(100, builder.build())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        val notificationManager = applicationContext
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val name: CharSequence = applicationContext.getString(R.string.app_name)
        val description = "Service example notification channel"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(
            CHANNEL_ID,
            name,
            importance
        )
        channel.description = description
        channel.setShowBadge(false)
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        notificationManager.createNotificationChannel(channel)
    }

}