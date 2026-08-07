# Sesión 1: Fundamentos de Kotlin Multiplatform (KMP)

### Alondra Correa Carranza
### ¿Qué es `commonMain`?
Es el módulo central de un proyecto KMP donde se escribe el código que se comparte entre todas las plataformas (Android, iOS, JVM, etc.). El código aquí debe ser **Kotlin puro**, lo que significa que no puede depender de librerías específicas de una plataforma (como las de Android o Java).

### ¿Por qué no se puede usar `java.util.UUID` en `commonMain`?
Porque `java.util.UUID` es parte de la librería estándar de **Java (JDK)**. Como KMP busca ser multiplataforma, el código en `commonMain` debe poder compilarse también para iOS (que usa Native/Objective-C) o Web, donde el JDK de Java no existe. Para usar UUIDs en `commonMain`, se debe usar una librería multiplataforma o el mecanismo `expect`/`actual`.

### ¿Qué hace `expect` / `actual`?
Es el mecanismo que permite a KMP acceder a APIs específicas de cada plataforma desde el código compartido:

1.  **`expect` (en `commonMain`):** Define una "promesa". Declara que existirá una función, clase o propiedad, pero no da su implementación. Es como decir: "Espero que cada plataforma implemente esto a su manera".
2.  **`actual` (en `androidMain`, `iosMain`, etc.):** Proporciona la implementación real y específica para esa plataforma. Aquí sí se pueden usar librerías propias de cada sistema (como `java.util.UUID` en Android/JVM o `NSUUID` en iOS).
