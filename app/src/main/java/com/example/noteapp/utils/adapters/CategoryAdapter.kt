package com.example.noteapp.utils.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp.data.models.Note
import com.example.noteapp.databinding.ItemCategoryBinding

class CategoryAdapter(
    private val onNoteClick: (Note, NavController) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<Pair<String, List<Note>>>() {
        override fun areItemsTheSame(oldItem: Pair<String, List<Note>>, newItem: Pair<String, List<Note>>): Boolean =
            oldItem.first == newItem.first
        override fun areContentsTheSame(oldItem: Pair<String, List<Note>>, newItem: Pair<String, List<Note>>): Boolean =
            oldItem == newItem
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    fun submitList(newList: List<Pair<String, List<Note>>>) {
        differ.submitList(newList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (category, notes) = differ.currentList[position]
        holder.bind(category, notes)
    }

    override fun getItemCount(): Int = differ.currentList.size

    inner class ViewHolder(val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(category: String, notes: List<Note>) {
            binding.textViewCategory.text = category
            val noteAdapter = NoteAdapter(onNoteClick)
            binding.notesRecyclerView.apply {
                adapter = noteAdapter
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                setHasFixedSize(false)
            }
            noteAdapter.submitList(notes)
        }
    }
}