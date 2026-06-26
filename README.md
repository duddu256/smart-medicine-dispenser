# MedDispense IoT: Smart 6-Compartment Medicine Dispenser

MedDispense IoT is a modern Android application designed to interface with a smart 6-compartment servo-controlled medicine dispenser. It combines hardware-software synchronization, AI-powered prescription scanning, and multi-user role-based access to ensure medication compliance and safety.

## 🚀 Features

### 1. **Multi-Role Access Control (RBAC)**
Tailored experiences for three distinct user types:
*   **Patient:** View daily schedules, countdowns to the next dose, and trigger dispenser switches.
*   **Doctor:** Over-The-Air (OTA) prescription management. Search patients by ID and directly configure dispenser hardware schedules remotely.
*   **Family Member:** Monitor compliance in real-time, receive push alerts for missed doses, and view activity logs.

### 2. **AI Prescription Scanner**
*   Powered by **Gemini-3.5-Flash** via Google AI SDK.
*   Scan pharmacy labels or paper prescriptions using the device camera.
*   Automatically extracts medication names, dosages, and timings to map them directly to available dispenser slots.

### 3. **Smart Hardware Integration (Simulation)**
*   **6 Physical Slots:** Manage individual compartments for complex medication regimes.
*   **IoT Sync:** Simulated synchronization with servo motors and limit switches.
*   **Pill Tracking:** Real-time monitoring of pill counts with low-stock warnings.
*   **Compliance Logs:** Automated logging of "TAKEN" vs "MISSED" doses based on microtrigger events.

### 4. **Live Dashboard**
*   High-precision system clock synchronized with dispenser hardware.
*   Dynamic countdown timer for the upcoming dose.
*   Visual compliance rate dial.

## 🛠 Tech Stack

*   **UI Framework:** Jetpack Compose (Modern Declarative UI)
*   **Language:** Kotlin
*   **Architecture:** MVVM (Model-View-ViewModel)
*   **AI Integration:** Gemini-3.5-Flash (Google AI)
*   **State Management:** Kotlin Coroutines & Flow
*   **Storage:** Simulated IoT registry and persistence.

## 📦 Getting Started

### Prerequisites
*   Android Studio (Latest Version)
*   Gemini API Key (Obtainable from [Google AI Studio](https://aistudio.google.com/))

### Installation & Setup

1.  **Clone the Repository:**
    ```bash
    git clone https://github.com/your-username/smart-medicine-dispenser.git
    ```

2.  **Configure Environment Variables:**
    *   Create a file named `.env` in the project root directory.
    *   Add your Gemini API Key:
        ```env
        GEMINI_API_KEY=your_actual_api_key_here
        ```
    *   Refer to `.env.example` for the format.

3.  **Build the Project:**
    *   Open the project in Android Studio.
    *   Sync Gradle and build the `:app` module.
    *   *Note: The project uses the default Android debug keystore. Ensure `signingConfig` for `debugConfig` is not forcing a non-existent local file.*

4.  **Run the App:**
    *   Deploy to a physical device or emulator.
    *   Use the "Role Switcher" at the top to toggle between Patient, Doctor, and Family views.

## 📸 Screenshots

*(Add screenshots here showing Dashboard, AI Scanner, and Doctor Portal)*

## 🛡️ Security & Privacy
*   Sensitive API keys are managed via the `Secrets Gradle Plugin` and should never be committed to version control.
*   Patient IDs (e.g., `P-48902`) are used for secure hardware association.

## 📄 License
This project is licensed under the MIT License - see the LICENSE file for details.
