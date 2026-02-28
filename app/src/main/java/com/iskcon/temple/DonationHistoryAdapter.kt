package com.iskcon.temple

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.iskcon.temple.R

class DonationHistoryAdapter(
    private val donationList: List<DonationEntity>
) : RecyclerView.Adapter<DonationHistoryAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvType: TextView = itemView.findViewById(R.id.tvDonationType)
        val tvAmount: TextView = itemView.findViewById(R.id.tvDonationAmount)
        val tvStatus: TextView = itemView.findViewById(R.id.tvDonationStatus)
        val tvDate: TextView = itemView.findViewById(R.id.tvDonationDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_donation_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val donation = donationList[position]

        holder.tvType.text = donation.donationType
        holder.tvAmount.text = "₹ ${donation.amount}"
        holder.tvStatus.text = donation.status
        holder.tvDate.text = donation.dateTime

        // Optional color indication
        if (donation.status == "SUCCESS") {
            holder.tvStatus.setTextColor(holder.itemView.context.getColor(android.R.color.holo_green_dark))
        } else {
            holder.tvStatus.setTextColor(holder.itemView.context.getColor(android.R.color.holo_red_dark))
        }
    }

    override fun getItemCount(): Int = donationList.size
}
