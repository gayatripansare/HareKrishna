package com.iskcon.temple

data class Event(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val date: String = "",           // Format: "DD-MM-YYYY"
    val timestamp: Long = 0L,        // For sorting
    val createdBy: String = "",      // Admin UID who created this
    val createdAt: Long = 0L         // When it was created
) {
    // Empty constructor for Firestore
    constructor() : this("", "", "", "", 0L, "", 0L)
}