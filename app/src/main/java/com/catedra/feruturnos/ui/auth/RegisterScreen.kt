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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.catedra.feruturnos.R

@Composable
fun RegisterScreen(
    authState: AuthState,
    onRegistrar: (
        email: String,
        password: String,
        name: String,
        celphone: Int,
        address: String,
        photoUri: Uri?
    ) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var celphoneText by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> photoUri = uri }

    val nombreValido = name.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$"))
    val celularValido = celphoneText.length >= 10
    val celphone = celphoneText.toIntOrNull()

    val formValido =
        email.isNotBlank() &&
        password.isNotBlank() &&
        address.isNotBlank() &&
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
            contentDescription = "Logo",
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text(
            text = "Crear cuenta",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        //* NOMBRE */
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Ingrese su nombre completo") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        if (name.isNotBlank() && !nombreValido) {
            Text(
                text = "Solo se permiten letras",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )
        }

        //* EMAIL */
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Ingrese su email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        //* CONTRASEÑA */
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Ingrese su contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        //* CELULAR */
        OutlinedTextField(
            value = celphoneText,
            onValueChange = { celphoneText = it.filter { char -> char.isDigit() } },
            label = { Text("Ingrese su celular") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = celphoneText.isNotBlank() && !celularValido,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        if (celphoneText.isNotBlank() && !celularValido) {
            Text(
                text = "Debe tener al menos 10 dígitos",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            )
        }

        //* DIRECCIÓN */
        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Ingrese su dirección") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        //* FOTO */
        Button(
            onClick = {
                photoPicker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Text("Elegir foto")
        }

        photoUri?.let { uri ->
            AsyncImage(
                model = uri,
                contentDescription = "Foto seleccionada",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .padding(bottom = 12.dp),
                contentScale = ContentScale.Crop
            )
        }

        Button(
            onClick = {
                onRegistrar(
                    email,
                    password,
                    name,
                    celphone ?: 0,
                    address,
                    photoUri
                )
            },
            enabled = formValido,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrarme")
        }

        if (authState is AuthState.Error) {
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