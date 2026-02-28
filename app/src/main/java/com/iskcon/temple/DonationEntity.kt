package com.iskcon.temple

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "donations")
data class DonationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val donationType: String,
    val amount: String,
    val status: String,
    val dateTime: String
)