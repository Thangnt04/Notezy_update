package com.example.noteapp.data.models

import android.os.Parcelable
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize


@IgnoreExtraProperties
@Parcelize
data class Note(
    var title: String? = null,
    val dateOfCreation: String? = null,
    val timeOfCreation: String? = null,
    var dateOfUpdate: String? = null,
    var timeOfUpdate: String? = null,
    var description: String? = null,
    val addByUid: String? = null,
    var id: String? = null,
    var UpdatedNote: Boolean = false,
    var reminderTime: Long? = null,
    var Finished: Boolean = false,
    var category: String? = null,
    var imageBase64: String? = null // Lưu chuỗi Base64 của ảnh
) : Parcelable