package com.iskcon.temple

data class Festival(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val date: String = "",           // Format: "DD-MM-YYYY"
    val month: Int = 0,
    val day: Int = 0,
    val year: Int = 0,
    val fasting: String = "",        // ✅ Make sure this field exists!
    val isLunarDate: Boolean = true,
    val isEditable: Boolean = true
) {
    // Empty constructor for Firestore
    constructor() : this("", "", "", "", 0, 0, 0, "", true, true)
}