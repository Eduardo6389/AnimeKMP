# Sesión 5 — Clean Architecture + Koin (DI) + Shared ViewModel

El "cutover": la app deja de ser la plantilla Museum y queda ordenada en capas.

## Estructura (capas)

```
domain/                 (reglas puras; NO conoce Ktor ni SQLDelight)
  model/                Movie, MovieDetail
  repository/           MovieRepository (interfaz)
  usecase/              GetPopulares, GetFavoritas, BuscarPeliculas, GetDetalle, MarcarFavorita
data/                   (implementaciones concretas)
  remote/ dto/          TmdbApi, DTOs, mapper
  local/                DriverFactory, MovieLocalDataSource (SQLDelight)
  repository/           MovieRepositoryImpl (offline-first)
presentation/           (ViewModels + estado de pantalla)
  list/ detail/ favorites/
di/                     Koin (dataModule, domainModule, viewModelModule + módulo de plataforma)
screens/                UI Compose (list, detail)
```

## Qué se hizo

1. **5 casos de uso** (`domain/usecase/UseCases.kt`), invocables como funciones (`operator invoke`).
2. **3 ViewModels** con su `data class State` (cargando/datos/error):
   `MovieListViewModel`, `MovieDetailViewModel`, `FavoritesViewModel`.
3. **Koin**:
   - `dataModule` + `domainModule` + `viewModelModule` en común.
   - El `CineDb` (que necesita cosas de plataforma) lo aporta un **módulo de plataforma**:
     `initKoinAndroid(context)` (Android) y `initKoinIos()` (iOS).
   - Android arranca en `CineApp.onCreate`; iOS en `iOSApp.swift` (`KoinIosKt.doInitKoinIos()`).
4. **Limpieza**: se borraron los archivos Museum y los ViewModels viejos; `MuseumApp` → `CineApp`.

## Puntos finos (para depurar/explicar)

- **`domain` no importa Ktor ni SQLDelight**: depende solo de la interfaz `MovieRepository`.
- **ViewModels compartidos**: la misma clase Kotlin la usan Android e iOS.
- **`stateIn(WhileSubscribed)`**: el estado es "frío" hasta que la UI lo observa (ahorra recursos).

## Tests

`presentation/ViewModelsTest.kt` (9) con `FakeMovieRepository` (sin red/DB). Cubren: carga inicial,
error de red, "datos cacheados no se tapan con error", refresco manual, detalle desde DB, datos extra
de red, alternar favorito, favoritas filtradas y reactivas.

**Batería completa: 40 tests en verde** (`./gradlew :shared:iosSimulatorArm64Test`).
