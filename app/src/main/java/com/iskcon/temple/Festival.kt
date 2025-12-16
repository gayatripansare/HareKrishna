package com.iskcon.temple



data class Festival(
    val id: String,              // Unique identifier like "janmashtami"
    val name: String,            // Festival name like "Janmashtami"
    val description: String,     // Description like "Appearance day of Lord Krishna"
    val date: String,            // Date in format "26-08-2025"
    val month: Int,              // Month number (1-12)
    val day: Int,                // Day number (1-31)
    val isLunarDate: Boolean = true  // True for Hindu calendar festivals
)