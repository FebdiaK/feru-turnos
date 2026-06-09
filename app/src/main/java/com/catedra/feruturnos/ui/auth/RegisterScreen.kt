package com.catedra.feruturnos.ui.auth

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.catedra.feruturnos.R
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream

@Composable
fun RegisterScreen(
    authState: AuthState,
    onRegistrar: (
        email: String,
        password: String,
        name: String,
        celphone: String,
        photoUri: Uri?
    ) -> Unit
) {
    var isRegistering by remember { mutableStateOf(false) }
    var ocultarError by remember { mutableStateOf(false) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var celphoneText by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(authState) {
        if (authState is AuthState.Error) {
            isRegistering = false
            ocultarError = false
        }

        if (authState is AuthState.Autenticado) {
            isRegistering = false
        }
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        photoUri = uri
        ocultarError = true
    }

    val cameraPreviewLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            photoUri = bitmapToUri(context, bitmap)
            ocultarError = true
        }
    }

    val nombreValido = name.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$"))
    val celularValido = celphoneText.length >= 10
    val celphone = celphoneText.toIntOrNull()

    val formValido =
        email.isNotBlank() &&
                password.isNotBlank() &&
                photoUri != null &&
                nombreValido &&
                celularValido &&
                celphone != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_medium),
            contentDescription = stringResource(R.string.logo),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text(
            text = stringResource(R.string.crear_cuenta),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                ocultarError = true
            },
            label = {
                Text(stringResource(R.string.ingrese_su_nombre_completo))
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        if (name.isNotBlank() && !nombreValido) {
            Text(
                text = stringResource(R.string.solo_se_permiten_letras),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )
        }

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                ocultarError = true
                isRegistering = false
            },
            label = {
                Text(stringResource(R.string.ingrese_su_email))
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                ocultarError = true
                isRegistering = false
            },
            label = {
                Text(stringResource(R.string.ingrese_su_contrasena))
            },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        OutlinedTextField(
            value = celphoneText,
            onValueChange = {
                celphoneText = it.filter { char -> char.isDigit() }
                ocultarError = true
                isRegistering = false
            },
            label = {
                Text(stringResource(R.string.ingrese_su_celular))
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            isError = celphoneText.isNotBlank() && !celularValido,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        if (celphoneText.isNotBlank() && !celularValido) {
            Text(
                text = stringResource(R.string.debe_tener_al_menos_10_digitos),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    cameraPreviewLauncher.launch(null)
                },
                enabled = !isRegistering,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.camara))
            }

            Button(
                onClick = {
                    photoPicker.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                },
                enabled = !isRegistering,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.galeria))
            }
        }

        photoUri?.let { uri ->
            AsyncImage(
                model = uri,
                contentDescription = stringResource(R.string.foto_seleccionada),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .padding(bottom = 12.dp),
                contentScale = ContentScale.Crop
            )
        }

        Button(
            onClick = {
                ocultarError = true
                isRegistering = true

                onRegistrar(
                    email,
                    password,
                    name,
                    celphoneText,
                    photoUri
                )
            },
            enabled = formValido && !isRegistering,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isRegistering) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(stringResource(R.string.registrarme))
            }
        }

        if (authState is AuthState.Error && !ocultarError && !isRegistering) {
            Text(
                text = authState.mensaje,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 16.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

fun bitmapToUri(
    context: android.content.Context,
    bitmap: Bitmap
): Uri {
    val file = File(context.cacheDir, "profile_photo_preview.jpg")

    FileOutputStream(file).use { outputStream ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
    }

    return Uri.fromFile(file)
}