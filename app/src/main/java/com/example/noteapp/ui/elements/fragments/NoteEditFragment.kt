//package com.example.noteapp.ui.elements.fragments
//
//import android.annotation.SuppressLint
//import android.app.DatePickerDialog
//import android.app.TimePickerDialog
//import android.os.Bundle
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
//import com.example.noteapp.databinding.FragmentNoteEditBinding
//import com.example.noteapp.ui.view_models.NoteViewModel
//import com.example.noteapp.utils.response.ResultStatus
//import com.google.android.material.bottomsheet.BottomSheetDialog
//import com.google.android.material.snackbar.Snackbar
//import com.google.firebase.auth.ktx.auth
//import com.google.firebase.ktx.Firebase
//import java.text.SimpleDateFormat
//import java.util.*
//
//class NoteEditFragment : Fragment(){
//    private var _binding: FragmentNoteEditBinding? = null
//    private val binding get() = _binding!!
//    private val noteViewModel: NoteViewModel by activityViewModels()
//    private val args: NoteEditFragmentArgs by navArgs()
//    private var reminderTime: Long? = null
//    private lateinit var note: Note
//    private lateinit var bottomSheetDialog: BottomSheetDialog
//    private lateinit var bottomSheetView: View
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        note = args.note
//        reminderTime = note.reminderTime
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        _binding = FragmentNoteEditBinding.inflate(inflater,container,false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//
//        //Khởi tạo BottomShetDialog
//        bottomSheetDialog = BottomSheetDialog(requireContext())
//        bottomSheetView = layoutInflater.inflate(R.layout.bottomsheet_layout_2,null)
//        bottomSheetDialog.setContentView(bottomSheetView)
//
//        bindNoteData() //lấy dữ liệu của note hiển thị lên màn hình
//
//        binding.apply {
//            imageButtonBackToNoteList.setOnClickListener{
//                navigateToNoteListFragment()
//            }
//            moreButton.setOnClickListener(){
//                bottomSheetDialog.show()
//            }
//        }
//
//        //Xử lý sự kiện cho BottomSheetMenu
//        bottomSheetView.findViewById<View>(R.id.set_reminder).setOnClickListener {
//            val calendar = Calendar.getInstance(TimeZone.getTimeZone(getString(R.string.vietnam_time_zone)))
//            val currentTime = calendar.timeInMillis
//            DatePickerDialog(requireContext(), { _, year, month, day ->
//                TimePickerDialog(requireContext(), { _, hour, minute ->
//                    calendar.set(year, month, day, hour, minute, 0)
//                    reminderTime = calendar.timeInMillis
//                    if(reminderTime!! <= currentTime){
//                        showSnackBar("Thời gian nhắc nhở phải sau thời gian hiện tại!")
//                        reminderTime = null
//                        binding.textViewReminder.text = "Chưa đặt"
//                        bottomSheetView.findViewById<TextView>(R.id.reminder_status).text = "Not Set"
//                    }else{
//                        val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
//                        val reminderDate = Date(reminderTime!!)
//                        binding.textViewReminder.text = simpleDateFormat.format(reminderDate)
//                        bottomSheetView.findViewById<TextView>(R.id.reminder_status).text = simpleDateFormat.format(reminderDate)
//                        Log.d("NoteEditFragment", "Reminder time set to: $reminderTime ($reminderDate)")
//                    }
//                    bottomSheetDialog.dismiss()
//                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),true).show()
//            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
//        }
//        //Xử lý nút Save trong BottomSheet
//        bottomSheetView.findViewById<View>(R.id.buttonSave).setOnClickListener {
//            applySaveNote()
//            bottomSheetDialog.dismiss()
//        }
//        bottomSheetView.findViewById<View>(R.id.close_button).setOnClickListener {
//            bottomSheetDialog.dismiss()
//        }
//        //quan sát LiveData
//        noteViewModel.noteUpdateResultStatus.observe(viewLifecycleOwner) { resultStatus ->
//            when (resultStatus){
//                is ResultStatus.Loading ->{
//                    binding.progressBar.isVisible = true
//                    binding.imageButtonBackToNoteList.isEnabled = false
//                    binding.moreButton.isEnabled = false
//                    bottomSheetView.findViewById<View>(R.id.buttonSave).isEnabled = false
//                    Log.d("NoteEditFragment", "Editing note...")
//                }
//                is ResultStatus.Success ->{
//                    binding.progressBar.isVisible = false
//                    binding.imageButtonBackToNoteList.isEnabled = true
//                    binding.moreButton.isEnabled = true
//                    bottomSheetView.findViewById<View>(R.id.buttonSave).isEnabled = true
//                    showSnackBar(getString(R.string.note_success_update_message))
//                    noteViewModel.listNotes()
//                    navigateToNoteListFragment()
//                }
//                is ResultStatus.Error ->{
//                    binding.progressBar.isVisible = false
//                    binding.imageButtonBackToNoteList.isEnabled = true
//                    binding.moreButton.isEnabled = true
//                    bottomSheetView.findViewById<View>(R.id.buttonSave).isEnabled = true
//                    showSnackBar(resultStatus.message ?: "Lỗi khi chỉnh sửa ghi chú")
//                }
//                null ->{
//                    Log.d("NoteEditFragment", "NoteEditResultStatus is null")
//                    binding.imageButtonBackToNoteList.isEnabled = true
//                    binding.moreButton.isEnabled = true
//                    bottomSheetView.findViewById<View>(R.id.buttonSave).isEnabled = true
//                }
//            }
//        }
//    }
//
//    private fun applySaveNote() {
//        binding.apply {
//            val updatedTitle = editTextNoteTitle.text.toString()
//            val updatedDescription = editTextNoteDescription.text.toString()
//
//            val currentTime = getCurrentTime()
//            val currentDate = getCurrentDate()
//            val currentUserUid = Firebase.auth.currentUser?.uid ?: return@apply
//
//            if (updatedTitle.isEmpty()){
//                showSnackBar(getString(R.string.title_cant_be_empty))
//                return@apply
//            }
//
//            //Tạo bản ghi cập nhật mới
//            val updatedNote = note.copy(
//                title = updatedTitle,
//                description = updatedDescription,
//                dateOfUpdate = currentDate,
//                timeOfUpdate = currentTime,
//                reminderTime = reminderTime,
//                UpdatedNote = true,
//                addByUid = currentUserUid
//            )
//            noteViewModel.updateNote(requireContext(),updatedNote)
//        }
//    }
//
//
//
//    private fun getCurrentTime(): String {
//        val tz = TimeZone.getTimeZone((getString(R.string.vietnam_time_zone)))
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
//    private fun showSnackBar(message: String) {
//        Snackbar.make(requireView(),message,Snackbar.LENGTH_SHORT).show()
//    }
//
//    private fun navigateToNoteListFragment() {
//        val navAction = NoteEditFragmentDirections.actionNoteEditFragmentToNoteListFragment()
//        findNavController().navigate(navAction)
//        noteViewModel.resetNoteStatus()
//        Log.d("NoteEditFragment", "Navigated to NoteListFragment")
//    }
//
//
//    private fun bindNoteData() {
//        binding.apply {
//            editTextNoteTitle.setText(note.title)
//            editTextNoteDescription.setText(note.description)
//            textViewNoteDate.text = note.dateOfUpdate ?: note.dateOfCreation // Hiển thị ngày cập nhật hoặc tạo
//
//            if (note.reminderTime != null) {
//                val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
//                val reminderDate = Date(note.reminderTime!!)
//                textViewReminder.text = sdf.format(reminderDate)
//                bottomSheetView.findViewById<TextView>(R.id.reminder_status).text = sdf.format(reminderDate)
//            } else {
//                textViewReminder.text = "Chưa đặt"
//                bottomSheetView.findViewById<TextView>(R.id.reminder_status).text = "Not set"
//            }
//        }
//    }
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}

package com.example.noteapp.ui.elements.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
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
import com.example.noteapp.databinding.FragmentNoteEditBinding
import com.example.noteapp.ui.view_models.NoteViewModel
import com.example.noteapp.utils.response.ResultStatus
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class NoteEditFragment : Fragment() {
    private var _binding: FragmentNoteEditBinding? = null
    private val binding get() = _binding!!
    private val noteViewModel: NoteViewModel by activityViewModels()
    private val args: NoteEditFragmentArgs by navArgs()
    private var reminderTime: Long? = null
    private lateinit var note: Note
    private var imageUri: Uri? = null
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var bottomSheetView: View

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        note = args.note
        reminderTime = note.reminderTime
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoteEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Khởi tạo BottomSheetDialog
        bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetView = layoutInflater.inflate(R.layout.bottomsheet_layout_2, null)
        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetView.findViewById<View>(R.id.remove_image).isVisible = note.imageBase64 != null

        bindNoteData()

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
                        val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
                        val reminderDate = Date(reminderTime!!)
                        binding.textViewReminder.text = simpleDateFormat.format(reminderDate)
                        bottomSheetView.findViewById<TextView>(R.id.reminder_status).text = simpleDateFormat.format(reminderDate)
                        Log.d("NoteEditFragment", "Reminder time set to: $reminderTime ($reminderDate)")
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
            note = note.copy(imageBase64 = null)
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

        noteViewModel.noteUpdateResultStatus.observe(viewLifecycleOwner) { resultStatus ->
            when (resultStatus) {
                is ResultStatus.Loading -> {
                    binding.progressBar.isVisible = true
                    binding.imageButtonBackToNoteList.isEnabled = false
                    binding.moreButton.isEnabled = false
                    bottomSheetView.findViewById<View>(R.id.buttonSave).isEnabled = false
                    Log.d("NoteEditFragment", "Editing note...")
                }
                is ResultStatus.Success -> {
                    binding.progressBar.isVisible = false
                    binding.imageButtonBackToNoteList.isEnabled = true
                    binding.moreButton.isEnabled = true
                    bottomSheetView.findViewById<View>(R.id.buttonSave).isEnabled = true
                    showSnackBar(getString(R.string.note_success_update_message))
                    noteViewModel.listNotes()
                    navigateToNoteListFragment()
                }
                is ResultStatus.Error -> {
                    binding.progressBar.isVisible = false
                    binding.imageButtonBackToNoteList.isEnabled = true
                    binding.moreButton.isEnabled = true
                    bottomSheetView.findViewById<View>(R.id.buttonSave).isEnabled = true
                    showSnackBar(resultStatus.message ?: "Lỗi khi chỉnh sửa ghi chú")
                }
                null -> {
                    Log.d("NoteEditFragment", "NoteEditResultStatus is null")
                    binding.imageButtonBackToNoteList.isEnabled = true
                    binding.moreButton.isEnabled = true
                    bottomSheetView.findViewById<View>(R.id.buttonSave).isEnabled = true
                }
            }
        }
    }

    private fun applySaveNote() {
        binding.apply {
            val updatedTitle = editTextNoteTitle.text.toString()
            val updatedDescription = editTextNoteDescription.text.toString()

            val currentTime = getCurrentTime()
            val currentDate = getCurrentDate()
            val currentUserUid = Firebase.auth.currentUser?.uid ?: return@apply

            if (updatedTitle.isEmpty()) {
                showSnackBar(getString(R.string.title_cant_be_empty))
                return@apply
            }

            val updatedNote = note.copy(
                title = updatedTitle,
                description = updatedDescription,
                dateOfUpdate = currentDate,
                timeOfUpdate = currentTime,
                reminderTime = reminderTime,
                UpdatedNote = true,
                addByUid = currentUserUid
            )
            noteViewModel.updateNote(requireContext(), updatedNote, imageUri)
        }
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

    private fun showSnackBar(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
    }

    private fun navigateToNoteListFragment() {
        val navAction = NoteEditFragmentDirections.actionNoteEditFragmentToNoteListFragment()
        findNavController().navigate(navAction)
        noteViewModel.resetNoteStatus()
        Log.d("NoteEditFragment", "Navigated to NoteListFragment")
    }

    private fun bindNoteData() {
        binding.apply {
            editTextNoteTitle.setText(note.title)
            editTextNoteDescription.setText(note.description)
            textViewNoteDate.text = note.dateOfUpdate ?: note.dateOfCreation

            if (note.reminderTime != null) {
                val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
                val reminderDate = Date(note.reminderTime!!)
                textViewReminder.text = sdf.format(reminderDate)
                bottomSheetView.findViewById<TextView>(R.id.reminder_status).text = sdf.format(reminderDate)
            } else {
                textViewReminder.text = "Not set"
                bottomSheetView.findViewById<TextView>(R.id.reminder_status).text = "Not set"
            }

            if (note.imageBase64 != null) {
                try {
                    val decodedBytes = Base64.decode(note.imageBase64, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    imageViewNote.setImageBitmap(bitmap)
                    imageViewNote.isVisible = true
                } catch (e: Exception) {
                    Log.e("NoteEditFragment", "Failed to decode image: ${e.message}", e)
                    imageViewNote.isVisible = false
                }
            } else {
                imageViewNote.isVisible = false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}