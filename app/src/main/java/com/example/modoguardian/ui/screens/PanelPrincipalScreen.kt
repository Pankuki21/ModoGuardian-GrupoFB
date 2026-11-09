package com.example.modoguardian.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import com.example.modoguardian.model.Usuario
import com.example.modoguardian.ui.theme.AzulInsertCode
import kotlinx.coroutines.launch

private enum class SeccionPanel(val etiqueta: String, val icono: String) {
    INICIO("Inicio", IconosUi.Home),
    CAMARAS("Cámaras", IconosUi.Videocam),
    EVENTOS("Eventos", IconosUi.EventNote),
    ALERTAS("Alertas", IconosUi.Warning),
    PERFIL("Perfil", IconosUi.Person)
}

private data class CamaraLocal(val nombre: String, val zona: String, val enLinea: Boolean, val grabando: Boolean)
private data class EventoLocal(val titulo: String, val lugar: String, val hora: String, val estado: String, val tipo: String)

private val camarasDemo = listOf(
    CamaraLocal("Entrada Principal", "Zona Sur", true, true),
    CamaraLocal("Perímetro Norte", "Zona Sur", true, true),
    CamaraLocal("Bodega A", "Zona Sur", false, false),
    CamaraLocal("Estacionamiento", "Zona Sur", true, true)
)

private val eventosDemo = listOf(
    EventoLocal("Movimiento no autorizado", "Perímetro Norte · Zona 02", "14:32", "Crítica", "Alerta"),
    EventoLocal("Acceso autorizado", "Entrada Principal · Credencial #4582", "14:10", "Autorizado", "Evento"),
    EventoLocal("Cámara desconectada", "Bodega A · Error de enlace", "13:55", "Resuelto", "Alerta"),
    EventoLocal("Ronda completada", "Perímetro Sur · Ruta completa", "13:42", "Confirmada", "Evento"),
    EventoLocal("Puerta abierta fuera de horario", "Almacén Central", "13:48", "Pendiente", "Crítica")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanelPrincipalScreen(
    usuario: Usuario,
    modoOscuro: Boolean,
    onCambiarModoOscuro: (Boolean) -> Unit,
    onCerrarSesion: () -> Unit
) {
    var seccion by remember { mutableStateOf(SeccionPanel.INICIO) }
    var camaraSeleccionada by remember { mutableStateOf<CamaraLocal?>(null) }
    var detalleAlerta by remember { mutableStateOf(false) }
    var dialogo by remember { mutableStateOf<String?>(null) }
    var eventos by remember { mutableStateOf(eventosDemo) }
    var filtroEvento by remember { mutableStateOf("Todos") }
    var pushActivo by remember { mutableStateOf(true) }
    var alertasCriticas by remember { mutableStateOf(true) }
    var dosPasos by remember { mutableStateOf(true) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    fun avisar(mensaje: String) { scope.launch { snackbar.showSnackbar(mensaje) } }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                SeccionPanel.entries.forEach { destino ->
                    NavigationBarItem(
                        selected = seccion == destino,
                        onClick = { seccion = destino; detalleAlerta = false; camaraSeleccionada = null },
                        icon = { IconoUi(destino.icono, contentDescription = destino.etiqueta) },
                        label = { Text(destino.etiqueta, fontSize = 10.sp) }
                    )
                }
            }
        }
    ) { inset ->
        Column(Modifier.fillMaxSize().padding(inset)) {
            when (seccion) {
                SeccionPanel.INICIO -> InicioContenido(
                    usuario = usuario,
                    onIrCamaras = { seccion = SeccionPanel.CAMARAS },
                    onIrEventos = { seccion = SeccionPanel.EVENTOS },
                    onIrAlertas = { seccion = SeccionPanel.ALERTAS; detalleAlerta = true },
                    onReporte = { avisar("Prototipo local: reporte de incidencia no conectado a servicios.") }
                )
                SeccionPanel.CAMARAS -> {
                    if (camaraSeleccionada == null) ListaCamaras(onSeleccionar = { camaraSeleccionada = it })
                    else VistaCamara(camaraSeleccionada!!, onVolver = { camaraSeleccionada = null }, onAccion = { avisar(it) })
                }
                SeccionPanel.EVENTOS -> EventosContenido(
                    eventos = eventos,
                    filtro = filtroEvento,
                    onFiltro = { filtroEvento = it },
                    onReporte = { avisar("Prototipo local: exportación PDF aún no conectada.") }
                )
                SeccionPanel.ALERTAS -> if (detalleAlerta) {
                    DetalleAlerta(
                        confirmada = eventos.firstOrNull { it.tipo == "Crítica" }?.estado == "Confirmada",
                        onVolver = { detalleAlerta = false },
                        onConfirmar = {
                            eventos = eventos.map { if (it.tipo == "Crítica") it.copy(estado = "Confirmada") else it }
                            avisar("Alerta confirmada localmente.")
                        },
                        onFalsa = {
                            eventos = eventos.map { if (it.tipo == "Crítica") it.copy(estado = "Falsa alarma") else it }
                            avisar("Marcada como falsa alarma localmente.")
                        },
                        onAccion = { avisar(it) }
                    )
                } else AlertasContenido(eventos, onAbrir = { detalleAlerta = true })
                SeccionPanel.PERFIL -> PerfilContenido(
                    usuario = usuario,
                    modoOscuro = modoOscuro,
                    onModoOscuro = onCambiarModoOscuro,
                    pushActivo = pushActivo,
                    onPush = { pushActivo = it },
                    alertasCriticas = alertasCriticas,
                    onCriticas = { alertasCriticas = it },
                    dosPasos = dosPasos,
                    onDosPasos = { dosPasos = it; avisar("Preferencia de demostración actualizada.") },
                    onOpcion = { dialogo = it },
                    onCerrarSesion = onCerrarSesion
                )
            }
        }
    }
    dialogo?.let { titulo ->
        AlertDialog(
            onDismissRequest = { dialogo = null },
            title = { Text(titulo) },
            text = { Text("Esta sección se muestra como prototipo local de InsertCode. No modifica servicios ni dispositivos reales.") },
            confirmButton = { TextButton(onClick = { dialogo = null }) { Text("Entendido") } }
        )
    }
}

@Composable
private fun InicioContenido(usuario: Usuario, onIrCamaras: () -> Unit, onIrEventos: () -> Unit, onIrAlertas: () -> Unit, onReporte: () -> Unit) {
    ColumnaDesplazable {
        FilaMarca(usuario.nombre, "Turno activo · Zona Sur", onIrAlertas)
        TarjetaPanel(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Estado general", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("INSERCODE  ·  GUARDIÁN OS V2.4", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.5.sp)
                }
                Text("•••", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(22.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Indicador("TOTAL", "12", "Cámaras", IconosUi.Videocam, Modifier.weight(1f), AzulInsertCode)
                Indicador("ACTIVAS", "11", "En línea", IconosUi.CheckCircle, Modifier.weight(1f), MaterialTheme.colorScheme.secondary)
                Indicador("ATENCIÓN", "1", "Mantención", IconosUi.Warning, Modifier.weight(1f), MaterialTheme.colorScheme.tertiary)
            }
            Spacer(Modifier.height(22.dp))
            Text("CAPACIDAD OPERATIVA                                      91%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            androidx.compose.material3.LinearProgressIndicator(progress = { .91f }, modifier = Modifier.fillMaxWidth(), color = AzulInsertCode, trackColor = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconoUi(IconosUi.AccessTime, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                Text("  Última sincronización: hace 2 min", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.weight(1f))
                Text("● Demo local", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.labelSmall)
            }
        }
        TituloSeccion("Cámaras asignadas", "Ver todas", onIrCamaras)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            camarasDemo.take(2).forEach { CamaraResumen(it, Modifier.weight(1f), onIrCamaras) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            camarasDemo.drop(2).forEach { CamaraResumen(it, Modifier.weight(1f), onIrCamaras) }
        }
        TituloSeccion("Eventos en vivo", "Ver historial", onIrEventos)
        TarjetaPanel(Modifier.fillMaxWidth()) {
            eventosDemo.take(4).forEachIndexed { i, evento ->
                EventoFila(evento)
                if (i < 3) androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            }
        }
        Button(onClick = onReporte, colors = ButtonDefaults.buttonColors(containerColor = AzulInsertCode), modifier = Modifier.fillMaxWidth().height(54.dp)) {
            IconoUi(IconosUi.Warning, null)
            Spacer(Modifier.width(8.dp))
            Text("Reportar incidencia", fontWeight = FontWeight.Bold)
        }
        NotaLocal()
    }
}

@Composable
private fun ListaCamaras(onSeleccionar: (CamaraLocal) -> Unit) {
    ColumnaDesplazable {
        Encabezado("Cámaras", "Monitoreo de dispositivos asignados")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EtiquetaDato("12 asignadas", IconosUi.Videocam)
            EtiquetaDato("11 en línea", IconosUi.Wifi)
        }
        camarasDemo.forEach { camera ->
            TarjetaPanel(Modifier.fillMaxWidth(), alHacerClic = { onSeleccionar(camera) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(if (camera.enLinea) Color(0xFF102C36) else Color(0xFF322B20)), contentAlignment = Alignment.Center) {
                        IconoUi(if (camera.enLinea) IconosUi.Videocam else IconosUi.WifiOff, null, tint = if (camera.enLinea) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(camera.nombre, fontWeight = FontWeight.Bold)
                        Text("${camera.zona} · INSERCODE CAM-${camarasDemo.indexOf(camera) + 1}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                    EstadoPildora(if (camera.enLinea) "En línea" else "Sin conexión", if (camera.enLinea) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error)
                }
                Spacer(Modifier.height(14.dp))
                Box(Modifier.fillMaxWidth().height(112.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFF07131D)), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconoUi(if (camera.enLinea) IconosUi.Videocam else IconosUi.WifiOff, null, tint = if (camera.enLinea) Color(0xFF285344) else Color(0xFF725241), modifier = Modifier.size(34.dp))
                        Text(if (camera.enLinea) "Vista previa local" else "Cámara fuera de línea", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                    }
                    Text(if (camera.grabando) "● REC" else "● OFFLINE", color = if (camera.grabando) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.TopStart).padding(10.dp))
                }
                Text("Abrir controles  ›", color = AzulInsertCode, style = MaterialTheme.typography.labelLarge, modifier = Modifier.align(Alignment.End).padding(top = 8.dp))
            }
        }
        NotaLocal()
    }
}

@Composable
private fun VistaCamara(camera: CamaraLocal, onVolver: () -> Unit, onAccion: (String) -> Unit) {
    ColumnaDesplazable {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onVolver) { IconoUi(IconosUi.ArrowBack, "Volver") }
            Column {
                Text(camera.nombre, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("VISTA PREVIA · DEMO · INSERCODE", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.sp)
            }
        }
        Box(Modifier.fillMaxWidth().height(360.dp).clip(RoundedCornerShape(24.dp)).background(Color(0xFF07131D)), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconoUi(if (camera.enLinea) IconosUi.Videocam else IconosUi.WifiOff, null, tint = if (camera.enLinea) Color(0xFF1C4B3E) else MaterialTheme.colorScheme.error, modifier = Modifier.size(72.dp))
                Spacer(Modifier.height(12.dp))
                Text(if (camera.enLinea) "Vista previa simulada" else "Sin conexión", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("No hay transmisión real configurada", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
            }
            Text("MODO DEMOSTRACIÓN\nINSERCODE CAM-${camarasDemo.indexOf(camera) + 1}", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.TopStart).padding(16.dp))
            EstadoPildora(if (camera.grabando) "● REC" else "EN ESPERA", if (camera.grabando) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant, Modifier.align(Alignment.TopEnd).padding(12.dp))
        }
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            ControlCamara("Foto", IconosUi.CameraAlt) { onAccion("La captura solo se previsualiza en este prototipo local.") }
            ControlCamara(if (camera.grabando) "Parar" else "Grabar", IconosUi.CheckCircle) { onAccion("Control de grabación disponible en la vista de demostración.") }
            ControlCamara("Hablar", IconosUi.MicOff) { onAccion("Audio bidireccional no configurado.") }
            ControlCamara("Luz", IconosUi.FlashlightOn) { onAccion("Luz de cámara no conectada en esta versión.") }
            ControlCamara("Pantalla", IconosUi.Fullscreen) { onAccion("La vista ampliada requiere conectar una cámara.") }
        }
        TarjetaPanel(Modifier.fillMaxWidth()) {
            Text("Estado del dispositivo", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                EstadoCaja("ESTADO", if (camera.enLinea) "En línea" else "Sin conexión", if (camera.enLinea) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error, Modifier.weight(1f))
                EstadoCaja("SEÑAL", if (camera.enLinea) "Excelente" else "Sin señal", AzulInsertCode, Modifier.weight(1f))
                EstadoCaja("GRABACIÓN", if (camera.grabando) "Muestra local" else "Detenida", MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
            }
        }
        NotaLocal()
    }
}

@Composable
private fun EventosContenido(eventos: List<EventoLocal>, filtro: String, onFiltro: (String) -> Unit, onReporte: () -> Unit) {
    var busqueda by remember { mutableStateOf("") }
    ColumnaDesplazable {
        Encabezado("Historial", "Turno actual · 14:00 – 22:00")
        TarjetaPanel(Modifier.fillMaxWidth()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ResumenEvento("HOY", "24", "eventos registrados", AzulInsertCode, Modifier.weight(1f))
                ResumenEvento("CRÍTICAS", eventos.count { it.tipo == "Crítica" }.toString(), "alertas críticas", MaterialTheme.colorScheme.error, Modifier.weight(1f))
                ResumenEvento("PENDIENTE", eventos.count { it.estado == "Pendiente" }.toString(), "requiere atención", MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
            }
            Spacer(Modifier.height(20.dp))
            Row { Text("PROGRESO DEL TURNO", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall); Spacer(Modifier.weight(1f)); Text("62%  ·  5h 12m restantes", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall) }
            Spacer(Modifier.height(8.dp))
            androidx.compose.material3.LinearProgressIndicator(progress = { .62f }, modifier = Modifier.fillMaxWidth(), color = AzulInsertCode, trackColor = MaterialTheme.colorScheme.surfaceVariant)
        }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Todos", "Alertas", "Eventos", "Accesos").forEach { opcion ->
                FilterChip(selected = filtro == opcion, onClick = { onFiltro(opcion) }, label = { Text(opcion) })
            }
        }
        androidx.compose.material3.OutlinedTextField(value = busqueda, onValueChange = { busqueda = it }, label = { Text("Buscar por cámara, zona o tipo") }, leadingIcon = { IconoUi(IconosUi.Search, null) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Text("◷   14:00 – 15:00                                      ${eventos.size} eventos", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
        val filtrados = eventos.filter { evento ->
            val coincideFiltro = when (filtro) {
                "Alertas" -> evento.tipo == "Alerta" || evento.tipo == "Crítica"
                "Eventos" -> evento.tipo == "Evento"
                "Accesos" -> evento.titulo.contains("Acceso", true)
                else -> true
            }
            coincideFiltro && (busqueda.isBlank() || "${evento.titulo} ${evento.lugar}".contains(busqueda, true))
        }
        filtrados.forEach { EventoTarjeta(it) }
        Button(onClick = onReporte, modifier = Modifier.fillMaxWidth().height(54.dp)) {
            IconoUi(IconosUi.Description, null); Spacer(Modifier.width(8.dp)); Text("Preparar reporte de turno")
        }
        NotaLocal()
    }
}

@Composable
private fun AlertasContenido(eventos: List<EventoLocal>, onAbrir: () -> Unit) {
    ColumnaDesplazable {
        Encabezado("Alertas", "Atención y respuesta del turno")
        TarjetaPanel(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconoUi(IconosUi.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) { Text("Alertas críticas", fontWeight = FontWeight.Bold); Text("Requieren respuesta del operador", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) }
                EstadoPildora("3 nuevas", MaterialTheme.colorScheme.error)
            }
        }
        eventos.filter { it.tipo == "Crítica" || it.tipo == "Alerta" }.forEach { evento ->
            TarjetaPanel(Modifier.fillMaxWidth(), alHacerClic = onAbrir) {
                Row(verticalAlignment = Alignment.Top) {
                    Box(Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFF3A202E)), contentAlignment = Alignment.Center) {
                        IconoUi(IconosUi.Security, null, tint = MaterialTheme.colorScheme.error)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(evento.titulo, fontWeight = FontWeight.Bold)
                        Text(evento.lugar, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        Text("${evento.hora}  ·  Cámara 04", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                    }
                    EstadoPildora(evento.estado, if (evento.estado == "Pendiente") MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary)
                }
                Spacer(Modifier.height(10.dp))
                Text("Ver detalle y acciones  ›", color = AzulInsertCode, style = MaterialTheme.typography.labelLarge, modifier = Modifier.align(Alignment.End))
            }
        }
        NotaLocal()
    }
}

@Composable
private fun DetalleAlerta(confirmada: Boolean, onVolver: () -> Unit, onConfirmar: () -> Unit, onFalsa: () -> Unit, onAccion: (String) -> Unit) {
    ColumnaDesplazable {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onVolver) { IconoUi(IconosUi.ArrowBack, "Volver") }
            Text("Alerta crítica", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f)); EstadoPildora("● CRÍTICO", MaterialTheme.colorScheme.error)
        }
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF301D2B)), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp)) {
                Text("Movimiento no autorizado detectado", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                FilaDato(IconosUi.Place, "Ubicación", "Perímetro Norte – Cámara 04")
                FilaDato(IconosUi.AccessTime, "Registro", "Evento de demostración · 14:32")
                FilaDato(IconosUi.Security, "Confianza IA", "92% · Persona detectada")
                Spacer(Modifier.height(16.dp))
                androidx.compose.material3.LinearProgressIndicator(progress = { .92f }, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.secondary)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.weight(1f).height(140.dp).clip(RoundedCornerShape(20.dp)).background(Color(0xFF07131D)), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) { IconoUi(IconosUi.Videocam, null, tint = AzulInsertCode, modifier = Modifier.size(32.dp)); Text("Video de muestra", color = MaterialTheme.colorScheme.onSurfaceVariant); Text("No hay grabación conectada", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall) }
            }
            Box(Modifier.weight(1f).height(140.dp).clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.surface), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) { IconoUi(IconosUi.Place, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(32.dp)); Text("PERÍMETRO NORTE", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall); Text("CAM-04", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }
            }
        }
        TarjetaPanel(Modifier.fillMaxWidth()) {
            Text("Detalles del evento", fontWeight = FontWeight.Bold)
            FilaDato(IconosUi.Warning, "Tipo", "Intrusión perimetral")
            FilaDato(IconosUi.Place, "Zona", "Perímetro Norte · Sector B")
            FilaDato(IconosUi.Videocam, "Cámara", "CAM-04")
            FilaDato(IconosUi.AccessTime, "Duración", "12 segundos")
        }
        Text("ACCIONES REQUERIDAS", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelLarge, letterSpacing = 1.sp)
        Button(onClick = onConfirmar, enabled = !confirmada, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error), modifier = Modifier.fillMaxWidth().height(58.dp)) {
            IconoUi(IconosUi.Security, null); Spacer(Modifier.width(8.dp)); Text(if (confirmada) "Alerta confirmada localmente" else "Confirmar alerta · Demo local")
        }
        OutlinedButton(onClick = onFalsa, modifier = Modifier.fillMaxWidth().height(54.dp)) { Text("✓   Falsa alarma", color = MaterialTheme.colorScheme.tertiary) }
        OutlinedButton(onClick = { onAccion("Reporte de incidencia preparado en modo local.") }, modifier = Modifier.fillMaxWidth().height(54.dp)) { Text("▧   Reportar incidencia con foto") }
        OutlinedButton(onClick = { onAccion("No hay un número de central configurado para llamar.") }, modifier = Modifier.fillMaxWidth().height(54.dp)) { Text("☎   Llamar a central") }
        TarjetaPanel(Modifier.fillMaxWidth()) { Text("Protocolo InsertCode · demostración", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary); Text("Los tiempos de respuesta y el escalamiento deben configurarse con el servicio central.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        NotaLocal()
    }
}

@Composable
private fun PerfilContenido(
    usuario: Usuario, modoOscuro: Boolean, onModoOscuro: (Boolean) -> Unit,
    pushActivo: Boolean, onPush: (Boolean) -> Unit,
    alertasCriticas: Boolean, onCriticas: (Boolean) -> Unit,
    dosPasos: Boolean, onDosPasos: (Boolean) -> Unit,
    onOpcion: (String) -> Unit, onCerrarSesion: () -> Unit
) {
    ColumnaDesplazable {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text("Mi Perfil", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold); Text("Cuenta y preferencias del turno", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            IconButton(onClick = { onOpcion("Configuración") }) { IconoUi(IconosUi.Settings, "Configuración") }
        }
        TarjetaPanel(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(72.dp).clip(CircleShape).background(AzulInsertCode), contentAlignment = Alignment.Center) {
                    Text(usuario.nombre.take(2).uppercase(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(usuario.nombre, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(usuario.rol.name.lowercase().replaceFirstChar { it.uppercase() } + " · InsertCode", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(usuario.correo, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp)); EstadoPildora("● Turno activo", MaterialTheme.colorScheme.secondary)
                }
            }
        }
        TarjetaPanel(Modifier.fillMaxWidth()) {
            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                EstadisticaPerfil("14:00 – 22:00", "Turno", IconosUi.AccessTime)
                EstadisticaPerfil("12", "Cámaras", IconosUi.Videocam)
                EstadisticaPerfil("3", "Resueltas", IconosUi.Warning)
            }
        }
        TituloSeccionSimple("MI CUENTA")
        GrupoOpciones {
            OpcionPerfil(IconosUi.Videocam, "Mis cámaras asignadas", "12 dispositivos") { onOpcion("Cámaras asignadas") }
            OpcionPerfil(IconosUi.History, "Historial de turnos", "Turnos anteriores y registros") { onOpcion("Historial de turnos") }
            OpcionPerfil(IconosUi.Shield, "Credencial de guardia", "Documento de demostración") { onOpcion("Credencial de guardia") }
        }
        TituloSeccionSimple("SEGURIDAD")
        GrupoOpciones {
            OpcionPerfil(IconosUi.Lock, "Cambiar contraseña", "") { onOpcion("Cambiar contraseña") }
            OpcionPerfil(IconosUi.Security, "Verificación en 2 pasos", "Preferencia local", dosPasos, onDosPasos)
            OpcionPerfil(IconosUi.Notifications, "Notificaciones push", "Avisos de la aplicación", pushActivo, onPush)
            OpcionPerfil(IconosUi.Warning, "Alertas críticas", "Prioridad alta", alertasCriticas, onCriticas)
        }
        TituloSeccionSimple("APLICACIÓN · INSERCODE")
        GrupoOpciones {
            OpcionPerfil(IconosUi.Settings, "Modo oscuro", "Tema de monitoreo", modoOscuro, onModoOscuro)
            OpcionPerfil(IconosUi.HelpOutline, "Ayuda y soporte InsertCode", "Guías y contacto") { onOpcion("Ayuda y soporte InsertCode") }
            OpcionPerfil(IconosUi.Description, "Términos y políticas de seguridad", "") { onOpcion("Términos y políticas") }
        }
        OutlinedButton(onClick = onCerrarSesion, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
            IconoUi(IconosUi.Logout, null); Spacer(Modifier.width(8.dp)); Text("Cerrar sesión")
        }
        Text("InsertCode · Modo Guardián · Prototipo local", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.CenterHorizontally))
        NotaLocal()
    }
}

@Composable private fun ColumnaDesplazable(contenido: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(14.dp), content = contenido)
}

@Composable private fun Encabezado(titulo: String, subtitulo: String) {
    Column {
        Text(titulo, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(subtitulo, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable private fun FilaMarca(nombre: String, subtitulo: String, onAlertas: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(46.dp).clip(CircleShape).background(AzulInsertCode), contentAlignment = Alignment.Center) { Text(nombre.take(2).uppercase(), fontWeight = FontWeight.Bold) }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) { Text("Hola, ${nombre.substringBefore(' ')}", fontWeight = FontWeight.Bold); Text(subtitulo, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) }
        IconButton(onClick = onAlertas) { IconoUi(IconosUi.Notifications, "Alertas") }
        EstadoPildora("✓ LOCAL", MaterialTheme.colorScheme.secondary)
    }
}

@Composable private fun TituloSeccion(titulo: String, accion: String, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) { Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); TextButton(onClick = onClick) { Text(accion) } }
}

@Composable private fun TarjetaPanel(modifier: Modifier = Modifier, alHacerClic: (() -> Unit)? = null, contenido: @Composable ColumnScope.() -> Unit) {
    Card(onClick = alHacerClic ?: {}, enabled = alHacerClic != null, modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp), content = contenido)
    }
}

@Composable private fun Indicador(etiqueta: String, valor: String, ayuda: String, icono: String, modifier: Modifier, color: Color) {
    Column(modifier.clip(RoundedCornerShape(18.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(10.dp)) {
        IconoUi(icono, null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(Modifier.height(8.dp)); Text(etiqueta, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp)
        Text(valor, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(ayuda, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp)
    }
}

@Composable private fun CamaraResumen(camera: CamaraLocal, modifier: Modifier, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(10.dp)) {
            Box(Modifier.fillMaxWidth().height(84.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFF07131D)), contentAlignment = Alignment.Center) {
                IconoUi(if (camera.enLinea) IconosUi.Videocam else IconosUi.WifiOff, null, tint = if (camera.enLinea) AzulInsertCode else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(32.dp))
                if (camera.grabando) EstadoPildora("● REC", MaterialTheme.colorScheme.error, Modifier.align(Alignment.TopStart).padding(5.dp))
            }
            Spacer(Modifier.height(8.dp)); Text(camera.nombre, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Text(camera.zona, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
            Text(if (camera.enLinea) "● En línea" else "Sin conexión", color = if (camera.enLinea) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable private fun EventoFila(evento: EventoLocal) {
    val color = when (evento.tipo) { "Crítica" -> MaterialTheme.colorScheme.error; "Alerta" -> MaterialTheme.colorScheme.tertiary; else -> AzulInsertCode }
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(36.dp).clip(CircleShape).background(color.copy(alpha = .18f)), contentAlignment = Alignment.Center) { IconoUi(if (evento.tipo == "Evento") IconosUi.CheckCircle else IconosUi.Warning, null, tint = color, modifier = Modifier.size(20.dp)) }
        Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text(evento.titulo, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold); Text("${evento.hora} · ${evento.lugar}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) }
        EstadoPildora(evento.estado, color)
    }
}

@Composable private fun EventoTarjeta(evento: EventoLocal) {
    val color = if (evento.tipo == "Crítica" || evento.tipo == "Alerta") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
    TarjetaPanel(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(4.dp).height(74.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) { Text(evento.titulo, fontWeight = FontWeight.Bold); Text(evento.lugar, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall); Text("${evento.hora}   ·   INSERCODE CAM-04", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall) }
            EstadoPildora(evento.estado, color)
        }
    }
}

@Composable private fun EstadoPildora(texto: String, color: Color, modifier: Modifier = Modifier) {
    Box(modifier.clip(CircleShape).background(color.copy(alpha = .14f)).padding(horizontal = 10.dp, vertical = 6.dp)) { Text(texto, color = color, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold) }
}

@Composable private fun NotaLocal() { Text("Vista previa local · Datos de demostración · InsertCode", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .65f), style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }

@Composable private fun ResumenEvento(titulo: String, cantidad: String, descripcion: String, color: Color, modifier: Modifier) {
    Column(modifier) { Text(titulo, color = color, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp); Text(cantidad, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Text(descripcion, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp) }
}

@Composable private fun ControlCamara(titulo: String, icono: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onClick, modifier = Modifier.size(52.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface)) { IconoUi(icono, titulo, tint = AzulInsertCode) }
        Text(titulo, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp)
    }
}

@Composable private fun EstadoCaja(titulo: String, valor: String, color: Color, modifier: Modifier) {
    Column(modifier.clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.background).padding(10.dp)) { Text(titulo, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall); Spacer(Modifier.height(6.dp)); Text(valor, color = color, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall) }
}

@Composable private fun EtiquetaDato(texto: String, icono: String) {
    Row(Modifier.clip(CircleShape).background(MaterialTheme.colorScheme.surface).padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) { IconoUi(icono, null, tint = AzulInsertCode, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text(texto, style = MaterialTheme.typography.labelMedium) }
}

@Composable private fun FilaDato(icono: String, titulo: String, valor: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) { IconoUi(icono, null, tint = AzulInsertCode, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(10.dp)); Text(titulo, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f)); Text(valor, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall) }
}

@Composable private fun EstadisticaPerfil(valor: String, titulo: String, icono: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) { IconoUi(icono, null, tint = AzulInsertCode); Spacer(Modifier.height(6.dp)); Text(valor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium); Text(titulo, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall) }
}

@Composable private fun TituloSeccionSimple(titulo: String) { Text(titulo, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, letterSpacing = 1.sp) }

@Composable private fun GrupoOpciones(contenido: @Composable ColumnScope.() -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
        Column(content = contenido)
    }
}

@Composable private fun OpcionPerfil(icono: String, titulo: String, subtitulo: String, valor: Boolean? = null, onCambio: ((Boolean) -> Unit)? = null, onClick: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) { IconoUi(icono, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp)) }
        Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(titulo, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold); if (subtitulo.isNotBlank()) Text(subtitulo, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) }
        if (valor != null && onCambio != null) Switch(checked = valor, onCheckedChange = onCambio)
        else if (onClick != null) TextButton(onClick = onClick) { Text("›", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.headlineSmall) }
    }
    androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.background)
}

