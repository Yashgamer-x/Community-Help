package com.unh.communityhelp.mainmenu.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unh.communityhelp.mainmenu.model.HelpRequest
import com.unh.communityhelp.mainmenu.model.decodeImage
import com.unh.communityhelp.mainmenu.viewmodel.HomeViewModel
import com.unh.communityhelp.ui.theme.CommunityHelpTheme

@Composable
fun HomeView(
    viewModel: HomeViewModel = viewModel()
) {
    // Observe the list from our ViewModel
    val requests = viewModel.helpRequests

    Box(modifier = Modifier.fillMaxSize()) {
        if (viewModel.isLoading) {
            // Show a loader while fetching from Firestore
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (requests.isEmpty()) {
            // Show a message if no tasks are found in the user's city/expertise
            Text(
                text = "No help requests found in your area.",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Now passing the real HelpRequest object to each card
                items(requests) { request ->
                    HelpTaskCard(request = request)
                }
            }
        }
    }
}

@Composable
fun HelpTaskCard(
    request: HelpRequest,
    modifier: Modifier = Modifier
) {
    // Memoize the bitmap to prevent redundant decoding during recomposition
    val imageBitmap = remember(request.image) { request.decodeImage() }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            UserHeader(userName = "Helper Needed") // You can pass request.authorName if you add it later

            TaskImage(bitmap = imageBitmap)

            TaskDetails(
                title = request.title,
                location = request.location,
                description = request.description
            )
        }
    }
}

@Composable
private fun UserHeader(userName: String) {
    Row(
        modifier = Modifier.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.padding(6.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(userName, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun TaskImage(bitmap: ImageBitmap?) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(220.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "Task visual",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.alpha(0.3f)
                    )
                    Text(
                        "No Image",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.alpha(0.5f),
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskDetails(title: String, location: String, description: String) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = location,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                modifier = Modifier.basicMarquee()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 20.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeViewPreview() {
    CommunityHelpTheme {
        // Mock data for previewing the UI without Firebase
        val mockRequest = HelpRequest(
            title = "Help with Trash Removal",
            description = "Need someone to help me remove trash from the backyard.",
            location = "West Haven, CT",
            image = ""
        )

        Column(Modifier.padding(16.dp)) {
            HelpTaskCard(request = mockRequest)
        }
    }
}