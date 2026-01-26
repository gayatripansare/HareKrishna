package com.iskcon.temple

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*

class FestivalCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val firestore = FirebaseFirestore.getInstance()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d("FestivalCheckWorker", "🔍 ========== WORKER STARTED ==========")
            Log.d("FestivalCheckWorker", "🕐 Current time: ${Date()}")
            checkUpcomingFestivalsAndEvents()
            Log.d("FestivalCheckWorker", "✅ ========== WORKER COMPLETED ==========")
            Result.success()
        } catch (e: Exception) {
            Log.e("FestivalCheckWorker", "❌ Error: ${e.message}", e)
            Result.failure()
        }
    }

    private suspend fun checkUpcomingFestivalsAndEvents() {
        val notificationHelper = NotificationHelper(applicationContext)
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        Log.d("FestivalCheckWorker", "📅 Today's date: ${today.time}")

        // ✅ Check festivals from Firestore
        try {
            val festivalsSnapshot = firestore.collection("festivals")
                .get()
                .await()

            Log.d("FestivalCheckWorker", "📅 Found ${festivalsSnapshot.size()} festivals in Firestore")

            festivalsSnapshot.forEach { doc ->
                try {
                    val festival = doc.toObject(Festival::class.java)
                    checkAndNotify(
                        festival.name,
                        festival.description,
                        festival.date,
                        festival.id,
                        today,
                        notificationHelper
                    )
                } catch (e: Exception) {
                    Log.e("FestivalCheckWorker", "Error parsing festival: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("FestivalCheckWorker", "❌ Error loading festivals: ${e.message}")
        }

        // ✅ Check custom events from Firestore
        try {
            val eventsSnapshot = firestore.collection("custom_events")
                .get()
                .await()

            Log.d("FestivalCheckWorker", "📅 Found ${eventsSnapshot.size()} custom events in Firestore")

            eventsSnapshot.forEach { doc ->
                try {
                    val event = doc.toObject(Event::class.java)
                    checkAndNotify(
                        event.name,
                        event.description,
                        event.date,
                        event.id,
                        today,
                        notificationHelper
                    )
                } catch (e: Exception) {
                    Log.e("FestivalCheckWorker", "Error parsing event: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("FestivalCheckWorker", "❌ Error loading custom events: ${e.message}")
        }
    }

    private fun checkAndNotify(
        name: String,
        description: String,
        dateStr: String,
        id: String,
        today: Calendar,
        notificationHelper: NotificationHelper
    ) {
        if (dateStr.isEmpty()) return

        try {
            val eventDate = parseDateString(dateStr)
            val daysUntil = daysBetween(today, eventDate)

            Log.d("FestivalCheckWorker", "📆 $name: $daysUntil days away (Date: $dateStr)")

            // ✅ Send notifications 1 day before and on the day
            when (daysUntil) {
                1 -> {
                    Log.d("FestivalCheckWorker", "🔔 Sending 1-day reminder for $name")
                    notificationHelper.showEventNotification(name, description, 1, id)
                }
                0 -> {
                    Log.d("FestivalCheckWorker", "🎉 Sending today notification for $name")
                    notificationHelper.showEventNotification(name, description, 0, id)
                }
                else -> {
                    Log.d("FestivalCheckWorker", "⏭️ $name is $daysUntil days away - no notification")
                }
            }
        } catch (e: Exception) {
            Log.e("FestivalCheckWorker", "❌ Error parsing date $dateStr: ${e.message}")
        }
    }

    private fun parseDateString(dateStr: String): Calendar {
        // Format: "DD-MM-YYYY"
        val parts = dateStr.split("-")
        return Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, parts[0].toInt())
            set(Calendar.MONTH, parts[1].toInt() - 1)  // Month is 0-based
            set(Calendar.YEAR, parts[2].toInt())
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    private fun daysBetween(start: Calendar, end: Calendar): Int {
        val startMillis = start.timeInMillis
        val endMillis = end.timeInMillis
        val diff = endMillis - startMillis
        return (diff / (1000 * 60 * 60 * 24)).toInt()
    }
}