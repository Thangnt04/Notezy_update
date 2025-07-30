package com.example.noteapp.ui.elements.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.noteapp.R
import com.example.noteapp.databinding.FragmentNewNoteBinding

class NewNoteFragment : Fragment() {
    private lateinit var binding: FragmentNewNoteBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            // Xử lý nút Back
            backButton.setOnClickListener {
                findNavController().navigate(R.id.action_newNoteFragment_to_noteListFragment)
            }

            // Xử lý sự kiện nhấn vào các CardView
            idea.setOnClickListener {
                val action = NewNoteFragmentDirections.actionNewNoteFragmentToAddIdeaNoteFragment("Interesting Idea")
                findNavController().navigate(action)
            }

            buying.setOnClickListener {
                val action = NewNoteFragmentDirections.actionNewNoteFragmentToAddBuyingNoteFragment("Buying Something")
                findNavController().navigate(action)
            }

            goal.setOnClickListener {
                val action = NewNoteFragmentDirections.actionNewNoteFragmentToAddGoalNoteFragment("Goals")
                findNavController().navigate(action)
            }

            guidance.setOnClickListener {
                val action = NewNoteFragmentDirections.actionNewNoteFragmentToAddGuidanceNoteFragment("Guidance")
                findNavController().navigate(action)
            }

            task.setOnClickListener {
                val action = NewNoteFragmentDirections.actionNewNoteFragmentToAddTaskNoteFragment("Task")
                findNavController().navigate(action)
            }
        }
    }
}