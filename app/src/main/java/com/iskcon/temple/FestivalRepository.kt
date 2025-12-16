package com.iskcon.temple

import java.util.*

object FestivalRepository {

    fun getIskconFestivals(): List<Festival> {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val festivals = mutableListOf<Festival>()

        // Generate festivals for CURRENT YEAR and NEXT YEAR
        festivals.addAll(generateFestivalsForYear(currentYear))
        festivals.addAll(generateFestivalsForYear(currentYear + 1))

        return festivals
    }

    private fun generateFestivalsForYear(year: Int): List<Festival> {
        return listOf(
            // Major Krishna Festivals
            Festival(
                id = "janmashtami_$year",
                name = "Janmashtami",
                description = "Appearance day of Lord Krishna",
                date = "26-08-$year",
                month = 8,
                day = 26,
                isLunarDate = true
            ),
            Festival(
                id = "radhashtami_$year",
                name = "Radhashtami",
                description = "Appearance day of Srimati Radharani",
                date = "03-09-$year",
                month = 9,
                day = 3,
                isLunarDate = true
            ),
            Festival(
                id = "govardhan_puja_$year",
                name = "Govardhan Puja",
                description = "Festival celebrating Krishna lifting Govardhan Hill",
                date = "21-10-$year",
                month = 10,
                day = 21,
                isLunarDate = true
            ),
            Festival(
                id = "gaura_purnima_$year",
                name = "Gaura Purnima",
                description = "Appearance day of Lord Chaitanya Mahaprabhu",
                date = "12-03-$year",
                month = 3,
                day = 12,
                isLunarDate = true
            ),
            Festival(
                id = "ram_navami_$year",
                name = "Ram Navami",
                description = "Appearance day of Lord Rama",
                date = "06-04-$year",
                month = 4,
                day = 6,
                isLunarDate = true
            ),
            Festival(
                id = "nrsimha_chaturdashi_$year",
                name = "Nrsimha Chaturdashi",
                description = "Appearance day of Lord Nrsimhadeva",
                date = "11-05-$year",
                month = 5,
                day = 11,
                isLunarDate = true
            ),

            // Ekadashi Festivals
            Festival(
                id = "utpati_ekadashi_$year",
                name = "Utpatti Ekadashi",
                description = "Happy Utpatti Ekadashi! Hare Krishna!",
                date = "15-11-$year",
                month = 11,
                day = 15,
                isLunarDate = true
            ),
            Festival(
                id = "mokshada_ekadashi_$year",
                name = "Mokshada Ekadashi",
                description = "Happy Mokshada Ekadashi! Hare Krishna!",
                date = "01-12-$year",
                month = 12,
                day = 1,
                isLunarDate = true
            ),
            Festival(
                id = "saphala_ekadashi_$year",
                name = "Saphala Ekadashi",
                description = "Happy Saphala Ekadashi! Hare Krishna!",
                date = "15-12-$year",
                month = 12,
                day = 15,
                isLunarDate = true
            ),
            Festival(
                id = "putrada_ekadashi_$year",
                name = "Putrada Ekadashi",
                description = "Happy Putrada Ekadashi! Hare Krishna!",
                date = "30-12-$year",
                month = 12,
                day = 30,
                isLunarDate = true
            ),

            // Additional Major Festivals
            Festival(
                id = "makar_sankranti_$year",
                name = "Makar Sankranti",
                description = "Auspicious day marking Sun's transition",
                date = "14-01-$year",
                month = 1,
                day = 14,
                isLunarDate = false
            ),
            Festival(
                id = "vasant_panchami_$year",
                name = "Vasant Panchami",
                description = "Advent of Spring season",
                date = "02-02-$year",
                month = 2,
                day = 2,
                isLunarDate = true
            ),
            Festival(
                id = "nityananda_trayodashi_$year",
                name = "Nityananda Trayodashi",
                description = "Appearance of Lord Nityananda",
                date = "10-02-$year",
                month = 2,
                day = 10,
                isLunarDate = true
            ),
            Festival(
                id = "snana_yatra_$year",
                name = "Snana Yatra",
                description = "Bathing ceremony of Lord Jagannath",
                date = "08-06-$year",
                month = 6,
                day = 8,
                isLunarDate = true
            ),
            Festival(
                id = "ratha_yatra_$year",
                name = "Ratha Yatra",
                description = "Festival of Lord Jagannath's Chariot",
                date = "23-06-$year",
                month = 6,
                day = 23,
                isLunarDate = true
            ),
            Festival(
                id = "guru_purnima_$year",
                name = "Guru Purnima (Vyasa Puja)",
                description = "Honoring spiritual masters",
                date = "10-07-$year",
                month = 7,
                day = 10,
                isLunarDate = true
            ),
            Festival(
                id = "balarama_purnima_$year",
                name = "Balarama Purnima",
                description = "Appearance day of Lord Balarama",
                date = "16-08-$year",
                month = 8,
                day = 16,
                isLunarDate = true
            ),
            Festival(
                id = "ganesh_chaturthi_$year",
                name = "Ganesh Chaturthi",
                description = "Appearance of Lord Ganesha",
                date = "27-08-$year",
                month = 8,
                day = 27,
                isLunarDate = true
            ),
            Festival(
                id = "prabhupada_appearance_$year",
                name = "Srila Prabhupada's Appearance",
                description = "Appearance of ISKCON Founder Acharya",
                date = "01-09-$year",
                month = 9,
                day = 1,
                isLunarDate = true
            ),
            Festival(
                id = "kartik_begins_$year",
                name = "Kartik Month Begins",
                description = "Most auspicious month for devotion",
                date = "17-10-$year",
                month = 10,
                day = 17,
                isLunarDate = true
            ),
            Festival(
                id = "diwali_$year",
                name = "Diwali (Deepavali)",
                description = "Festival of Lights",
                date = "20-10-$year",
                month = 10,
                day = 20,
                isLunarDate = true
            ),
            Festival(
                id = "gopastami_$year",
                name = "Gopastami",
                description = "Day Krishna began cow herding",
                date = "28-10-$year",
                month = 10,
                day = 28,
                isLunarDate = true
            ),
            Festival(
                id = "prabhupada_disappearance_$year",
                name = "Srila Prabhupada's Disappearance",
                description = "Disappearance of ISKCON Founder Acharya",
                date = "14-10-$year",
                month = 10,
                day = 14,
                isLunarDate = true
            ),
            Festival(
                id = "kartik_ends_$year",
                name = "Kartik Month Ends",
                description = "End of most sacred month",
                date = "15-11-$year",
                month = 11,
                day = 15,
                isLunarDate = true
            ),
            Festival(
                id = "gita_jayanti_$year",
                name = "Gita Jayanti",
                description = "Appearance day of Bhagavad-gita",
                date = "01-12-$year",
                month = 12,
                day = 1,
                isLunarDate = true
            )
        )
    }
}