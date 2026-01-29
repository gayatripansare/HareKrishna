package com.iskcon.temple

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton

open class BaseActivity : AppCompatActivity() {

    private lateinit var donationFab: FloatingActionButton

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        addDonationFab()
    }

    private fun addDonationFab() {
        donationFab = FloatingActionButton(this).apply {
            // Use heart icon for donation
            setImageResource(R.drawable.ic_donate)

            // Beautiful orange/saffron color
            backgroundTintList = ContextCompat.getColorStateList(context, R.color.bhagwa_saffron)
            imageTintList = ContextCompat.getColorStateList(context, android.R.color.white)

            // Normal size
            size = FloatingActionButton.SIZE_NORMAL

            // Add ripple effect
            isClickable = true
            isFocusable = true

            // Elevation for shadow
            compatElevation = 6f

            // Click listener
            setOnClickListener {
                openDonationActivity()
            }

            // Content description for accessibility
            contentDescription = "Donate"
        }

        // Position at bottom-right with proper margins
        val rootView = window.decorView.findViewById<ViewGroup>(android.R.id.content)
        val fabContainer = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        val fabParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = android.view.Gravity.BOTTOM or android.view.Gravity.END
            // To move the button up, we increase the bottom margin.
            // To move it left, we increase the right margin.
            val rightMarginInPx = (30 * resources.displayMetrics.density).toInt() // 24dp to shift left
            val bottomMarginInPx = (90 * resources.displayMetrics.density).toInt() // 32dp to shift up
            setMargins(0, 0, rightMarginInPx, bottomMarginInPx)
        }

        fabContainer.addView(donationFab, fabParams)
        rootView.addView(fabContainer)
    }

    private fun openDonationActivity() {
        val intent = Intent(this, DonateActivity::class.java)
        startActivity(intent)
    }

    // Method to hide donation FAB (for specific screens if needed)
    protected fun hideDonationFab() {
        if (::donationFab.isInitialized) {
            donationFab.hide()
        }
    }

    // Method to show donation FAB
    protected fun showDonationFab() {
        if (::donationFab.isInitialized) {
            donationFab.show()
        }
    }
}