package com.iskcon.temple

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

class SongAdapter(
    private val songs: List<Song>,
    private val isAdmin: Boolean = false,
    private val onSongClick: (Song) -> Unit,
    private val onDeleteClick: ((Song) -> Unit)? = null
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    inner class SongViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view.findViewById(R.id.card_song)
        val songImage: ImageView = view.findViewById(R.id.img_song)
        val songTitle: TextView = view.findViewById(R.id.txt_song_title)
        val songArtist: TextView = view.findViewById(R.id.txt_song_artist)
        val songDuration: TextView = view.findViewById(R.id.txt_song_duration)
        val categoryBadge: TextView = view.findViewById(R.id.txt_category)
        val btnDelete: ImageButton = view.findViewById(R.id.btn_delete_song)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]

        holder.songTitle.text = song.title
        holder.songArtist.text = if (song.artist.isNotEmpty()) song.artist else "Unknown Artist"
        holder.songDuration.text = if (song.duration.isNotEmpty()) song.duration else "0:00"
        holder.categoryBadge.text = if (song.category.isNotEmpty()) song.category else "Bhajan"

        // Load song image with Glide
        if (song.imageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(song.imageUrl)
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.ic_music_placeholder)
                .error(R.drawable.ic_music_placeholder)
                .centerCrop()
                .into(holder.songImage)
        } else {
            holder.songImage.setImageResource(R.drawable.ic_music_placeholder)
        }

        // Show/hide delete button based on admin status
        if (isAdmin) {
            holder.btnDelete.visibility = View.VISIBLE
            holder.btnDelete.setOnClickListener {
                onDeleteClick?.invoke(song)
            }
        } else {
            holder.btnDelete.visibility = View.GONE
        }

        // Click listener for playing song
        holder.cardView.setOnClickListener {
            onSongClick(song)
        }
    }

    override fun getItemCount(): Int = songs.size
}