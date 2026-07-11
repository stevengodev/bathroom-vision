# Explicación de las APIs de analíticas

Este archivo describe qué hace cada endpoint nuevo, qué parámetros acepta y por qué algunos valores pueden aparecer como null.

## 1. Dashboard general

### Endpoint
GET /api/dashboard/overview

### ¿Qué hace?
Devuelve un resumen general del estado operativo del sistema para que un administrador pueda ver rápidamente:
- Total de baños registrados.
- Baños disponibles.
- Baños ocupados.
- Baños en mantenimiento.
- Incidencias activas.
- Mantenimientos abiertos.
- Mantenimientos cerrados.

### Parámetros
No recibe parámetros.

### ¿Por qué algunos datos pueden venir en null?
No deberían aparecer null en este endpoint, porque todos los valores se calculan a partir de tablas existentes. Si algún valor aparece null, normalmente significa que:
- No hay datos en la tabla correspondiente.
- El registro no tiene valor asignado en la base de datos.
- La consulta no encontró coincidencias para ese campo.

---

## 2. Estadísticas de incidencias

### Endpoint
GET /api/statistics/incidents

### ¿Qué hace?
Devuelve estadísticas de incidencias agrupadas para identificar:
- Cuántas incidencias hay por baño.
- Cuántas incidencias hay por bloque.
- Cuántas incidencias hay por categoría (Limpieza o Mantenimiento).

### Parámetros
- groupBy: indica cómo agrupar los resultados.
  - bathroom: agrupa por ID del baño.
  - block: agrupa por nombre del bloque.
  - category: agrupa por categoría de la incidencia.
- sort: define el orden de los resultados.
  - desc: de mayor a menor cantidad.
  - asc: de menor a mayor cantidad.

### Ejemplos de uso
- GET /api/statistics/incidents?groupBy=bathroom&sort=desc
- GET /api/statistics/incidents?groupBy=block&sort=asc
- GET /api/statistics/incidents?groupBy=category&sort=desc

### ¿Por qué algunos datos pueden venir en null?
En este endpoint, los campos pueden aparecer en null cuando:
- El baño no tiene bloque asociado.
- La incidencia no tiene categoría asignada.
- El registro de la incidencia está incompleto.

En la respuesta:
- bathroomId puede venir null si el agrupamiento no es por baño.
- blockName puede venir null si el agrupamiento no es por bloque.
- category puede venir null si el agrupamiento no es por categoría.

---

## 3. Estadísticas de mantenimiento

### Endpoint
GET /api/statistics/maintenance

### ¿Qué hace?
Devuelve información relacionada con mantenimientos, incluyendo:
- Cantidad de mantenimientos abiertos.
- Cantidad de mantenimientos cerrados.
- Historial de mantenimientos según filtros.

### Parámetros
- status: filtra por estado del mantenimiento.
  - open: solo abiertos.
  - closed: solo cerrados.
  - Si no se envía, se incluyen todos.
- bathroomId: filtra por el ID de un baño específico.
- startDate: fecha inicial del rango de búsqueda.
- endDate: fecha final del rango de búsqueda.

### Ejemplos de uso
- GET /api/statistics/maintenance
- GET /api/statistics/maintenance?status=open
- GET /api/statistics/maintenance?bathroomId=5
- GET /api/statistics/maintenance?status=open&bathroomId=5&startDate=2026-01-01&endDate=2026-07-09

### ¿Por qué algunos datos pueden venir en null?
En este endpoint, null puede aparecer cuando:
- El mantenimiento no tiene baño asociado.
- El mantenimiento aún no tiene fecha de cierre.
- El mantenimiento no tiene fecha de reportado.
- No se aplica filtro por baño.

En particular:
- resolvedAt puede venir null si el mantenimiento sigue abierto.
- bathroomId puede venir null si el mantenimiento no está asociado a un baño.
- blockName puede venir null si el baño no tiene bloque registrado.

---

## 4. Cómo enviar los parámetros correctamente

Como algunos parámetros son strings, conviene tener en cuenta lo siguiente:

### groupBy
Solo se aceptan estos valores:
- bathroom
- block
- category

Si se envía otro valor, el sistema tomará el comportamiento por defecto de bathroom.

### sort
Solo se aceptan:
- desc
- asc

### status en mantenimiento
Solo se aceptan:
- open
- closed

### fechas
Se pueden enviar en dos formatos:
- YYYY-MM-DD, por ejemplo: 2026-01-01
- YYYY-MM-DDTHH:mm:ss, por ejemplo: 2026-01-01T09:30:00

Si se envía solo la fecha, el sistema la interpreta como inicio del día.

### bathroomId
Debe ser un número entero, por ejemplo:
- bathroomId=5

---

## 5. Resumen rápido

- Dashboard: muestra indicadores generales del sistema.
- Incidents: agrupa incidencias por baño, bloque o categoría.
- Maintenance: filtra y muestra historial de mantenimientos.
- Null en la respuesta no siempre es un error; muchas veces indica que el dato no existe o no aplica para ese registro.
