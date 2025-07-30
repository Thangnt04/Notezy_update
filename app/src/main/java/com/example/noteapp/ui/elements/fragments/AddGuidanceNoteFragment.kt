package com.example.noteapp.ui.elements.fragments

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
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
import com.example.noteapp.databinding.FragmentAddNoteBinding
import com.example.noteapp.ui.view_models.NoteViewModel
import com.example.noteapp.utils.response.ResultStatus
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

class AddGuidanceNoteFragment : Fragment() {
    private lateinit var binding: FragmentAddNoteBinding
    private val noteViewModel: NoteViewModel by activityViewModels()
    private val args: AddGuidanceNoteFragmentArgs by navArgs()
    private var reminderTime: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Xóa các trường nhập liệu
        binding.editTextNoteTitle.text?.clear()
        binding.editTextNoteDescription.text?.clear()
        binding.textViewReminder.text = "Nhắc nhở: Chưa đặt"

        binding.apply {
            // Quay lại danh sách ghi chú
            buttonBackToNoteList.setOnClickListener {
                navigateToNoteListFragment()
            }

            // Đặt thời gian nhắc nhở
            buttonSetReminder.setOnClickListener {
                val calendar = Calendar.getInstance(TimeZone.getTimeZone(getString(R.string.vietnam_time_zone)))
                val currentTime = calendar.timeInMillis
                DatePickerDialog(requireContext(), { _, year, month, day ->
                    TimePickerDialog(requireContext(), { _, hour, minute ->
                        calendar.set(year, month, day, hour, minute, 0)
                        reminderTime = calendar.timeInMillis
                        if (reminderTime!! <= currentTime) {
                            showSnackBar("Thời gian nhắc nhở phải sau thời gian hiện tại!")
                            reminderTime = null
                            textViewReminder.text = "Nhắc nhở: Chưa đặt"
                        } else {
                            val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
                            val reminderDate = Date(reminderTime!!)
                            textViewReminder.text = "Nhắc nhở: ${sdf.format(reminderDate)}"
                            Log.d("AddGuidanceNoteFragment", "Reminder time set to: $reminderTime ($reminderDate)")
                        }
                    }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
            }

            // Lưu ghi chú
            buttonSave.setOnClickListener {
                applySaveNote()
            }
        }

        // Quan sát kết quả lưu ghi chú
        noteViewModel.noteAddResultStatus.observe(viewLifecycleOwner) { resultStatus ->
            when (resultStatus) {
                is ResultStatus.Loading -> {
                    binding.progressBar.isVisible = true
                    binding.buttonSave.isEnabled = false
                    binding.buttonBackToNoteList.isEnabled = false
                    Log.d("AddGuidanceNoteFragment", "Adding note...")
                }
                is ResultStatus.Success -> {
                    binding.progressBar.isVisible = false
                    binding.buttonSave.isEnabled = true
                    binding.buttonBackToNoteList.isEnabled = true
                    showSnackBar(getString(R.string.note_added_successfully))
                    noteViewModel.listNotes() // Cập nhật danh sách ghi chú
                    noteViewModel.resetNoteStatus() // Đặt lại trạng thái
                    navigateToNoteListFragment() // Điều hướng về NoteListFragment
                }
                is ResultStatus.Error -> {
                    binding.progressBar.isVisible = false
                    binding.buttonSave.isEnabled = true
                    binding.buttonBackToNoteList.isEnabled = true
                    showSnackBar(resultStatus.message ?: "Lỗi khi thêm ghi chú")
                }
                null -> {
                    Log.d("AddGuidanceNoteFragment", "noteAddResultStatus is null")
                    binding.buttonSave.isEnabled = true
                    binding.buttonBackToNoteList.isEnabled = true
                }
            }
        }

        // Kiểm tra quyền đặt lịch báo thức trên Android 12+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                Snackbar.make(requireView(), "Vui lòng cấp quyền đặt lịch báo thức trong Cài đặt > Ứng dụng > [Tên ứng dụng] > Pin > Cho phép đặt báo thức chính xác.", Snackbar.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            }
        }

        // Kiểm tra và yêu cầu quyền thông báo (Android 13+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (requireContext().checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }

    private fun navigateToNoteListFragment() {
        val navAction = AddGuidanceNoteFragmentDirections.actionAddGuidanceNoteFragmentToNoteListFragment()
        findNavController().navigate(navAction)
    }

    private fun applySaveNote() {
        binding.apply {
            val title = editTextNoteTitle.text.toString()
            val description = editTextNoteDescription.text.toString()

            val currentTime = getCurrentTime()
            val currentDate = getCurrentDate()
            val currentUserUid = Firebase.auth.currentUser?.uid ?: return@apply

            if (title.isNotEmpty()) {
                val note = Note(
                    title = title,
                    dateOfCreation = currentDate,
                    timeOfCreation = currentTime,
                    description = description,
                    addByUid = currentUserUid,
                    id = UUID.randomUUID().toString(),
                    reminderTime = reminderTime,
                    UpdatedNote = false,
                    Finished = false,
                    category = args.category // Đặt category từ tham số
                )
                noteViewModel.addNote(requireContext(), note)
            } else {
                showSnackBar(getString(R.string.title_cant_be_empty))
            }
        }
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
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