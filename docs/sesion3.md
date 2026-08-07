# Sesión 3 — Networking con Ktor + kotlinx.serialization

CineKMP empieza a bajar **películas reales** de TMDB.

## Qué se hizo

1. **API key protegida** (`local.properties` → Gradle → constante):
   - `local.properties` tiene `tmdb.apikey=...` (archivo en `.gitignore`, nunca se sube).
   - En `shared/build.gradle.kts` la tarea `generateCineBuildConfig` **genera** el archivo
     `com/jetbrains/kmpapp/config/CineBuildConfig.kt` con la key como constante.
   - El código lee `CineBuildConfig.TMDB_API_KEY`. Sin key: compila y los tests pasan (usan
     MockEngine), pero la API real devolvería 401.

2. **DTOs** (`data/dto/MovieDto.kt`): `MoviePageDto`, `MovieDto`, `MovieDetailDto`, `GenreDto`.
   Usan `@Serializable` y `@SerialName` para casar el JSON de TMDB con nombres Kotlin limpios.

3. **Cliente Ktor** (`data/remote/TmdbApi.kt`):
   - `crearTmdbHttpClient(apiKey, engine?)`: configura JSON, base URL, `api_key`, idioma y
     `expectSuccess=true`. El parámetro `engine` permite inyectar **MockEngine** en tests.
   - `TmdbApi` con 5 endpoints: `populares`, `topRated`, `nowPlaying`, `detalle(id)`, `buscar(query)`.

4. **Mapper** (`data/remote/MovieMapper.kt`): `MovieDto.toDomain()` / `MovieDetailDto.toDomain()`
   → modelos limpios `domain/model/Movie.kt` y `MovieDetail.kt` (arma la `posterUrl` completa y el año).

5. **Tests con MockEngine** (`commonTest/data/remote/`): 10 de red (éxito, vacío, 401, 500, fallo de
   red, verificación de `api_key`/`page`/`query`, parseo de detalle) + 7 del mapper.

## Cómo probar

```bash
./gradlew :shared:iosSimulatorArm64Test   # corre los tests de commonTest en el target nativo
```

> Nota de arquitectura: el código ya se organiza en `data/` y `domain/` pensando en la
> Clean Architecture de la Sesión 5, para no reordenar todo después.
