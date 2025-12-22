package com.iskcon.temple

import android.app.Application
import android.util.Log
import androidx.work.*
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class IskconApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Log.d("IskconApplication", "🚀 App starting...")

        // Initialize festivals in Firestore (first time only)
        initializeFestivalsInFirestore()

        // Schedule daily festival checks
        scheduleFestivalChecks()
    }

    private fun initializeFestivalsInFirestore() {
        val firestore = FirebaseFirestore.getInstance()

        // ✅ FIX: Check if festivals already exist first
        firestore.collection("festivals")
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // No festivals exist, initialize them
                    Log.d("IskconApplication", "📅 No festivals found, initializing...")
                    uploadFestivalsToFirestore()
                } else {
                    Log.d("IskconApplication", "✅ Festivals already exist in Firestore")
                }
            }
            .addOnFailureListener { e ->
                Log.e("IskconApplication", "❌ Error checking festivals: ${e.message}")
            }
    }

    private fun uploadFestivalsToFirestore() {
        val firestore = FirebaseFirestore.getInstance()
        val festivals = getVaishnavaCalendar2025_2026() // Get all festivals

        var uploadedCount = 0

        festivals.forEach { festival ->
            firestore.collection("festivals")
                .document(festival.id)
                .set(festival)
                .addOnSuccessListener {
                    uploadedCount++
                    if (uploadedCount == festivals.size) {
                        Log.d("IskconApplication", "✅ All ${festivals.size} festivals uploaded!")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("IskconApplication", "❌ Error uploading ${festival.name}: ${e.message}")
                }
        }
    }

    private fun scheduleFestivalChecks() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(false)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<FestivalCheckWorker>(
            1, TimeUnit.DAYS,
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setInitialDelay(1, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "festival_notification_check",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )

        Log.d("IskconApplication", "✅ Festival notification worker scheduled!")
    }

    // ✅ ADD THIS: Festival calendar data
    private fun getVaishnavaCalendar2025_2026(): List<Festival> {
        return listOf(
            // March 2025
            Festival("festival_mar_15_2025", "Festival of Jagannath Misra", "Appearance celebration", "15-03-2025", 3, 15, 2025, "—-"),
            Festival("festival_mar_26_2025", "Dvadasi (Ekadashi vrata)", "Fasting day", "26-03-2025", 3, 26, 2025, "Next day after Sunrise"),

            // April 2025
            Festival("rama_navami_2025", "Sri Rama Navami", "Appearance day of Lord Rama", "06-04-2025", 4, 6, 2025, "Fasting till Sunset"),
            Festival("ekadashi_apr_8_2025", "Ekadashi", "Fasting day", "08-04-2025", 4, 8, 2025, "Next day after Sunrise"),
            Festival("hanuman_jayanti_2025", "Hanuman Jayanti", "Appearance of Lord Hanuman", "12-04-2025", 4, 12, 2025, "—-"),
            Festival("ekadashi_apr_24_2025", "Ekadashi", "Fasting day", "24-04-2025", 4, 24, 2025, "Next day after Sunrise"),
            Festival("akshaya_tritiya_2025", "Akshaya Tritiya", "Chandan Yatra begins", "30-04-2025", 4, 30, 2025, "—-"),

            // May 2025
            Festival("ekadashi_may_8_2025", "Ekadashi", "Fasting day", "08-05-2025", 5, 8, 2025, "Next day after Sunrise"),
            Festival("narasimha_chaturdashi_2025", "Narasimha Chaturdashi", "Appearance of Lord Narasimhadeva", "11-05-2025", 5, 11, 2025, "Fasting till dusk"),
            Festival("ekadashi_may_23_2025", "Ekadashi", "Fasting day", "23-05-2025", 5, 23, 2025, "Next day after Sunrise"),

            // June 2025
            Festival("panihati_2025", "Panihati Chida Dahi Utsava", "Festival of Chipped Rice", "09-06-2025", 6, 9, 2025, "—-"),
            Festival("snana_yatra_2025", "Jagannatha Snana Yatra", "Bathing ceremony of Lord Jagannath", "11-06-2025", 6, 11, 2025, "—-"),
            Festival("bhaktivinoda_disappearance_2025", "Srila Bhaktivinoda Thakura – Disappearance", "Remembrance day", "25-06-2025", 6, 25, 2025, "Fasting till noon"),
            Festival("ratha_yatra_2025", "Jagannatha Ratha Yatra", "Festival of Lord Jagannath's Chariot", "27-06-2025", 6, 27, 2025, "—-"),

            // July 2025
            Festival("ekadashi_jul_6_2025", "Ekadashi", "Fasting day", "06-07-2025", 7, 6, 2025, "Next day after Sunrise"),
            Festival("iskcon_founding_2025", "ISKCON Founding Day", "As per Certificate of Incorporation", "13-07-2025", 7, 13, 2025, "—-"),
            Festival("ekadashi_jul_21_2025", "Ekadashi", "Fasting day", "21-07-2025", 7, 21, 2025, "Next day after Sunrise"),

            // August 2025
            Festival("ekadashi_aug_5_2025", "Ekadashi and Jhulan Yatra Begins", "Fasting day", "05-08-2025", 8, 5, 2025, "Next day after Sunrise"),
            Festival("balarama_jayanti_2025", "Balarama Jayanti", "Appearance of Lord Balarama", "09-08-2025", 8, 9, 2025, "Fasting till noon"),
            Festival("janmashtami_2025", "Sri Krishna Janmashtami", "Appearance of Lord Sri Krishna", "16-08-2025", 8, 16, 2025, "Fasting till midnight"),
            Festival("nandotsava_2025", "Nandotsava & Sri Vyasa Puja", "Appearance of Srila Prabhupada", "17-08-2025", 8, 17, 2025, "Fasting till noon"),
            Festival("ekadashi_aug_19_2025", "Ekadashi", "Fasting day", "19-08-2025", 8, 19, 2025, "Next day after Sunrise"),
            Festival("radhashtami_2025", "Radhashtami", "Appearance of Srimati Radharani", "31-08-2025", 8, 31, 2025, "Fasting till noon"),

            // September 2025
            Festival("vamana_jayanti_2025", "Vamana Jayanti", "Appearance of Lord Vamanadeva", "04-09-2025", 9, 4, 2025, "Fasting observed previous day till noon"),
            Festival("bhaktivinoda_appearance_2025", "Srila Bhaktivinoda Thakura – Appearance", "Appearance day", "05-09-2025", 9, 5, 2025, "Fasting till noon"),
            Festival("ekadashi_sep_17_2025", "Ekadashi", "Fasting day", "17-09-2025", 9, 17, 2025, "Next day after Sunrise"),

            // October 2025
            Festival("vijaya_dashami_2025", "Vijaya Dashami", "Dasara Festival", "02-10-2025", 10, 2, 2025, "—-"),
            Festival("ekadashi_oct_3_2025", "Ekadashi", "Fasting day", "03-10-2025", 10, 3, 2025, "Next day after Sunrise"),
            Festival("ekadashi_oct_17_2025", "Ekadashi", "Fasting day", "17-10-2025", 10, 17, 2025, "Next day after Sunrise"),
            Festival("govardhan_puja_2025", "Govardhana Puja, Go Puja", "Festival of Govardhan Hill", "22-10-2025", 10, 22, 2025, "—-"),
            Festival("prabhupada_disappearance_2025", "Srila Prabhupada – Disappearance", "Disappearance day", "25-10-2025", 10, 25, 2025, "Fasting till noon"),

            // November 2025
            Festival("chaturmasya_ends_2025", "Chaturmasya Ends", "End of four-month vrata", "04-11-2025", 11, 4, 2025, "—-"),
            Festival("ekadashi_nov_15_2025", "Ekadashi", "Fasting day", "15-11-2025", 11, 15, 2025, "Next day after Sunrise"),

            // December 2025
            Festival("gita_jayanti_2025", "Gita Jayanti", "Appearance of Bhagavad-gita", "01-12-2025", 12, 1, 2025, "Next day after Sunrise"),
            Festival("bhaktisiddhanta_disappearance_2025", "Srila Bhaktisiddhanta Sarasvati Thakura – Disappearance", "Disappearance day", "08-12-2025", 12, 8, 2025, "Fasting till noon"),
            Festival("vaikuntha_ekadashi_2025", "Vaikuntha Ekadashi", "Most auspicious Ekadashi", "30-12-2025", 12, 30, 2025, "No Fasting"),

            // January 2026
            Festival("ekadashi_jan_14_2026", "Ekadashi", "Fasting day", "14-01-2026", 1, 14, 2026, "Next day after Sunrise"),
            Festival("advaita_acharya_2026", "Sri Advaita Acharya – Appearance", "Appearance day", "25-01-2026", 1, 25, 2026, "Fasting till noon"),
            Festival("varahadeva_2026", "Sri Varahadeva – Appearance", "Appearance of Lord Varaha", "30-01-2026", 1, 30, 2026, "Fasting observed previous day till noon"),
            Festival("nityananda_trayodashi_2026", "Nityananda Trayodashi", "Appearance of Sri Nityananda Prabhu", "31-01-2026", 1, 31, 2026, "Fasting till noon"),

            // February 2026
            Festival("bhaktisiddhanta_appearance_2026", "Srila Bhaktisiddhanta Sarasvati Thakura – Appearance", "Appearance day", "06-02-2026", 2, 6, 2026, "Fasting till noon"),
            Festival("ekadashi_feb_13_2026", "Ekadashi", "Fasting day", "13-02-2026", 2, 13, 2026, "Next day after Sunrise"),
            Festival("ekadashi_feb_27_2026", "Ekadashi", "Fasting day", "27-02-2026", 2, 27, 2026, "Next day after Sunrise"),

            // March 2026
            Festival("gaura_purnima_2026", "Sri Gaura Purnima", "Appearance of Sri Chaitanya Mahaprabhu", "03-03-2026", 3, 3, 2026, "Fasting till Moonrise")
        )
    }
}