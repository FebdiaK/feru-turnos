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
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.border
import androidx.compose.ui.res.stringResource
import com.catedra.feruturnos.R

@Composable
fun ProfileContent(
    profileState: ProfileState,
    onNavigateToContacts: () -> Unit
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

        if (profileState.photo.isNotBlank()) {
            AsyncImage(
                model = profileState.photo,
                contentDescription = "Foto de perfil",
                modifier = Modifier
                    .padding(24.dp)
                    .size(144.dp)
                    .clip(CircleShape)
                    .border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.secondary,
                        shape = CircleShape
                    ),
                contentScale = ContentScale.Crop
            )
        }

        Text(
            text = profileState.name,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 24.dp),
            textAlign = TextAlign.Center
        )
        Text(
            text = "# ${profileState.contactId}",
            color = MaterialTheme.colorScheme.tertiary,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(profileState.email)
        Text("${profileState.celphone}")

        /**if (profileState.stars.isNotEmpty()) {
            val promedio = profileState.stars.average()
            Text( "Puntuación ${String.format("%.1f", promedio)}★" )
        }*/

        Button(
            onClick = {
                onNavigateToContacts()
            },
            enabled = true,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top= 16.dp)
        ) {
            Text(stringResource(R.string.contactos))
        }
    }
}
/**
@Preview(showBackground = true)
@Composable
fun ProfileContentPreview() {
    ProfileContent(
        profileState = ProfileState(
            uid = "12gfdfdf34fhdggfd56",
            contactId = "123456",
            name = "Casandra Marisel Elizondo",
            email = "casandra@email.com",
            address = "Berazategui",
            photo = "https://s3.ppllstatics.com/canarias7/www/multimedia/201704/14/media/cortadas/462076-1g_CSN462076_MG3928385--1248x702.jpg",
            celphone = 1122334455,
            friends = []
        )
    )
}*/