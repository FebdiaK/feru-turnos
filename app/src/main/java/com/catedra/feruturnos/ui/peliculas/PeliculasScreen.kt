package com.catedra.feruturnos.ui.peliculas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.catedra.feruturnos.data.model.Pelicula

/**
 * Pantalla de listado de películas.
 *
 * Este composable es STATEFUL: obtiene el ViewModel y recolecta el estado.
 * No modificar la estructura de este composable — completar solo PeliculaItem más abajo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeliculasScreen(
    onNavegar: (String) -> Unit,
    onCerrarSesion: () -> Unit,
    viewModel: PeliculasViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Peliculas") },
                actions = {
                    IconButton(onClick = onCerrarSesion) {
                        Icon(Icons.Default.ExitToApp, "Cerrar sesion")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.cargando -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        TextButton(onClick = { viewModel.cargarPeliculas() }) {
                            Text("Reintentar")
                        }
                    }
                }
            }

            else -> {
                PeliculasContenido(
                    uiState = uiState,
                    onConsultaCambia = { viewModel.actualizarBusqueda(it) },
                    onPeliculaClick = { onNavegar(it.id) },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

/**
 * Contenido principal de la pantalla: campo de búsqueda + lista.
 *
 * Este composable es STATELESS: recibe todo lo que necesita como parámetros.
 * No modificar este composable.
 */
@Composable
fun PeliculasContenido(
    uiState: PeliculasUiState,
    onConsultaCambia: (String) -> Unit,
    onPeliculaClick: (Pelicula) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {

        OutlinedTextField(
            value = uiState.consulta,
            onValueChange = onConsultaCambia,
            placeholder = { Text("Buscar películas...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyColumn {
            items(
                items = uiState.peliculasFiltradas,
                key = { it.id }
            ) { pelicula ->
                PeliculaItem(
                    pelicula = pelicula,
                    onClick = { onPeliculaClick(pelicula) }
                )
            }
        }
    }
}

/**
 * Ítem individual de la lista de películas.
 *
 * ETAPA 1 DEL LAB: completar este composable.
 *
 * Comparación con el Lab 2A:
 * En el lab anterior, la representación visual de cada película estaba dividida en
 * dos archivos: item_pelicula.xml (estructura) y PeliculaAdapter.kt (datos).
 * Acá, ambas responsabilidades viven en esta única función composable.
 *
 * Este composable es STATELESS: recibe una Pelicula y un callback onClick.
 * No gestiona ningún estado propio.
 *
 * TODO Etapa 1: reemplazar el comentario TODO con el código del composable.
 * Usar ListItem con:
 *   - headlineContent:    Text con el título de la película
 *   - supportingContent:  Text con "${pelicula.anio} · ${pelicula.genero}"
 *   - trailingContent:    Icon(Icons.Default.ChevronRight, contentDescription = null)
 * Aplicar modifier.clickable { onClick() } al ListItem.
 * Agregar HorizontalDivider() después del ListItem.
 *
 * Pista: el modifier que recibe este composable debe aplicarse al ListItem,
 * no crear uno nuevo. Así el composable padre puede controlarlo desde afuera.
 */
@Composable
fun PeliculaItem(
    pelicula: Pelicula,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
     ListItem(
         headlineContent = { Text(text = pelicula.titulo) },
         supportingContent = { Text(text = "${pelicula.anio} · ${pelicula.genero}") },
         trailingContent = { Icon(
             imageVector = Icons.Default.ChevronRight,
             contentDescription = null
         ) },
         modifier = modifier.clickable { onClick() }
     )
     HorizontalDivider()
}
