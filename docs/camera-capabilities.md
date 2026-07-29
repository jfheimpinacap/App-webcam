# Diagnóstico de capacidades de cámara

## Alcance

La aplicación enumera los identificadores entregados por `CameraManager` y consulta sus `CameraCharacteristics`: lente, orientación del sensor, nivel de hardware, capacidades, cámara lógica e identificadores físicos, zoom, enfoque, autoenfoque, estabilización y rangos AE. También inspecciona `SCALER_STREAM_CONFIGURATION_MAP` para salidas `SurfaceTexture` (PRIVATE) y `YUV_420_888`, priorizando 1280 × 720 y 1920 × 1080.

Esta etapa usa Camera2 porque necesita leer directamente los metadatos del framework. CameraX es una biblioteca de más alto nivel útil para captura y vista previa, pero no se incorpora hasta decidir la estrategia posterior. El inspector nunca abre un `CameraDevice`, no crea una sesión, no captura y no solicita ni declara `CAMERA`.

## Cómo interpretar los resultados

Una resolución declarada no demuestra que pueda funcionar con un rango AE concreto. `CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES` describe objetivos AE disponibles, no el producto cartesiano de rangos y resoluciones.

Cuando el mapa ofrece `getOutputMinFrameDuration` y su valor es positivo, la aplicación calcula `1.000.000.000 / duraciónNs` y lo etiqueta **Máximo teórico declarado**. Es una estimación de metadatos: las restricciones de sesión, formato, procesamiento, exposición, temperatura y fabricante pueden impedir sostenerla. Si falta la duración, solo se informa que la resolución fue declarada y que los FPS siguen sin verificar.

Por eso 720p30, 1080p30 y especialmente 1080p60 conservan estados prudentes. 1080p60 continúa siendo exploratorio aunque aparezcan por separado 1080p y un rango de 60 FPS. Solo una sesión futura, abierta con permiso y medida en el dispositivo, podrá validar funcionalmente cada combinación.

## Validación local pendiente

Después de revisar el Summary, crear y revisar el PR, hacer Merge y Sync local:

1. Abrir `android-app/` en Android Studio con JDK 17, ejecutar Gradle Sync, build debug y pruebas unitarias.
2. Ejecutar la prueba instrumentada en un emulador sin depender de que exponga una cámara concreta.
3. Instalar y ejecutar en Galaxy A30 (Android 9) y Galaxy A21s (Android 12), sin aceptar solicitudes de permisos (no debería aparecer ninguna).
4. Revisar tema claro/oscuro y orientación vertical/horizontal.
5. Registrar fabricante, modelo, versión/API, cantidad e IDs de cámaras; lente, nivel Camera2, capacidades e IDs físicos; tamaños PRIVATE/YUV, rangos AE, duraciones/FPS teóricos e indicadores 720p30, 1080p30 y 1080p60.
6. Registrar errores y diferencias entre teléfonos, incluidos valores ausentes o desconocidos.

No deben anotarse los resultados como captura validada: esta herramienta únicamente recopila lo declarado por Android y no escribe ni envía informes fuera de la aplicación.
