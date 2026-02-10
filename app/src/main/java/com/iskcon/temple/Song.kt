package com.iskcon.temple

data class Song(
    val id: String = "",
    val title: String = "",
    val artist: String = "",
    val cloudinaryUrl: String = "",
    val duration: String = "",
    val imageUrl: String = "",
    val category: String = "Bhajan", // Bhajan, Kirtan, Mantra, etc.
    val lyrics: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    // No-argument constructor for Firestore
    constructor() : this("", "", "", "", "", "", "", "", 0L)
}