
Perfecto. Si el sistema solo tiene **dos actores (Administrador y Usuario)**, los casos de uso quedan más simples y consistentes.

## Actores

- **Administrador**
- **Usuario**

---

# CU-01 Importar Leyes
**Actor:** Administrador

**Objetivo:** Importar leyes y votaciones desde la API de la Asamblea Nacional.

**Precondiciones**

- El administrador ha iniciado sesión.
**Flujo principal**

1. El administrador selecciona la opción **Importar leyes**.
2. El sistema obtiene la información desde la API.
3. Almacena los datos en la base de datos.
4. El sistema confirma la importación.
**Postcondición**

- Las leyes quedan registradas en el sistema.

---

# CU-02 Normalizar Lenguaje Legislativo
**Actor:** Administrador

**Objetivo:** Simplificar el contenido de las leyes para facilitar su comprensión.

**Precondiciones**

- Existen leyes registradas.
**Flujo principal**

1. El administrador ejecuta la normalización.
2. El sistema identifica términos jurídicos.
3. Genera una versión simplificada.
4. Guarda los cambios.
**Postcondición**

- Las leyes cuentan con una versión en lenguaje sencillo.

---

# CU-03 Gestionar Perfiles Políticos
**Actor:** Administrador

**Objetivo:** Registrar, actualizar o eliminar información de actores políticos.

**Precondiciones**

- El administrador ha iniciado sesión.
**Flujo principal**

1. Selecciona un perfil político.
2. Registra o modifica la información.
3. Guarda los cambios.
**Postcondición**

- La información queda actualizada.

---

# CU-04 Vincular Votos con Promesas
**Actor:** Administrador

**Objetivo:** Asociar las votaciones con las promesas electorales.

**Precondiciones**

- Existen votos y promesas registradas.
**Flujo principal**

1. El administrador ejecuta el proceso.
2. El sistema analiza las coincidencias.
3. Relaciona los votos con las promesas.
4. Guarda el resultado.
**Postcondición**

- Las relaciones quedan almacenadas.

---

# CU-05 Calcular Coherencia Política
**Actor:** Administrador

**Objetivo:** Generar el porcentaje de coherencia entre promesas y votaciones.

**Precondiciones**

- Existen vínculos entre votos y promesas.
**Flujo principal**

1. El administrador inicia el cálculo.
2. El sistema procesa la información.
3. Calcula el porcentaje de coherencia.
4. Actualiza los indicadores.
**Postcondición**

- El índice de coherencia queda actualizado.

---

# CU-06 Consultar Información Política
**Actor:** Usuario

**Objetivo:** Consultar información pública de los políticos.

**Precondiciones**

- Existen datos registrados.
**Flujo principal**

1. El usuario realiza una búsqueda.
2. El sistema muestra:

- Datos generales.
- Partido político.
- Cargo.
- Historial de votaciones.
- Índice de coherencia.
**Postcondición**

- El usuario visualiza la información solicitada.

---

# CU-07 Realizar Auditoría Ciudadana
**Actor:** Usuario

**Objetivo:** Consultar indicadores de transparencia.

**Precondiciones**

- Existen métricas calculadas.
**Flujo principal**

1. El usuario selecciona un político.
2. El sistema presenta:

- Coherencia.
- Asistencia.
- Leyes propuestas.
- Votaciones.
- Reportes.
**Postcondición**

- El usuario obtiene la auditoría del político.

---

# CU-08 Visualizar Métricas
**Actor:** Usuario

**Objetivo:** Consultar gráficos e indicadores de desempeño.

**Precondiciones**

- Existen estadísticas disponibles.
**Flujo principal**

1. El usuario accede al panel de métricas.
2. El sistema genera gráficos dinámicos.
3. Muestra la información histórica.
**Postcondición**

- Las métricas son visualizadas.

---

# CU-09 Calificar a un Político
**Actor:** Usuario

**Objetivo:** Emitir una calificación sobre el desempeño de un político.

**Precondiciones**

- El usuario ha iniciado sesión.
**Flujo principal**

1. Selecciona un político.
2. Ingresa una calificación.
3. Envía la valoración.
4. El sistema actualiza el promedio.
**Postcondición**

- La reputación del político queda actualizada.

---

# CU-10 Comentar sobre un Político
**Actor:** Usuario

**Objetivo:** Publicar un comentario relacionado con un político.

**Precondiciones**

- El usuario ha iniciado sesión.
**Flujo principal**

1. Selecciona un político.
2. Escribe un comentario.
3. Envía la información.
4. El sistema publica el comentario.
**Postcondición**

- El comentario queda registrado.

---

# CU-11 Generar Reportes
**Actor:** Usuario

**Objetivo:** Exportar reportes de transparencia.

**Precondiciones**

- Existen datos disponibles.
**Flujo principal**

1. El usuario selecciona el tipo de reporte.
2. El sistema genera el documento.
3. El usuario lo descarga.
**Postcondición**

- El reporte queda exportado.

---

## Resumen de actores

Actor  | Casos de uso
------ | ------------
**Administrador** | CU-01 Importar Leyes, CU-02 Normalizar Lenguaje, CU-03 Gestionar Perfiles Políticos, CU-04 Vincular Votos, CU-05 Calcular Coherencia
**Usuario** | CU-06 Consultar Información Política, CU-07 Realizar Auditoría Ciudadana, CU-08 Visualizar Métricas, CU-09 Calificar a un Político, CU-10 Comentar sobre un Político, CU-11 Generar Reportes
