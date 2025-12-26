package com.iskcon.temple



data class GalleryImage(
    val id: String = "",
    val imageUrl: String = "",
    val title: String = "",
    val category: String = "", // "deity" or "events"
    val timestamp: Long = 0L
) {
    // Empty constructor needed for Firestore
    constructor() : this("", "", "", "", 0L)
}