//package com.example.noteapp.ui.elements.fragments
//
//import android.annotation.SuppressLint
//import android.app.AlarmManager
//import android.app.DatePickerDialog
//import android.app.TimePickerDialog
//import android.content.Context
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.os.Bundle
//import android.provider.Settings
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.core.view.isVisible
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.activityViewModels
//import androidx.navigation.fragment.findNavController
//import androidx.navigation.fragment.navArgs
//import com.example.noteapp.R
//import com.example.noteapp.data.models.Note
//import com.example.noteapp.databinding.FragmentAddNoteBinding
//import com.example.noteapp.ui.view_models.NoteViewModel
//import com.example.noteapp.utils.response.ResultStatus
//import com.google.android.material.bottomsheet.BottomSheetDialog
//import com.google.android.material.snackbar.Snackbar
//import com.google.firebase.auth.ktx.auth
//import com.google.firebase.ktx.Firebase
//import java.text.SimpleDateFormat
//import java.util.Calendar
//import java.util.Date
//import java.util.Locale
//import java.util.TimeZone
//import java.util.UUID
//
//class AddBuyingNoteFragment : Fragment(){
//    private var _binding: FragmentAddNoteBinding? = null
//    private val binding get() = _binding!!
//    private val noteViewModel: NoteViewModel by activityViewModels()
//    private val args: AddBuyingNoteFragmentArgs by navArgs()
//    private var reminderTime: Long? =null
//    private lateinit var bottomSheetDialog: BottomSheetDialog
//    private lateinit var bottomSheetView: View
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        _binding = FragmentAddNoteBinding.inflate(inflater, container,false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        //Xóa nội dung mặc định và hiển thị ngày tạo
//        binding.editTextNoteTitle.text?.clear()
//        binding.editTextNoteDescription.text?.clear()
//        binding.textViewReminder.text = "Chưa đặt"
//        binding.textViewNoteDate.text = getCurrentDate()
//
//        //Khởi tạo BottomSheetDialog
//        bottomSheetDialog = BottomSheetDialog(requireContext())
//        bottomSheetView = layoutInflater.inflate(R.layout.bottomsheet_layout_2,null)
//        bottomSheetDialog.setContentView(bottomSheetView)
//
//        binding.apply {
//            imageButtonBackToNoteList.setOnClickListener{
//                navigateToNoteListFragment()
//            }
//            moreButton.setOnClickListener{
//                bottomSheetDialog.show()
//            }
//        }
//
//        //Xử lý event BottomSheetMenu
//        bottomSheetView.findViewById<View>(R.id.set_reminder).setOnClickListener {
//            val calendar = Calendar.getInstance(TimeZone.getTimeZone(getString(R.string.vietnam_time_zone)))
//            val currentTime = calendar.timeInMillis
//            DatePickerDialog(requireContext(), { _, year, month, day ->
//                TimePickerDialog(requireContext(), { _, hour, minute ->
//                    calendar.set(year, month, day, hour, minute, 0)
//                    reminderTime = calendar.timeInMillis
//                    if (reminderTime!! <= currentTime) {
//                        showSnackBar("Thời gian nhắc nhở phải sau thời gian hiện tại!")
//                        reminderTime = null
//                        binding.textViewReminder.text = "Chưa đặt"
//                        bottomSheetView.findViewById<TextView>(R.id.reminder_status).text = "Not set"
//                    } else {
//                        val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
//                        val reminderDate = Date(reminderTime!!)
//                        binding.textViewReminder.text = sdf.format(reminderDate)
//                        bottomSheetView.findViewById<TextView>(R.id.reminder_status).text = sdf.format(reminderDate)
//                        Log.d("AddBuyingNoteFragment", "Reminder time set to: $reminderTime ($reminderDate)")
//                    }
//                    bottomSheetDialog.dismiss()
//                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
//            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
//        }
//
//        // Xử lý nút Save trong BottomSheet
//        bottomSheetView.findViewById<View>(R.id.buttonSave).setOnClickListener {
//            applySaveNote()
//            bottomSheetDialog.dismiss()
//        }
//
//        // Xử lý nút đóng BottomSheet
//        bottomSheetView.findViewById<View>(R.id.close_button).setOnClickListener {
//            bottomSheetDialog.dismiss()
//        }
//
//        noteViewModel.noteAddResultStatus.observe(viewLifecycleOwner) { resultStatus ->
//            when (resultStatus) {
//                is ResultStatus.Loading -> {
//                    binding.progressBar.isVisible = true
//                    binding.imageButtonBackToNoteList.isEnabled = false
//                    binding.moreButton.isEnabled = false
//                    bottomSheetView.findViewById<View>(R.id.buttonSave).isEnabled = false
//                    Log.d("AddBuyingNoteFragment", "Adding note...")
//                }
//                is ResultStatus.Success -> {
//                    binding.progressBar.isVisible = false
//                    binding.imageButtonBackToNoteList.isEnabled = true
//                    binding.moreButton.isEnabled = true
//                    bottomSheetView.findViewById<View>(R.id.buttonSave).isEnabled = true
//                    showSnackBar(getString(R.string.note_added_successfully))
//                    noteViewModel.listNotes() // Cập nhật danh sách ghi chú
//                    navigateToNoteListFragment()
//                }
//                is ResultStatus.Error -> {
//                    binding.progressBar.isVisible = false
//                    binding.imageButtonBackToNoteList.isEnabled = true
//                    binding.moreButton.isEnabled = true
//                    bottomSheetView.findViewById<View>(R.id.buttonSave).isEnabled = true
//                    showSnackBar(resultStatus.message ?: "Lỗi khi thêm ghi chú")
//                }
//                null -> {
//                    Log.d("AddBuyingNoteFragment", "noteAddResultStatus is null")
//                    binding.imageButtonBackToNoteList.isEnabled = true
//                    binding.moreButton.isEnabled = true
//                    bottomSheetView.findViewById<View>(R.id.buttonSave).isEnabled = true
//                }
//            }
//        }
//
//        // Kiểm tra quyền đặt lịch báo thức trên Android 12+
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
//            val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
//            if (!alarmManager.canScheduleExactAlarms()) {
//                Snackbar.make(
//                    requireView(),
//                    "Vui lòng cấp quyền đặt lịch báo thức trong Cài đặt > Ứng dụng > [Tên ứng dụng] > Pin > Cho phép đặt báo thức chính xác.",
//                    Snackbar.LENGTH_LONG
//                ).show()
//                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
//                startActivity(intent)
//            }
//        }
//
//        // Kiểm tra và yêu cầu quyền thông báo (Android 13+)
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
//            if (requireContext().checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
//                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
//            }
//        }
//    }
//
//    private fun navigateToNoteListFragment() {
//        val navAction = AddBuyingNoteFragmentDirections.actionAddBuyingNoteFragmentToNoteListFragment()
//        findNavController().navigate(navAction)
//        noteViewModel.resetNoteStatus()
//    }
//
//    private fun applySaveNote() {
//        binding.apply {
//            val title = editTextNoteTitle.text.toString()
//            val description = editTextNoteDescription.text.toString()
//
//            val currentTime = getCurrentTime()
//            val currentDate = getCurrentDate()
//            val currentUserUid = Firebase.auth.currentUser?.uid ?: return@apply
//
//            if (title.isNotEmpty()) {
//                val note = Note(
//                    title = title,
//                    dateOfCreation = currentDate,
//                    timeOfCreation = currentTime,
//                    description = description,
//                    addByUid = currentUserUid,
//                    id = UUID.randomUUID().toString(),
//                    reminderTime = reminderTime,
//                    UpdatedNote = false,
//                    Finished = false,
//                    category = args.category
//                )
//                noteViewModel.addNote(requireContext(), note)
//            } else {
//                showSnackBar(getString(R.string.title_cant_be_empty))
//            }
//        }
//    }
//
//    private fun showSnackBar(message: String) {
//        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
//    }
//
//    private fun getCurrentTime(): String {
//        val tz = TimeZone.getTimeZone(getString(R.string.vietnam_time_zone))
//        val c = Calendar.getInstance(tz)
//        val hours = String.format("%02d", c.get(Calendar.HOUR_OF_DAY))
//        val minutes = String.format("%02d", c.get(Calendar.MINUTE))
//        return "$hours:$minutes"
//    }
//
//    @SuppressLint("SimpleDateFormat")
//    private fun getCurrentDate(): String {
//        val currentDateObject = Date()
//        val formatter = SimpleDateFormat("dd-MM-yyyy")
//        return formatter.format(currentDateObject)
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}


package com.example.noteapp.ui.elements.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
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
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class AddBuyingNoteFragment : Fragment() {
    private var _binding: FragmentAddNoteBinding? = null
    private val binding get() = _binding!!
    private val noteViewModel: NoteViewModel by activityViewModels()
    private val args: AddBuyingNoteFragmentArgs by navArgs()
    private var reminderTime: Long? = null
    private var imageUri: Uri? = null
    private lateinit var bottomSheetDialog: BottomSheetDialog

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageUri = result.data?.data
            imageUri?.let { uri ->
                binding.imageViewNote.setImageURI(uri)
                binding.imageViewNote.isVisible = true
                bottomSheetView.findViewById<View>(R.id.remove_image).isVisible = true
            }
        }
    }

    private lateinit var bottomSheetView: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.editTextNoteTitle.text?.clear()
        binding.editTextNoteDescription.text?.clear()
        binding.textViewReminder.text = "Not set"
        binding.textViewNoteDate.text = getCurrentDate()
        binding.imageViewNote.isVisible = false

        // Khởi tạo BottomSheetDialog
        bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetView = layoutInflater.inflate(R.layout.bottomsheet_layout_2, null)
        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetView.findViewById<View>(R.id.remove_image).isVisible = false

        binding.apply {
            imageButtonBackToNoteList.setOnClickListener {
                navigateToNoteListFragment()
            }

            moreButton.setOnClickListener {
                bottomSheetDialog.show()
            }
        }

        // Xử lý sự kiện cho BottomSheetMenu
        bottomSheetView.findViewById<View>(R.id.set_reminder).setOnClickListener {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone(getString(R.string.vietnam_time_zone)))
            val currentTime = calendar.timeInMillis
            DatePickerDialog(requireContext(), { _, year, month, day ->
                TimePickerDialog(requireContext(), { _, hour, minute ->
                    calendar.set(year, month, day, hour, minute, 0)
                    reminderTime = calendar.timeInMillis
                    if (reminderTime!! <= currentTime) {
                        showSnackBar("Thời gian nhắc nhở phải sau thời gian hiện tại!")
                        reminderTime = null
                        binding.textViewReminder.text = "Not set"
                        bottomSheetView.findViewById<TextView>(R.id.reminder_status).text = "Not set"
                    } else {
                        val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
                        val reminderDate = Date(reminderTime!!)
                        binding.textViewReminder.text = sdf.format(reminderDate)
                        bottomSheetView.findViewById<TextView>(R.id.reminder_status).text = sdf.format(reminderDate)
                        Log.d("AddBuyingNoteFragment", "Reminder time set to: $reminderTime ($reminderDate)")
                    }
                    bottomSheetDialog.dismiss()
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        bottomSheetView.findViewById<View>(R.id.add_image).setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            }
            pickImageLauncher.launch(intent)
            bottomSheetDialog.dismiss()
        }

        bottomSheetView.findViewById<View>(R.id.remove_image).setOnClickListener {
            imageUri = null
            binding.imageViewNote.setImageURI(null)
            binding.imageViewNote.isVisible = false
            bottomSheetView.findViewById<View>(R.id.remove_image).isVisible = false
            bottomSheetDialog.dismiss()
        }

        bottomSheetView.findViewById<View>(R.id.buttonSave).setOnClickListener {
            applySaveNote()
            bottomSheetDialog.dismiss()
        }

        bottomSheetView.findViewById<View>(R.id.close_button).setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        noteViewModel.noteAddResultStatus.observe(viewLifecycleOwner) { resultStatus ->
            when (resultStatus) {
                is ResultStatus.Loading -> {
                    binding.progressBar.isVisible = true
                    binding.imageButtonBackToNoteList.isEnabled = false
                    binding.moreButton.isEnabled = false
                    bottomSheetView.findViewById<View>(R.id.buttonSave).isEnabled = false
                    Log.d("AddBuyingNoteFragment", "Adding note...")
                }
                is ResultStatus.Success -> {
                    binding.progressBar.isVisible = false
                    binding.imageButtonBackToNoteList.isEnabled = true
                    binding.moreButton.isEnabled = true
                    bottomSheetView.findViewById<View>(R.id.buttonSave).isEnabled = true
                    showSnackBar(getString(R.string.note_added_successfully))
                    navigateToNoteListFragment()
                }
                is ResultStatus.Error -> {
                    binding.progressBar.isVisible = false
                    binding.imageButtonBackToNoteList.isEnabled = true
                    binding.moreButton.isEnabled = true
                    bottomSheetView.findViewById<View>(R.id.buttonSave).isEnabled = true
                    showSnackBar(resultStatus.message ?: "Lỗi khi thêm ghi chú")
                }
                null -> {
                    Log.d("AddBuyingNoteFragment", "noteAddResultStatus is null")
                }
            }
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                Snackbar.make(
                    requireView(),
                    "Vui lòng cấp quyền đặt lịch báo thức trong Cài đặt > Ứng dụng > [Tên ứng dụng] > Pin > Cho phép đặt báo thức chính xác.",
                    Snackbar.LENGTH_LONG
                ).show()
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            }
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (requireContext().checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES), 102)
            }
        }
    }

    private fun navigateToNoteListFragment() {
        val navAction = AddBuyingNoteFragmentDirections.actionAddBuyingNoteFragmentToNoteListFragment()
        findNavController().navigate(navAction)
        noteViewModel.resetNoteStatus()
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
                    category = args.category
                )
                noteViewModel.addNote(requireContext(), note, imageUri)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}