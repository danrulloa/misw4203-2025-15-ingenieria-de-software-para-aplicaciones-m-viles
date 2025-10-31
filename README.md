# README – Entorno de desarrollo y pruebas para app en **Kotlin** (Android)

Este documento describe, paso a paso, cómo preparar el ambiente de **desarrollo** y de **pruebas** para una aplicación escrita en Kotlin, usando **Android Studio** y **Gradle**. Incluye enlaces oficiales de referencia.

> 📝 Suponemos un proyecto Android/Kotlin estándar con Gradle y estructura típica `app/`.  
> Si tu caso es **Kotlin Multiplatform** o **backend en Kotlin (Ktor/Spring)**, agrega una sección específica al final.

---

## 1. Requisitos previos

1. **Sistema operativo**
   - Windows 10/11, macOS reciente o Linux.
2. **Java / JDK**
   - Android Studio trae un JDK embebido.  
     Si necesitas uno externo, usa **JDK 17**.
3. **Espacio en disco**
   - Mínimo **10–15 GB** para SDKs, emuladores y dependencias Gradle.
4. **Acceso a Internet**
   - Necesario para descargar el SDK de Android, dependencias de Gradle y plugins.

---

## 2. Instalar Android Studio

1. Ir al sitio oficial:  
   https://developer.android.com/studio?hl=es-419
2. Descargar la **versión estable**.
3. Instalar con las **opciones por defecto**.
4. Al abrir por primera vez, dejar que descargue:
   - **Android SDK**
   - **Android SDK Platform Tools**
   - **Android Emulator** (si vas a probar en emulador)

**Otros enlaces útiles:**

- Documentación Kotlin: https://kotlinlang.org/docs/home.html
- Guía de primeros pasos con Android: https://developer.android.com/kotlin?hl=es-419

---

## 3. Configurar el SDK y los dispositivos

1. Abrir **Android Studio** → **More Actions** → **SDK Manager**.
2. Instalar:
   - Último **Android SDK Platform** (por ej. Android 14 o la que use el proyecto).
   - **Android SDK Build-Tools**.
   - **Android Emulator** (opcional).
3. Crear un **dispositivo virtual (AVD)**:  
   **Device Manager** → **Create Device** → elegir teléfono → elegir imagen de sistema → **Finish**.

> 💡 También puedes usar un **dispositivo físico** activando “Opciones de desarrollador” y “Depuración por USB”.

---

## 4. Clonar / abrir el proyecto

1. Desde terminal:
```
   git clone https://github.com/danrulloa/misw4203-2025-15-ingenieria-de-software-para-aplicaciones-m-viles.git
   cd misw4203-2025-15-ingenieria-de-software-para-aplicaciones-m-viles
```
2. Si el nombre de la carpeta cambia en GitHub, usa el **nombre exacto** que aparezca después de clonar.
3. Abrir **Android Studio** → **Open** → seleccionar la **carpeta del proyecto** que acabas de clonar.
4. Esperar a que Android Studio haga el **Gradle sync** (la primera vez puede tardar porque descargará todas las dependencias).
5. Si el proyecto tiene varios módulos, asegúrate de que el módulo **`app`** esté marcado como **módulo de ejecución**.

---

## 5. Estructura esperada del proyecto
```
.
├── app/
│   ├── src/
│   │   ├── main/           
│   │   ├── test/           
│   │   └── androidTest/   
│   └── build.gradle(.kts)
├── build.gradle(.kts)
├── settings.gradle(.kts)
└── gradle/               
```

---

## 6. Compilar y ejecutar la app

### 6.1 Desde Android Studio

1. Selecciona el **módulo** `app`.
2. Elige el **dispositivo** donde quieres ejecutar (AVD o dispositivo físico).
3. Haz clic en **Run ▶**.
4. Android Studio compilará el proyecto y desplegará el **APK** en el dispositivo elegido.

### 6.2 Desde línea de comandos (Gradle Wrapper)

Dentro de la carpeta del proyecto:

Linux / macOS:
./gradlew assembleDebug

Windows:
gradlew.bat assembleDebug

El APK quedará normalmente en:

app/build/outputs/apk/debug/app-debug.apk

Ese APK lo puedes instalar **manualmente** en un dispositivo Android.

---

## 7. Pruebas (tests)

El objetivo del repo suele incluir **pruebas unitarias** y **pruebas instrumentadas**.

### 7.1 Pruebas unitarias (rápidas, JVM)

Se ejecutan en la máquina local, **sin emulador**:

./gradlew test

Buscan las pruebas en:
app/src/test/...

### 7.2 Pruebas instrumentadas (en dispositivo/emulador)

Se ejecutan dentro de un dispositivo Android real o emulador:

./gradlew connectedAndroidTest

Buscan las pruebas en:
app/src/androidTest/...

Requiere tener:
- un **emulador corriendo**, o
- un **dispositivo conectado por USB**.

---

## 8. Enlaces de interés

- Android Studio (oficial, ES): https://developer.android.com/studio?hl=es-419
- Kotlin en Android: https://developer.android.com/kotlin?hl=es-419
- Documentación Kotlin: https://kotlinlang.org/docs/home.html
- Testing en Android: https://developer.android.com/training/testing?hl=es-419
- Gradle para Android: https://developer.android.com/studio/build?hl=es-419

---




