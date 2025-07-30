//package com.example.noteapp.data.repositories
//
//import android.app.AlarmManager
//import android.app.PendingIntent
//import android.content.Context
//import android.content.Intent
//import android.net.Uri
//import android.util.Log
//import com.example.noteapp.ReminderReceiver
//import com.example.noteapp.data.models.Note
//import com.example.noteapp.utils.firebase_services.FirebaseCollections.NOTE_ADDED_BY_UID_FIELD
//import com.example.noteapp.utils.firebase_services.FirebaseCollections.NOTE_COLLECTION
//import com.example.noteapp.utils.firebase_services.FirebaseCollections.NOTE_DATE_FIELD
//import com.example.noteapp.utils.firebase_services.FirebaseCollections.NOTE_TIME_FIELD
//import com.example.noteapp.utils.response.EmptyResult
//import com.example.noteapp.utils.response.ResultStatus
//import com.google.firebase.auth.ktx.auth
//import com.google.firebase.firestore.CollectionReference
//import com.google.firebase.firestore.FirebaseFirestoreException
//import com.google.firebase.firestore.Query
//import com.google.firebase.firestore.ktx.firestore
//import com.google.firebase.ktx.Firebase
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.tasks.await
//import kotlinx.coroutines.withContext
//import java.util.UUID
//
//class NoteRepository {
//    private val noteCollection: CollectionReference by lazy {
//        Firebase.firestore.collection(NOTE_COLLECTION)
//    }
//
//    private val currentUserUid: String?
//        get() = Firebase.auth.currentUser?.uid.also {
//            Log.d("NoteRepository", "Current user UID: $it")
//        }
//
//    suspend fun listNotes(isFinished: Boolean = false): ResultStatus<MutableList<Note>> = withContext(Dispatchers.IO) {
//        val userId = currentUserUid
//        if (userId == null) {
//            Log.e("NoteRepository", "User not authenticated")
//            return@withContext ResultStatus.Error("User not authenticated")
//        }
//        try {
//            Log.d("NoteRepository", "Fetching notes with userId: $userId, isFinished: $isFinished")
//            val querySnapshot = noteCollection
//                .whereEqualTo(NOTE_ADDED_BY_UID_FIELD, userId)
//              //.whereEqualTo("Finished", isFinished)
//                .orderBy(NOTE_DATE_FIELD, Query.Direction.DESCENDING)
//                .orderBy(NOTE_TIME_FIELD, Query.Direction.DESCENDING)
//                .get()
//                .await()
//            val notes = querySnapshot.documents.mapNotNull { doc ->
//                val note = doc.toObject(Note::class.java)?.copy(id = doc.id)
//                Log.d("NoteRepository", "Document ${doc.id}: $note")
//                note
//            }.toMutableList()
//            Log.d("NoteRepository", "Loaded ${notes.size} notes with isFinished=$isFinished, data: $notes")
//            ResultStatus.Success(notes)
//        } catch (e: FirebaseFirestoreException) {
//            Log.e("NoteRepository", "Firestore error: ${e.code}, ${e.message}", e)
//            ResultStatus.Error(e.message ?: "Firestore error")
//        } catch (e: Exception) {
//            Log.e("NoteRepository", "Unexpected error: ${e.message}", e)
//            ResultStatus.Error(e.message ?: "Unknown error")
//        }
//    }
//
//    suspend fun addNote(context: Context, note: Note): ResultStatus<Note> = withContext(Dispatchers.IO) {
//        val userId = currentUserUid
//        if (userId == null) {
//            Log.e("NoteRepository", "User not logged in")
//            return@withContext ResultStatus.Error("User not logged in")
//        }
//        try {
//            Log.d("NoteRepository", "Adding note with userId: $userId, note: $note")
//            val noteWithUser = note.copy(addByUid = userId, Finished = false)
//
//
//
//            val noteDoc = noteCollection.add(noteWithUser).await()
//            val addedNote = noteWithUser.copy(id = noteDoc.id)
//            setReminder(context, addedNote)
//            Log.d("NoteRepository", "Note added successfully with ID: ${noteDoc.id}, data: $addedNote")
//            ResultStatus.Success(addedNote)
//        } catch (e: FirebaseFirestoreException) {
//            Log.e("NoteRepository", "Firestore error: ${e.code}, ${e.message}", e)
//            ResultStatus.Error(e.message ?: "Firestore error")
//        } catch (e: Exception) {
//            Log.e("NoteRepository", "Unexpected error: ${e.message}", e)
//            ResultStatus.Error(e.message ?: "Unknown error")
//        }
//    }
//
//    suspend fun updateNote(context: Context, note: Note): ResultStatus<Note> = withContext(Dispatchers.IO) {
//        val noteId = note.id
//        if (noteId.isNullOrEmpty()) {
//            Log.e("NoteRepository", "Failed to update note: Null or empty Note ID. Note data: $note")
//            return@withContext ResultStatus.Error("Null or empty Note ID")
//        }
//        try {
//            noteCollection.document(noteId).set(note).await()
//            setReminder(context, note)
//            Log.d("NoteRepository", "Note updated successfully: $noteId, data: $note")
//            ResultStatus.Success(note)
//        } catch (e: FirebaseFirestoreException) {
//            val errorMessage = when (e.code) {
//                FirebaseFirestoreException.Code.PERMISSION_DENIED -> "Permission denied: ${e.message}"
//                FirebaseFirestoreException.Code.NOT_FOUND -> "Note not found: $noteId"
//                else -> e.message ?: "Unknown Firestore error"
//            }
//            Log.e("NoteRepository", "Failed to update note: $errorMessage, note: $note", e)
//            ResultStatus.Error(errorMessage)
//        } catch (e: SecurityException) {
//            Log.e("NoteRepository", "SecurityException: Cannot schedule exact alarm. ${e.message}")
//            ResultStatus.Error("Không thể đặt lịch báo thức. Vui lòng cấp quyền trong cài đặt.")
//        } catch (e: Exception) {
//            Log.e("NoteRepository", "Unexpected error updating note: ${e.message}, note: $note", e)
//            ResultStatus.Error(e.message ?: "Unknown error")
//        }
//    }
//
//    suspend fun deleteNote(noteId: String): EmptyResult = withContext(Dispatchers.IO) {
//        try {
//            noteCollection.document(noteId).delete().await()
//            Log.d("NoteRepository", "Note deleted successfully: $noteId")
//            EmptyResult.Success()
//        } catch (e: FirebaseFirestoreException) {
//            val errorMessage = when (e.code) {
//                FirebaseFirestoreException.Code.PERMISSION_DENIED -> "Permission denied: ${e.message}"
//                FirebaseFirestoreException.Code.NOT_FOUND -> "Note not found: $noteId"
//                else -> e.message ?: "Unknown Firestore error"
//            }
//            Log.e("NoteRepository", "Failed to delete note: $errorMessage", e)
//            EmptyResult.Error(errorMessage)
//        } catch (e: Exception) {
//            Log.e("NoteRepository", "Unexpected error deleting note: ${e.message}", e)
//            EmptyResult.Error(e.message ?: "Unknown error")
//        }
//    }
//
//    suspend fun searchNotes(query: String): ResultStatus<MutableList<Note>> = withContext(Dispatchers.IO) {
//        val userId = currentUserUid
//        if (userId == null) {
//            Log.e("NoteRepository", "User not authenticated")
//            return@withContext ResultStatus.Error("User not authenticated")
//        }
//        try {
//            Log.d("NoteRepository", "Searching notes with userId: $userId, query: $query")
//            val result = noteCollection
//                .whereEqualTo(NOTE_ADDED_BY_UID_FIELD, userId)
//                .whereGreaterThanOrEqualTo("title", query)
//                .whereLessThanOrEqualTo("title", query + "\uf8ff")
//                .orderBy("title")
//                .orderBy(NOTE_DATE_FIELD, Query.Direction.DESCENDING)
//                .orderBy(NOTE_TIME_FIELD, Query.Direction.DESCENDING)
//                .get()
//                .await()
//            val notes = result.documents.mapNotNull { doc ->
//                val note = doc.toObject(Note::class.java)?.copy(id = doc.id)
//                Log.d("NoteRepository", "Search document ${doc.id}: $note")
//                note
//            }.toMutableList()
//            Log.d("NoteRepository", "Found ${notes.size} notes matching query: $query, data: $notes")
//            ResultStatus.Success(notes)
//        } catch (e: FirebaseFirestoreException) {
//            val errorMessage = when (e.code) {
//                FirebaseFirestoreException.Code.FAILED_PRECONDITION -> {
//                    if (e.message?.contains("index is currently building") == true) {
//                        "Index is being built. Please try again later: ${e.message}"
//                    } else {
//                        "Query requires an index: ${e.message}"
//                    }
//                }
//                FirebaseFirestoreException.Code.PERMISSION_DENIED -> "Permission denied: ${e.message}"
//                else -> e.message ?: "Unknown Firestore error"
//            }
//            Log.e("NoteRepository", "Failed to search notes: $errorMessage", e)
//            ResultStatus.Error(errorMessage)
//        } catch (e: Exception) {
//            Log.e("NoteRepository", "Unexpected error searching notes: ${e.message}", e)
//            ResultStatus.Error(e.message ?: "Unknown error")
//        }
//    }
//
//    private fun setReminder(context: Context, note: Note) {
//        note.reminderTime?.let { reminderTime ->
//            val currentTime = System.currentTimeMillis()
//            if (reminderTime <= currentTime) {
//                Log.w("NoteRepository", "Reminder time ($reminderTime) is in the past. Current time: $currentTime")
//                return
//            }
//
//            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//            val intent = Intent(context, ReminderReceiver::class.java).apply {
//                putExtra("NOTE_TITLE", note.title)
//                putExtra("NOTE_ID", note.id)
//            }
//            val pendingIntent = PendingIntent.getBroadcast(
//                context, note.id.hashCode(), intent,
//                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//            )
//
//            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
//                if (alarmManager.canScheduleExactAlarms()) {
//                    alarmManager.setExactAndAllowWhileIdle(
//                        AlarmManager.RTC_WAKEUP,
//                        reminderTime,
//                        pendingIntent
//                    )
//                    Log.d("NoteRepository", "Exact alarm set for note ${note.id} at $reminderTime")
//                } else {
//                    Log.w("NoteRepository", "Cannot schedule exact alarm for note ${note.id}. Permission denied.")
//                }
//            } else {
//                alarmManager.setExactAndAllowWhileIdle(
//                    AlarmManager.RTC_WAKEUP,
//                    reminderTime,
//                    pendingIntent
//                )
//                Log.d("NoteRepository", "Alarm set for note ${note.id} at $reminderTime")
//            }
//        }
//    }
//}

package com.example.noteapp.data.repositories

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.noteapp.ReminderReceiver
import com.example.noteapp.data.models.Note
import com.example.noteapp.utils.firebase_services.FirebaseCollections.NOTE_ADDED_BY_UID_FIELD
import com.example.noteapp.utils.firebase_services.FirebaseCollections.NOTE_COLLECTION
import com.example.noteapp.utils.firebase_services.FirebaseCollections.NOTE_DATE_FIELD
import com.example.noteapp.utils.firebase_services.FirebaseCollections.NOTE_TIME_FIELD
import com.example.noteapp.utils.response.EmptyResult
import com.example.noteapp.utils.response.ResultStatus
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class NoteRepository {
    private val noteCollection: CollectionReference by lazy {
        Firebase.firestore.collection(NOTE_COLLECTION)
    }

    private val currentUserUid: String?
        get() = Firebase.auth.currentUser?.uid.also {
            Log.d("NoteRepository", "Current user UID: $it")
        }

    suspend fun listNotes(isFinished: Boolean = false): ResultStatus<MutableList<Note>> = withContext(Dispatchers.IO) {
        val userId = currentUserUid
        if (userId == null) {
            Log.e("NoteRepository", "User not authenticated")
            return@withContext ResultStatus.Error("User not authenticated")
        }
        try {
            Log.d("NoteRepository", "Fetching notes with userId: $userId, isFinished: $isFinished")
            val querySnapshot = noteCollection
                .whereEqualTo(NOTE_ADDED_BY_UID_FIELD, userId)
                .orderBy(NOTE_DATE_FIELD, Query.Direction.DESCENDING)
                .orderBy(NOTE_TIME_FIELD, Query.Direction.DESCENDING)
                .get()
                .await()
            val notes = querySnapshot.documents.mapNotNull { doc ->
                val note = doc.toObject(Note::class.java)?.copy(id = doc.id)
                Log.d("NoteRepository", "Document ${doc.id}: $note")
                note
            }.toMutableList()
            Log.d("NoteRepository", "Loaded ${notes.size} notes with isFinished=$isFinished, data: $notes")
            ResultStatus.Success(notes)
        } catch (e: FirebaseFirestoreException) {
            Log.e("NoteRepository", "Firestore error: ${e.code}, ${e.message}", e)
            ResultStatus.Error(e.message ?: "Firestore error")
        } catch (e: Exception) {
            Log.e("NoteRepository", "Unexpected error: ${e.message}", e)
            ResultStatus.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun addNote(context: Context, note: Note, imageUri: Uri? = null): ResultStatus<Note> = withContext(Dispatchers.IO) {
        val userId = currentUserUid
        if (userId == null) {
            Log.e("NoteRepository", "User not logged in")
            return@withContext ResultStatus.Error("User not logged in")
        }
        try {
            Log.d("NoteRepository", "Adding note with userId: $userId, note: $note")
            var noteWithUser = note.copy(addByUid = userId, Finished = false)

            // Lưu ảnh cục bộ nếu có
            imageUri?.let { uri ->
                val imagePath = saveImageToLocal(context, uri)
                noteWithUser = noteWithUser.copy(imagePath = imagePath)
            }

            val noteDoc = noteCollection.add(noteWithUser).await()
            val addedNote = noteWithUser.copy(id = noteDoc.id)
            setReminder(context, addedNote)
            Log.d("NoteRepository", "Note added successfully with ID: ${noteDoc.id}, data: $addedNote")
            ResultStatus.Success(addedNote)
        } catch (e: FirebaseFirestoreException) {
            Log.e("NoteRepository", "Firestore error: ${e.code}, ${e.message}", e)
            ResultStatus.Error(e.message ?: "Firestore error")
        } catch (e: Exception) {
            Log.e("NoteRepository", "Unexpected error: ${e.message}", e)
            ResultStatus.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun updateNote(context: Context, note: Note, imageUri: Uri? = null): ResultStatus<Note> = withContext(Dispatchers.IO) {
        val noteId = note.id
        if (noteId.isNullOrEmpty()) {
            Log.e("NoteRepository", "Failed to update note: Null or empty Note ID. Note data: $note")
            return@withContext ResultStatus.Error("Null or empty Note ID")
        }
        try {
            var updatedNote = note
            // Lưu ảnh cục bộ nếu có
            imageUri?.let { uri ->
                val imagePath = saveImageToLocal(context, uri)
                updatedNote = updatedNote.copy(imagePath = imagePath)
            }

            noteCollection.document(noteId).set(updatedNote).await()
            setReminder(context, updatedNote)
            Log.d("NoteRepository", "Note updated successfully: $noteId, data: $updatedNote")
            ResultStatus.Success(updatedNote)
        } catch (e: FirebaseFirestoreException) {
            val errorMessage = when (e.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> "Permission denied: ${e.message}"
                FirebaseFirestoreException.Code.NOT_FOUND -> "Note not found: $noteId"
                else -> e.message ?: "Unknown Firestore error"
            }
            Log.e("NoteRepository", "Failed to update note: $errorMessage, note: $note", e)
            ResultStatus.Error(errorMessage)
        } catch (e: SecurityException) {
            Log.e("NoteRepository", "SecurityException: Cannot schedule exact alarm. ${e.message}")
            ResultStatus.Error("Không thể đặt lịch báo thức. Vui lòng cấp quyền trong cài đặt.")
        } catch (e: Exception) {
            Log.e("NoteRepository", "Unexpected error updating note: ${e.message}, note: $note", e)
            ResultStatus.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun deleteNote(noteId: String): EmptyResult = withContext(Dispatchers.IO) {
        try {
            noteCollection.document(noteId).delete().await()
            Log.d("NoteRepository", "Note deleted successfully: $noteId")
            EmptyResult.Success()
        } catch (e: FirebaseFirestoreException) {
            val errorMessage = when (e.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> "Permission denied: ${e.message}"
                FirebaseFirestoreException.Code.NOT_FOUND -> "Note not found: $noteId"
                else -> e.message ?: "Unknown Firestore error"
            }
            Log.e("NoteRepository", "Failed to delete note: $errorMessage", e)
            EmptyResult.Error(errorMessage)
        } catch (e: Exception) {
            Log.e("NoteRepository", "Unexpected error deleting note: ${e.message}", e)
            EmptyResult.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun searchNotes(query: String): ResultStatus<MutableList<Note>> = withContext(Dispatchers.IO) {
        val userId = currentUserUid
        if (userId == null) {
            Log.e("NoteRepository", "User not authenticated")
            return@withContext ResultStatus.Error("User not authenticated")
        }
        try {
            Log.d("NoteRepository", "Searching notes with userId: $userId, query: $query")
            val result = noteCollection
                .whereEqualTo(NOTE_ADDED_BY_UID_FIELD, userId)
                .whereGreaterThanOrEqualTo("title", query)
                .whereLessThanOrEqualTo("title", query + "\uf8ff")
                .orderBy("title")
                .orderBy(NOTE_DATE_FIELD, Query.Direction.DESCENDING)
                .orderBy(NOTE_TIME_FIELD, Query.Direction.DESCENDING)
                .get()
                .await()
            val notes = result.documents.mapNotNull { doc ->
                val note = doc.toObject(Note::class.java)?.copy(id = doc.id)
                Log.d("NoteRepository", "Search document ${doc.id}: $note")
                note
            }.toMutableList()
            Log.d("NoteRepository", "Found ${notes.size} notes matching query: $query, data: $notes")
            ResultStatus.Success(notes)
        } catch (e: FirebaseFirestoreException) {
            val errorMessage = when (e.code) {
                FirebaseFirestoreException.Code.FAILED_PRECONDITION -> {
                    if (e.message?.contains("index is currently building") == true) {
                        "Index is being built. Please try again later: ${e.message}"
                    } else {
                        "Query requires an index: ${e.message}"
                    }
                }
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> "Permission denied: ${e.message}"
                else -> e.message ?: "Unknown Firestore error"
            }
            Log.e("NoteRepository", "Failed to search notes: $errorMessage", e)
            ResultStatus.Error(errorMessage)
        } catch (e: Exception) {
            Log.e("NoteRepository", "Unexpected error searching notes: ${e.message}", e)
            ResultStatus.Error(e.message ?: "Unknown error")
        }
    }

    private fun setReminder(context: Context, note: Note) {
        note.reminderTime?.let { reminderTime ->
            val currentTime = System.currentTimeMillis()
            if (reminderTime <= currentTime) {
                Log.w("NoteRepository", "Reminder time ($reminderTime) is in the past. Current time: $currentTime")
                return
            }

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra("NOTE_TITLE", note.title)
                putExtra("NOTE_ID", note.id)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, note.id.hashCode(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminderTime,
                        pendingIntent
                    )
                    Log.d("NoteRepository", "Exact alarm set for note ${note.id} at $reminderTime")
                } else {
                    Log.w("NoteRepository", "Cannot schedule exact alarm for note ${note.id}. Permission denied.")
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
                )
                Log.d("NoteRepository", "Alarm set for note ${note.id} at $reminderTime")
            }
        }
    }

    private fun saveImageToLocal(context: Context, imageUri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val file = File(context.filesDir, "note_images/${UUID.randomUUID()}.jpg")
            file.parentFile?.mkdirs()
            inputStream?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            Log.e("NoteRepository", "Failed to save image: ${e.message}", e)
            null
        }
    }
}