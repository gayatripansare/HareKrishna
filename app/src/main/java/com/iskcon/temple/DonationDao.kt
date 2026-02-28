package com.iskcon.temple

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface DonationDao {

    @Insert
    suspend fun insertDonation(donation: DonationEntity)

    @Query("SELECT * FROM donations ORDER BY id DESC")
    suspend fun getAllDonations(): List<DonationEntity>

    @Query("DELETE FROM donations")
    suspend fun deleteAllDonations()
}