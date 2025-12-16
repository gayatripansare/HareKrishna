package com.iskcon.temple



import android.app.Application
import androidx.work.*
import java.util.concurrent.TimeUnit

class IskconApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        scheduleFestivalChecks()
    }

    private fun scheduleFestivalChecks() {
        val workRequest = PeriodicWorkRequestBuilder<FestivalCheckWorker>(
            1, TimeUnit.DAYS, // Check once every day
            15, TimeUnit.MINUTES // Flex interval
        ).setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED) // No internet needed
                .setRequiresBatteryNotLow(false) // Can run even on low battery
                .build()
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "festival_check",
            ExistingPeriodicWorkPolicy.KEEP, // Keep existing schedule
            workRequest
        )
    }
}