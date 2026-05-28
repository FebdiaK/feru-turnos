package com.catedra.feruturnos.ui.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import coil.compose.AsyncImage
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale

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

        if (profileState.photo.isNotBlank()) {
            AsyncImage(
                model = profileState.photo,
                contentDescription = "Foto de perfil",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
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
            photo = "https://res.cloudinary.com/dmde9k4fp/image/upload/v1779992377/profile_images/oevupbvdxhthn26nuul8.png%22(string)uid%22XEerqfgqjoe0eyodrPxUaoJmxpZ2",
            celphone = 1122334455
        )
    )
}