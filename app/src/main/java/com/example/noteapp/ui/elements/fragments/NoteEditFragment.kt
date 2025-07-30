package com.example.noteapp.ui.elements.fragments

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.noteapp.R
import com.example.noteapp.data.models.Note
import com.example.noteapp.databinding.FragmentNoteEditBinding
import com.example.noteapp.ui.view_models.NoteViewModel
import com.example.noteapp.utils.response.ResultStatus
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*

class NoteEditFragment : Fragment() {
    private val args: NoteEditFragmentArgs by navArgs()
    private lateinit var note: Note
    private lateinit var binding: FragmentNoteEditBinding
    private val noteViewModel: NoteViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        note = args.note
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNoteEditBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindNoteData()

        binding.apply {
            buttonSave.setOnClickListener {
                val updatedTitle = editTextNoteTitle.text.toString()
                val updatedDescription = editTextNoteDescription.text.toString()

                if (updatedTitle.isEmpty()) {
                    showSnackBar(getString(R.string.title_cant_be_empty))
                    return@setOnClickListener
                }

                freezeUiActions()

                note = note.copy(
                    title = updatedTitle,
                    description = updatedDescription,
                    dateOfUpdate = getCurrentDate(),
                    timeOfUpdate = getCurrentTime(),
                    UpdatedNote = true
                )

                noteViewModel.updateNote(requireContext(), note)
            }

            imageButtonBackToNoteView.setOnClickListener {
                navigateToNoteListFragment() // Quay về NoteListFragment thay vì NoteViewFragment
            }
        }

        // Quan sát kết quả cập nhật
        noteViewModel.noteUpdateResultStatus.observe(viewLifecycleOwner) { resultStatus ->
            when (resultStatus) {
                is ResultStatus.Loading -> {
                    Log.d("NoteEditFragment", "Updating note...")
                }
                is ResultStatus.Success -> {
                    unfreezeUiActions()
                    showSnackBar(getString(R.string.note_success_update_message))
                    navigateToNoteListFragment()
                    noteViewModel.resetNoteStatus()
                }
                is ResultStatus.Error -> {
                    unfreezeUiActions()
                    showSnackBar(resultStatus.message ?: "Lỗi khi cập nhật ghi chú")
                }
                null -> {
                    Log.d("NoteEditFragment", "Received null status, ignoring")
                    // Không làm gì khi nhận null, vì đây là trạng thái sau reset
                }
            }
        }
    }

    private fun freezeUiActions() {
        binding.apply {
            imageButtonBackToNoteView.isClickable = false
            buttonSave.isClickable = false
        }
    }

    private fun unfreezeUiActions() {
        binding.apply {
            imageButtonBackToNoteView.isClickable = true
            buttonSave.isClickable = true
        }
    }

    private fun navigateToNoteListFragment() {
        val navAction = NoteEditFragmentDirections.actionNoteEditFragmentToNoteListFragment()
        findNavController().navigate(navAction)
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
    }

    private fun bindNoteData() {
        binding.apply {
            editTextNoteTitle.setText(note.title)
            editTextNoteDescription.setText(note.description)

            if (note.reminderTime != null) {
                val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
                val reminderDate = Date(note.reminderTime!!)
                textViewReminder.text = sdf.format(reminderDate)
            } else {
                textViewReminder.text = "Chưa đặt"
            }
        }
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = note.reminderTime ?: System.currentTimeMillis()
        }
        val currentTime = System.currentTimeMillis()

        DatePickerDialog(requireContext(), { _, year, month, day ->
            TimePickerDialog(requireContext(), { _, hour, minute ->
                calendar.set(year, month, day, hour, minute, 0)
                val selectedTime = calendar.timeInMillis
                if (selectedTime <= currentTime) {
                    showSnackBar("Thời gian nhắc nhở phải sau thời gian hiện tại!")
                    note.reminderTime = null
                    bindNoteData()
                } else {
                    note.reminderTime = selectedTime
                    val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
                    binding.textViewReminder.text = sdf.format(Date(selectedTime))
                }
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun getCurrentTime(): String {
        val tz = TimeZone.getTimeZone(getString(R.string.vietnam_time_zone))
        val c = Calendar.getInstance(tz)
        val hours = String.format("%02d", c.get(Calendar.HOUR_OF_DAY))
        val minutes = String.format("%02d", c.get(Calendar.MINUTE))
        return "$hours:$minutes"
    }

    @SuppressLint("SimpleDateFormat")
    private fun getCurrentDate(): String {
        val currentDateObject = Date()
        val formatter = SimpleDateFormat("dd-MM-yyyy")
        return formatter.format(currentDateObject)
    }
}