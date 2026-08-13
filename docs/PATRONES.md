# Patrones de diseño

| # | Patrón | Archivo y línea | Problema que resuelve |
|---|---|---|---|
| 1 | Repository | `shared/src/commonMain/kotlin/com/jetbrains/kmpapp/data/repository/ItemRepositoryImpl.kt:19` | Oculta si los datos vienen de SQLDelight o de Jikan. |
| 2 | Adapter / Mapper | `shared/src/commonMain/kotlin/com/jetbrains/kmpapp/data/remote/ApiMapper.kt:8` | Evita que la forma del JSON de Jikan llegue al dominio o a la UI. |
| 3 | Command / Use Case | `shared/src/commonMain/kotlin/com/jetbrains/kmpapp/domain/usecase/GetItems.kt:6` | Cada acción del usuario queda pequeña, invocable y fácil de probar. |
| 4 | Factory Method | `shared/src/commonMain/kotlin/com/jetbrains/kmpapp/data/local/DriverFactory.kt:5` | Cada plataforma crea su driver SQLite sin cambiar commonMain. |
| 5 | Observer | `shared/src/commonMain/kotlin/com/jetbrains/kmpapp/presentation/ListaViewModel.kt:49` | StateFlow permite que la UI reaccione a cambios de estado. |
| 6 | Facade | `shared/src/commonMain/kotlin/com/jetbrains/kmpapp/data/remote/ApiClient.kt:43` | ApiClient deja una interfaz pequeña sobre la configuración de Ktor. |
| 7 | Singleton gestionado | `shared/src/commonMain/kotlin/com/jetbrains/kmpapp/di/KoinModules.kt:31` | Koin mantiene una sola instancia de HttpClient, DB y repositorios. |
| 8 | State | `shared/src/commonMain/kotlin/com/jetbrains/kmpapp/domain/model/AppError.kt:3` | AppError representa estados de error excluyentes y tipados. |
| 9 | Strategy | `shared/src/commonMain/kotlin/com/jetbrains/kmpapp/domain/model/Orden.kt:3` | Orden cambia la estrategia de consulta sin cambiar quien la consume. |
| 10 | Builder | `shared/src/commonMain/kotlin/com/jetbrains/kmpapp/data/remote/ApiClient.kt:22` | La configuración DSL construye HttpClient con plugins y valores centralizados. |
| 11 | Decorator | `shared/src/commonMain/kotlin/com/jetbrains/kmpapp/data/remote/ApiClient.kt:28` | HttpRequestRetry añade reintento/backoff al cliente sin cambiar cada endpoint. |

Los patrones 1–9 son los obligatorios del proyecto. Los patrones 10 y 11 son los dos adicionales elegidos porque ya resuelven necesidades reales del cliente HTTP; no se agregaron solo para completar la tabla.
