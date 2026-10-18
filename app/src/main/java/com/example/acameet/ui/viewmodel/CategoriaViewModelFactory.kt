
package com.example.acameet.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.acameet.data.repository.CategoriaRepository
import kotlinx.coroutines.flow.StateFlow

class CategoriaViewModelFactory(
    private val repository: CategoriaRepository,
    private val usuarioIdActivo: StateFlow<Int?>
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(CategoriaViewModel::class.java)) {
            return CategoriaViewModel(
                repository = repository,
                usuarioIdActivo = usuarioIdActivo
            ) as T
        }

        throw IllegalArgumentException(
            "ViewModel desconocido: ${modelClass.name}"
        )
    }
}
