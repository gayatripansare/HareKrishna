package com.iskcon.temple

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdminFestivalAdapter(
    private val festivals: List<Festival>,
    private val onEditClick: (Festival) -> Unit
) : RecyclerView.Adapter<AdminFestivalAdapter.FestivalViewHolder>() {

    inner class FestivalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_festival_name)
        val tvDate: TextView = itemView.findViewById(R.id.tv_festival_date)
        val tvFasting: TextView = itemView.findViewById(R.id.tv_festival_fasting)
        val btnEdit: View = itemView.findViewById(R.id.btn_edit)

        fun bind(festival: Festival) {
            tvName.text = festival.name
            tvDate.text = festival.date
            tvFasting.text = if (festival.fasting.isNotEmpty()) "Fasting: ${festival.fasting}" else ""

            btnEdit.setOnClickListener {
                onEditClick(festival)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FestivalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_festival, parent, false)
        return FestivalViewHolder(view)
    }

    override fun onBindViewHolder(holder: FestivalViewHolder, position: Int) {
        holder.bind(festivals[position])
    }

    override fun getItemCount() = festivals.size
}