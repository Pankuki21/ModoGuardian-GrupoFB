package com.example.modoguardian.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()

    val navigationEvent = _navigationEvent.asSharedFlow()

    // Envía un evento para navegar hacia una pantalla.
    fun navigateTo(route: String) {
        viewModelScope.launch {
            _navigationEvent.emit(
                NavigationEvent.NavigateTo(route)
            )
        }
    }

    // Envía un evento para regresar a la pantalla anterior.
    fun navigateBack() {
        viewModelScope.launch {
            _navigationEvent.emit(
                NavigationEvent.NavigateBack
            )
        }
    }
}