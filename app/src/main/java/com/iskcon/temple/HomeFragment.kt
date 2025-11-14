package com.iskcon.temple

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        setupQuickAccessCards(view)

        return view
    }

    private fun setupQuickAccessCards(view: View) {
        // Daily Schedule Card
        view.findViewById<CardView>(R.id.card_schedule)?.setOnClickListener {
            navigateToSchedule()
        }

        // Scripture Card (Services)
        view.findViewById<CardView>(R.id.card_services)?.setOnClickListener {
            navigateToServices()
        }

        // Gallery Card
        view.findViewById<CardView>(R.id.card_gallery)?.setOnClickListener {
            navigateToGallery()
        }

        // More Card
        view.findViewById<CardView>(R.id.card_more)?.setOnClickListener {
            navigateToMore()
        }
    }

    private fun navigateToSchedule() {
        val scheduleFragment = ScheduleFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, scheduleFragment)
            .addToBackStack("home")
            .commit()
    }

    private fun navigateToServices() {
        val servicesFragment = ServicesFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, servicesFragment)
            .addToBackStack("home")
            .commit()
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
}