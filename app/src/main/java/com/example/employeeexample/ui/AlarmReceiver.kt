package com.example.employeeexample.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class AlarmReceiver :BroadcastReceiver(){

    override fun onReceive(context: Context, intent: Intent?) {
        val intent=Intent(context,AlarmService::class.java).apply {
            action=ACTION_ALARM_START
        }
        ContextCompat.startForegroundService(context,intent)
    }
}