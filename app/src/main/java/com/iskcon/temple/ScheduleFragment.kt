package com.iskcon.temple

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class ScheduleFragment : Fragment() {

    private lateinit var rvUpcomingFestivals: RecyclerView
    private lateinit var progressFestivals: ProgressBar
    private lateinit var tvFestivalsEmpty: TextView
    private lateinit var festivalAdapter: UserFestivalAdapter

    private val firestore = FirebaseFirestore.getInstance()
    private val combinedList = mutableListOf<Any>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_schedule, container, false)

        initViews(view)
        setupRecyclerView()
        loadAllFestivalsAndEvents()

        return view
    }

    private fun initViews(view: View) {
        rvUpcomingFestivals = view.findViewById(R.id.rv_upcoming_festivals)
        progressFestivals = view.findViewById(R.id.progress_festivals)
        tvFestivalsEmpty = view.findViewById(R.id.tv_festivals_empty)
    }

    private fun setupRecyclerView() {
        festivalAdapter = UserFestivalAdapter(combinedList)
        rvUpcomingFestivals.adapter = festivalAdapter
        rvUpcomingFestivals.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun loadAllFestivalsAndEvents() {
        progressFestivals.visibility = View.VISIBLE
        combinedList.clear()
        loadFestivals()
    }

    private fun loadFestivals() {
        firestore.collection("festivals")
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    try {
                        val festival = doc.toObject(Festival::class.java)
                        // Only add upcoming festivals (today or future)
                        if (getDaysUntil(festival.date) >= 0) {
                            combinedList.add(festival)
                        }
                    } catch (e: Exception) {
                        // skip invalid
                    }
                }
                // After festivals loaded, load custom events
                loadCustomEvents()
            }
            .addOnFailureListener {
                // Even if festivals fail, try loading events
                loadCustomEvents()
            }
    }

    private fun loadCustomEvents() {
        firestore.collection("custom_events")
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    try {
                        val event = doc.toObject(Event::class.java)
                        // Only add upcoming events (today or future)
                        if (getDaysUntil(event.date) >= 0) {
                            combinedList.add(event)
                        }
                    } catch (e: Exception) {
                        // skip invalid
                    }
                }

                // Sort by nearest date first
                sortCombinedList()

                // Update adapter
                festivalAdapter.updateList(combinedList)

                // Show/hide empty state
                if (combinedList.isEmpty()) {
                    tvFestivalsEmpty.visibility = View.VISIBLE
                } else {
                    tvFestivalsEmpty.visibility = View.GONE
                }

                progressFestivals.visibility = View.GONE
            }
            .addOnFailureListener {
                progressFestivals.visibility = View.GONE
                if (combinedList.isEmpty()) {
                    tvFestivalsEmpty.visibility = View.VISIBLE
                }
            }
    }

    private fun sortCombinedList() {
        combinedList.sortWith(Comparator { a, b ->
            val dateA = when (a) {
                is Festival -> a.date
                is Event -> a.date
                else -> ""
            }
            val dateB = when (b) {
                is Festival -> b.date
                is Event -> b.date
                else -> ""
            }
            compareDates(dateA, dateB)
        })
    }

    private fun compareDates(dateA: String, dateB: String): Int {
        return try {
            val partsA = dateA.split("-")
            val partsB = dateB.split("-")

            val calA = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, partsA[0].toInt())
                set(Calendar.MONTH, partsA[1].toInt() - 1)
                set(Calendar.YEAR, partsA[2].toInt())
            }
            val calB = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, partsB[0].toInt())
                set(Calendar.MONTH, partsB[1].toInt() - 1)
                set(Calendar.YEAR, partsB[2].toInt())
            }
            calA.timeInMillis.compareTo(calB.timeInMillis)
        } catch (e: Exception) {
            0
        }
    }

    private fun getDaysUntil(dateStr: String): Int {
        return try {
            val parts = dateStr.split("-")
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val eventDate = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, parts[0].toInt())
                set(Calendar.MONTH, parts[1].toInt() - 1)
                set(Calendar.YEAR, parts[2].toInt())
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val diff = eventDate.timeInMillis - today.timeInMillis
            (diff / (1000 * 60 * 60 * 24)).toInt()
        } catch (e: Exception) {
            -1
        }
    }
}