# Oniria – Asistente de Finanzas Personales

![Proyecto](https://img.shields.io/badge/Proyecto-Oniria-blueviolet?style=for-the-badge)  
![Estado](https://img.shields.io/badge/Estado-En%20Desarrollo-success?style=for-the-badge)  
![Plataforma](https://img.shields.io/badge/Plataforma-Android-brightgreen?style=for-the-badge)

---

## Descripción del proyecto

**Oniria** es un asistente para tus **finanzas personales**.  
No busca darte un consejo millonario, sino **ayudarte a tener control total de tus finanzas**.  

Desde Oniria puedes saber cuánto dinero tienes ahorrado, cuándo gastaste tu último centavo o incluso **recibir recomendaciones inteligentes** sobre tus gastos gracias a su **asistente de Inteligencia Artificial**.

---

## Características principales

- **Gráficas atractivas:**  
  Si lo tuyo no son los números en bruto, Oniria convierte tus gastos en visuales agradables y fáciles de entender.  

- **Seguimiento de metas:**  
  Mantén control de tus metas financieras más importantes.  

- **Recordatorios personales:**  
  Para que nunca se te olvide pagar nada.  

- **Historial detallado de gastos e ingresos:**  
  ¿No recuerdas en qué se fueron esos 5 quetzales? Oniria lo tiene todo registrado.  

- **Asistente personal con Inteligencia Artificial:**  
  Siempre al tanto de tus bolsillos.  

- **Gestor de documentos importantes:**  
  Guarda tus documentos más preciados directamente desde la aplicación.  
  Toma una foto y Oniria los almacenará en tu espacio personal de Google Drive.

---

## Instalación

Se incluye un enlace directo a **Google Drive** con un **APK funcional para Android**:

[Descargar APK de Oniria](https://drive.google.com/file/d/1sjmgS4VlD_HTWIf3N_4SwL12erw11ffG/view?usp=sharing)

### Pasos de instalación

1. Descarga el archivo APK desde el enlace anterior.  
2. En Android, autoriza a Google Drive como fuente confiable para descargas externas (fuera de Google Play Store).  
3. En algunos casos, Google analizará la app por seguridad. Acepta el análisis y continúa con la instalación.  
4. **Nota:** No está disponible para descarga en iOS.

---

## Integración con N8N

La aplicación cuenta con **tres integraciones principales** mediante **N8N**:

| # | Flujo | Descripción | Disparador |
|---|--------|--------------|-------------|
| 1 | Subida a Google Drive | Sube imágenes directamente a una carpeta personal mediante un webhook. | Webhook |
| 2 | Registro en Google Sheets | Agrega automáticamente los gastos e ingresos del usuario en una hoja de cálculo estructurada. | Webhook |
| 3 | Notificaciones en Telegram | Envía un mensaje semanal al usuario con su resumen de gastos e ingresos obtenidos del Google Sheets anterior. | Programado por tiempo |

---

## Tecnologías utilizadas

- **Android (APK)**
- **Google Drive API**
- **Google Sheets API**
- **Telegram Bot API**
- **N8N (automatización de flujos)**
- **Diferentes Modelos de Inteligencia Artificial**

---

## Estado del proyecto

El proyecto se encuentra **en desarrollo activo**.  
Se están mejorando los flujos de automatización, optimizando el rendimiento y trabajando en nuevas funciones de análisis financiero.

---

## Documentación

Toda la documentación del proyecto, incluyendo configuraciones, flujos de N8N y recursos adicionales, está disponible en la siguiente carpeta de Google Drive:

[📁 Abrir carpeta de documentación en Google Drive](https://drive.google.com/drive/folders/1cbm1Uhf2a4blvlDbKC-Xg0RW2C1aDaJE?usp=sharing)

---

## Licencia

Este proyecto se distribuye con fines educativos y de demostración.  
Todos los derechos reservados © 2025 Oniria Team.
