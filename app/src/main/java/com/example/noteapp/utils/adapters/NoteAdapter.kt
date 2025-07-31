//package com.example.noteapp.utils.adapters
//
//import android.text.format.DateFormat
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.navigation.NavController
//import androidx.navigation.findNavController
//import androidx.recyclerview.widget.AsyncListDiffer
//import androidx.recyclerview.widget.DiffUtil
//import androidx.recyclerview.widget.RecyclerView
//import com.example.noteapp.R
//import com.example.noteapp.data.models.Note
//import com.example.noteapp.databinding.ItemNoteBinding
//
//class NoteAdapter(
//    private val onNoteClick: (Note, NavController) -> Unit // Callback để xử lý click
//) : RecyclerView.Adapter<NoteAdapter.ViewHolder>() {
//
//    private val diffCallback = object : DiffUtil.ItemCallback<Note>() {
//        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean = oldItem.id == newItem.id
//        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean = oldItem == newItem
//    }
//
//    private val differ = AsyncListDiffer(this, diffCallback)
//
//    fun submitList(newList: List<Note>) {
//        differ.submitList(newList)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        return ViewHolder(
//            ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        )
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val note = differ.currentList[position]
//        holder.bind(note)
//        holder.binding.cardViewNote.setOnClickListener {
//            onNoteClick(note, holder.itemView.findNavController()) // Gọi callback với note và NavController
//        }
//    }
//
//    override fun getItemCount(): Int = differ.currentList.size
//
//    inner class ViewHolder(val binding: ItemNoteBinding) : RecyclerView.ViewHolder(binding.root) {
//        fun bind(note: Note) {
//            binding.apply {
//                textViewNoteTitle.text = note.title
//                textViewNoteDescription.text = note.description
//
//                if (note.UpdatedNote) {
//                    textViewNoteDate.text = note.dateOfUpdate
//                    textViewNoteTime.text = note.timeOfUpdate
//                    textViewDateLabel.text = itemView.context.getString(R.string.updated_in)
//                } else {
//                    textViewNoteDate.text = note.dateOfCreation
//                    textViewNoteTime.text = note.timeOfCreation
//                    textViewDateLabel.text = itemView.context.getString(R.string.created_in)
//                }
//
//                // Hiển thị trạng thái hoàn thành
//                if (note.Finished) {
//                    textViewNoteTitle.setTextColor(itemView.context.getColor(R.color.green_700))
//                    textViewNoteDescription.setTextColor(itemView.context.getColor(R.color.green_500))
//                } else {
//                    textViewNoteTitle.setTextColor(itemView.context.getColor(R.color.black))
//                    textViewNoteDescription.setTextColor(itemView.context.getColor(R.color.dark_grey))
//                }
//            }
//        }
//    }
//}

package com.example.noteapp.utils.adapters

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp.R
import com.example.noteapp.data.models.Note
import com.example.noteapp.databinding.ItemNoteBinding

class NoteAdapter(
    private val onNoteClick: (Note, NavController) -> Unit
) : RecyclerView.Adapter<NoteAdapter.ViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean = oldItem == newItem
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    fun submitList(newList: List<Note>) {
        differ.submitList(newList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = differ.currentList[position]
        holder.bind(note)
        holder.binding.cardViewNote.setOnClickListener {
            onNoteClick(note, holder.itemView.findNavController())
        }
    }

    override fun getItemCount(): Int = differ.currentList.size

    inner class ViewHolder(val binding: ItemNoteBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(note: Note) {
            binding.apply {
                textViewNoteTitle.text = note.title
                textViewNoteDescription.text = note.description

                if (note.UpdatedNote) {
                    textViewNoteDate.text = note.dateOfUpdate
                    textViewNoteTime.text = note.timeOfUpdate
                    textViewDateLabel.text = itemView.context.getString(R.string.updated_in)
                } else {
                    textViewNoteDate.text = note.dateOfCreation
                    textViewNoteTime.text = note.timeOfCreation
                    textViewDateLabel.text = itemView.context.getString(R.string.created_in)
                }

                if (note.Finished) {
                    textViewNoteTitle.setTextColor(itemView.context.getColor(R.color.black))
                    textViewNoteDescription.setTextColor(itemView.context.getColor(R.color.dark_grey))
                } else {
                    textViewNoteTitle.setTextColor(itemView.context.getColor(R.color.black))
                    textViewNoteDescription.setTextColor(itemView.context.getColor(R.color.dark_grey))
                }
            }
        }
    }
}