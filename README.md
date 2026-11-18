# DSY1105-004D-CRAMIREZ
Desarrollo de aplicaciones moviles


## Insercode · Guardián (ampliación)

Se agregaron, manteniendo el estilo Material3 de la app y sus componentes (`BotonGoogle`, `ImagenInteligente`, `PremiumCard`):

- **Login** (pantalla inicial): correo/clave, "Recordarme" (DataStore), recuperar clave, Google y "Crear cuenta" (lleva al registro original).
- **Inicio**: estado general de cámaras, cámaras asignadas, eventos en vivo, protocolo y botón *Reportar Incidencia*.
- **Cámaras / Detalle de cámara**: lista con filtros, vista en vivo simulada, FOTO / PARAR / HABLAR / LUZ / FULL y estado del dispositivo.
- **Eventos (Historial)**: resumen del turno, pestañas, búsqueda, filtros, agrupación por hora y **exportar PDF**.
- **Alertas / Detalle de alerta**: confirmar y enviar a Central, falsa alarma, incidencia con foto, llamar a Central y cuenta regresiva de 2 min con auto-escalado.
- **Reportar incidencia**: tipo, descripción y foto (cámara o galería) guardada en ROOM.
- **Perfil**: foto, ajustes persistentes (2 pasos, push, alertas críticas, modo oscuro), cambiar contraseña, credencial, ayuda, términos y cerrar sesión.
- **ROOM v2**: nuevas tablas `eventos` e `incidencias` (migración 1→2, los usuarios existentes se conservan).
