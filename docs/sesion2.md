# Sesión 2: Memoria y Concurrencia en KMP

### Alondra Correa Carranza

### ¿Qué era `freeze()` y por qué desapareció?
`freeze()` era una función del antiguo modelo de memoria de Kotlin/Native (iOS) que convertía un objeto en inmutable para poder compartirlo de forma segura entre diferentes hilos.
Desapareció porque era muy complejo de usar y causaba cierres inesperados (crashes) si intentabas modificar un objeto congelado.
Con el nuevo modelo de memoria de Kotlin, los objetos se pueden compartir entre hilos de forma automática y sencilla, igual que en Android o Java.
