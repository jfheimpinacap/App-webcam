# Flujo de desarrollo e integración

## Identificación de tareas

Las tareas nuevas avanzan la numeración principal:

- `001`
- `002`
- `003`

Las correcciones mantienen el número de la tarea y añaden un consecutivo:

- `C001-1`
- `C001-2`
- `C001-3`

Una corrección no convierte el trabajo en la siguiente tarea principal.

## Flujo obligatorio

```text
Codex trabaja la tarea
→ Codex entrega Summary
→ revisión del Summary
→ creación del PR autorizada por el usuario
→ comprobaciones del PR en verde
→ Merge
→ Sync local en VS Code
→ pruebas locales
→ siguiente prompt
```

No se avanza al siguiente prompt hasta completar PR, Merge, Sync y validación local de la tarea actual. Codex no debe crear un PR ni hacer Merge sin una autorización posterior del usuario. Los builds y las pruebas funcionales se ejecutarán localmente por el usuario; si fallan, se abrirá un prompt correctivo de la misma tarea.

## Estados que no deben confundirse

- **Reportado por Codex:** el cambio está todavía pendiente de revisión. Un commit reportado no significa que se haya integrado.
- **Integrado en GitHub:** solo después de que exista un PR aprobado y posteriormente fusionado.
- **Disponible localmente:** solo después de realizar el Sync local.

Un workspace aislado de Codex puede carecer de remote. Esta característica del entorno debe comunicarse sin configurar uno, intentar publicar ni inventar una integración con GitHub.

## Criterio de avance

El Summary permite revisar alcance, cambios e inspecciones, pero no reemplaza las comprobaciones del PR ni las pruebas reales. Solo tras PR, Merge, Sync y validación local puede cerrarse operativamente la tarea y comenzar el siguiente prompt.
