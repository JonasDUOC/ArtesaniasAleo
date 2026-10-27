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
│       ├── relation/     🔄 ProductoConImagenes, CarritoItemConProducto,
│       │                    DetalleConProducto, PedidoConDetalles
│       ├── dao/          🔄 UsuarioDao, CategoriaDao, ProductoDao, CarritoDao,
│       │                    FavoritoDao, PedidoDao
│       ├── database/     ⬜ AppDatabase.kt (registra entidades y DAOs,
│       │                    crea la cuenta admin al primer inicio)  ← SIGUIENTE
│       └── datastore/    ⬜ SesionDataStore.kt (guarda quién inició sesión)
├── repository/           ⬜ UsuarioRepository, ProductoRepository...
├── model/                ✅ UiState de cada pantalla, ProductoUi, CategoriaUi, PedidoUi,
│                            DatosPagoUiState, PerfilUiState, ProductosDemo
├── viewmodel/            ⬜ LoginViewModel, CatalogoViewModel, CarritoViewModel...
├── ui/
│   ├── theme/            ✅ Color, Type, Shape, Theme (paleta y fuentes del mockup)
│   ├── screens/          ✅ login/ PantallaLogin, registro/ PantallaRegistro (3 etapas),
│   │                        catalogo/, detalle/, categorias/, favoritos/, carrito/, pago/,
│   │                        perfil/, seguimiento/, terminos/, recuperar/, admin/
│   ├── components/       ✅ Botones, CampoTexto, CampoSelector, BarraSuperior, TarjetaProducto,
│   │                        GrillaProductos, FilaLista, FormularioPago, MenuLateral
│   └── utils/            ⬜ WindowSizeUtils.kt
├── navigation/           🔄 Rutas, AppNavegacion (NavHost + menú lateral), EstadoDemo (provisional)
└── MainActivity.kt
```

Leyenda: ✅ terminado · 🔄 creado, falta validar · ⬜ por hacer

**Flujo:** Pantalla → ViewModel → Repository → DAO → Room, y de vuelta con Flow → StateFlow → la pantalla se actualiza. Las pantallas nunca tocan el DAO directamente.

**Plan:** construir de abajo hacia arriba y terminar primero **registro + login** completos antes del resto.

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

---

## Preguntas para el profesor

- [ ] ¿Entidades en `data/local/entity` o en `model/`?
- [ ] ¿La demo será en un solo dispositivo? (Room no comparte datos entre teléfonos)
- [ ] ¿Qué se exige sobre el modo offline?
- [ ] ¿`minSdk` 36 o 33? (en clases se dijo 36, las guías dicen API 33+; con 36 la app solo se instala en Android 16 o superior)
- [ ] ¿Se acepta Room 2.8 (`androidx.room`) o se exige Room 3 (`androidx.room3`)?
- [ ] Gráfico de "visitas" del panel admin: el MER no registra visitas. ¿Se agrega una tabla o se usa otra métrica?

---

## Registro de avances

*(La entrada más reciente va primero. Máximo 5–8 líneas por entrada.)*

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
**Autor:** Jonas · **Rama:** `[rama]`

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
