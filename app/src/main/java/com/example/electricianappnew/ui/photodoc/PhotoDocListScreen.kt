package com.example.electricianappnew.ui.photodoc

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items // Correct LazyGrid items import
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage // Correct import
// import coil3.request.ImageRequest // Removed as unused
// import coil3.compose.LocalPlatformContext // Removed as unused
import com.example.electricianappnew.data.model.PhotoDoc
import com.example.electricianappnew.ui.photodoc.viewmodel.PhotoDocListViewModel
import com.example.electricianappnew.ui.theme.ElectricianAppNewTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PhotoDocListScreen(
    modifier: Modifier = Modifier,
    viewModel: PhotoDocListViewModel = hiltViewModel(),
    onAddPhotoClick: () -> Unit,
    onPhotoClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddPhotoClick) {
                Icon(Icons.Default.AddAPhoto, contentDescription = "Add Photo")
            }
        }
    ) { paddingValues ->
        Column(modifier = modifier.padding(paddingValues).padding(16.dp)) {
            Text("Photo Documentation", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.height(16.dp))

            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                uiState.errorMessage != null -> {
                    Text(
                        text = uiState.errorMessage ?: "Unknown error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                uiState.photos.isEmpty() -> {
                    Text("No photos found.", modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 128.dp),
                        contentPadding = PaddingValues(4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.photos, key = { it.id }) { photo ->
                            PhotoGridItem(
                                photo = photo,
                                dateFormatter = dateFormatter,
                                onClick = { onPhotoClick(photo.id) },
                                onDeleteClick = { viewModel.deletePhoto(photo) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoGridItem(
    photo: PhotoDoc,
    dateFormatter: SimpleDateFormat,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.aspectRatio(1f)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Simplified AsyncImage call for diagnostics (no builder, no crossfade)
            AsyncImage(
                model = photo.filePath, // Pass path directly
                contentDescription = photo.caption ?: "Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(4.dp)
                    .align(Alignment.BottomStart)
            ) {
                Column {
                    Text(
                        text = photo.caption ?: "No Caption",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                     Text(
                        text = dateFormatter.format(photo.dateTaken),
                        color = Color.LightGray,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
             IconButton(
                onClick = onDeleteClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(24.dp)
                    .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete Photo",
                    tint = Color.White
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PhotoDocListScreenPreview() {
    ElectricianAppNewTheme {
        // Provide dummy data or mock ViewModel for a better preview if needed
        PhotoDocListScreen(onAddPhotoClick = {}, onPhotoClick = {})
    }
}
