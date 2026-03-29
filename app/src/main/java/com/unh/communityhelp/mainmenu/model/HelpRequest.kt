package com.unh.communityhelp.mainmenu.model

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.firebase.Timestamp

data class HelpRequest(
    val id: String = "",
    val authorId: String = "",
    val title: String = "",
    val description: String = "",
    val image: String = "", // Matches your (string) image field
    val location: String = "",
    var authorName: String = "Loading...",
    val createdAt: Timestamp? = null
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