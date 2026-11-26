package com.duoc.appfacosta.data.model

enum class EventKind { ALERTA, CRITICA, EVENTO, ACCESO, RONDA }

data class SecurityEvent(
    val id: Int,
    val kind: EventKind,
    val title: String,
    val subtitle: String,
    val time: String,
    val status: String = ""
)

data class Camera(
    val id: String,
    val name: String,
    val zone: String,
    val online: Boolean,
    val recording: Boolean
)

enum class UserRole(val label: String) {
    ADMIN("Admin"),
    SUPERVISOR("Supervisor"),
    OPERADOR("Operador"),
    USUARIO("Usuario")
}
data class UserSession(
    val name: String,
    val email: String,
    val role: UserRole = UserRole.USUARIO
) {
    val initials: String
        get() = name.take(2).uppercase()
}
data class DemoUser(
    val name: String,
    val email: String,
    val password: String,
    val role: UserRole
)
object DemoUsers {
    private val users = listOf(
        DemoUser("Admin",
            "admin@guardian.test",
            "123456", UserRole.ADMIN),
        DemoUser("Supervisor",
            "supervisor@guardian.test",
            "123456", UserRole.SUPERVISOR),
        DemoUser("Operador",
            "operador@guardian.test",
            "123456", UserRole.OPERADOR)
    )
    fun find(email: String): DemoUser? =
        users.firstOrNull { it.email.equals(
            email.trim(), ignoreCase = true)
        }
}
