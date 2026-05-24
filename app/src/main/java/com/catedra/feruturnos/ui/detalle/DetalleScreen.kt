package com.catedra.feruturnos.ui.detalle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.catedra.feruturnos.data.model.Pelicula
import com.catedra.feruturnos.data.repository.PeliculasRepositorio

/**
 * Pantalla de detalle de una película.
 *
 * Este archivo está COMPLETO. Usalo como referencia para entender:
 * - Cómo se reciben argumentos de navegación como parámetros del composable
 * - Cómo se usa LaunchedEffect para cargar datos al entrar a la pantalla
 * - Cómo se gestiona el estado local con remember y mutableStateOf
 * - Cómo se estructura un Scaffold con TopAppBar y botón de retroceso
 * - Cómo se usa collectAsStateWithLifecycle (ver PeliculasScreen.kt)
 *
 * Observá que NO hay:
 * - Ningún método observe()
 * - Ningún adapter.submitList()
 * - Ningún código de actualización manual de elementos visuales
 * Eso es lo que Compose reemplaza respecto al modelo tradicional del Lab 2A.
 *
 * No modificar este archivo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleScreen(
    peliculaId: String,
    onVolver: () -> Unit
) {
    val repositorio = remember { PeliculasRepositorio() }
    var pelicula by remember { mutableStateOf<Pelicula?>(null) }
    var cargando by remember { mutableStateOf(true) }

    // LaunchedEffect se ejecuta cuando el composable entra en composición.
    // Si peliculaId cambia, cancela el efecto anterior y lanza uno nuevo.
    // Se cancela automáticamente cuando el composable sale del árbol.
    LaunchedEffect(peliculaId) {
        cargando = true
        pelicula = repositorio.obtenerPelicula(peliculaId)
        cargando = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(pelicula?.titulo ?: "Detalle") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        if (cargando) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            pelicula?.let { p ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = p.titulo,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "Dir. ${p.director}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${p.anio}  ·  ${p.genero}  ·  ${p.duracionMinutos} min",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = p.descripcion,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.4f
                    )
                }
            }
        }
    }
}
