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



## 🛡️ Security & Privacy
*   Sensitive API keys are managed via the `Secrets Gradle Plugin` and should never be committed to version control.
*   Patient IDs (e.g., `P-48902`) are used for secure hardware association.

## 📄 License
This project is licensed under the MIT License - see the LICENSE file for details.
