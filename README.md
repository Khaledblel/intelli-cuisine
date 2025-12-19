# Intelli-Cuisine

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Platform: Android](https://img.shields.io/badge/platform-android-green.svg)](https://www.android.com/)
[![Built in Java](https://img.shields.io/badge/language-java-blue.svg)](https://developer.android.com/)
  
> **Native Android application leveraging Gemini AI to generate personalized recipes from available ingredients, featuring Firebase synchronization and a smart cooking assistant.**

---

## Table of Contents

- [About](#about)
- [Features](#features)
- [Screenshots](#screenshots)
- [Getting Started](#getting-started)
    - [Requirements](#requirements)
    - [Installation](#installation)
    - [Firebase Setup](#firebase-setup)
    - [Gemini API Configuration](#gemini-api-configuration)
    - [Running the App](#running-the-app)
- [Usage](#usage)
- [Contributing](#contributing)
- [Known Issues](#known-issues)
- [License](#license)
- [Contact](#contact)

---

## About

**Intelli-Cuisine** is a smart, native Android app designed to inspire your culinary creativity. By leveraging Google Gemini AI, Intelli-Cuisine provides personalized recipe suggestions based on ingredients you have on hand. The app features real-time synchronization with Firebase, ensuring your preferences and ingredient lists are always backed up. The integrated cooking assistant offers guidance throughout your cooking process.

---

## Features

- **AI-Powered Recipe Generation**  
  Get personalized recipes using Gemini AI based on your available ingredients.

- **Ingredient Management**  
  Easily add, remove, and manage your pantry items.

- **Firebase Synchronization**  
  Seamless cloud backup and sync with Firebase.

- **Smart Cooking Assistant**  
  Interactive step-by-step instructions for each recipe.

- **User Profiles**  
  Save your favorite recipes and ingredient preferences.

---

## Screenshots

<!-- Replace the links below with your actual screenshots -->
<p align="center">
  <img src="docs/screenshots/home.png" width="250">
  <img src="docs/screenshots/ingredient-manager.png" width="250">
  <img src="docs/screenshots/recipe-details.png" width="250">
  <img src="docs/screenshots/cooking-assistant.png" width="250">
</p>

---

## Getting Started

### Requirements

- **Android Studio** (Giraffe or newer recommended)
- **Java 8+**
- **Android device or emulator (API 23+)**
- **Google Firebase Account**
- **Gemini API Access** (API Key)

---

### Installation

1. **Clone the Repository**
    ```bash
    git clone https://github.com/Khaledblel/intelli-cuisine.git
    cd intelli-cuisine
    ```

2. **Open in Android Studio**
   - Launch **Android Studio**
   - _File_ → _Open..._ → Select the cloned folder

3. **Sync Gradle**
   - Allow Gradle sync to complete
   - Download dependencies if prompted

---

### Firebase Setup

1. **Create a Firebase Project**
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Add a new Project

2. **Register your Android App**
   - Add your app’s package name (see `app/build.gradle`)

3. **Download `google-services.json`**
   - Place the file in `app/`

4. **Enable Authentication & Firestore**
   - Enable desired authentication method (Email, Google, etc.)
   - Set up Firestore Database (in test mode for development)

---

### Gemini API Configuration

1. **Get a Gemini API Key**
   - Register for API access at [Gemini AI](https://ai.google.dev/)

2. **Store API Key Securely**
   - Create or update `local.properties` (NOT checked into git):
     ```
     GEMINI_API_KEY=your-real-gemini-api-key
     ```
   - Or follow your organization’s method for secrets management.

3. **Update Configuration**
   - In `GeminiManager.java` (or your network/configuration class), ensure it loads the Gemini key securely.

---

### Running the App

- Connect your device or start an emulator.
- Press **Run** in Android Studio or use
    ```bash
    ./gradlew assembleDebug
    ```
- Sign in with Firebase-enabled credentials if prompted.

---

## Usage

1. **Add Ingredients:**
    - Tap the floating plus button on the Ingredients tab.
    - Select or enter ingredients you have.

2. **Generate Recipes:**
    - Press "Generate Recipes" for Gemini AI to suggest personalized dishes.
    - View the list of recipes; tap a recipe to see full details.

3. **Cooking Assistant:**
    - When viewing a recipe, tap "Start Cooking" to enter assistant mode.
    - Follow step-by-step instructions, with tips and timers.

4. **Syncing:**
    - Ensure you are signed in for automatic Firebase sync of preferences and pantry.

5. **Favorites & Profiles:**
    - Save favorite recipes and customize your dietary preferences in your profile.

---

## Contributing

Contributions are welcome!  
Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

- Report bugs & request features via [Issues](https://github.com/Khaledblel/intelli-cuisine/issues)
- Open Pull Requests for improvements
- For major changes, please open an issue first

---

## Known Issues

- **No Dark Mode Support:**  
  The current version does not provide a dark/night UI theme, which can impact usability in low-light conditions.

- **No English Localization:**  
  The app interface only supports its default language (likely Arabic or another non-English language), making it inaccessible for English speakers. Multilanguage support is planned.

- **Offline Usage Limitations:**  
  Recipe generation and sync require an active Internet connection.

- **Limited Dietary Restrictions:**  
  Advanced dietary preferences (e.g., vegan, gluten-free) are not fully supported in AI suggestions.

- **Accessibility Features**  
  Accessibility improvements (screen reader support, font scaling) are minimal in this version.

Please report other bugs or suggest enhancements via [the issues page](https://github.com/Khaledblel/intelli-cuisine/issues).

---

## License

This project is licensed under the [MIT License](LICENSE).

---

## Contact

Author: **Khaledblel**  
- [GitHub Profile](https://github.com/Khaledblel)  
- For feature requests, bug reports, or questions, feel free to open an issue!

---
