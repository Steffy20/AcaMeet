
package com.example.acameet.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.acameet.data.repository.LugarRepository
import kotlinx.coroutines.flow.StateFlow

class LugarViewModelFactory(
    private val repository: LugarRepository,
    private val usuarioIdActivo: StateFlow<Int?>
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(LugarViewModel::class.java)) {
            return LugarViewModel(
                repository = repository,
                usuarioIdActivo = usuarioIdActivo
            ) as T
        }

        throw IllegalArgumentException(
            "ViewModel desconocido: ${modelClass.name}"
        )
    }
}
