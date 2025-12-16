package com.iskcon.temple



import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.util.*

class FestivalCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        checkUpcomingFestivals()
        return Result.success()
    }

    private fun checkUpcomingFestivals() {
        val festivals = FestivalRepository.getIskconFestivals()
        val notificationHelper = NotificationHelper(applicationContext)
        val today = Calendar.getInstance()

        festivals.forEach { festival ->
            if (festival.date.isNotEmpty()) {
                val festivalDate = parseFestivalDate(festival.date)
                val daysUntil = daysBetween(today, festivalDate)

                // Notify 7 days before, 3 days before, 1 day before, and on the day
                when (daysUntil) {
                    7, 3, 1, 0 -> {
                        notificationHelper.showFestivalNotification(festival, daysUntil)
                    }
                }
            }
        }
    }

    private fun parseFestivalDate(dateStr: String): Calendar {
        val parts = dateStr.split("-")
        return Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, parts[0].toInt())
            set(Calendar.MONTH, parts[1].toInt() - 1)
            set(Calendar.YEAR, parts[2].toInt())
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    private fun daysBetween(start: Calendar, end: Calendar): Int {
        val startMillis = Calendar.getInstance().apply {
            set(Calendar.YEAR, start.get(Calendar.YEAR))
            set(Calendar.MONTH, start.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, start.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val endMillis = Calendar.getInstance().apply {
            set(Calendar.YEAR, end.get(Calendar.YEAR))
            set(Calendar.MONTH, end.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, end.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val diff = endMillis - startMillis
        return (diff / (1000 * 60 * 60 * 24)).toInt()
    }
}