package com.catedra.feruturnos.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview(showBackground = true)
@Composable
fun AuthScreenPreview() {
    AuthScreen(
        authState = AuthState.NoAutenticado,
        onIniciarSesion = { _, _ -> },
        onRegistrar = { _, _ -> }
    )
}