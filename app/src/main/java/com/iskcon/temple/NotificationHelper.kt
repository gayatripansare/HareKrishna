package com.iskcon.temple

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "iskcon_festivals_events"
        const val CHANNEL_NAME = "ISKCON Festivals & Events"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for ISKCON festivals and temple events"
                enableLights(true)
                lightColor = Color.parseColor("#FF6200") // Saffron color
                enableVibration(true)
                setShowBadge(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showEventNotification(name: String, description: String, daysUntil: Int, eventId: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("event_id", eventId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            eventId.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val (title, sweetMessage) = getSweetMessage(name, description, daysUntil)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(description)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$description\n\n$sweetMessage"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(Color.parseColor("#FF6200")) // Saffron color
            .build()

        if (ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            with(NotificationManagerCompat.from(context)) {
                notify(eventId.hashCode(), notification)
            }
        }
    }

    private fun getSweetMessage(name: String, description: String, daysUntil: Int): Pair<String, String> {
        val nameLower = name.lowercase()

        return when (daysUntil) {
            // 1 DAY BEFORE MESSAGES
            1 -> {
                val title = "🔔 Tomorrow is $name"
                val message = when {
                    nameLower.contains("janmashtami") || nameLower.contains("krishna") ->
                        "Prepare for the divine celebration! May Lord Krishna bless you! 🙏 Hare Krishna!"

                    nameLower.contains("radha") ->
                        "Prepare to celebrate the Queen of Vrindavan! 🌸 Radhe Radhe!"

                    nameLower.contains("gaura") || nameLower.contains("chaitanya") ->
                        "Get ready to celebrate the Golden Avatar! 🎊 Haribol!"

                    nameLower.contains("ram") || nameLower.contains("rama") ->
                        "Prepare for the celebration of Lord Rama! 🏹 Jai Shri Ram!"

                    nameLower.contains("narasimha") || nameLower.contains("nrsimha") ->
                        "Tomorrow we celebrate the fierce protector of devotees! 🦁 Jai Narasimha!"

                    nameLower.contains("ekadashi") ->
                        "Prepare for your spiritual fast tomorrow! May it purify your heart! 🙏 Hare Krishna!"

                    nameLower.contains("balarama") || nameLower.contains("balaram") ->
                        "Tomorrow we celebrate Lord Balarama! Jai Balaram! 🙏"

                    nameLower.contains("prabhupada") ->
                        "Tomorrow we honor our beloved Srila Prabhupada! 🙏 Hare Krishna!"

                    nameLower.contains("ratha yatra") || nameLower.contains("rath yatra") ->
                        "Prepare for the grand chariot festival! 🚩 Jai Jagannath!"

                    nameLower.contains("govardhan") ->
                        "Tomorrow we celebrate Govardhan Puja! 🏔️ Jai Shri Krishna!"

                    nameLower.contains("diwali") || nameLower.contains("deepavali") ->
                        "Prepare for the festival of lights! 🪔 Happy Diwali!"

                    nameLower.contains("gita jayanti") ->
                        "Tomorrow we celebrate the appearance of Bhagavad Gita! 📖 Hare Krishna!"

                    else ->
                        "Don't miss this special event tomorrow! See you at the temple! 🙏 Hare Krishna!"
                }
                Pair(title, message)
            }

            // ON THE DAY MESSAGES
            0 -> {
                val title = "🎉 ${getGreeting(nameLower)} $name!"
                val message = when {
                    nameLower.contains("janmashtami") || nameLower.contains("krishna") ->
                        "May Lord Krishna bless you with love and devotion! 🙏 Hare Krishna Hare Krishna!"

                    nameLower.contains("radha") ->
                        "May Radharani shower Her divine grace upon you! 🌸🙏 Radhe Radhe!"

                    nameLower.contains("gaura") || nameLower.contains("chaitanya") ->
                        "May Lord Gauranga fill your life with chanting and dancing! 🎊🙏 Haribol!"

                    nameLower.contains("ram") || nameLower.contains("rama") ->
                        "May Lord Rama bless you with strength and righteousness! 🏹🙏 Jai Shri Ram!"

                    nameLower.contains("narasimha") || nameLower.contains("nrsimha") ->
                        "May Lord Narasimha protect you from all obstacles! 🦁🙏 Jai Narasimha!"

                    nameLower.contains("ekadashi") ->
                        "May your fast bring you closer to Krishna! Wishing you spiritual strength! 🙏 Hare Krishna!"

                    nameLower.contains("balarama") || nameLower.contains("balaram") ->
                        "May Lord Balarama give you strength and devotion! 🙏 Jai Balaram!"

                    nameLower.contains("prabhupada") ->
                        "Remembering our beloved Srila Prabhupada with gratitude! 🙏 All glories to Srila Prabhupada!"

                    nameLower.contains("ratha yatra") || nameLower.contains("rath yatra") ->
                        "May Lord Jagannath bless you! Pull the rope of devotion! 🚩🙏 Jai Jagannath!"

                    nameLower.contains("govardhan") ->
                        "May Krishna's blessings flow like the sacred Govardhan Hill! 🏔️🙏 Hare Krishna!"

                    nameLower.contains("diwali") || nameLower.contains("deepavali") ->
                        "May the divine light illuminate your life! 🪔🙏 Happy Diwali!"

                    nameLower.contains("gita jayanti") ->
                        "May the wisdom of Bhagavad Gita guide your life! 📖🙏 Hare Krishna!"

                    nameLower.contains("feast") || nameLower.contains("prasadam") ->
                        "Join us for divine prasadam and kirtan! 🍽️🙏 Hare Krishna!"

                    nameLower.contains("kirtan") ->
                        "Join us for melodious kirtan! 🎵🙏 Hare Krishna Hare Rama!"

                    else ->
                        "Join us for this special celebration at the temple! 🙏 Hare Krishna!"
                }
                Pair(title, message)
            }

            else -> Pair("🔔 $name in $daysUntil days", "Mark your calendar! 🙏 Hare Krishna!")
        }
    }

    private fun getGreeting(nameLower: String): String {
        return when {
            nameLower.contains("happy") || nameLower.contains("jayanti") ||
                    nameLower.contains("appearance") || nameLower.contains("janma") -> "Happy"
            nameLower.contains("today") -> "Today is"
            else -> "Happy"
        }
    }

    // Legacy method for backward compatibility
    fun showFestivalNotification(festival: Festival, daysUntil: Int) {
        showEventNotification(festival.name, festival.description, daysUntil, festival.id)
    }
}