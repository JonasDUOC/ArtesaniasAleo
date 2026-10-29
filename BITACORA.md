# Bitácora — App Artesanías Ale y Leo (DSY1105)

Equipo: Jonas y Max · App Android en Kotlin (Jetpack Compose, MVVM, Room)

---

## Estado por capa

```
com.example.artesaniasaleo
├── data/
│   └── local/
│       ├── entity/       ✅ Usuario, PerfilUsuario, Direccion, Tarjeta, Categoria,
│       │                    Producto, ImagenProducto, CarritoItem, Favorito,
│       │                    Pedido, PedidoDetalle
│       ├── relation/     ✅ ProductoConImagenes, CarritoItemConProducto,
│       │                    DetalleConProducto, PedidoConDetalles, UsuarioConPerfil
│       ├── dao/          ✅ UsuarioDao, CategoriaDao, ProductoDao, CarritoDao,
│       │                    FavoritoDao, PedidoDao
│       ├── database/     ✅ AppDatabase.kt (versión 2; registra entidades y DAOs;
│       │                    datos semilla: admin, cliente Ana, 4 categorías, 8 productos)
│       └── datastore/    ✅ SesionDataStore.kt (guarda quién inició sesión)
├── repository/           ✅ UsuarioRepository, CategoriaRepository, ProductoRepository,
│                            CarritoRepository, FavoritoRepository, PedidoRepository
├── model/                ✅ UiState de cada pantalla, ProductoUi, CategoriaUi, PedidoUi,
│                            DatosPagoUiState, PerfilUiState, EstadoOperacion, EstadoSesion,
│                            EstadoCompra (sealed class), ProductosDemo (solo para @Preview)
├── viewmodel/            ✅ Factory, Sesion, Login, Registro, Catalogo, Carrito, Perfil, Pedidos, Admin, Recuperar, Qr + Mapeos.kt
├── ui/
│   ├── theme/            ✅ Color, Type, Shape, Theme (paleta y fuentes del mockup)
│   ├── screens/          ✅ login/ PantallaLogin, registro/ PantallaRegistro (3 etapas),
│   │                        catalogo/, detalle/, categorias/, favoritos/, carrito/, pago/,
│   │                        perfil/, seguimiento/, terminos/, recuperar/, admin/,
│   │                        qr/ PantallaEscanearQr (CameraX + ML Kit)
│   ├── components/       ✅ Botones, CampoTexto, CampoSelector, BarraSuperior, TarjetaProducto,
│   │                        GrillaProductos, FilaLista, FormularioPago, MenuLateral,
│   │                        PantallaCarga, ImagenArtesania, SelectorImagenes (galería y cámara)
│   └── utils/            ✅ Validaciones.kt (correo, RUT, tarjeta), WindowSizeUtils.kt
├── navigation/           ✅ Rutas, AppNavegacion (NavHost + menú lateral). EstadoDemo eliminado
└── MainActivity.kt
```

Leyenda: ✅ terminado · 🔄 creado, falta validar · ⬜ por hacer

**Flujo:** Pantalla → ViewModel → Repository → DAO → Room, y de vuelta con Flow → StateFlow → la pantalla se actualiza. Las pantallas nunca tocan el DAO directamente.

**Plan:** construir de abajo hacia arriba y terminar primero **registro + login** completos antes del resto.

**Cuenta admin de prueba:** `admin@aleyleo.cl` / `admin123` (se crea sola al instalar la app).

---

## Decisiones de diseño

| Decisión | Por qué |
|---|---|
| Una sola tabla `usuario` con campo `rol` | Cliente y admin comparten todo; el admin solo cambia el rol |
| `usuario` y `perfil_usuario` separados | Acceso (correo, contraseña) vs. datos personales, que se completan en el último paso del registro (CU-03) |
| Perfil, dirección y tarjeta usan `usuarioId` como PK | Relación 1 a 1: cada usuario tiene solo uno |
| Favorito, carrito y detalle de pedido con PK compuesta | Evita duplicados (ej.: el mismo producto dos veces en favoritos) |
| Carrito y pedido no se relacionan entre sí | El carrito es temporal; al comprar se **copia** a `pedido_detalle` y se vacía |
| `RESTRICT` en producto, categoría y usuario de pedidos | No perder el historial de ventas; se usa `estado` (inactivo/bloqueado) en vez de borrar |
| Precios en `Int` | Los pesos chilenos no tienen decimales |
| Fechas como `Long` | Room no guarda fechas sin un convertidor |
| Imágenes como ruta (`String`) | Se copian a la carpeta de la app; la BD guarda solo la ruta |
| Contraseñas sin hash (por ahora) | Para no complicar; mejora opcional al final |
| Paquetes `data/local` y `repository` aparte | Preparar la Unidad 3 (API REST): solo cambiará el repository |
| Room 2.8 (`androidx.room`) en vez de Room 3 | Es la versión que enlaza el curso y tiene más ejemplos; Room 3 solo aporta multiplataforma |
| Un DAO por funcionalidad, no por tabla | Un DAO puede consultar varias tablas; así registro y compra caben en una sola transacción |
| `@Transaction` en registro y compra | Se guardan varias tablas a la vez: o se guarda todo o nada |
| Lecturas con `Flow`, escrituras con `suspend` | La pantalla se actualiza sola cuando cambian los datos y la UI nunca se congela |
| Paquete `data/local/relation` | Clases que juntan varias tablas en un resultado (ej.: producto con sus imágenes); no son tablas |
| `dynamicColor = false` en el tema | Si no, Android 12+ usa los colores del fondo de pantalla e ignora la paleta del mockup |
| Componentes sin estado propio (`valor` + `onValorChange`) | El dato vive en el ViewModel (MVVM); el componente solo lo muestra y avisa cambios |
| Dependencia `material-icons-core` | Material 3 1.4.0 ya no incluye los íconos; core trae los básicos sin el peso de extended |
| Un `UiState` (`data class`) por pantalla en `model/` | La pantalla solo dibuja el estado; cambiarlo con `copy()` crea uno nuevo y Compose redibuja |
| Registro en una sola pantalla con `pasoActual` | Los datos de la etapa 1 se necesitan al final para guardar el usuario; un solo estado no los pierde |
| Regiones y comunas fijas en `model/Regiones.kt` | El MER las guarda como texto en `direccion`; no hay tabla. Selectores dependientes (CU-10) |
| Calle y número en campos separados | Así están en la entidad `Direccion` de Jonas |
| `AppDatabase` como singleton (`getDatabase`) | Abrir la base es costoso; una sola instancia evita conexiones duplicadas al mismo archivo |
| Datos semilla (admin + catálogo de prueba) en `Callback.onCreate` | Se insertan una sola vez al crear la base; ids escritos a mano para que las FK calcen |
| Cuenta admin creada en `Callback.onCreate` | Se ejecuta una sola vez, al crear la base; así la cuenta existe desde el primer inicio (cuenta preexistente) |
| `fallbackToDestructiveMigration` (solo desarrollo) | Si cambia una entidad y se sube `version`, la base se recrea en vez de fallar; en producción se usarían migraciones |
| Gradle JVM en 21 | Es el JDK que trae Android Studio; con 25 Gradle tomaba el JRE de VS Code, que no sirve para compilar |
| DataStore guarda solo el `id` del usuario | El rol y el estado se leen siempre desde Room; si el admin los cambia, la app se entera sola |
| Sesión dentro de `UsuarioRepository` | Login, registro y cerrar sesión siempre usan Room y DataStore juntos |
| Repository lanza excepciones con el mensaje del caso de uso | El repository decide qué está mal; el ViewModel lo atrapa (`try/catch`) y lo muestra como `Error` |
| `SinTarjetaException` en la compra | Excepción propia para que el ViewModel redirija a "Agregar tarjeta" en vez de mostrar un error |
| Correos guardados sin espacios y en minúsculas | `Ana@Mail.com` y `ana@mail.com` son la misma cuenta |
| Fotos copiadas a `filesDir/productos/` con nombre único (UUID) | La imagen de la galería podría borrarse; la copia queda dentro de la app |
| Producto con ventas → `inactivo`; sin ventas → se borra con sus fotos | Se conserva el historial de pedidos sin dejar archivos huérfanos |
| `EstadoPedido` centraliza los textos de estado | Deben coincidir exactamente (tilde incluida) con el SQL y la entidad `Pedido` |
| `ProductoUi` en `model/` en vez de usar las entidades en la UI | Junta lo que muestra la tarjeta (producto, imagen, favorito); la pantalla no depende de Room |
| Un solo NavHost en `AppNavegacion` | Decide qué pantalla se ve; las pantallas no se conocen entre sí, solo avisan con callbacks |
| `EstadoDemo` provisional (datos en memoria) | Permite probar las pantallas antes de los ViewModel; se reemplaza por ViewModel + Room |
| Un solo `EstadoPedido` (enum en `model/`) | Una sola fuente de verdad; en la BD se guarda su `etiqueta` (texto), que coincide con el SQL y la entidad `Pedido` |
| `lifecycle-viewmodel-compose` con la misma versión que `lifecycle-runtime-ktx` | Las librerías de lifecycle deben ir en la misma versión; `viewModel()` crea el ViewModel dentro de un Composable |
| `material3-window-size-class` sin versión | La toma del Compose BOM; se usará en `WindowSizeUtils` (Guía 9) para la grilla adaptable |
| Una sola `AppViewModelFactory` (sin Hilt) | Los ViewModel reciben su repository por constructor; la Factory los arma y comparte la misma BD y DataStore |
| `EstadoOperacion` (sealed class) reutilizable | Cargando / Exito / Error de cualquier operación; reemplaza los `cargando`, `errorGeneral` y `...Exitoso` sueltos de cada UiState |
| La sesión decide la navegación (`LaunchedEffect(sesion)`) | Login y registro solo guardan la sesión; un único observador lleva al catálogo/resumen o al login. Si hay sesión guardada, la app abre directo |
| `SesionViewModel` fuera del NavHost | Vive con la Activity: el rol (menú) y cerrar sesión se comparten en todas las pantallas |
| Usuario bloqueado = sin sesión | Si el admin bloquea una cuenta, `SesionViewModel` la saca al login |
| Registro: `correoDisponible()` en la etapa 1 | CU-01 pide avisar del correo ya registrado antes de pedir el código, no al final |
| Registro sin navegación propia | `registrar()` guarda la sesión y el mismo observador de sesión lleva al catálogo (CU-03: inicia sesión automáticamente) |
| `RegistroViewModel` dentro de `composable(REGISTRO)` | Sigue vivo al ir a Términos y volver, así no se pierde lo escrito (CU-04) |
| `CatalogoViewModel` compartido (fuera del NavHost) | Inicio, Detalle, Categorías y Favoritos usan la misma lista de Room (ViewModel compartido, Guía 11) |
| `combine` de productos + favoritos + categorías | Cualquier cambio en Room (favorito, producto, categoría) recalcula el estado y redibuja todas las pantallas |
| Búsqueda en su propio `StateFlow` | El TextField se actualiza al instante; el filtrado se calcula en `CatalogoUiState` |
| `EstadoCompra` (sealed) propio para la compra | Además de éxito/error tiene el caso `SinTarjeta`, que redirige a datos de pago (CU-24) |
| `telefono` en `PerfilUsuario` (BD versión 2) | CU-09 lo pide editable; se agrega como opcional. Se decidió no modificar el MER: se explica en la presentación |
| Datos semilla con `INSERT OR IGNORE` y `onDestructiveMigration` | Al subir la versión Room recrea las tablas sin llamar a `onCreate`; así los datos se insertan igual y nunca se duplican |
| Imágenes con `BitmapFactory` reducido (sin librerías) | Fotos de cámara pesan MB; se cargan en segundo plano y achicadas. No agrega dependencias |
| Menú del admin = opciones admin + las del cliente | CU-27: la interfaz del admin es la del cliente más el panel |
| Vencimiento de tarjeta como texto `MM/AA` | Así viene en la tarjeta (sin día); Room no guarda fechas sin convertidor. Se valida formato y que no esté vencida con `YearMonth` |
| `obtenerWindowSizeClass()` en `ui/utils` (Guía 9) | Un solo lugar calcula el tamaño; las pantallas reciben `columnas` y sus `@Preview` siguen funcionando |
| Validaciones en `ui/utils/Validaciones.kt` | Funciones puras reutilizables (ViewModel y demo) y testeables en la Unidad 3 |

---

## Preguntas para el profesor

- [ ] ¿Entidades en `data/local/entity` o en `model/`?
- [ ] ¿La demo será en un solo dispositivo? (Room no comparte datos entre teléfonos)
- [ ] ¿Qué se exige sobre el modo offline?
- [ ] ¿`minSdk` 36 o 33? (en clases se dijo 36, las guías dicen API 33+; con 36 la app solo se instala en Android 16 o superior)
- [ ] ¿Se acepta Room 2.8 (`androidx.room`) o se exige Room 3 (`androidx.room3`)?
- [ ] Gráfico de "visitas" del panel admin: el MER no registra visitas. ¿Se agrega una tabla o se usa otra métrica?
- [ ] ¿El repositorio de GitHub debe ser privado? (hoy es público)

---

## Registro de avances

*(La entrada más reciente va primero. Máximo 5–8 líneas por entrada.)*

### 2026-10-28 — Lectura de códigos QR
**Autor:** Max · **Rama:** `feature/lectura-qr`

- **Qué:** `PantallaEscanearQr` (CameraX muestra la cámara y ML Kit lee el QR) y `QrViewModel`, que interpreta el texto: `producto:N` abre la pieza y `pedido:N` abre el pedido. El cliente ve el detalle o el seguimiento de **su** pedido; el administrador abre la edición de la pieza o el detalle del pedido. Botón QR junto al buscador del catálogo y opción "Escanear QR" en el menú.
- **Por qué:** la Prueba 2 exige lectura QR con la cámara. Une la pieza física con la app: etiqueta en la feria, inventario en el taller y QR pegado en el paquete.
- **Conecta con:** `CatalogoViewModel`, `AdminViewModel` y `PedidosViewModel` (para comprobar que el id existe) y `Rutas.ESCANEAR_QR`. Dependencias nuevas: `camera-camera2`, `camera-lifecycle`, `camera-view` y `barcode-scanning`.
- **Pendiente:** control de stock (requisito del proyecto); en el emulador se prueba con **Extended Controls > Camera > Virtual scene images** y QR generados en qrcode-monkey.com.

### 2026-10-28 — Bitácora al día y cierre de la Unidad 2
**Autor:** Jonas · **Rama:** `docs/bitacora`

- **Qué:** "Estado por capa" incluye los archivos nuevos (`UsuarioConPerfil`, `EstadoOperacion`/`EstadoSesion`/`EstadoCompra`, `PantallaCarga`, `ImagenArtesania`, `SelectorImagenes`) y los datos semilla. Se registra la decisión sobre `telefono` y una pregunta nueva para el profesor.
- **Por qué:** dejar la bitácora coherente con `main` (PR #17) antes de la presentación. Todas las pantallas usan Room; `EstadoDemo` ya no existe.
- **Conecta con:** todo el proyecto; resume el cierre de los ViewModel (PR #12 a #17).
- **Pendiente:** gráficos del resumen con datos de demo (el MER no registra visitas); fotos de cámara pueden verse giradas (no se lee EXIF); borrar en GitHub las ramas ya mergeadas. Decisiones que cierran pendientes anteriores: `telefono` no se agrega al MER; el vencimiento de la tarjeta queda como texto `MM/AA`.

### 2026-10-28 — Recuperar contraseña, orden por precio y diseño adaptable
**Autor:** Jonas · **Rama:** `feature/ajustes-finales`

- **Qué:** `PantallaRecuperar` y `RecuperarViewModel` con dos etapas (correo → código 1234 + nueva contraseña) que usan `recuperarContrasenia`. Botón de orden por precio ascendente/descendente en `PantallaCategoria` (`CatalogoViewModel.cambiarOrden`). `ui/utils/WindowSizeUtils.kt` (`obtenerWindowSizeClass`, `columnasGrilla`): las grillas muestran 2, 3 o 4 columnas. La validación de tarjeta rechaza fechas vencidas.
- **Por qué:** completa CU-07 y CU-15, y la Guía 9 (Window Size Classes).
- **Conecta con:** `AppNavegacion` calcula las columnas una vez y las pasa a Inicio, Favoritos y Categoría.
- **Pendiente:** agregar `telefono` (opcional) a PERFIL CLIENTE en el MER.

### 2026-10-28 — App completa con Room (todos los ViewModel)
**Autor:** Jonas · **Rama:** `integracion/app-completa`

- **Qué:** nuevos `CarritoViewModel` (carrito, datos de pago, compra con `EstadoCompra` y redirección si no hay tarjeta), `PerfilViewModel` (3 secciones Editar/Guardar), `PedidosViewModel` (seguimiento, notificaciones, estados, ventas de hoy), `AdminViewModel` (CRUD de categorías y productos con imágenes, control de usuarios) y `RecuperarViewModel`; conversiones en `viewmodel/Mapeos.kt`. Imágenes: `ImagenArtesania` y `SelectorImagenes` (galería `GetMultipleContents`, cámara `TakePicture` + permiso + `FileProvider`), carrusel `HorizontalPager` en el detalle. BD versión 2: `PerfilUsuario.telefono`, cliente demo Ana. Se eliminó `EstadoDemo`.
- **Por qué:** todas las pantallas leen y escriben en Room; flujo completo cliente → compra → admin → seguimiento.
- **Conecta con:** todos los repositories; `AndroidManifest` (permiso CAMERA, provider) y `res/xml/file_paths.xml`.
- **Pendiente:** CU-07 sin etapa de nueva contraseña (falta UI); botón de orden descendente (CU-15); `WindowSizeUtils`; gráficos del resumen con datos reales; agregar `telefono` al MER.

### 2026-10-28 — Catálogo con Room (CatalogoViewModel)
**Autor:** Jonas · **Rama:** `feature/catalogo-viewmodel`

- **Qué:** en `viewmodel/` se creó `CatalogoViewModel` (une con `combine` productos activos, ids de favoritos del usuario y categorías; convierte `ProductoConImagenes` → `ProductoUi`). `CatalogoUiState` suma `categorias`, `favoritos`, `producto(id)`, `productosDeCategoria(id)` (por precio) y busca también en la descripción; deja `cargando`/`errorGeneral` (ahora `EstadoOperacion`). `PantallaCatalogo` recibe `carga`. Inicio, Detalle, Categorías y Favoritos leen de Room.
- **Por qué:** CU-12 a CU-19 con datos reales; un favorito marcado en una pantalla se ve en todas porque todas leen la misma consulta.
- **Conecta con:** `ProductoRepository`, `CategoriaRepository`, `FavoritoRepository` y `UsuarioRepository.usuarioIdSesion`.
- **Pendiente:** puente provisional en `AppNavegacion` que copia productos y categorías de Room a `EstadoDemo` (carrito y gestión de contenido); botón para ordenar por precio descendente (CU-15) en `PantallaCategoria` (Maxi).

### 2026-10-28 — Datos semilla del catálogo
**Autor:** Jonas · **Rama:** `feature/datos-semilla`

- **Qué:** en `AppDatabase`, el callback `onCreate` (ahora `datosInicialesCallback`) inserta además 4 categorías y 8 productos de prueba (sin imágenes). El RUT del admin queda con formato `11.111.111-1`, igual al del registro.
- **Por qué:** la base solo tenía al admin; sin productos, el catálogo con Room se vería vacío. Los productos reales los creará el admin desde Gestión de contenido (CU-32).
- **Conecta con:** `ProductoDao.observarActivos` y `CategoriaDao.observarTodas`, que usará el `CatalogoViewModel`.
- **Pendiente:** el callback solo corre al crear la base: hay que desinstalar la app para verlos. Imágenes reales de los productos.

### 2026-10-27 — Registro con Room (RegistroViewModel)
**Autor:** Jonas · **Rama:** `feature/registro-viewmodel`

- **Qué:** en `viewmodel/` se creó `RegistroViewModel` (etapa 1 valida y revisa `correoDisponible`; etapa 2 código fijo 1234; etapa 3 valida RUT y dirección y llama a `registrar`). `RegistroUiState` deja de tener `cargando`/`errorGeneral`/`registroExitoso`; `PantallaRegistro` recibe `operacion` y muestra el error general. Se quitó el registro de `EstadoDemo` y el `if` provisional de "Cerrar sesión".
- **Por qué:** las cuentas nuevas quedan guardadas en Room (usuario + perfil + dirección en una transacción) e inician sesión solas (CU-01 a CU-03).
- **Conecta con:** `UsuarioRepository.correoDisponible` y `registrar`; `SesionViewModel` navega al catálogo.
- **Pendiente:** CU-07 recuperar contraseña (la pantalla necesita etapas de código y nueva contraseña); el perfil sigue mostrando datos demo hasta `PerfilViewModel`; RUT del admin sembrado sin puntos (`11111111-1`) vs. formato del registro (`12.345.678-5`).

### 2026-10-27 — Factory, sesión y login con Room
**Autor:** Jonas · **Rama:** `feature/login-viewmodel`

- **Qué:** en `viewmodel/` se crearon `AppViewModelFactory`, `SesionViewModel` (lee `usuarioActual`: sin sesión / activa con `esAdmin` según `usuario.rol`) y `LoginViewModel`. En `model/`: `EstadoOperacion` y `EstadoSesion` (sealed class); `LoginUiState` deja solo campos y errores. `PantallaLogin` recibe `operacion`. `AppNavegacion` abre según la sesión guardada y navega al cambiar la sesión. Validaciones movidas a `ui/utils/Validaciones.kt`.
- **Por qué:** el login deja de ser demo: valida contra Room (CU-05) y guarda la sesión en DataStore; el menú usa el rol real.
- **Conecta con:** `UsuarioRepository` (login, usuarioActual, cerrarSesion); `EstadoDemo` ya no tiene login ni `esAdmin`.
- **Pendiente:** `RegistroViewModel` (el registro demo aún entra sin sesión real); usar `EstadoOperacion` en Registro y Catálogo.

### 2026-10-27 — Correcciones de la revisión y dependencias para ViewModel
**Autor:** Jonas · **Rama:** `fix/revision`

- **Qué:** la carpeta `data/local/relacion` se renombró a `relation` (coincide con su paquete). `PedidoRepository` usa el enum `EstadoPedido` de `model/` y se borró el `object` duplicado; `marcarEnPreparacion` y `marcarEnviado` pasan a `cambiarEstado(pedidoId, nuevo)`. Se agregaron `lifecycle-viewmodel-compose` y `material3-window-size-class`.
- **Por qué:** evitar dos definiciones con el mismo nombre y dejar listas las dependencias para los ViewModel y la pantalla adaptable.
- **Conecta con:** `cambiarEstado` lo usará el ViewModel del admin (la pantalla ya entrega un `EstadoPedido`); las dependencias, `AppNavegacion` y `ui/utils/WindowSizeUtils`.
- **Pendiente:** decidir el campo `telefono` (está en `PerfilUiState` y `PedidoUi`, pero no en `PerfilUsuario` ni en el MER): agregarlo como opcional o quitarlo de las pantallas.

### 2026-10-27 — Integración de pantallas con la capa de datos
**Autor:** Jonas · **Rama:** `integracion/pantallas`

- **Qué:** se juntó `main` (base de datos, sesión y repositories) con `feature/pantallas-admin`, que contiene catálogo, navegación, pantallas de cliente y pantallas de admin. El único conflicto fue esta bitácora; se conservaron las entradas de ambos.
- **Por qué:** las 4 ramas de UI se crearon una tras otra desde una versión anterior a la base de datos; integrarlas en una sola rama evita resolver el mismo conflicto cuatro veces.
- **Conecta con:** deja en `main` la UI completa y la capa de datos juntas, listas para los ViewModel.
- **Pendiente:** crear las ramas nuevas siempre desde `main` actualizado.

### 2026-10-27 — Pantallas del administrador
**Autor:** Maxi · **Rama:** `feature/pantallas-admin`

- **Qué:** en `ui/screens/admin/`: Resumen (gráficos de barras), Notificaciones y detalle del pedido (cambia el estado a "En preparación" y "Enviado"), Gestión de contenido (categorías y productos con formularios) y Control de usuarios (bloquear y hacer administrador, con confirmación). Menú lateral según el rol.
- **Por qué:** cubren los casos de uso del administrador del mockup. Demo: el correo `admin@aleyleo.cl` entra como administrador.
- **Conecta con:** `EstadoDemo` (provisional) guarda los cambios en memoria; con Jonas pasan a `CategoriaDao`, `ProductoDao`, `PedidoDao` y `UsuarioDao`.
- **Pendiente:** lectura de QR, ViewModels, imágenes reales de productos y datos reales del gráfico.

### 2026-10-27 — Pantallas del cliente y menú lateral
**Autor:** Maxi · **Rama:** `feature/pantallas-cliente`

- **Qué:** pantallas de términos, recuperar contraseña, detalle, categorías, favoritos, carrito, datos de pago, perfil (3 pestañas) y seguimiento de pedidos. Menú lateral (`ModalNavigationDrawer`) y componentes `GrillaProductos`, `FilaLista`, `FormularioPago`, `MenuLateral`. Rutas con argumento (`detalle/{id}`).
- **Por qué:** cubren los casos de uso del cliente con el diseño del mockup. `FormularioPago` se reutiliza en el pago y en el perfil.
- **Conecta con:** `EstadoDemo` guarda los datos en memoria y valida (correo, RUT, código 1234, tarjeta) solo mientras no existan los ViewModel; luego cada parte pasa a su ViewModel y a Room (con Jonas).
- **Pendiente:** pantallas del administrador, lectura de QR y ViewModels.

### 2026-10-27 — Navegación entre pantallas
**Autor:** Maxi · **Rama:** `feature/navegacion`

- **Qué:** se agregó la dependencia Navigation Compose. En `navigation/` se crearon `Rutas` (nombres de las pantallas) y `AppNavegacion` (NavHost: login → registro → catálogo). `MainActivity` ahora solo aplica el tema y llama a `AppNavegacion`. Los productos demo pasaron a `model/ProductosDemo.kt`.
- **Por qué:** un solo NavHost decide qué pantalla se ve; las pantallas no se conocen entre sí, solo avisan con callbacks. `popUpTo` saca el login del historial al entrar al catálogo.
- **Conecta con:** los estados en `AppNavegacion` son provisionales (`remember`); se reemplazan por los ViewModel (con Jonas).
- **Pendiente:** rutas de términos, recuperar contraseña, detalle, carrito, perfil y admin.

### 2026-10-27 — Pantalla de catálogo (Inicio)
**Autor:** Maxi · **Rama:** `feature/pantalla-catalogo`

- **Qué:** en `model/` se crearon `ProductoUi` y `CatalogoUiState` (con `productosFiltrados` calculado). En `ui/screens/catalogo/` se creó `PantallaCatalogo`: barra "Inicio", buscador "Buscar artesanías…", grilla de 2 columnas con `TarjetaProducto` y "Sin resultados", con `@Preview`.
- **Por qué:** `ProductoUi` junta lo que muestra la tarjeta (datos de `Producto`, `ImagenProducto` y `Favorito`), así la pantalla no depende de las entidades de Room. La grilla es `LazyVerticalGrid` para dibujar solo lo visible.
- **Conecta con:** el `CatalogoViewModel` (con Jonas) cargará los productos desde `ProductoRepository` y los convertirá a `ProductoUi`.
- **Pendiente:** navegación, detalle de producto e imagen real de los productos.

### 2026-10-26 — Sesión (DataStore) y repositories
**Autor:** Jonas · **Rama:** `feature/sesion-repository`

- **Qué:** en `data/local/datastore/` se creó `SesionDataStore` (guarda el id del usuario con sesión; lo expone como `Flow`). En `repository/` se crearon 6 repositories: Usuario (login, registro, sesión, perfil, control de usuarios), Categoria, Producto (copia las fotos a la app), Carrito, Favorito y Pedido (compra, estados, ventas del día).
- **Por qué:** el repository junta las fuentes de datos de cada funcionalidad y aplica las reglas de los casos de uso (ej.: CU-05 cuenta inexistente / contraseña incorrecta / bloqueada). Los ViewModel solo llaman al repository y nunca tocan los DAO.
- **Conecta con:** usa los DAO, `AppDatabase` y `SesionDataStore`; lo usarán los ViewModel.
- **Pendiente:** crear los ViewModel (login y registro primero) y su `ViewModelProvider.Factory`. Falta agregar a CU-05 la excepción de cuenta bloqueada.

### 2026-10-26 — Base de datos (AppDatabase)
**Autor:** Jonas · **Rama:** `feature/database`

- **Qué:** en `data/local/database/` se creó `AppDatabase`: registra las 11 entidades y los 6 DAOs, y es singleton (`getDatabase`). Al crearse por primera vez inserta la cuenta admin con su perfil. Gradle JVM quedó en 21.
- **Por qué:** es el único punto de acceso a la base local. Al compilar, Room (KSP) validó todo el SQL de `dao/` y las clases de `relation/`; también quedó resuelto el `tableName` de `CarritoItem`.
- **Conecta con:** la usarán los repository para obtener los DAO.
- **Pendiente:** ambos deben tener Gradle JVM en 21 (archivo `gradle/gradle-daemon-jvm.properties`). La lógica de los DAO se probará al usarlos en cada funcionalidad y con pruebas en la Unidad 3.

### 2026-10-25 — Estados de pantalla y pantallas de login y registro
**Autor:** Maxi · **Ramas:** `feature/model`, `feature/pantalla-login`, `feature/pantalla-registro`

- **Qué:** en `model/` se crearon `LoginUiState`, `RegistroUiState` (3 etapas, con `puedeEnviar` / `puedeContinuar` calculados) y `Regiones.kt`. En `ui/screens/` se crearon `PantallaLogin` y `PantallaRegistro` (etapas cuenta, código y datos), con `@Preview`.
- **Por qué:** las pantallas no guardan datos: reciben un estado y avisan las acciones (MVVM). Las validaciones irán en el ViewModel.
- **Conecta con:** usan `ui/components` y `ui/theme`; los ViewModel (con Jonas) entregarán el estado y guardarán en `Usuario`, `PerfilUsuario` y `Direccion`.
- **Pendiente:** ViewModels, pantalla de términos, recuperar contraseña y navegación.

### 2026-10-24 — Componentes reutilizables (ui/components)
**Autor:** Maxi · **Rama:** `feature/ui-components`

- **Qué:** en `ui/components/` se crearon `Botones.kt` (`BotonPrincipal`, `BotonSecundario`), `CampoTexto.kt`, `CampoSelector.kt` (lista desplegable), `BarraSuperior.kt` y `TarjetaProducto.kt` (+ `formatearPrecio`), cada uno con `@Preview`. Se agregó `material-icons-core` en Gradle.
- **Por qué:** las pantallas reutilizan las mismas piezas del mockup; reciben datos y eventos por parámetros, así el estado queda en el ViewModel.
- **Conecta con:** usa `ui/theme`; lo usarán las pantallas de `ui/screens`.
- **Pendiente:** la imagen de `TarjetaProducto` es un degradado provisional hasta tener la ruta real desde la BD.

### 2026-10-24 — Tema visual de la app (ui/theme)
**Autor:** Maxi · **Rama:** `feature/ui-theme`

- **Qué:** en `ui/theme/` se adaptaron `Color.kt` (paleta clara y oscura del mockup), `Theme.kt` (esquemas Material 3, `dynamicColor = false`, roles `surfaceContainer*`) y `Type.kt` (títulos con serifa, textos sin serifa); se crearon `Shape.kt` (esquinas del mockup) y `VistaPreviaTema.kt` (`@Preview` claro y oscuro).
- **Por qué:** las pantallas toman colores, fuentes y formas desde `MaterialTheme`; el diseño queda consistente y se cambia en un solo lugar.
- **Conecta con:** lo usarán todas las pantallas y componentes de `ui/` a través de `ArtesaniasAleoTheme`.
- **Pendiente:** el `Button` de Material 3 no toma las formas del tema; se resuelve con `BotonPrincipal` en `ui/components`.

### 2026-10-24 — Dependencias de Room y DAOs
**Autor:** Jonas · **Rama:** `jonas`

- **Qué:** Gradle con Room 2.8.5, KSP 2.3.11 y DataStore 1.2.1 (`libs.versions.toml` y ambos `build.gradle.kts`); imports de las entidades cambiados de `androidx.room3` a `androidx.room`. Se crearon 6 DAOs en `data/local/dao/` (Usuario, Categoria, Producto, Carrito, Favorito, Pedido) y 4 clases en `data/local/relation/`. `Direccion` suma el campo `departamento`.
- **Por qué:** KSP genera la implementación de los DAO al compilar. El registro (usuario + perfil + dirección) y la compra (pedido + detalle + vaciar carrito) usan `@Transaction` para guardar todo o nada.
- **Conecta con:** usa las entidades; lo usará el repository; se registran en `AppDatabase`.
- **Pendiente:** corregir `tableName` de `CarritoItem` (falla el SQL en CarritoDao y PedidoDao); validar todo el SQL al crear `AppDatabase`; dejar `departamento` como `String? = null`, porque es opcional en CU-03.

### 2026-10-22 — Entidades de Room
**Autor:** Jonas · **Rama:** `feature/modelo-datos`

- **Qué:** se crearon las 11 entidades en `data/local/entity/` (Usuario, PerfilUsuario, Direccion, Tarjeta, Categoria, Producto, ImagenProducto, CarritoItem, Favorito, Pedido, PedidoDetalle).
- **Por qué:** son las tablas de la base local; todo lo demás (DAO, repository, pantallas) se construye sobre ellas.
- **Conecta con:** `AppDatabase` y los DAO (aún no creados).
- **Pendiente:** revisar que el índice único de `Usuario` apunte a `"correo"` y que todas las entidades tengan sus `Index` en las FK.

### 2026-10-21 — Configuración inicial
**Autor:** Jonas

- **Qué:** proyecto creado en Android Studio, SDK configurado, repositorio en GitHub con `.gitignore`.
- **Por qué:** `local.properties` queda fuera del repo porque tiene la ruta del SDK de cada computador.

---

## Cómo actualizar esta bitácora

1. Al terminar un avance, agrega una entrada arriba de la última (corta) y actualiza el **Estado por capa** si corresponde.
2. Súbela en el mismo commit que tu cambio.
3. Si quieres que una IA la redacte, pégale este prompt, junto con la bitácora actual y lo que hiciste (archivos cambiados, `git diff --stat` o tu explicación):

```
Actualiza esta bitácora de un proyecto Android (Kotlin, MVVM, Room) con el avance que te describo.

Reglas:
- Escribe en español, breve: máximo 5–8 líneas por entrada.
- Agrega UNA entrada nueva al inicio de "Registro de avances" con este formato:
  ### AAAA-MM-DD — Título corto
  **Autor:** [nombre] · **Rama:** `[rama]`
  - **Qué:** qué se hizo y en qué package/archivo.
  - **Por qué:** la razón (para poder explicarla al profesor).
  - **Conecta con:** qué capa lo usa o de cuál depende.
  - **Pendiente:** solo si hay algo pendiente o una duda.
- Actualiza "Estado por capa" (⬜ → 🔄 → ✅) solo si una capa cambió de estado.
- Si se tomó una decisión de diseño nueva, agrégala a la tabla con su "por qué" en una línea.
- Preguntas para el profesor:
  - Si en lo que hice aparece una duda que debe resolver el profesor, agrégala como casilla `- [ ]` en "Preguntas para el profesor", con el contexto mínimo entre paréntesis.
  - Si una pregunta ya se respondió, márcala `- [x]` y agrega la respuesta en la misma línea (ej.: "→ Respuesta: minSdk 36").
  - No borres preguntas ni dupliques las que ya están.
  - Al final de tu respuesta, lista aparte las preguntas que siguen pendientes.
- No inventes nada que no esté en la información que te doy. Si falta un dato (autor, rama, fecha), pregúntalo.
- Devuelve el documento completo actualizado.

Bitácora actual:
[pegar BITACORA.md]

Lo que hice:
[describir el avance o pegar git diff --stat]
```
