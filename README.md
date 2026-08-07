# 🎬 CineKMP — Repositorio Semilla (Bootcamp Kotlin Multiplatform)

Proyecto base **funcional** para el Bootcamp KMP (julio 2026). Parte de la plantilla oficial
de JetBrains (Kotlin Multiplatform + Compose Multiplatform) y es el punto de partida que
irás transformando en **CineKMP** a lo largo de las 9 sesiones.

![Kotlin](https://img.shields.io/badge/Kotlin-2.3.21-blueviolet)
![Gradle](https://img.shields.io/badge/Gradle-9.3.1-02303A)
![Compose](https://img.shields.io/badge/Compose%20MP-1.11.0-4285F4)
![Platforms](https://img.shields.io/badge/Targets-Android%20%7C%20iOS-green)

- Stack incluido: **Ktor** (red) · **kotlinx.serialization** · **Koin** (DI) · **ViewModels**
  compartidos · **Navigation Compose** · **Coil** (imágenes) · UI compartida en **Compose MP**.
- Targets: **Android** + **iOS** con UI 100% compartida.

> ℹ️ **Sobre el estado inicial:** el seed corre hoy como una mini-app de ejemplo (catálogo del
> *Metropolitan Museum*, con una **API pública que NO requiere key**). Eso demuestra que todo
> compila y corre en Android e iOS. A partir de la **Sesión 3** reemplazarás su capa de datos
> por la de **TMDB (películas)** para convertirla en CineKMP. Arrancar de una app que ya
> funciona y refactorizarla es exactamente el flujo real de trabajo.

---

## 📁 Estructura del proyecto

```
CineKMP/
├── gradlew / gradlew.bat          # Gradle wrapper (NO necesitas instalar Gradle)
├── settings.gradle.kts            # rootProject.name = "CineKMP"
├── gradle/libs.versions.toml      # Version catalog (todas las dependencias y versiones)
├── shared/                        # 🧠 MÓDULO COMPARTIDO (el corazón del curso)
│   └── src/
│       ├── commonMain/kotlin/com/jetbrains/kmpapp/
│       │   ├── App.kt             # UI raíz + navegación (Compose MP)
│       │   ├── data/              # Ktor API, modelos, repositorio   ← Sesiones 3 y 4
│       │   ├── di/Koin.kt         # Inyección de dependencias         ← Sesión 5
│       │   └── screens/           # list/ y detail/ (Screen+ViewModel) ← Sesiones 5-7
│       ├── commonMain/composeResources/   # strings/imágenes compartidas
│       └── iosMain/kotlin/.../MainViewController.kt  # punto de entrada iOS
├── androidApp/                    # App Android (host de la UI compartida)
└── iosApp/                        # App iOS (proyecto Xcode)
    ├── iosApp.xcodeproj
    └── iosApp/ (ContentView.swift, iOSApp.swift)
```

> El paquete se mantiene como `com.jetbrains.kmpapp` a propósito: renombrarlo tocaría el
> proyecto Xcode y podría romper la compilación. Puedes renombrarlo más adelante como
> ejercicio avanzado.

---

## ✅ Requisitos previos

| Herramienta | Necesario para | Nota |
|---|---|---|
| **JDK 21 (LTS)** o superior | Compilar todo | Gradle 9.3.1 también corre con Java 26 |
| **Android Studio** (+ plugin *Kotlin Multiplatform*) | Android / emulador | Recomendado para desarrollar |
| **Xcode completo** | Compilar iOS | ⚠️ Las *Command Line Tools* **no** bastan |
| **Gradle** | — | No hace falta instalarlo: se usa `./gradlew` |

> ⚠️ **Para iOS** necesitas **Xcode** (App Store). Tras instalarlo, ejecútalo una vez y corre:
> `sudo xcode-select -s /Applications/Xcode.app/Contents/Developer`
>
> 💡 Diagnostica tu entorno con: `brew install kdoctor && kdoctor`

---

## ▶️ Cómo correrlo

### Opción A — Android por terminal (lo más rápido para probar)

```bash
cd ~/repositorios/CineKMP

./gradlew --version                    # verifica el wrapper
./gradlew :androidApp:assembleDebug    # 1ª vez: descarga dependencias (tarda)
./gradlew :androidApp:installDebug     # con emulador/dispositivo conectado
```
El APK queda en `androidApp/build/outputs/apk/debug/`.

### Opción B — Android Studio (recomendado para desarrollar)

1. **Open** → selecciona la carpeta `~/repositorios/CineKMP`.
2. Espera el *Gradle sync* (la 1ª vez descarga Kotlin/Native y dependencias).
3. Selecciona la config **androidApp** + un emulador → ▶️ **Run**.

### Opción C — iOS (requiere Xcode instalado)

1. Abre `iosApp/iosApp.xcodeproj` en **Xcode**.
2. Elige un simulador (p. ej. *iPhone 16*) → ▶️ **Run**.
   Xcode compila el framework Kotlin automáticamente (`embedAndSignAppleFrameworkForXcode`).

### Comandos útiles

```bash
./gradlew tasks                          # lista de tareas disponibles
./gradlew :shared:allTests               # tests del módulo compartido (todas las plataformas)
./gradlew :shared:iosSimulatorArm64Test  # tests solo en simulador iOS
./gradlew clean                          # limpia builds
```

---

## 🧭 Mapa del Bootcamp (9 sesiones · julio 2026)

| Sesión | Fecha | Qué tocarás en este repo |
|---|---|---|
| **1** | Vie 3 jul | Correr el seed en Android/iOS; explorar `shared/` y `expect`/`actual`. |
| **2** | Jue 9 jul | Utilidades multiplataforma + Corrutinas/Flow. |
| **3** | Vie 10 jul | Reemplazar `data/MuseumApi.kt` por `TmdbApi` + DTOs/mappers. |
| **4** | Jue 16 jul | Añadir **SQLDelight** (persistencia offline-first) a `data/`. |
| **5** | Vie 17 jul | Reorganizar en `domain/data/presentation/di`; casos de uso; Koin. |
| **6** | Jue 23 jul | Evolucionar `screens/list` y `screens/detail` (Compose MP). |
| **7** | Vie 24 jul | Navegación avanzada, búsqueda, favoritos, Coil, interop de UI. |
| **8** | Jue 30 jul | Consumir `shared` desde **SwiftUI nativo** + SKIE en `iosApp/`. |
| **9** | Vie 31 jul | Tests, **CI (GitHub Actions)** y proyecto final. |

📚 Material teórico y tareas:
- `~/Desktop/Kotlin_Multiplatform_KMP/Manual_Bootcamp_KMP.md`
- `~/Desktop/Kotlin_Multiplatform_KMP/KMP_Base_de_Conocimiento.md`

---

## 🔑 Configurar la API key de TMDB (a partir de la Sesión 3)

La key **nunca** se sube a Git. Guárdala en `local.properties` (ya ignorado por `.gitignore`):

```properties
# local.properties
tmdb.apikey=TU_API_KEY_AQUI
```
Luego se expone al código vía `BuildConfig`/parámetro de Gradle (lo verás en clase).

---

## 🐛 Solución de problemas

- **El primer build tarda mucho:** normal. Descarga Gradle, el toolchain de Kotlin/Native
  (cientos de MB) y las dependencias. Ten paciencia y buena conexión.
- **Error de JDK / toolchain con Java 26:** instala **JDK 21 (LTS)** y añade en
  `gradle.properties`: `org.gradle.java.home=/ruta/a/tu/jdk-21`.
- **iOS no compila:** casi siempre falta **Xcode completo** (no bastan las Command Line Tools).
- **Push rechazado por `workflow scope`:** usa remoto **SSH**
  (`git remote set-url origin git@github.com:USUARIO/REPO.git`) o añade el scope con
  `gh auth refresh -s workflow`.

---

*Seed del Bootcamp KMP · julio 2026 · Basado en la plantilla oficial
[`Kotlin/kmp-app-template`](https://github.com/Kotlin/kmp-app-template) de JetBrains
(Apache 2.0, ver `LICENSE`).*
