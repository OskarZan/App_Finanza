package com.example.financeszan.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.financeszan.ui.viewmodel.ProfileViewModel
import java.io.File

enum class LaboralStatus(val displayName: String) {
    EMPLOYED("Asalariado"),
    SELF_EMPLOYED("Autónomo"),
    STUDENT("Estudiante"),
    RETIRED("Jubilado")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(viewModel.profilePhotoUri) }
    var firstName by remember { mutableStateOf(viewModel.firstName) }
    var lastName by remember { mutableStateOf(viewModel.lastName) }
    var laboralStatus by remember { mutableStateOf(viewModel.laboralStatus) }
    var showStatusDialog by remember { mutableStateOf(false) }
    var hasChanges by remember { mutableStateOf(false) }
    
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { 
            imageUri = it
            hasChanges = true 
        }
    }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && imageUri != null) {
            hasChanges = true
        }
    }
    
    fun getTmpFileUri(): Uri {
        val tmpFile = File.createTempFile("tmp_image_file", ".png", context.cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            tmpFile
        )
    }
    
    var showChooseImageDialog by remember { mutableStateOf(false) }
    
    if (showChooseImageDialog) {
        AlertDialog(
            onDismissRequest = { showChooseImageDialog = false },
            title = { Text("Seleccionar imagen") },
            text = { Text("¿Cómo deseas añadir tu foto de perfil?") },
            confirmButton = {
                TextButton(
                    onClick = { 
                        showChooseImageDialog = false
                        imageUri = getTmpFileUri()
                        cameraLauncher.launch(imageUri)
                    }
                ) {
                    Text("Cámara")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showChooseImageDialog = false
                        photoPickerLauncher.launch("image/*")
                    }
                ) {
                    Text("Galería")
                }
            }
        )
    }
    
    if (showStatusDialog) {
        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = { Text("Situación Laboral") },
            text = {
                Column {
                    LaboralStatus.values().forEach { status ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    laboralStatus = status
                                    hasChanges = true
                                    showStatusDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = laboralStatus == status,
                                onClick = {
                                    laboralStatus = status
                                    hasChanges = true
                                    showStatusDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(status.displayName)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.saveProfileData(firstName, lastName, laboralStatus, imageUri)
                            hasChanges = false
                        },
                        enabled = hasChanges
                    ) {
                        Text("Guardar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Foto de perfil
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .clickable { showChooseImageDialog = true },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Añadir foto",
                        modifier = Modifier.size(50.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Nombre y apellidos
            Text(
                text = "Datos personales",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = firstName,
                onValueChange = { 
                    firstName = it
                    hasChanges = true
                },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = lastName,
                onValueChange = { 
                    lastName = it
                    hasChanges = true
                },
                label = { Text("Apellidos") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Situación laboral
            Text(
                text = "Situación laboral",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 8.dp)
            )
            
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showStatusDialog = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = laboralStatus?.displayName ?: "Seleccionar",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Seleccionar"
                    )
                }
            }
        }
    }
} 