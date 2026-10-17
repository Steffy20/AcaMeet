package com.example.acameet.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.acameet.data.local.entity.Lugar
import com.example.acameet.data.repository.LugarRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LugarViewModel(
    private val repository: LugarRepository
) : ViewModel() {

    // Lista de lugares almacenados en Room
    val lugares: StateFlow<List<Lugar>> =
        repository.obtenerLugares()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // Registrar un lugar
    fun registrarLugar(
        lugar: Lugar,
        onResultado: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            repository.insertarLugar(lugar)

            onResultado(
                true,
                "Lugar registrado correctamente"
            )
        }
    }

    // Buscar un lugar por ID
    fun obtenerLugarPorId(
        id: Int,
        onResultado: (Lugar?) -> Unit
    ) {
        viewModelScope.launch {
            val lugar = repository.obtenerLugarPorId(id)
            onResultado(lugar)
        }
    }

    // Buscar un lugar por nombre
    fun obtenerLugarPorNombre(
        nombre: String,
        onResultado: (Lugar?) -> Unit
    ) {
        viewModelScope.launch {
            val lugar = repository.obtenerLugarPorNombre(nombre)
            onResultado(lugar)
        }
    }

    // Actualizar un lugar
    fun actualizarLugar(
        lugar: Lugar,
        onResultado: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            repository.actualizarLugar(lugar)

            onResultado(
                true,
                "Lugar actualizado correctamente"
            )
        }
    }

    // Eliminar un lugar
    fun eliminarLugar(
        lugar: Lugar,
        onResultado: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            repository.eliminarLugar(lugar)

            onResultado(
                true,
                "Lugar eliminado correctamente"
            )
        }
    }
}