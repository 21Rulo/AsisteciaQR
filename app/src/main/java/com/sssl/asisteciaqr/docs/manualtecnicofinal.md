Manual Técnico: Sistema AsistenciaQR
Este documento detalla la arquitectura, implementación y lógica técnica de la aplicación AsistenciaQR, diseñada para la gestión automatizada de asistencia en entornos académicos mediante tecnologías móviles avanzadas.
1. Introducción
   Propósito del Sistema
   El sistema AsistenciaQR tiene como objetivo digitalizar y automatizar el registro de asistencia mediante el uso de códigos QR dinámicos, eliminando la necesidad de listas físicas y reduciendo el error humano.
   Problemática que resuelve
   Optimiza el tiempo de clase al permitir un escaneo masivo y rápido, centraliza la información para la generación de estadísticas automáticas y garantiza la integridad de los datos mediante una base de datos relacional local.
   Alcance del Proyecto
   La aplicación abarca desde la creación de grupos y alumnos, la generación de tokens de identificación únicos, el escaneo en tiempo real, hasta la exportación de reportes administrativos en formato PDF.
   Tecnologías Utilizadas
   •
   Lenguaje: Kotlin 2.1.0
   •
   Interfaz: Jetpack Compose (UI Declarativa)
   •
   Persistencia: Room Database (SQLite)
   •
   Arquitectura: MVVM (Model-View-ViewModel)
   •
   Procesamiento: Kotlin Coroutines & Flow
   •
   Generación QR: ZXing (Zebra Crossing)
   •
   Documentación: PdfDocument API nativa de Android
2. Arquitectura del Sistema
   La aplicación implementa el patrón de arquitectura MVVM (Model-View-ViewModel), promoviendo la separación de responsabilidades y la facilidad de prueba.
   Estructura de Capas
1.
View (Vista): Compuesta por funciones Composable que reaccionan al estado expuesto por el ViewModel. No contiene lógica de negocio.
2.
ViewModel: Actúa como mediador. Gestiona el estado de la UI utilizando StateFlow y coordina las operaciones con el Repositorio. Sobrevive a cambios de configuración.
3.
Model (Data): Encapsula la lógica de acceso a datos a través de un Repositorio centralizado que abstrae la implementación de Room.
Flujo de Datos Unidireccional (UDF)
El sistema sigue un ciclo cerrado: El usuario genera un Evento (ej. clic en escanear), el ViewModel procesa la acción y actualiza el State, y la UI se recompone automáticamente para reflejar el nuevo estado.
Integración con Navigation Compose
Se utiliza un NavHost centralizado que gestiona las rutas de la aplicación mediante una sealed class Screen, permitiendo una navegación tipada y el paso de argumentos (como grupoId) entre pantallas.
3. Estructura del Proyecto
   El código fuente se organiza siguiendo las convenciones de limpieza y modularización:
   •
   data/: Contiene la configuración de Room (AppDatabase), las entidades y los DAOs.
   •
   repository/: Implementa el patrón Repository como única fuente de verdad.
   •
   model/: Definiciones de modelos de datos puros y enums (ej. PeriodoFiltro).
   •
   ui/:
   ◦
   screens/: Pantallas principales (Login, Home, Scan, Detalle).
   ◦
   components/: Elementos de UI reutilizables (Botones, Cards, Diálogos).
   ◦
   theme/: Configuración de Material 3 (Colores, Tipografía).
   •
   viewmodel/: Lógica de estado y eventos.
   •
   utils/: Clases de apoyo para generación de PDF, QR y formateo de fechas.
4. Persistencia y Base de Datos
   El motor de persistencia es Room, configurado con integridad referencial y migraciones automatizadas.
   Esquema Relacional
   •
   grupos: Tabla maestra con id auto-incremental.
   •
   alumnos: Vinculada a un grupo mediante grupoId (FK). Incluye un qrToken (UUID) indexado para búsquedas ultrarrápidas.
   •
   asistencias: Tabla de hechos que relaciona alumnos y grupos. Utiliza ON DELETE CASCADE para mantener la limpieza de datos si se elimina un registro padre.
   Optimizaciones
   •
   Índices: Aplicados en qrToken para reducir la latencia durante el escaneo de $O(n)$ a $O(\log n)$.
   •
   Flow: Los DAOs devuelven Flow<List<T>>, permitiendo que la base de datos notifique cambios a la UI de forma reactiva sin consultas manuales.
5. Lógica de Negocio
   La lógica reside en el AsistenciaViewModel, el cual gestiona procesos críticos mediante Corrutinas.
   Proceso de Registro de Asistencia
1.
Captura: Se recibe el qrToken desde la cámara.
2.
Validación: Se busca al alumno. Si no existe en el grupo actual, se dispara un ScanMessage.WrongGroup.
3.
Control de Duplicados: Se verifica si ya existe una asistencia para el alumnoId en la fecha actual (yyyy-MM-dd).
4.
Persistencia: Si es válido, se inserta el registro con un timestamp preciso.
6. Sistema QR y Escáner
   Generación de Tokens
   Cada alumno posee un qrToken único generado mediante UUID.randomUUID(). Esto evita la exposición de datos sensibles como matrículas o nombres dentro del código gráfico.
   Implementación del Escáner
   Utiliza ZXing Android Embedded optimizado para:
   •
   Escaneo Continuo: Permite registrar múltiples alumnos sin cerrar la cámara.
   •
   Delay de Control: Implementa un retardo de 1500ms entre lecturas para evitar registros accidentales del mismo código.
   •
   Feedback: Emite tonos auditivos (Beep) diferenciados para éxito o error, permitiendo una operación "manos libres" para el docente.
7. Sistema de Reportes PDF
   La aplicación genera documentos administrativos utilizando el lienzo de android.graphics.pdf.
   Estructura de Reportes
1.
Reporte de Grupo: Incluye estadísticas de rendimiento (Excelentes, Buenos, Regulares, Malos) basadas en porcentajes de asistencia.
2.
QRs Imprimibles: Genera una cuadrícula de códigos QR listos para ser impresos y entregados a los alumnos.
3.
Exportación: Los archivos se almacenan en el almacenamiento privado y se exponen mediante FileProvider para ser compartidos vía Email o WhatsApp de forma segura.
8. Seguridad
   •
   Validación de QR: Los tokens son UUIDs no predecibles.
   •
   Integridad: Restricciones de llave foránea en Room impiden datos huérfanos.
   •
   Privacidad: Uso de almacenamiento interno para la base de datos y FileProvider para el intercambio limitado de archivos.
   •
   Permisos: Gestión estricta de CAMERA y WRITE_EXTERNAL_STORAGE (según versión de API).
9. Dependencias y Librerías
   Librería
   Propósito
   Módulo
   Room
   Persistencia relacional de datos
   :app (Data)
   Navigation Compose
   Gestión de rutas y pantallas
   :app (UI)
   ZXing
   Generación y decodificación de QR
   :app (Utils/Scanner)
   PdfDocument
   Generación de reportes PDF
   :app (Utils)
   Lifecycle & ViewModel
   Gestión de estado y ciclo de vida
   :app (ViewModel)
   Material 3
   Sistema de diseño de interfaz
   :app (UI/Theme)
10. Requisitos Técnicos
    •
    SO Mínimo: Android 10.0 (API 29)
    •
    Target SDK: Android 15 (API 35/36)
    •
    Kotlin: 2.1.0
    •
    Gradle: 8.x
    •
    Hardware: Cámara trasera con autoenfoque (requerido para QR).
11. Instalación y Compilación
1.
Entorno: Instalar Android Studio (Ladybug o superior).
2.
Sincronización: Abrir el proyecto y ejecutar Gradle Sync.
3.
Compilación: Utilizar la tarea :app:assembleDebug para generar el APK de prueba.
4.
Ejecución: Desplegar en dispositivo físico para probar las capacidades de la cámara.
12. Posibles Mejoras Futuras
    •
    Sincronización Cloud: Integración con Firebase para respaldo en la nube.
    •
    Autenticación: Sistema de login multi-profesor con cifrado.
    •
    Análisis Predictivo: Identificación de alumnos en riesgo de deserción mediante IA.
    •
    Modo Offline: Mejorar la sincronización diferida cuando se recupere la conexión.
13. Conclusiones
    La arquitectura implementada en AsistenciaQR garantiza un sistema altamente mantenible y escalable. El uso de MVVM junto con Room proporciona una base sólida para el manejo de datos, mientras que Jetpack Compose ofrece una interfaz moderna y fluida. El sistema de tokens UUID asegura una capa de privacidad esencial para el entorno académico actual.