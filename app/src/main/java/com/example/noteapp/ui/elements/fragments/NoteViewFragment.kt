//package com.example.noteapp.ui.elements.fragments
//
//import android.app.DatePickerDialog
//import android.app.TimePickerDialog
//import android.os.Bundle
//import android.text.format.DateFormat
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.core.view.isVisible
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.activityViewModels
//import androidx.navigation.fragment.findNavController
//import androidx.navigation.fragment.navArgs
//import com.example.noteapp.R
//import com.example.noteapp.data.models.Note
//import com.example.noteapp.databinding.FragmentNoteViewBinding
//import com.example.noteapp.databinding.BottomsheetLayoutBinding
//import com.example.noteapp.ui.view_models.NoteViewModel
//import com.example.noteapp.utils.response.EmptyResult
//import com.example.noteapp.utils.response.ResultStatus
//import com.google.android.material.bottomsheet.BottomSheetDialog
//import com.google.android.material.snackbar.Snackbar
//import java.text.SimpleDateFormat
//import java.util.*
//
//class NoteViewFragment : Fragment() {
//    private val args: NoteViewFragmentArgs by navArgs()
//    private lateinit var note: Note
//    private lateinit var binding: FragmentNoteViewBinding
//    private val noteViewModel: NoteViewModel by activityViewModels()
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        note = args.note
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        binding = FragmentNoteViewBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        bindNoteData(note)
//
//        binding.apply {
//            backButton.setOnClickListener {
//                navigateToNoteList()
//            }
//
//            moreButton.setOnClickListener {
//                showBottomSheetMenu()
//            }
//        }
//
//        // Quan sát trạng thái xóa ghi chú
//        noteViewModel.noteDeleteStatus.observe(viewLifecycleOwner) { result ->
//            when (result) {
//                is EmptyResult.Success -> {
//                    showSnackBar(getString(R.string.note_deleted_successfully))
//                    noteViewModel.listNotes()
//                    navigateToNoteList()
//                }
//                is EmptyResult.Error -> {
//                    showSnackBar(result.message ?: "Lỗi khi xóa ghi chú")
//                }
//                null -> {
//                    Log.d("NoteViewFragment", "noteDeleteStatus is null")
//                    binding.progressBarNoteDeletion.isVisible = false
//                }
//            }
//            noteViewModel.resetNoteDeleteStatus()
//        }
//
//        // Quan sát trạng thái cập nhật ghi chú
//        noteViewModel.noteUpdateResultStatus.observe(viewLifecycleOwner) { resultStatus ->
//            when (resultStatus) {
//                is ResultStatus.Loading -> {
//                    binding.progressBarNoteDeletion.isVisible = true
//                    binding.backButton.isEnabled = false
//                    binding.moreButton.isEnabled = false
//                    Log.d("NoteViewFragment", "Đang cập nhật ghi chú...")
//                }
//                is ResultStatus.Success -> {
//                    binding.progressBarNoteDeletion.isVisible = false
//                    binding.backButton.isEnabled = true
//                    binding.moreButton.isEnabled = true
//                    showSnackBar(getString(R.string.note_success_update_message))
//                    note = resultStatus.data!! // Cập nhật note với dữ liệu mới
//                    bindNoteData(note)
//                }
//                is ResultStatus.Error -> {
//                    binding.progressBarNoteDeletion.isVisible = false
//                    binding.backButton.isEnabled = true
//                    binding.moreButton.isEnabled = true
//                    showSnackBar(resultStatus.message ?: "Lỗi khi cập nhật ghi chú")
//                }
//                null -> {
//                    Log.d("NoteViewFragment", "noteUpdateResultStatus is null")
//                    binding.progressBarNoteDeletion.isVisible = false
//                }
//            }
//        }
//    }
//
//    private fun showBottomSheetMenu() {
//        val dialogBinding = BottomsheetLayoutBinding.inflate(LayoutInflater.from(requireContext()))
//        val bottomSheetDialog = BottomSheetDialog(requireContext())
//        bottomSheetDialog.setContentView(dialogBinding.root)
//
//        dialogBinding.apply {
//            // Cập nhật trạng thái nhắc nhở
//            reminderStatus.text = if (note.reminderTime != null) {
//                SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date(note.reminderTime!!))
//            } else {
//                "Chưa đặt"
//            }
//
//            setReminder.setOnClickListener {
//                showDateTimePicker()
//                bottomSheetDialog.dismiss()
//            }
//
//            editNote.setOnClickListener {
//                navigateToNoteEditFragment()
//                bottomSheetDialog.dismiss()
//            }
//
//            markFinished.setOnClickListener {
//                note = note.copy(Finished = true)
//                noteViewModel.updateNote(requireContext(), note)
//                bottomSheetDialog.dismiss()
//            }
//
//            delete.setOnClickListener {
//                note.id?.let { noteId ->
//                    binding.progressBarNoteDeletion.isVisible = true
//                    noteViewModel.deleteNote(noteId)
//                } ?: showSnackBar("Không thể xóa. Ghi chú không hợp lệ.")
//                bottomSheetDialog.dismiss()
//            }
//
//            closeButton.setOnClickListener {
//                bottomSheetDialog.dismiss()
//            }
//        }
//
//        bottomSheetDialog.show()
//    }
//
//    private fun showDateTimePicker() {
//        val calendar = Calendar.getInstance().apply {
//            timeInMillis = note.reminderTime ?: System.currentTimeMillis()
//        }
//        val currentTime = System.currentTimeMillis()
//
//        DatePickerDialog(requireContext(), { _, year, month, day ->
//            TimePickerDialog(requireContext(), { _, hour, minute ->
//                calendar.set(year, month, day, hour, minute, 0)
//                val selectedTime = calendar.timeInMillis
//                if (selectedTime <= currentTime) {
//                    showSnackBar("Thời gian nhắc nhở phải sau thời gian hiện tại!")
//                } else {
//                    note = note.copy(reminderTime = selectedTime)
//                    noteViewModel.updateNote(requireContext(), note)
//                }
//            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
//        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
//    }
//
//    private fun bindNoteData(note: Note) {
//        binding.apply {
//            textViewNoteTitle.text = note.title
//            textViewNoteDescription.text = note.description
//
//            if (note.UpdatedNote) {
//                textViewNoteDate.text = note.dateOfUpdate
//                textViewDateLabel.text = getString(R.string.updated_in)
//            } else {
//                textViewNoteDate.text = note.dateOfCreation
//                textViewDateLabel.text = getString(R.string.created_in)
//            }
//
//            if (note.reminderTime != null) {
//                val calendar = Calendar.getInstance().apply { timeInMillis = note.reminderTime!! }
//                val date = DateFormat.format("dd/MM/yyyy", calendar).toString()
//
//                val time = DateFormat.format("HH:mm", calendar).toString()
//                textViewReminderDate.text = date
//                textViewReminderTime.text = time
//                textViewReminderLabel.isVisible = true
//                textViewReminderDate.isVisible = true
//                textViewReminderTime.isVisible = true
//            } else {
//                textViewReminderLabel.isVisible = false
//                textViewReminderDate.isVisible = false
//                textViewReminderTime.isVisible = false
//            }
//        }
//    }
//
//    private fun navigateToNoteList() {
//        val navAction = NoteViewFragmentDirections.actionNoteViewFragmentToNoteListFragment()
//        findNavController().navigate(navAction)
//    }
//
//    private fun navigateToNoteEditFragment() {
//        try {
//            val action = NoteViewFragmentDirections.actionNoteViewFragmentToNoteEditFragment(note)
//            if (note.id.isNullOrEmpty()) {
//                Log.e("NoteViewFragment", "ID ghi chú không hợp lệ: $note")
//                showSnackBar("Không thể chỉnh sửa. Ghi chú không hợp lệ.")
//            } else {
//                findNavController().navigate(action)
//            }
//        } catch (e: Exception) {
//            Log.e("NoteViewFragment", "Lỗi điều hướng: ${e.message}")
//            showSnackBar("Lỗi khi chuyển đến chỉnh sửa ghi chú")
//        }
//    }
//
//    private fun showSnackBar(message: String) {
//        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
//    }
//}

package com.example.noteapp.ui.elements.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.noteapp.R
import com.example.noteapp.data.models.Note
import com.example.noteapp.databinding.FragmentNoteViewBinding
import com.example.noteapp.databinding.BottomsheetLayoutBinding
import com.example.noteapp.ui.view_models.NoteViewModel
import com.example.noteapp.utils.response.EmptyResult
import com.example.noteapp.utils.response.ResultStatus
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*

class NoteViewFragment : Fragment() {
    private val args: NoteViewFragmentArgs by navArgs()
    private lateinit var note: Note
    private lateinit var binding: FragmentNoteViewBinding
    private val noteViewModel: NoteViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        note = args.note
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNoteViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindNoteData(note)

        binding.apply {
            backButton.setOnClickListener {
                navigateToNoteList()
            }

            moreButton.setOnClickListener {
                showBottomSheetMenu()
            }
        }

        noteViewModel.noteDeleteStatus.observe(viewLifecycleOwner) { result ->
            when (result) {
                is EmptyResult.Success -> {
                    showSnackBar(getString(R.string.note_deleted_successfully))
                    noteViewModel.listNotes()
                    navigateToNoteList()
                }
                is EmptyResult.Error -> {
                    showSnackBar(result.message ?: "Lỗi khi xóa ghi chú")
                }
                null -> {
                    Log.d("NoteViewFragment", "noteDeleteStatus is null")
                    binding.progressBarNoteDeletion.isVisible = false
                }
            }
            noteViewModel.resetNoteDeleteStatus()
        }

        noteViewModel.noteUpdateResultStatus.observe(viewLifecycleOwner) { resultStatus ->
            when (resultStatus) {
                is ResultStatus.Loading -> {
                    binding.progressBarNoteDeletion.isVisible = true
                    binding.backButton.isEnabled = false
                    binding.moreButton.isEnabled = false
                    Log.d("NoteViewFragment", "Đang cập nhật ghi chú...")
                }
                is ResultStatus.Success -> {
                    binding.progressBarNoteDeletion.isVisible = false
                    binding.backButton.isEnabled = true
                    binding.moreButton.isEnabled = true
                    showSnackBar(getString(R.string.note_success_update_message))
                    note = resultStatus.data!!
                    bindNoteData(note)
                }
                is ResultStatus.Error -> {
                    binding.progressBarNoteDeletion.isVisible = false
                    binding.backButton.isEnabled = true
                    binding.moreButton.isEnabled = true
                    showSnackBar(resultStatus.message ?: "Lỗi khi cập nhật ghi chú")
                }
                null -> {
                    Log.d("NoteViewFragment", "noteUpdateResultStatus is null")
                    binding.progressBarNoteDeletion.isVisible = false
                }
            }
        }
    }

    private fun showBottomSheetMenu() {
        val dialogBinding = BottomsheetLayoutBinding.inflate(LayoutInflater.from(requireContext()))
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(dialogBinding.root)

        dialogBinding.apply {
            reminderStatus.text = if (note.reminderTime != null) {
                SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date(note.reminderTime!!))
            } else {
                "Not set"
            }

            setReminder.setOnClickListener {
                showDateTimePicker()
                bottomSheetDialog.dismiss()
            }

            editNote.setOnClickListener {
                navigateToNoteEditFragment()
                bottomSheetDialog.dismiss()
            }

            markFinished.setOnClickListener {
                note = note.copy(Finished = true)
                noteViewModel.updateNote(requireContext(), note)
                bottomSheetDialog.dismiss()
            }

            delete.setOnClickListener {
                note.id?.let { noteId ->
                    binding.progressBarNoteDeletion.isVisible = true
                    noteViewModel.deleteNote(noteId)
                } ?: showSnackBar("Không thể xóa. Ghi chú không hợp lệ.")
                bottomSheetDialog.dismiss()
            }

            closeButton.setOnClickListener {
                bottomSheetDialog.dismiss()
            }
        }

        bottomSheetDialog.show()
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
                } else {
                    note = note.copy(reminderTime = selectedTime)
                    noteViewModel.updateNote(requireContext(), note)
                }
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun bindNoteData(note: Note) {
        binding.apply {
            textViewNoteTitle.text = note.title
            textViewNoteDescription.text = note.description

            if (note.UpdatedNote) {
                textViewNoteDate.text = note.dateOfUpdate
                textViewDateLabel.text = getString(R.string.updated_in)
            } else {
                textViewNoteDate.text = note.dateOfCreation
                textViewDateLabel.text = getString(R.string.created_in)
            }

            if (note.reminderTime != null) {
                val calendar = Calendar.getInstance().apply { timeInMillis = note.reminderTime!! }
                val date = DateFormat.format("dd/MM/yyyy", calendar).toString()
                val time = DateFormat.format("HH:mm", calendar).toString()
                textViewReminderDate.text = date
                textViewReminderTime.text = time
                textViewReminderLabel.isVisible = true
                textViewReminderDate.isVisible = true
                textViewReminderTime.isVisible = true
            } else {
                textViewReminderLabel.isVisible = false
                textViewReminderDate.isVisible = false
                textViewReminderTime.isVisible = false
            }

            if (note.imageBase64 != null) {
                try {
                    val decodedBytes = Base64.decode(note.imageBase64, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    imageViewNote.setImageBitmap(bitmap)
                    imageViewNote.isVisible = true
                } catch (e: Exception) {
                    Log.e("NoteViewFragment", "Failed to decode image: ${e.message}", e)
                    imageViewNote.isVisible = false
                }
            } else {
                imageViewNote.isVisible = false
            }
        }
    }

    private fun navigateToNoteList() {
        val navAction = NoteViewFragmentDirections.actionNoteViewFragmentToNoteListFragment()
        findNavController().navigate(navAction)
    }

    private fun navigateToNoteEditFragment() {
        try {
            val action = NoteViewFragmentDirections.actionNoteViewFragmentToNoteEditFragment(note)
            if (note.id.isNullOrEmpty()) {
                Log.e("NoteViewFragment", "ID ghi chú không hợp lệ: $note")
                showSnackBar("Không thể chỉnh sửa. Ghi chú không hợp lệ.")
            } else {
                findNavController().navigate(action)
            }
        } catch (e: Exception) {
            Log.e("NoteViewFragment", "Lỗi điều hướng: ${e.message}")
            showSnackBar("Lỗi khi chuyển đến chỉnh sửa ghi chú")
        }
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
    }
}