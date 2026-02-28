package com.iskcon.temple

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import android.content.Intent


class HomeFragment : Fragment() {

    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var imageSlider: ViewPager2
    private lateinit var sliderHandler: Handler
    private lateinit var sliderRunnable: Runnable
    private var currentPage = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        setupImageSlider(view)
        setupQuickAccessCards(view)
        loadTodaysEvents(view)

        // ✅ SOLUTION: Hide donation FAB on home screen
        hideDonationFabOnHome()

        return view
    }

    // ✅ NEW: Hide donation FAB when home screen is shown
    private fun hideDonationFabOnHome() {
        try {
            // MainActivity2 doesn't extend BaseActivity, so this won't work
            // But if parent activity extends BaseActivity, hide it
            if (activity is BaseActivity) {
                (activity as BaseActivity).hideDonationFab()
            }
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error hiding FAB: ${e.message}")
        }
    }

    private fun setupImageSlider(view: View) {
        imageSlider = view.findViewById(R.id.image_slider)

        val images = listOf(
            R.drawable.scroll1,
            R.drawable.scroll2,
            R.drawable.scroll3,
            R.drawable.scroll4,
            R.drawable.scroll5
        )

        // Create a very large list to simulate infinite scrolling
        val largeImageList = mutableListOf<Int>()
        for (i in 0 until 1000) {
            largeImageList.addAll(images)
        }

        val adapter = ImageSliderAdapter(largeImageList)
        imageSlider.adapter = adapter
        imageSlider.offscreenPageLimit = 1

        // Start in the middle to allow scrolling both ways
        val startPosition = 500 * images.size
        imageSlider.setCurrentItem(startPosition, false)
        currentPage = startPosition

        imageSlider.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currentPage = position
            }
        })

        // Auto-scroll with faster speed
        sliderHandler = Handler(Looper.getMainLooper())
        sliderRunnable = object : Runnable {
            override fun run() {
                imageSlider.setCurrentItem(currentPage + 1, true)
                sliderHandler.postDelayed(this, 2500) // 2.5 seconds
            }
        }
    }

    override fun onResume() {
        super.onResume()
        sliderHandler.postDelayed(sliderRunnable, 2500)

        // ✅ Hide FAB again when returning to home
        hideDonationFabOnHome()
    }

    override fun onPause() {
        super.onPause()
        sliderHandler.removeCallbacks(sliderRunnable)

        // ✅ Show FAB when leaving home screen
        try {
            if (activity is BaseActivity) {
                (activity as BaseActivity).showDonationFab()
            }
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error showing FAB: ${e.message}")
        }
    }

    private fun setupQuickAccessCards(view: View) {
        view.findViewById<CardView>(R.id.card_schedule)?.setOnClickListener {
            navigateToVaishnavaSongs()
        }

        // ✅ FIXED: Navigate to DonateActivity instead of DonationFragment
        view.findViewById<CardView>(R.id.card_donation)?.setOnClickListener {
            navigateToDonation()
        }

        view.findViewById<CardView>(R.id.card_gallery)?.setOnClickListener {
            navigateToGallery()
        }

        val cardChanting = view.findViewById<CardView>(R.id.card_chanting)
        cardChanting?.setOnClickListener {
            val intent = Intent(requireContext(), JapaActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadTodaysEvents(view: View) {
        val layoutEventsContainer = view.findViewById<LinearLayout>(R.id.layout_events_container) ?: return

        Log.d("HomeFragment", "🔍 Loading today's events...")

        val today = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        Log.d("HomeFragment", "📅 Today's date: $today")

        val allEvents = mutableListOf<EventItem>()

        firestore.collection("festivals")
            .whereEqualTo("date", today)
            .get()
            .addOnSuccessListener { festivalsSnapshot ->
                Log.d("HomeFragment", "✅ Found ${festivalsSnapshot.size()} festivals")

                festivalsSnapshot.forEach { doc ->
                    try {
                        val festival = doc.toObject(Festival::class.java)
                        allEvents.add(EventItem(festival.name, festival.description, festival.fasting))
                    } catch (e: Exception) {
                        Log.e("HomeFragment", "Error: ${e.message}")
                    }
                }

                loadCustomEvents(today, allEvents, layoutEventsContainer)
            }
            .addOnFailureListener { e ->
                Log.e("HomeFragment", "Error: ${e.message}")
                loadCustomEvents(today, allEvents, layoutEventsContainer)
            }
    }

    private fun loadCustomEvents(today: String, allEvents: MutableList<EventItem>, container: LinearLayout) {
        firestore.collection("custom_events")
            .whereEqualTo("date", today)
            .get()
            .addOnSuccessListener { eventsSnapshot ->
                Log.d("HomeFragment", "✅ Found ${eventsSnapshot.size()} custom events")

                eventsSnapshot.forEach { doc ->
                    try {
                        val event = doc.toObject(Event::class.java)
                        allEvents.add(EventItem(event.name, event.description, ""))
                    } catch (e: Exception) {
                        Log.e("HomeFragment", "Error: ${e.message}")
                    }
                }

                displayEvents(allEvents, container)
            }
            .addOnFailureListener { e ->
                Log.e("HomeFragment", "Error: ${e.message}")
                displayEvents(allEvents, container)
            }
    }

    private fun displayEvents(events: List<EventItem>, container: LinearLayout) {
        if (!isAdded || context == null) {
            Log.d("HomeFragment", "Fragment not attached, skipping display")
            return
        }

        container.removeAllViews()

        if (events.isEmpty()) {
            Log.d("HomeFragment", "No events today")
            return
        }

        Log.d("HomeFragment", "🎉 Displaying ${events.size} events")

        events.forEach { event ->
            val eventLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(32, 24, 32, 24)
                setBackgroundColor(android.graphics.Color.parseColor("#FFD700"))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 8, 0, 8)
                }
            }

            val titleLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            val starIcon = TextView(requireContext()).apply {
                text = "⭐ "
                textSize = 20f
                setTextColor(android.graphics.Color.parseColor("#FF6200"))
            }

            val nameText = TextView(requireContext()).apply {
                text = event.name
                textSize = 17f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(android.graphics.Color.parseColor("#000000"))
            }

            titleLayout.addView(starIcon)
            titleLayout.addView(nameText)
            eventLayout.addView(titleLayout)

            val descText = TextView(requireContext()).apply {
                text = event.description
                textSize = 15f
                setTextColor(android.graphics.Color.parseColor("#333333"))
                setPadding(0, 8, 0, 0)
            }
            eventLayout.addView(descText)

            if (event.fasting.isNotEmpty() && event.fasting != "—-") {
                val fastingText = TextView(requireContext()).apply {
                    text = "Fasting: ${event.fasting}"
                    textSize = 13f
                    setTextColor(android.graphics.Color.parseColor("#FF6200"))
                    setTypeface(null, android.graphics.Typeface.ITALIC)
                    setPadding(0, 8, 0, 0)
                }
                eventLayout.addView(fastingText)
            }

            container.addView(eventLayout)
        }
    }

    private fun navigateToVaishnavaSongs() {
        val vaishnavaSongsFragment = VaishnavaSongsFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, vaishnavaSongsFragment)
            .addToBackStack("home")
            .commit()
    }

    private fun navigateToDonation() {
        val intent = Intent(requireContext(), DonateActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToGallery() {
        val galleryFragment = GalleryFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, galleryFragment)
            .addToBackStack("home")
            .commit()
    }

    private fun navigateToMore() {
        val moreFragment = MoreFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, moreFragment)
            .addToBackStack("home")
            .commit()
    }

    data class EventItem(
        val name: String,
        val description: String,
        val fasting: String
    )

    inner class ImageSliderAdapter(private val images: List<Int>) :
        RecyclerView.Adapter<ImageSliderAdapter.ImageViewHolder>() {

        inner class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val imageView: ImageView = view.findViewById(R.id.slider_image)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_slider_image, parent, false)
            return ImageViewHolder(view)
        }

        override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
            holder.imageView.setImageResource(images[position])
        }

        override fun getItemCount(): Int = images.size
    }
}