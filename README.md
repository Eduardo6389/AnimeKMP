# AnimeKMP

Aplicación Kotlin Multiplatform de catálogo de anime usando Jikan v4.

## API

- Jikan v4 / MyAnimeList.
- No usa API key.
- Listado: `/top/anime?page=`.
- Detalle: `/anime/{id}/full`.
- Búsqueda: `/anime?q=`.
- El cliente limita las peticiones y reintenta un 429 con espera exponencial.

## Arquitectura

La lógica compartida vive en `shared` y la UI es nativa en cada plataforma.

```text
UI Android / iOS
       ↓
ViewModel compartido
       ↓
UseCase
       ↓
ItemRepository
   ↙         ↘
SQLDelight   Jikan/Ktor
```

- `domain`: modelos, contratos y casos de uso; no importa Ktor, SQLDelight ni Koin.
- `data`: API, mapper, persistencia y repositorio offline-first.
- `presentation`: cuatro ViewModels con `StateFlow`.
- `di`: Koin.
- `androidApp`: Jetpack Compose + Material 3.
- `iosApp`: SwiftUI + `NavigationStack`.

La estrategia de interoperabilidad iOS es el envoltorio manual permitido por el proyecto: los ViewModels exponen `observar(...)` y el wrapper Swift cancela el observador y llama `clearForIos()` en `deinit`.

## Funciones

Listado paginado, detalle, búsqueda con debounce de 300 ms, favoritos persistentes, funcionamiento offline con SQLDelight, caché con TTL de una hora, detección de conectividad, pull-to-refresh, orden local, tema persistente y borrado de caché.

## Compilar

Android:

```text
./gradlew :androidApp:assembleDebug
```

Tests y lint:

```text
./gradlew :shared:allTests ktlintCheck detekt
```

iOS requiere macOS y Xcode:

```text
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

Después abre `iosApp/iosApp.xcodeproj` en Xcode.

## JSON de prueba

En Windows ejecuta una vez:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\descargar_json.ps1
```

Guarda cinco respuestas reales de Jikan en `shared/src/commonTest/resources`.

## Capturas

| Android | iOS |
|---|---|
| ![Android](docs/capturas/android.png) | ![iOS](docs/capturas/ios.png) |

Antes de entregar se guardan las dos capturas con esos nombres.

## Documentación

- `docs/ARQUITECTURA.md`
- `docs/BITACORA.md`
- `docs/PATRONES.md`

## Limitaciones

La validación y ejecución completa de iOS necesita macOS/Xcode. En Windows se puede desarrollar y probar `shared` y Android; el job `ios` del CI valida el framework y el proyecto iOS en macOS.

## Porcentaje de lógica compartida

Dentro de `shared`, `commonMain` tiene 1239 líneas Kotlin y los source sets Android/iOS tienen 184. Eso deja aproximadamente **87.1%** del código Kotlin de `shared` en `commonMain`.
