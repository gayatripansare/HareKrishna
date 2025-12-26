package com.iskcon.temple

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class GalleryAdapter(
    private var imageList: List<GalleryImage>,
    private val onImageClick: (GalleryImage) -> Unit,
    private val onImageLongClick: ((GalleryImage) -> Unit)? = null  // ✅ Added long click
) : RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder>() {

    inner class GalleryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.iv_gallery_image)
        val titleText: TextView = itemView.findViewById(R.id.tv_image_title)

        fun bind(galleryImage: GalleryImage) {
            // Set title
            titleText.text = galleryImage.title

            // Load image with smart caching strategy
            Glide.with(itemView.context)
                .load(galleryImage.imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .skipMemoryCache(false)
                .placeholder(R.drawable.deity_krishna)
                .error(R.drawable.deity_krishna)
                .centerCrop()
                .into(imageView)

            // ✅ Handle normal click
            itemView.setOnClickListener {
                onImageClick(galleryImage)
            }

            // ✅ Handle long click (for delete)
            itemView.setOnLongClickListener {
                onImageLongClick?.invoke(galleryImage)
                true  // Return true to indicate the event was handled
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gallery_image, parent, false)
        return GalleryViewHolder(view)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        holder.bind(imageList[position])
    }

    override fun getItemCount(): Int = imageList.size

    fun updateImages(newImages: List<GalleryImage>) {
        imageList = newImages
        notifyDataSetChanged()
    }
}