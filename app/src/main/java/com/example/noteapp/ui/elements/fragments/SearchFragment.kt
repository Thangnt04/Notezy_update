package com.example.noteapp.ui.elements.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.noteapp.R
import com.example.noteapp.databinding.FragmentSearchBinding
import com.example.noteapp.ui.view_models.NoteViewModel
import com.example.noteapp.utils.adapters.NoteAdapter
import com.example.noteapp.utils.response.ResultStatus
import com.google.android.material.snackbar.Snackbar

class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private val noteViewModel: NoteViewModel by activityViewModels()
    private lateinit var noteAdapter: NoteAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Khởi tạo NoteAdapter với callback điều hướng
        noteAdapter = NoteAdapter { note, navController ->
            val action = SearchFragmentDirections.actionSearchFragmentToNoteViewFragment(note)
            navController.navigate(action)
        }

        binding.searchResultsRecyclerView.apply {
            adapter = noteAdapter
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            setHasFixedSize(false)
        }

        // Xử lý nút quay lại
        binding.buttonBackToNoteList.setOnClickListener {
            findNavController().navigate(R.id.action_searchFragment_to_noteListFragment)
        }

        // Xử lý tìm kiếm khi người dùng nhập
        binding.editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                if (query.isNotEmpty()) {
                    noteViewModel.searchNotes(query)
                } else {
                    noteAdapter.submitList(emptyList())
                    binding.textViewNoResults.isVisible = false
                }
            }
        })

        // Quan sát kết quả tìm kiếm
        noteViewModel.searchResultStatus.observe(viewLifecycleOwner) { resultStatus ->
            when (resultStatus) {
                is ResultStatus.Loading -> {
                    binding.progressBarSearch.isVisible = true
                    binding.searchResultsRecyclerView.isVisible = false
                    binding.textViewNoResults.isVisible = false
                    Log.d("SearchFragment", "Searching notes...")
                }
                is ResultStatus.Success -> {
                    binding.progressBarSearch.isVisible = false
                    val notes = resultStatus.data ?: emptyList()
                    noteAdapter.submitList(notes)
                    binding.searchResultsRecyclerView.isVisible = notes.isNotEmpty()
                    binding.textViewNoResults.isVisible = notes.isEmpty()
                    Log.d("SearchFragment", "Found ${notes.size} notes: $notes")
                }
                is ResultStatus.Error -> {
                    binding.progressBarSearch.isVisible = false
                    binding.searchResultsRecyclerView.isVisible = false
                    binding.textViewNoResults.isVisible = true
                    binding.textViewNoResults.text = "Lỗi: ${resultStatus.message}"
                    Snackbar.make(requireView(), resultStatus.message ?: "Lỗi khi tìm kiếm", Snackbar.LENGTH_LONG).show()
                    Log.e("SearchFragment", "Search error: ${resultStatus.message}")
                }
            }
        }
    }
}