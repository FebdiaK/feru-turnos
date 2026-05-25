package com.catedra.feruturnos.ui.profile

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import androidx.compose.runtime.*
import kotlinx.coroutines.tasks.await

@Composable
fun ProfileScreen() {

    val uid = FirebaseAuth.getInstance().currentUser?.uid
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var photo by remember { mutableStateOf("") }
    var celphone by remember { mutableStateOf(0) }

    LaunchedEffect(uid) {
        if (uid != null) {
            val document = Firebase.firestore
                .collection("users")
                .document(uid)
                .get()
                .await()
            name = document.getString("name") ?: ""
            email = document.getString("email") ?: ""
            address = document.getString("address") ?: ""
            photo = document.getString("photo") ?: ""
            celphone = document.getLong("celphone")?.toInt() ?: 0
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(text = "Pantalla de perfil")
        Text(text = "Id: $uid")
        Text(text = "Nombre: $name")
        Text(text = "Email: $email")
        Text(text = "Dirección: $address")
        Text(text = "Foto: $photo")
        Text(text = "Celu: $celphone")
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen()
}