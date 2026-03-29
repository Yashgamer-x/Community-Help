package com.unh.communityhelp.mainmenu.model

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Exclude

data class HelpRequest(
    @get:Exclude val id: String = "",
    val authorId: String = "",
    val authorName: String = "Loading...",
    val title: String = "",
    val description: String = "",
    val image: String = "",
    val location: String = "",
    val category: String = "",
    val createdAt: Timestamp? = null,
    // THE SELF-REFERENCE FIELD
    @get:Exclude var selfRef: DocumentReference? = null
)


fun HelpRequest.decodeImage(): ImageBitmap? {
    if (image.isBlank()) return null
    return try {
        val decodedBytes = android.util.Base64.decode(image, android.util.Base64.DEFAULT)
        android.graphics
            .BitmapFactory
            .decodeByteArray(decodedBytes, 0, decodedBytes.size)?.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}