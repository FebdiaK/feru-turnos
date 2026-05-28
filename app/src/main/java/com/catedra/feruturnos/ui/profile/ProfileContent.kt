package com.catedra.feruturnos.ui.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ProfileContent(
    profileState: ProfileState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (profileState.isLoading) {
            CircularProgressIndicator()
        }

        profileState.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error
            )
        }

        Text(text = "Foto: ${profileState.photo}")

        Text(
            text = profileState.name,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 24.dp),
            textAlign = TextAlign.Center
        )
        Text(
            text = "# ${profileState.uid}",
            color = MaterialTheme.colorScheme.tertiary,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(profileState.email)
        Text(profileState.address)
        Text("${profileState.celphone}")
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileContentPreview() {
    ProfileContent(
        profileState = ProfileState(
            uid = "123456",
            name = "Casandra Marisel Elizondo",
            email = "casandra@email.com",
            address = "Berazategui",
            photo = "foto.jpg",
            celphone = 1122334455
        )
    )
}