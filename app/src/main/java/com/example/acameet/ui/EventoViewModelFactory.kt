
package com.example.acameet.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.acameet.data.repository.EventoRepository
import kotlinx.coroutines.flow.StateFlow

class EventoViewModelFactory(
    private val repository: EventoRepository,
    private val usuarioIdActivo: StateFlow<Int?>
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(EventoViewModel::class.java)) {
            return EventoViewModel(
                repository = repository,
                usuarioIdActivo = usuarioIdActivo
            ) as T
        }

        throw IllegalArgumentException(
            "ViewModel desconocido: ${modelClass.name}"
        )
    }
}
