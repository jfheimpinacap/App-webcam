# Vista previa y validación de captura

## Arquitectura y alcance

CameraX controla una única sesión ligada al `LifecycleOwner`: `Preview` entrega la imagen a `PreviewView` y `ImageAnalysis` mide sin guardar ni convertir frames. Se eligió CameraX para resolver ciclo de vida, rotación, selección segura y superficies; Camera2 continúa como diagnóstico independiente y, mediante Interop acotado, relaciona `CameraInfo` con `cameraId`, consulta rangos AE y solicita 30–30 o 60–60 FPS.

La aplicación explica el uso de `CAMERA` antes de abrir el diálogo, distingue rechazo recuperable y acceso a ajustes cuando ya no puede solicitarse, y nunca abre una cámara sin concesión. Al detener, navegar o reemplazar una sesión se ejecuta `unbindAll`, se invalida su generación y se apaga el ejecutor.

`PreviewView` usa escalado `FIT_CENTER`. La resolución se solicita con `ResolutionSelector` y fallback cercano; por eso la resolución observada del `ImageProxy`, no la petición, es la evidencia. Se utiliza `STRATEGY_KEEP_ONLY_LATEST`, análisis en un hilo dedicado y cierre del proxy en `finally`.

## Medición e interpretación

Los timestamps monotónicos se toman con `System.nanoTime` (equivalente monotónico a `elapsedRealtimeNanos`). El medidor publica aproximadamente cada segundo: FPS de ventana, promedio desde el primer frame, mínimo/máximo de ventanas, duración, frames y tamaño observado. Reinicia al iniciar o cambiar cámara, tamaño o objetivo. Frames repetidos o regresivos se descartan.

Una evaluación provisional requiere al menos 3 segundos y 60 frames. 30 FPS se considera cercano desde 27 FPS; 60 FPS, desde 54 FPS. Se muestra siempre el valor real. Una resolución distinta es fallback y una muestra corta es insuficiente. La medición describe solo frames entregados al analizador: no equivale al refresco visual exacto, grabación, codificación ni transmisión.

1080p60 no está confirmado por metadatos. La opción solo se habilita como filtro preventivo cuando algún rango AE alcanza 60; puede ser rechazada, sustituida o degradada. Iluminación, tiempo de exposición, temperatura, carga del sistema y políticas del fabricante pueden reducir FPS. Una prueba exitosa valida únicamente esa sesión y dispositivo.

## Procedimiento físico

En **Galaxy A30 (Android 9/API 28)** y **Galaxy A21s (Android 12/API 31)**:

1. Verificar solicitud, rechazo, nueva solicitud, concesión y revocación de `CAMERA`; comprobar que no haya otros permisos.
2. Probar tema claro/oscuro, vertical/horizontal, segundo plano/regreso, inicio/detención y cada cámara que CameraX enumere.
3. Mantener cada sesión varios segundos en 1280×720 a 30 y 1920×1080 a 30. Intentar 1920×1080 a 60 solo si queda habilitado y aceptar que falle.
4. Registrar modelo, Android/API, `cameraId`, lente, resolución/FPS solicitados, resolución observada, promedio/mínimo/máximo, duración, frames, resultado, fallback/error y observaciones visuales.
5. Comparar con el diagnóstico Camera2 sin inferir que IDs auxiliares sean abribles ni que 1280×1048 del A21s sea 1080p.

La vista previa es local. No hay captura persistente, grabación, audio, red, USB, aplicación Windows ni envío de datos.
