package com.catedra.feruturnos.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun NotificationsScreen(
    onNotificationClick: (String) -> Unit
) {
    var notifications by remember { mutableStateOf<List<AppNotification>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }

    val uid = Firebase.auth.currentUser?.uid
    val scope = rememberCoroutineScope()

    LaunchedEffect(uid) {
        if (uid != null) {
            try {
                val result = Firebase.firestore
                    .collection("notifications")
                    .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()

                val allNotifications = result.documents.map { doc ->
                    doc.toAppNotification()
                }

                notifications = allNotifications.filter { notification ->
                    notification.relatedUsers.any { relatedUser ->
                        relatedUser.userId == uid
                    }
                }

            } catch (e: Exception) {
                error = e.message ?: "Error cargando notificaciones"
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        } else {
            error = "No hay usuario logueado"
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

    if (error.isNotBlank()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(error)
        }
        return
    }

    if (notifications.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("No tenés notificaciones")
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(notifications) { notification ->
            NotificationItem(
                notification = notification,
                currentUserId = uid ?: "",
                onClick = {
                    if (uid != null) {
                        scope.launch {
                            val updatedRelatedUsers =
                                notification.relatedUsers.map { relatedUser ->
                                    if (relatedUser.userId == uid) {
                                        relatedUser.copy(read = true)
                                    } else {
                                        relatedUser
                                    }
                                }

                            val firebaseRelatedUsers = updatedRelatedUsers.map { relatedUser ->
                                hashMapOf(
                                    "userId" to relatedUser.userId,
                                    "read" to relatedUser.read
                                )
                            }

                            Firebase.firestore
                                .collection("notifications")
                                .document(notification.id)
                                .update(
                                    "relatedUsers",
                                    firebaseRelatedUsers
                                )
                                .await()

                            notifications = notifications.map { item ->
                                if (item.id == notification.id) {
                                    item.copy(
                                        relatedUsers = updatedRelatedUsers
                                    )
                                } else {
                                    item
                                }
                            }
                            onNotificationClick(notification.reservationId)
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun NotificationItem(
    notification: AppNotification,
    currentUserId: String,
    onClick: () -> Unit
) {
    val currentUserRead =
        notification.relatedUsers
            .firstOrNull { it.userId == currentUserId }
            ?.read ?: false

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = 4.dp,
                color = if (!currentUserRead) {
                    MaterialTheme.colorScheme.secondary
                } else {
                    Color.White
                }
            )
            .background(
                if (currentUserRead) {
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
            fontWeight = if (currentUserRead) FontWeight.Normal else FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = notification.message,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (currentUserRead) FontWeight.Normal else FontWeight.Medium
        )
    }

    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .background(Color(0xFFF2F2F2))
    )
}

private fun DocumentSnapshot.toAppNotification(): AppNotification {
    val relatedUsersRaw = get("relatedUsers") as? List<*> ?: emptyList<Any>()

    val relatedUsers = relatedUsersRaw.mapNotNull { item ->
        val userMap = item as? Map<*, *>

        if (userMap != null) {
            RelatedNotificationUser(
                userId = userMap["userId"] as? String ?: "",
                read = userMap["read"] as? Boolean ?: false
            )
        } else {
            null
        }
    }

    return AppNotification(
        id = id,
        title = getString("title") ?: "",
        message = getString("message") ?: "",
        reservationId = getString("reservationId") ?: "",
        relatedUsers = relatedUsers,
        createdAt = getTimestamp("createdAt")
    )
}