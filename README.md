# Desafío Reacción

**Desafío Reacción** es una aplicación de juego para Android diseñada para poner a prueba y mejorar tus reflejos. Desarrollada íntegramente con **Kotlin** y **Jetpack Compose**, ofrece una experiencia fluida y moderna.

Este proyecto fue desarrollado con el objetivo principal de **practicar la programación en Android**, explorando herramientas modernas y patrones de diseño recomendados.

## 🚀 Características

*   **Juego de Velocidad:** Reacciona lo más rápido posible a los estímulos en pantalla.
*   **Modo Inverso:** Un desafío adicional que cambia la lógica del juego para poner a prueba tu adaptabilidad.
*   **Estadísticas:** Registro de tus mejores puntuaciones para que puedas seguir tu progreso.
*   **Interfaz Moderna:** Construida con Material Design 3 y animaciones suaves gracias a Jetpack Compose.
*   **Navegación Intuitiva:** Sistema de navegación fluido entre el menú, instrucciones, juego y estadísticas.

## 🛠️ Tecnologías Utilizadas

*   **Lenguaje:** [Kotlin](https://kotlinlang.org/)
*   **UI:** [Jetpack Compose](https://developer.android.com/jetpack/compose)
*   **Arquitectura:** MVVM (Model-View-ViewModel)
*   **Navegación:** Compose Navigation
*   **Componentes de Ciclo de Vida:** ViewModel, StateFlow
*   **Persistencia:** Room Database

## 📋 Requisitos

*   Android Studio Ladybug o posterior.
*   JDK 17 o superior.
*   Dispositivo Android con API 24 (Android 7.0) o superior.

## 🔧 Instalación y Ejecución

1.  Clona este repositorio:
    ```bash
    git clone https://github.com/tu-usuario/desafio-reaccion.git
    ```
2.  Abre el proyecto en **Android Studio**.
3.  Espera a que termine la sincronización de **Gradle**.
4.  Conecta un dispositivo físico o inicia un emulador.
5.  Haz clic en el botón **Run** (flecha verde).

## 📁 Estructura del Proyecto

*   `ui/`: Contiene todas las pantallas (`MainMenuScreen`, `GameScreen`, `StatsScreen`, `InstructionsScreen`) y los componentes de la interfaz.
*   `data/`: Lógica de persistencia (Room) y modelos de datos.
*   `MainActivity.kt`: Punto de entrada de la aplicación y configuración de la navegación.

---

