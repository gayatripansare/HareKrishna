package com.iskcon.temple

import android.os.Bundle
import android.widget.ImageView
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity

class NotificationSettingsActivity : AppCompatActivity() {

    private lateinit var festivalNotificationsSwitch: Switch
    private lateinit var eventNotificationsSwitch: Switch
    private lateinit var reminderNotificationsSwitch: Switch
    private lateinit var generalNotificationsSwitch: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_settings)

        initializeViews()
        setupToolbar()
        loadSettings()
        setupSwitchListeners()
    }

    private fun initializeViews() {
        festivalNotificationsSwitch = findViewById(R.id.switch_festival_notifications)
        eventNotificationsSwitch = findViewById(R.id.switch_event_notifications)
        reminderNotificationsSwitch = findViewById(R.id.switch_reminder_notifications)
        generalNotificationsSwitch = findViewById(R.id.switch_general_notifications)
    }

    private fun setupToolbar() {
        findViewById<ImageView>(R.id.back_button).setOnClickListener {
            finish()
        }
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences("NotificationPrefs", MODE_PRIVATE)
        festivalNotificationsSwitch.isChecked = prefs.getBoolean("festival_notifications", true)
        eventNotificationsSwitch.isChecked = prefs.getBoolean("event_notifications", true)
        reminderNotificationsSwitch.isChecked = prefs.getBoolean("reminder_notifications", true)
        generalNotificationsSwitch.isChecked = prefs.getBoolean("general_notifications", true)
    }

    private fun setupSwitchListeners() {
        val prefs = getSharedPreferences("NotificationPrefs", MODE_PRIVATE)

        festivalNotificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("festival_notifications", isChecked).apply()
        }

        eventNotificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("event_notifications", isChecked).apply()
        }

        reminderNotificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("reminder_notifications", isChecked).apply()
        }

        generalNotificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("general_notifications", isChecked).apply()
        }
    }
}