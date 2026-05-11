# Manual de Usuario: AsistenciaQR (Módulo Profesor)

**Aplicación:** AsistenciaQR  
**Perfil:** Profesor / Administrador de Grupo  
**Versión:** 1.0  
**Fecha:** Junio 2024  

---

## 1. Introducción
Bienvenido al manual de usuario de **AsistenciaQR**, la herramienta definitiva para la gestión de asistencia en el aula mediante tecnología de códigos QR. Esta aplicación ha sido diseñada para optimizar el tiempo del docente, eliminando el pase de lista tradicional y proporcionando estadísticas precisas y reportes profesionales en tiempo real.

Con AsistenciaQR, podrá gestionar múltiples grupos, realizar escaneos masivos en segundos y generar evidencias documentales (PDF) con un solo toque.

## 2. Requisitos del Sistema
Para asegurar un funcionamiento óptimo, su dispositivo debe cumplir con los siguientes requisitos:
*   **Sistema Operativo:** Android 8.0 (Oreo) o superior.
*   **Cámara:** Sensor trasero con autoenfoque (indispensable para el escaneo).
*   **Almacenamiento:** Espacio disponible para la generación y guardado de reportes PDF.
*   **Permisos requeridos:** Cámara y Almacenamiento (para guardar documentos).

---

## 3. Acceso y Seguridad Inicial

### 3.1 Inicio de Sesión
El acceso al perfil de profesor está restringido para proteger la integridad de los registros.
1.  **Ingresar Contraseña:** Escriba su clave en el campo correspondiente.
    *   *Nota:* La contraseña predeterminada es `profesor123`.
2.  **Visibilidad:** Puede usar el icono del ojo para verificar los caracteres ingresados.
3.  **Ingresar:** Presione el botón para acceder a su panel principal.

### 3.2 Configuración de Seguridad
Se recomienda encarecidamente personalizar sus credenciales al primer ingreso:
*   **Cambiar Contraseña:** Acceda al icono de engrane (⚙️) y seleccione "Cambiar contraseña". Requiere mínimo 6 caracteres.
*   **PIN de Recuperación:** Configure un PIN de 4 dígitos. Este le permitirá recuperar el acceso si olvida su contraseña principal.
*   **Resetear:** Si necesita volver a los valores de fábrica, use la opción "Resetear configuración" (esto no borra sus grupos, solo restablece la clave a `profesor123`).

---

## 4. Gestión de Grupos

### 4.1 Panel Principal (Mis Grupos)
Al ingresar, verá su lista de clases. Cada tarjeta muestra el **Nombre del Grupo** y la **Materia**.
*   **Cerrar Sesión:** Use el icono de salida en la barra superior al terminar su jornada.
*   **Navegación:** Toque cualquier tarjeta para entrar a la gestión específica de ese grupo.

### 4.2 Creación de Nuevos Grupos
1.  Presione el botón flotante **"+"** en la esquina inferior derecha.
2.  **Campos Requeridos:** Ingrese el Nombre del Grupo (ej. 3º A) y la Materia (ej. Física).
3.  **Confirmar:** Presione "Crear Grupo". El registro aparecerá inmediatamente en su lista.

---

## 5. Control de Asistencia (Escaneo QR)

### 5.1 El Escáner Continuo
Esta es la función principal de la app. Permite registrar a todo un grupo sin tocar la pantalla entre cada alumno.
1.  **Permisos:** Otorgue permiso de cámara cuando la app lo solicite.
2.  **Enfoque:** Apunte al QR del alumno a una distancia de 15-20 cm.
3.  **Seguridad:** El sistema espera 1.5 segundos antes de leer el mismo código otra vez, evitando registros duplicados accidentales.

### 5.2 Retroalimentación Inmediata
*   **Visual:** Tarjeta verde (Éxito), naranja (Ya registrado) o roja (Error/No pertenece al grupo).
*   **Auditiva:** Un pitido corto para éxito, dos para advertencia y un tono largo para error.
*   **Tablero en Vivo:** En la parte superior verá el conteo de Presentes, Faltas y Total en tiempo real.

### 5.3 Vista de Lista (Estadísticas)
Toque el icono de **Gráfica (📊)** para alternar entre la cámara y la lista nominal. Aquí podrá ver quién falta por llegar y la hora exacta en la que ingresaron los presentes.

---

## 6. Historial y Reportes PDF

### 6.1 Consulta y Filtros
En la sección de historial, puede segmentar la asistencia por:
*   **Día / Semana / Mes / Todos.**
El sistema clasificará a los alumnos automáticamente por colores (Excelente, Bueno, Regular, Riesgo) según su porcentaje de asistencia.

### 6.2 Generación de Documentos
1.  **Reporte Grupal:** Toque el icono de **PDF (📄)** en la barra superior del historial.
2.  **Reporte Individual:** Toque el nombre de un alumno y seleccione la opción de reporte.
3.  **Ubicación:** Los archivos se guardan en:  
    `Android > data > com.sssl.asisteciaqr > files > Documents > AsistenciaQR`

---

## 7. Solución de Problemas (FAQ)

| Problema | Causa Probable | Solución |
| :--- | :--- | :--- |
| **La cámara no enciende** | Permiso denegado. | Vaya a ajustes del sistema > Aplicaciones > AsistenciaQR > Permisos y active la Cámara. |
| **No lee el código QR** | Brillo bajo o reflejos. | Pida al alumno aumentar el brillo de su celular y evite luces directas sobre la pantalla. |
| **"PIN Incorrecto"** | Error de memoria. | Verifique su PIN de 4 dígitos o use la opción de resetear configuración si lo olvidó. |
| **No aparecen los PDFs** | El explorador no refrescó. | Use una app de "Administrador de Archivos" y busque la ruta mencionada en la sección 6.2. |
| **"No pertenece al grupo"** | Alumno en grupo equivocado. | Verifique que el alumno haya seleccionado la materia correcta en su propia aplicación. |

---

## 8. Conclusión
**AsistenciaQR** busca simplificar la labor docente mediante la automatización. El uso constante de los reportes y el monitoreo de los alumnos en "Riesgo" permitirá una intervención temprana para mejorar los índices de retención escolar.

Si presenta errores técnicos no contemplados en este manual, contacte al soporte técnico de su institución.
