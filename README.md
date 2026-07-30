# App-webcam

> **Estado:** vista previa local y medición de captura del Prompt 004 implementadas; pendientes de integración y validación física.

App-webcam busca permitir que un teléfono Android funcione como webcam para un PC con Windows mediante una conexión local USB o Wi-Fi. El MVP usará OBS y OBS Virtual Camera como puente hacia aplicaciones como Discord; una cámara virtual propia queda reservada para una etapa posterior.

## Objetivos

- Capturar video con las cámaras frontal y trasera de Android, en orientación vertical u horizontal.
- Transportarlo con baja latencia por USB o por la red Wi-Fi local.
- Recibirlo, decodificarlo, mostrar una vista previa y exponer métricas en Windows.
- Facilitar el uso práctico con OBS y Discord sin cuentas, nube ni transmisión por Internet.
- Evolucionar desde una interfaz sencilla hacia un producto publicable.

## Alcance inicial previsto

El desarrollo progresivo contempla perfiles de 720p a 30 FPS y 1080p a 30 FPS. También explorará 1080p a 60 FPS, sin garantizarlo: cada perfil dependerá de las capacidades detectadas en el teléfono y el PC, la conexión, los códecs disponibles y las pruebas reales. Se prevén vista previa en ambos equipos, métricas de FPS, resolución, bitrate y latencia, reconexión automática y, más adelante, controles remotos de cámara.

No forman parte de las primeras versiones el audio, las cuentas, las suscripciones, el hosting, los servidores externos, la nube, la base de datos, la autenticación ni la telemetría.

## Componentes principales

1. **Aplicación Android:** captura y codifica el video.
2. **Transporte local:** USB mediante ADB y TCP durante el desarrollo, o Wi-Fi local, compartiendo un protocolo cuando sea técnicamente viable.
3. **Aplicación Windows:** recibe, decodifica y presenta el video y sus métricas.
4. **Integración MVP:** aplicación Windows → OBS → OBS Virtual Camera → Discord.
5. **Integración futura:** cámara virtual propia para Windows, fuera del primer MVP.

## Stack inicial

| Área | Tecnologías y decisiones |
| --- | --- |
| Android | Kotlin, Jetpack Compose, CameraX, MediaCodec, Coroutines y Flow |
| Windows | Python, PySide6 y GStreamer; OpenCV solo ante una necesidad concreta; Pytest para pruebas futuras |
| Distribución Windows | PyInstaller o Nuitka, pendiente de evaluación |
| Conexión | ADB + TCP para USB durante el desarrollo y Wi-Fi local |
| Cámara virtual | OBS Virtual Camera en el MVP; C++ para una integración nativa futura |

No se fija todavía una versión de Python: se elegirá tras comprobar la compatibilidad conjunta de PySide6, GStreamer y las dependencias que realmente se adopten.

## Compatibilidad objetivo

- **PC inicial:** Windows 10; Windows 11 se evaluará posteriormente.
- **Teléfono de referencia:** Android 9 o superior, sin prometer compatibilidad universal.
- **Dispositivos de prueba iniciales:** Samsung Galaxy A30 con Android 9 y Samsung Galaxy A21s con Android 12.

Las resoluciones, FPS y funciones disponibles se detectarán en tiempo de ejecución. El soporte real dependerá del hardware, software, conexión y resultados de pruebas posteriores.

## Aplicación Android actual

El directorio [`android-app/`](android-app/) contiene una aplicación Kotlin/Compose. Con contexto previo solicita exclusivamente `CAMERA`, enumera las cámaras utilizables por CameraX y ofrece vista previa local para probar 720p30 y 1080p30. 1080p60 permanece exploratorio y solo se habilita preventivamente cuando los metadatos de la cámara alcanzan 60 FPS. `ImageAnalysis` informa resolución y FPS realmente entregados al analizador, separados de lo solicitado. El diagnóstico Camera2 previo continúa accesible.

La apertura en Android Studio, la sincronización de Gradle, la compilación y la ejecución se validarán localmente después del flujo de revisión, PR, Merge y Sync. Esta incorporación al workspace no implica que esas comprobaciones ya hayan sido realizadas.

## Limitaciones actuales

Una sesión aporta evidencia provisional solo para ese teléfono, cámara y momento. Todavía no existen grabación, audio, transmisión, aplicación Windows, decodificación, integración con OBS ni cámara virtual.

## Documentación

- [Arquitectura conceptual](docs/architecture.md)
- [Hoja de ruta provisional](docs/roadmap.md)
- [Flujo de desarrollo e integración](docs/development-workflow.md)
- [Fundación técnica Android](docs/android-foundation.md)
- [Diagnóstico de capacidades Camera2](docs/camera-capabilities.md)
- [Validación de vista previa y captura](docs/camera-preview-validation.md)
