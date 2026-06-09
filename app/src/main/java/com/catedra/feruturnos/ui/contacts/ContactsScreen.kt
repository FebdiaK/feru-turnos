package com.catedra.feruturnos.ui.contacts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.catedra.feruturnos.R

@Composable
fun ContactsScreen(
    contacts: List<ContactUser>,
    isInviting: Boolean = false,
    onInviteClick: (List<ContactUser>) -> Unit
) {

    var selectedContacts by remember { mutableStateOf<List<ContactUser>>(emptyList()) }

    Box(modifier = Modifier.fillMaxSize()) {

        if (contacts.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.no_tenes_contactos_agregados))
            }
            return
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 16.dp,
                end = 16.dp,
                bottom = 88.dp
            )
        ) {
            items(contacts) { friend ->

                val isSelected = selectedContacts.contains(friend)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    CompositionLocalProvider(
                        LocalTextStyle provides MaterialTheme.typography.titleMedium
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            AsyncImage(
                                model = friend.photo,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(friend.name)

                                    Text(
                                        text = "#${friend.contactId}",
                                        color = Color.Gray,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }

                                Text(friend.celphone, style = MaterialTheme.typography.bodyMedium)
                            }

                            if (isInviting) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        selectedContacts = if (checked) {
                                            selectedContacts + friend
                                        } else {
                                            selectedContacts - friend
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        if (isInviting) {
            Button(
                onClick = { onInviteClick(selectedContacts) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                enabled = selectedContacts.isNotEmpty()
            ) {
                Text(stringResource(R.string.invitar))
            }
        }
    }
}