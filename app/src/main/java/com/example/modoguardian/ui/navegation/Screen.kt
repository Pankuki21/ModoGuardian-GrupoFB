package com.example.modoguardian.ui.navegation
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object Events : Screen("events")
    object EventDetail : Screen("event_detail/{eventId}") {
        fun createRoute(eventId: String) = "event_detail/$eventId"
    }
}