package com.saitawngpha.ghostaddemo.adjobscheduler

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PersistableBundle
import com.saitawngpha.ghostaddemo.services.ForegroundAdService

/**
 * JobScheduler that acts as a watchdog - restarts the service every few seconds
 * This is the "self-healing" mechanism
 */
object AdJobScheduler {
    const val JOB_ID = 4242

    fun scheduleAdJob(context: Context) {
        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

        // Cancel existing job before scheduling new one
        jobScheduler.cancel(JOB_ID)

        val jobInfo = JobInfo.Builder(JOB_ID, ComponentName(context, AdJobService::class.java))
            .setMinimumLatency(2500L) // Wait at least 2.5 seconds
            .setOverrideDeadline(6000L) // Must run within 5 seconds
            .setPersisted(true) // Survive reboots
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY) // Run with any network
            .build()

        jobScheduler.schedule(jobInfo)
    }
}

/**
 * JobService that restarts the ForegroundAdService when triggered
 */
class AdJobService : android.app.job.JobService() {
    override fun onStartJob(p0: JobParameters?): Boolean {
        // Restart the foreground service
        val intent = Intent(this, ForegroundAdService::class.java)

        // Android O+ requires startForegroundService
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }

        // Reschedule the next job
        AdJobScheduler.scheduleAdJob(this)

        return false // Job is finished
    }

    override fun onStopJob(p0: JobParameters?): Boolean {
        // Reschedule if job was stopped prematurely
        AdJobScheduler.scheduleAdJob(this)
        return true // Reschedule the job
    }
}