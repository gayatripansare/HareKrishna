package com.iskcon.temple

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class VaishnavaSongsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView
    private lateinit var btnAdminUpload: Button
    private lateinit var songAdapter: SongAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private val songs = mutableListOf<Song>()
    private var isAdmin = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_vaishnava_songs, container, false)

        initViews(view)
        checkAdminAccess()
        setupRecyclerView()
        loadSongs()

        return view
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.recycler_songs)
        progressBar = view.findViewById(R.id.progress_bar)
        emptyView = view.findViewById(R.id.empty_view)
        btnAdminUpload = view.findViewById(R.id.btn_admin_upload)

        // Admin upload button click
        btnAdminUpload.setOnClickListener {
            openAdminUpload()
        }
    }

    private fun setupRecyclerView() {
        songAdapter = SongAdapter(
            songs = songs,
            isAdmin = isAdmin,
            onSongClick = { song ->
                openMusicPlayer(song)
            },
            onDeleteClick = { song ->
                showDeleteConfirmation(song)
            }
        )

        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = songAdapter
            setHasFixedSize(true)
        }
    }

    private fun checkAdminAccess() {
        // Check if admin is logged in using SharedPreferences
        val sharedPref = requireActivity().getSharedPreferences("AdminPrefs", Context.MODE_PRIVATE)
        isAdmin = sharedPref.getBoolean("isAdminLoggedIn", false)

        // Show/hide admin button
        btnAdminUpload.visibility = if (isAdmin) View.VISIBLE else View.GONE

        Log.d("VaishnavaSongs", "Admin logged in: $isAdmin")
    }

    private fun openAdminUpload() {
        val intent = Intent(requireContext(), AdminSongsActivity::class.java)
        startActivity(intent)
    }

    private fun loadSongs() {
        progressBar.visibility = View.VISIBLE
        emptyView.visibility = View.GONE

        firestore.collection("vaishnava_songs")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                songs.clear()

                for (doc in documents) {
                    try {
                        val song = doc.toObject(Song::class.java).copy(id = doc.id)
                        songs.add(song)
                    } catch (e: Exception) {
                        Log.e("VaishnavaSongs", "Error parsing song: ${e.message}")
                    }
                }

                progressBar.visibility = View.GONE

                if (songs.isEmpty()) {
                    emptyView.visibility = View.VISIBLE
                } else {
                    songAdapter.notifyDataSetChanged()
                }

                Log.d("VaishnavaSongs", "Loaded ${songs.size} songs")
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                emptyView.visibility = View.VISIBLE
                Toast.makeText(requireContext(), "Failed to load songs: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("VaishnavaSongs", "Error loading songs: ${e.message}")
            }
    }

    private fun showDeleteConfirmation(song: Song) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Song")
            .setMessage("Are you sure you want to delete \"${song.title}\"?")
            .setPositiveButton("Delete") { _, _ ->
                deleteSong(song)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteSong(song: Song) {
        progressBar.visibility = View.VISIBLE

        firestore.collection("vaishnava_songs")
            .document(song.id)
            .delete()
            .addOnSuccessListener {
                Log.d("VaishnavaSongs", "Song deleted: ${song.title}")
                Toast.makeText(requireContext(), "Song deleted successfully", Toast.LENGTH_SHORT).show()

                // Remove from local list
                songs.remove(song)
                songAdapter.notifyDataSetChanged()

                progressBar.visibility = View.GONE

                if (songs.isEmpty()) {
                    emptyView.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Failed to delete: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("VaishnavaSongs", "Delete error: ${e.message}")
            }
    }

    private fun openMusicPlayer(song: Song) {
        val intent = Intent(requireContext(), MusicPlayerActivity::class.java).apply {
            putExtra("SONG_ID", song.id)
            putExtra("SONG_TITLE", song.title)
            putExtra("SONG_ARTIST", song.artist)
            putExtra("SONG_URL", song.cloudinaryUrl)
            putExtra("SONG_IMAGE", song.imageUrl)
            putExtra("SONG_DURATION", song.duration)
        }
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        // Reload songs when coming back from admin upload
        loadSongs()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up if needed
    }
}