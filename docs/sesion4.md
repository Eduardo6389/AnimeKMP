# Sesión 4 — Persistencia local con SQLDelight (offline-first)

CineKMP ahora guarda las películas en una base de datos local: la app **funciona sin internet**.

## Qué se hizo

1. **Plugin + dependencias** (`libs.versions.toml`, `shared/build.gradle.kts`):
   SQLDelight `2.3.2` (runtime, coroutines-extensions, android-driver, native-driver) + bloque
   `sqldelight { databases { create("CineDb") { packageName = "com.jetbrains.kmpapp.db" } } }`.

2. **Esquema** (`shared/src/commonMain/sqldelight/.../db/Movie.sq`): tabla `MovieEntity` y queries
   `selectAll`, `selectById`, `selectFavorites`, `insertIgnore`, `updateData`, `setFavorite`.
   SQLDelight genera `CineDb`, `MovieQueries` y `MovieEntity`.

3. **Drivers por plataforma** (`data/local/DriverFactory.*.kt`):
   Android usa `AndroidSqliteDriver` (necesita `Context`); iOS usa `NativeSqliteDriver`.
   Son **dos clases** (no `expect/actual`) porque sus constructores difieren; ambas exponen `createDriver()`.

4. **Fuente de datos local** (`data/local/MovieLocalDataSource.kt`): interfaz + impl SQLDelight.
   `observar*` devuelven `Flow` reactivos (`.asFlow().mapToList`), y `guardarTodas` corre en una
   transacción.

5. **Repositorio offline-first** (`domain/repository/MovieRepository.kt` +
   `data/repository/MovieRepositoryImpl.kt`): la UI **observa la DB**; la red solo la **refresca**.

## Detalles importantes (para depurar/explicar en clase)

- **Preservar favoritos al refrescar:** un `INSERT OR REPLACE` borraría `isFavorite`. Además el
  `UPSERT` (`ON CONFLICT DO UPDATE`) necesita SQLite **3.24+**, que **no** está en Android minSdk 24.
  Solución portable: `INSERT OR IGNORE` (crea si no existe) + `UPDATE` (refresca sin tocar el favorito),
  ambos dentro de una transacción.
- **Tipos:** SQLite guarda enteros como `Long` y no tiene `Boolean`; convertimos `Int↔Long` y
  `isFavorite` es `0/1`.

## Tests

`data/repository/MovieRepositoryTest.kt` (6): refrescar guarda en local, observar lee sin red,
marcar favorita, **refrescar preserva el favorito**, buscar, observar por id. Usan un
`MovieLocalDataSource` fake en memoria + `TmdbApi` con MockEngine.

> La integración con el driver real de SQLite se ejercita al correr la app (Android/iOS).
