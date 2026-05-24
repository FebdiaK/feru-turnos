package com.catedra.feruturnos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.catedra.feruturnos.ui.auth.AuthScreen
import com.catedra.feruturnos.ui.auth.AuthState
import com.catedra.feruturnos.ui.auth.AuthViewModel
import com.catedra.feruturnos.ui.navigation.AppNavigation
import com.catedra.feruturnos.ui.theme.FeruTurnosTheme

/**
 * Activity contenedora principal.
 * Su única responsabilidad es inicializar el árbol de composables.
 * No modificar este archivo.
 */
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FeruTurnosTheme {
                val authState by
                authViewModel.authState.collectAsStateWithLifecycle()
                // when sobre el estado de autenticacion:
                // el compilador garantiza que todos los casos estan cubiertos
                when (authState) {
                    is AuthState.NoAutenticado, is AuthState.Error -> {
                        AuthScreen(
                            authState = authState,
                            onIniciarSesion = { email, password ->
                                authViewModel.iniciarSesion(email, password)
                            },
                            onRegistrar = { email, password ->
                                authViewModel.registrar(email, password)
                            }
                        )
                    }
                    is AuthState.Autenticado -> {
                        AppNavigation(
                            onCerrarSesion = { authViewModel.cerrarSesion() }
                        )
                    }
                }
            }
        }
    }
}
