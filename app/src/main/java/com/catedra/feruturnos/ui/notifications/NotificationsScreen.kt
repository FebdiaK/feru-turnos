package com.catedra.feruturnos.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import androidx.compose.foundation.clickable
import kotlinx.coroutines.launch

@Composable
fun NotificationsScreen(
    onNotificationClick: (String) -> Unit
) {
    var notifications by remember { mutableStateOf<List<AppNotification>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val uid = Firebase.auth.currentUser?.uid

    LaunchedEffect(uid) {
        if (uid != null) {
            try {
                val result = Firebase.firestore
                    .collection("notifications")
                    .whereEqualTo("userId", uid)
                    .get()
                    .await()

                notifications = result.documents.mapNotNull { doc ->
                    doc.toObject(AppNotification::class.java)?.copy(
                        id = doc.id
                    )
                }

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
        }
        return
    }

    if (notifications.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No tenés notificaciones")
        }
        return
    }

    val scope = rememberCoroutineScope()

    LazyColumn {
        items(notifications) { notification ->

            NotificationItem(
                notification = notification,
                onClick = {
                    scope.launch {

                        Firebase.firestore
                            .collection("notifications")
                            .document(notification.id)
                            .update("read", true)
                            .await()

                        onNotificationClick(
                            notification.reservationId
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun NotificationItem(notification: AppNotification, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable {
                onClick()
            }
            .fillMaxWidth()
            .border(
                width = 4.dp,
                color = if (!notification.read) {
                    MaterialTheme.colorScheme.secondary
                } else {
                    Color.White
                }
            )
            .background(
                if (notification.read) {
                    Color.White
                } else {
                    MaterialTheme.colorScheme.tertiary
                }
            )
            .padding(16.dp)
    ) {
        Text(
            text = notification.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (notification.read) FontWeight.Normal else FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = notification.message,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (notification.read) FontWeight.Normal else FontWeight.Medium
        )
    }

    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .background(Color(0xFFF2F2F2))
    )
}
/**
@Preview(showBackground = true)
@Composable
fun NotificationsScreenPreview() {
    NotificationsScreen()
}*/