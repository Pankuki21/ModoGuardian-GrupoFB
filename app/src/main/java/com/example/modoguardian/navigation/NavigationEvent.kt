package com.example.modoguardian.navigation

sealed class NavigationEvent {

    data class NavigateTo(
        val route: String
    ) : NavigationEvent()

    data object NavigateBack : NavigationEvent()
}