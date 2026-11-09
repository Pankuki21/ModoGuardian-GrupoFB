package com.example.modoguardian.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GuardianColorScheme = darkColorScheme(
    primary = AzulInsertCode,
    onPrimary = TextoPrincipal,
    secondary = VerdeEstado,
    onSecondary = FondoGuardian,
    tertiary = AmarilloAtencion,
    error = RojoAlerta,
    background = FondoGuardian,
    onBackground = TextoPrincipal,
    surface = SuperficieGuardian,
    onSurface = TextoPrincipal,
    surfaceVariant = SuperficieElevadaGuardian,
    onSurfaceVariant = TextoSecundario,
    outline = TextoSecundario
)

private val GuardianLightColorScheme = lightColorScheme(
    primary = AzulInsertCode,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    secondary = Color(0xFF008F70),
    tertiary = Color(0xFF8A5B00),
    error = Color(0xFFB3261E),
    background = Color(0xFFF3F6FB),
    onBackground = Color(0xFF111827),
    surface = Color.White,
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFE5EAF2),
    onSurfaceVariant = Color(0xFF536177),
    outline = Color(0xFF7D899B)
)

@Composable
fun ModoGuardianTheme(
    modoOscuro: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (modoOscuro) GuardianColorScheme else GuardianLightColorScheme,
        content = content
    )
}
