package com.iskcon.temple

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class UserFestivalAdapter(
    private var festivalList: MutableList<Any> // Contains both Festival and Event objects
) : RecyclerView.Adapter<UserFestivalAdapter.FestivalViewHolder>() {

    private val monthNames = arrayOf(
        "JAN", "FEB", "MAR", "APR", "MAY", "JUN",
        "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"
    )

    inner class FestivalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDay: TextView = itemView.findViewById(R.id.tv_festival_day)
        val tvMonth: TextView = itemView.findViewById(R.id.tv_festival_month)
        val tvName: TextView = itemView.findViewById(R.id.tv_festival_name)
        val tvDescription: TextView = itemView.findViewById(R.id.tv_festival_description)
        val tvFasting: TextView = itemView.findViewById(R.id.tv_festival_fasting)
        val tvBadge: TextView = itemView.findViewById(R.id.tv_festival_badge)

        fun bind(item: Any) {
            when (item) {
                is Festival -> bindFestival(item)
                is Event -> bindEvent(item)
            }
        }

        private fun bindFestival(festival: Festival) {
            val parts = festival.date.split("-")
            val day = parts[0].toIntOrNull() ?: 0
            val month = parts[1].toIntOrNull() ?: 0

            tvDay.text = day.toString()
            tvMonth.text = if (month in 1..12) monthNames[month - 1] else ""
            tvName.text = festival.name
            tvDescription.text = festival.description

            // Show fasting if available
            if (festival.fasting.isNotEmpty()) {
                tvFasting.text = "🙏 Fasting: ${festival.fasting}"
                tvFasting.visibility = View.VISIBLE
            } else {
                tvFasting.visibility = View.GONE
            }

            showBadge(festival.date)
        }

        private fun bindEvent(event: Event) {
            val parts = event.date.split("-")
            val day = parts[0].toIntOrNull() ?: 0
            val month = parts[1].toIntOrNull() ?: 0

            tvDay.text = day.toString()
            tvMonth.text = if (month in 1..12) monthNames[month - 1] else ""
            tvName.text = event.name
            tvDescription.text = event.description
            tvFasting.visibility = View.GONE

            showBadge(event.date)
        }

        private fun showBadge(dateStr: String) {
            val daysUntil = getDaysUntil(dateStr)
            when (daysUntil) {
                0 -> {
                    tvBadge.text = "🎉 Today"
                    tvBadge.setBackgroundResource(R.drawable.badge_today)
                    tvBadge.visibility = View.VISIBLE
                }
                1 -> {
                    tvBadge.text = "Tomorrow"
                    tvBadge.setBackgroundResource(R.drawable.badge_tomorrow)
                    tvBadge.visibility = View.VISIBLE
                }
                else -> {
                    tvBadge.visibility = View.GONE
                }
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FestivalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_festival, parent, false)
        return FestivalViewHolder(view)
    }

    override fun onBindViewHolder(holder: FestivalViewHolder, position: Int) {
        holder.bind(festivalList[position])
    }

    override fun getItemCount() = festivalList.size

    fun updateList(newList: MutableList<Any>) {
        festivalList = newList
        notifyDataSetChanged()
    }
}