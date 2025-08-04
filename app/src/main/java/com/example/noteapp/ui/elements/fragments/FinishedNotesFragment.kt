package com.example.noteapp.ui.elements.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.noteapp.R
import com.example.noteapp.databinding.FragmentNoteFinishedBinding
import com.example.noteapp.databinding.FragmentNoteListBinding
import com.example.noteapp.ui.view_models.NoteViewModel
import com.example.noteapp.utils.adapters.NoteAdapter
import com.example.noteapp.utils.response.ResultStatus
import com.google.firebase.auth.FirebaseAuth

class FinishedNotesFragment : Fragment() {
    private lateinit var binding: FragmentNoteFinishedBinding
    private val noteViewModel: NoteViewModel by activityViewModels()
    private lateinit var noteAdapter: NoteAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNoteFinishedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Khởi tạo NoteAdapter với callback điều hướng
        noteAdapter = NoteAdapter { note, navController ->
            val action = FinishedNotesFragmentDirections.actionFinishedNotesFragmentToNoteViewFragment(note)
            navController.navigate(action)
        }

        binding.notesRecyclerView.apply {
            adapter = noteAdapter
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            setHasFixedSize(false)
        }

        // Tải danh sách ghi chú đã hoàn thành ngay khi vào fragment
        noteViewModel.listNotes(isFinished = true)

        // Xử lý sự kiện click trên tab bar
        binding.apply {
            tabHome.setOnClickListener {
                findNavController().navigate(R.id.action_finishedNotesFragment_to_noteListFragment)
            }

            tabFinished.setOnClickListener {
                noteViewModel.listNotes(isFinished = true)
            }

            iconAdd.setOnClickListener {
                findNavController().navigate(R.id.action_finishedNotesFragment_to_newNoteFragment)
            }

            tabSearch.setOnClickListener{
                findNavController().navigate(R.id.action_finishedNotesFragment_to_searchFragment)
            }

            tabSettings.setOnClickListener {
                findNavController().navigate(R.id.action_finishedNotesFragment_to_settingsFragment)
            }
        }

        // Quan sát danh sách ghi chú
        noteViewModel.notesResultStatus.observe(viewLifecycleOwner) { resultStatus ->
            Log.d("FinishedNotesFragment", "Received status: $resultStatus")
            when (resultStatus) {
                is ResultStatus.Loading -> {
                    binding.progressBarNotesLoading.isVisible = true
                    binding.notesRecyclerView.isVisible = false
                    binding.emptyNote.isVisible = false // Ẩn thông báo khi đang tải
                    Log.d("FinishedNotesFragment", "Loading notes...")
                }
                is ResultStatus.Success -> {
                    binding.progressBarNotesLoading.isVisible = false
                    val notesList = resultStatus.data?.filter { it.Finished }?.toMutableList() ?: mutableListOf()
                    Log.d("FinishedNotesFragment", "Received ${notesList.size} notes: $notesList")
                    noteAdapter.submitList(notesList)
                    binding.apply {
                        notesRecyclerView.isVisible = notesList.isNotEmpty()
                        emptyNote.isVisible = notesList.isEmpty()
                        if (notesList.isEmpty()) {
                            emptyNote.isVisible = true
                        }
                    }
                }
                is ResultStatus.Error -> {
                    binding.progressBarNotesLoading.isVisible = false
                    Log.e("FinishedNotesFragment", "Error loading notes: ${resultStatus.message}")
                    Toast.makeText(requireContext(), resultStatus.message ?: "Lỗi tải ghi chú", Toast.LENGTH_LONG).show()
                    binding.apply {
                        notesRecyclerView.isVisible = false
                        emptyNote.isVisible = true
                    }
                }
            }
        }
    }
}