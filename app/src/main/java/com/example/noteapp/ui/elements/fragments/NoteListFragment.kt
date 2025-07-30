package com.example.noteapp.ui.elements.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.noteapp.R
import com.example.noteapp.databinding.FragmentNoteListBinding
import com.example.noteapp.ui.view_models.NoteViewModel
import com.example.noteapp.utils.adapters.CategoryAdapter
import com.example.noteapp.utils.response.ResultStatus

class NoteListFragment : Fragment() {
    private var _binding: FragmentNoteListBinding? = null
    private val binding get() = _binding!!
    private val noteViewModel: NoteViewModel by activityViewModels()
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoteListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Khởi tạo CategoryAdapter
        categoryAdapter = CategoryAdapter { note, navController ->
            val action = NoteListFragmentDirections.actionNoteListFragmentToNoteViewFragment(note)
            navController.navigate(action)
        }

        binding.categoryRecyclerView.apply {
            adapter = categoryAdapter
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(false)
        }

        // Tải danh sách ghi chú chưa hoàn thành
        noteViewModel.listNotes(isFinished = false)

        // Xử lý sự kiện click trên tab bar
        binding.apply {
            tabHome.setOnClickListener {
                noteViewModel.listNotes(isFinished = false)
            }

            tabFinished.setOnClickListener {
                findNavController().navigate(R.id.action_noteListFragment_to_finishedNotesFragment)
            }

            iconAdd.setOnClickListener {
                findNavController().navigate(R.id.action_noteListFragment_to_newNoteFragment)
            }

            tabSearch.setOnClickListener {
                findNavController().navigate(R.id.action_noteListFragment_to_searchFragment)
            }

            tabSettings.setOnClickListener {
                findNavController().navigate(R.id.action_noteListFragment_to_settingsFragment)
            }


        }

        // Quan sát danh sách ghi chú
        noteViewModel.notesResultStatus.observe(viewLifecycleOwner) { resultStatus ->
            when (resultStatus) {
                is ResultStatus.Loading -> {
                    binding.progressBarNotesLoading.isVisible = true
                    binding.categoryRecyclerView.isVisible = false
                    binding.textViewNotingToShow.isVisible = false
                    Log.d("NoteListFragment", "Loading notes...")
                }
                is ResultStatus.Success -> {
                    binding.progressBarNotesLoading.isVisible = false
                    val notesList = resultStatus.data?.filter { !it.Finished }?.toMutableList() ?: mutableListOf()
                    Log.d("NoteListFragment", "Raw data from ViewModel: ${resultStatus.data}")
                    Log.d("NoteListFragment", "Received ${notesList.size} notes: $notesList")
                    val categories = listOf(
                        "Interesting Idea", "Buying Something", "Goals", "Guidance", "Task", "Uncategorized"
                    )
                    val categoryMap = categories.map { category ->
                        category to notesList.filter {
                            it.category == category || (category == "Uncategorized" && it.category == null)
                        }
                    }.filter { it.second.isNotEmpty() }
                    Log.d("NoteListFragment", "Category map: $categoryMap")
                    categoryAdapter.submitList(categoryMap)
                    binding.apply {
                        categoryRecyclerView.isVisible = categoryMap.isNotEmpty()
                        textViewNotingToShow.isVisible = categoryMap.isEmpty()
                        if (categoryMap.isEmpty()) {
                            textViewNotingToShow.text = "Không có ghi chú"
                        }
                    }
                }
                is ResultStatus.Error -> {
                    binding.progressBarNotesLoading.isVisible = false
                    Log.e("NoteListFragment", "Error loading notes: ${resultStatus.message}")
                    Toast.makeText(requireContext(), resultStatus.message ?: "Lỗi tải ghi chú", Toast.LENGTH_LONG).show()
                    binding.apply {
                        categoryRecyclerView.isVisible = false
                        textViewNotingToShow.isVisible = true
                        textViewNotingToShow.text = "Lỗi tải ghi chú: ${resultStatus.message}"
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}