package com.catedra.feruturnos.ui.peliculas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catedra.feruturnos.data.repository.PeliculasRepositorio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import android.util.Log

class PeliculasViewModel : ViewModel() {
    private val repositorio = PeliculasRepositorio()
    private val consulta = MutableStateFlow("")
    val uiState: StateFlow<PeliculasUiState> =
        combine(repositorio.obtenerPeliculas(), consulta) { peliculas, consulta ->
            PeliculasUiState(
                peliculas = peliculas,
                consulta = consulta,
                cargando = false
            )
        }
            .catch {
                emit(PeliculasUiState(cargando = false, error = "No se pudieron cargar las peliculas"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = PeliculasUiState(cargando = true)
            )

    fun cargarPeliculas() {
    /**uiState.update {
            it.copy( cargando = true, error = null )
        }
        viewModelScope.launch {
            try {
                val resultado = repositorio.obtenerPeliculas()
                uiState.update {
                    it.copy( peliculas = resultado, cargando = false )
                }
            } catch (e: Exception) {
                uiState.update {
                    it.copy( error = e.message, cargando = false )
                }
            }
        }*/
    }


    fun actualizarBusqueda(nuevaConsulta: String) {
        consulta.value = nuevaConsulta
    }
}
