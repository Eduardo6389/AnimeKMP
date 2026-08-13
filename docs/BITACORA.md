# Bitácora

## Base

Se partió del proyecto usado durante las sesiones del curso porque ya tenía la configuración KMP compatible del seed. Se creó un repositorio nuevo para AnimeKMP y se adaptó la base al contrato del proyecto final.

## Cambio de dominio

Se retiró TMDB y el modelo `Movie`. Jikan no necesita API key, así que también se eliminó la generación de la llave de TMDB. Se implementaron `Item`, `ItemDetalle`, `Atributo`, DTOs de anime y `ApiMapper`.

## Arquitectura final

Se quitó Compose de `shared`. Android conserva su UI en `androidApp` y iOS usa SwiftUI nativo. La lógica de red, SQLDelight, repositorio, casos de uso y ViewModels vive en `shared`.

## Jikan

El reto propio de Jikan es el límite de peticiones. `ApiClient` serializa solicitudes con un intervalo de 1.1 s y reintenta respuestas 429 con espera exponencial. La caché evita llamadas innecesarias.

## Persistencia

Se sustituyó `Movie.sq` por `Item.sq`. Los favoritos se conservan al borrar o reemplazar la caché. El ordenamiento se realiza con queries SQL, no sobre una lista en memoria.

## iOS

Se eligió el envoltorio manual de StateFlow permitido por el documento en lugar de añadir otra dependencia. Cada wrapper Swift cancela la observación y libera el ViewModel en `deinit`.
