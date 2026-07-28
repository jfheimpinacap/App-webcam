# Fundación técnica Android

## Alcance actual

La base Android se encuentra íntegramente en `android-app/`. Es un proyecto Gradle llamado **AppWebcamAndroid** con un único módulo `app`; el nombre visible de la aplicación es **App Webcam**. El namespace y el `applicationId` son `cl.jfheimpinacap.appwebcam`.

La aplicación presenta una pantalla Compose estática que identifica el estado inicial y aclara que la cámara no está configurada. Esta etapa no incorpora captura, permisos, codificación, transporte ni integración con Windows.

## Plataforma y construcción

- Android mínimo: API 28 (Android 9).
- SDK de compilación y objetivo: API 36.
- Compatibilidad de fuentes Java y Kotlin JVM toolchain: 17.
- Scripts de construcción: Gradle Kotlin DSL.
- Dependencias y plugins: catálogo `gradle/libs.versions.toml`.
- Interfaz: Jetpack Compose con Compose BOM y Material 3.

El `minSdk` conserva la posibilidad de instalar la base en los Samsung Galaxy A30 con Android 9 y Galaxy A21s con Android 12 previstos para las pruebas. Esto no garantiza capacidades futuras de cámara, códecs, resoluciones o FPS, que dependen del hardware y se investigarán por separado.

## Estructura y dependencias

`app/src/main` contiene el manifest mínimo, `MainActivity`, la pantalla inicial, el tema claro/oscuro y los recursos de texto y estilo. `app/src/androidTest` contiene una prueba de interfaz Compose. No se agregó una prueba unitaria artificial porque esta pantalla estática todavía no contiene lógica genuina aislable; JUnit y el source set local quedan configurados para cuando exista esa lógica.

Las dependencias de producción se limitan a AndroidX Core KTX, Lifecycle Runtime KTX, Activity Compose, Compose UI, herramientas de preview y Material 3. Las dependencias instrumentadas se limitan a AndroidX Test, Espresso y Compose UI Test.

## Validación pendiente

Después de revisión, PR, Merge y Sync local, el proyecto deberá abrirse desde `android-app/` en Android Studio con JDK 17. Allí se realizarán Gradle Sync, compilación debug, pruebas disponibles, instalación en dispositivo o emulador y revisión de la pantalla en tema claro/oscuro y ambas orientaciones.

Codex no ejecutó esas validaciones funcionales en Prompt 002. Prompt 003 queda reservado para investigar capacidades reales de cámara; permisos, CameraX, perfiles de resolución/FPS, codificación, USB, Wi-Fi y transmisión continúan aplazados a tareas posteriores.
