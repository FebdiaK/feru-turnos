package com.catedra.feruturnos

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.catedra.feruturnos.ui.auth.AuthScreen
import com.catedra.feruturnos.ui.auth.AuthState
import com.catedra.feruturnos.ui.auth.AuthViewModel
import com.catedra.feruturnos.ui.auth.RegisterScreen
import com.catedra.feruturnos.ui.navigation.AppNavigation
import com.catedra.feruturnos.ui.theme.FeruTurnosTheme
import androidx.compose.ui.platform.LocalContext
import android.Manifest
import android.os.Build
import androidx.core.app.ActivityCompat
import android.content.Context
import com.catedra.feruturnos.ui.settings.LanguagePreferences
import com.catedra.feruturnos.ui.settings.LocaleHelper

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun attachBaseContext(newBase: Context) {
        val language = LanguagePreferences.getLanguage(newBase)
        val context = LocaleHelper.applyLanguage(newBase, language)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1001
            )
        }

        setContent {
            FeruTurnosTheme {
                val authState by authViewModel.authState.collectAsStateWithLifecycle()
                val context = LocalContext.current

                when (authState) {
                    is AuthState.NoAutenticado, is AuthState.Error -> {
                        val authNavController = rememberNavController()

                        NavHost(
                            navController = authNavController,
                            startDestination = "login"
                        ) {
                            composable("login") {
                                AuthScreen(
                                    authState = authState,
                                    onIniciarSesion = { email, password ->
                                        authViewModel.iniciarSesion(email, password)
                                    },
                                    onIrARegistro = {
                                        authNavController.navigate("register")
                                    }
                                )
                            }

                            composable("register") {
                                RegisterScreen(
                                    authState = authState,
                                    onRegistrar = {
                                        email,
                                        password,
                                        name,
                                        celphone,
                                        photoUri
                                        ->
                                        authViewModel.limpiarError()
                                        authViewModel.registrar(
                                            context = context,
                                            email = email,
                                            password = password,
                                            name = name,
                                            celphone = celphone,
                                            photoUri = photoUri
                                        )
                                    }
                                )
                            }
                        }
                    }

                    is AuthState.Autenticado -> {
                        AppNavigation(
                            startDestination = if (intent.getBooleanExtra("openNotifications", false)) {
                                "Notificaciones"
                            } else {
                                "Inicio"
                            },
                            onCerrarSesion = {
                                authViewModel.cerrarSesion()
                            }
                        )
                    }
                }
            }
        }
    }
}