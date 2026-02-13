### TypeTuto ⌨️

*A MonkeyType-inspired minimalist typing tutorial Java program built  with passion from 3 scholars from a random school in the plains of Cogon*

---

## 📌 Super Brief Description

**TypeTuto** is a modern, minimalist typing tutorial and performance tracker that is heavily inspired by MonkeyType.

* ⚡ Typing Speed (WPM)
* 🎯 Accuracy
* ✅ Correct & ❌ Incorrect characters
* 🏆 Performance Rank

The program will generate randomized text (words, numbers, or quotes), provides real-time character feedback, and calculates typing performance stats after each session.

MAIN GOAL:

> A program for users to improve speed, precision, and consistency.

---

## 🖥️ Interface & Features

### 1️⃣ Navigation Controls

  #### Word Mode (more may be added)
  
  * Words
  * Numbers
  * Quotes
  
  #### Language Mode (more may come soon)
  
  * English
  * Filipino
  
  #### Time Mode
  
  * 120s
  * 60s
  * 30s
  * 15s

### 2️⃣ Typing Area

  #### ⏳ Timer
  
  * Countdown based on selected time mode
  * Starts automatically when user starts typing
  
  #### 📝 Text Display
  
  * 3 lines visible at a time
  * 15 words per line
  * Updates itself as user progresses
  
  #### 🎨 Real-Time Character Feedback
  
  * **Grey** → Not yet typed
  * **White** → Correct character
  * **Red** → Incorrect character
  
  #### 📊 Live Statistics
  
  * WPM
  * Correct count
  * Wrong count
  * Accuracy
  * Rank (computed after session ends)

---

## 🗂️ Project Structure

```
typeTuto/
│
├── docs/
│   └── fileManagement.txt
│
├── pom.xml
│
├── src/
│   └── main/
│       ├── java/
│       │   └── typeTutor/
│       │       │
│       │       ├── app/
│       │       │   └── App.java
│       │       │
│       │       ├── controller/
│       │       │   └── MainController.java
│       │       │
│       │       ├── model/
│       │       │   ├── GameSession.java
│       │       │   ├── TypingStats.java
│       │       │   └── TextGenerator.java
│       │       │
│       │       └── view/
│       │           ├── MainFrame.java
│       │           ├── TypingPanel.java
│       │           ├── NavsPanel.java
│       │           ├── AppFonts.java
│       │           ├── GameStats.java
│       │           └── HeadingsPanel.java
│       │       
│       │
│       └── resources/
│           ├── icons/
│               ├── logo.png
│           ├── fonts/
│               ├── JetBrainsMono-VariableFont_wght.ttf
│               ├── MontSerrat-VariableFont_wght.ttf
│           └── text/
│               ├── words/
│               │   ├── english_1k.json
│               │   └── filipino.json
│               └── quotes/
│                   ├── english.json
│                   └── filipino.json
│
└── README.md
```

---

## 🧠 Architecture

  The project follows a simplified MVC structure:
  
  ### 🔹 app/
  
  **App.java**
  Entry point. Launches the Swing UI.
  
  ---
  
  ### 🔹 controller/
  
  **MainController.java**
  Handles:
  
  * Mode selection
  * Timer logic
  * Typing input processing
  * Game state transitions
  
  Acts as the bridge between UI and logic.
  
  ---
  
  ### 🔹 model/
  
  **GameSession.java**
  
  * Stores generated text
  * Tracks typing progress
  * Manages timer state
  
  **TypingStats.java**
  
  * Calculates WPM
  * Computes accuracy
  * Tracks correct/incorrect characters
  
  **TextGenerator.java**
  
  * Reads JSON files from `/resources`
  * Generates randomized word sets based on:
  
    * Word mode
    * Language
    * Time mode
  
  ---
  
  ### 🔹 view/
  
  **MainFrame.java**
  
  * Main application window
  
  **HeadingsPanel.java**
  
  * Title and subtitle
  
  **NavsPanel.java**
  
  * Mode selection controls
  
  **TypingPanel.java**
  
  * Text rendering
  * Character coloring
  * Timer display
  * Live statistics
  
  ---
  
  ### 🔹 resources/
  
  Contains:
  
  * Word banks (JSON)
  * Quotes
  * Fonts
  * Icons
    
  ---

### ▶️ Run the Program

  1. Once ZIP file is extracted, go inside extracted folder (typeTuto).
  2. -> typeTuto/src/main/java/typeTutor/app
  3. Open App.java
  4. Enjoy
  
  ## 🎯 Design Principles
  
  * Minimalist UI
  * Clear separation of concerns (MVC)
  * Resource-based file loading
  * Scalable and maintainable structure

---

## 🚀 Improvements that we want to make

  * A leaderboard system
  * User accounts
  * More modes
  * Performance history
  * Sound effects & animations
  * Graph-based performance analytics
