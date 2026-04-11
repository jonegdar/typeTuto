# ⌨️ TypeTutoV2

*A MonkeyType-inspired minimalist typing tutorial built with Java (Swing + Maven).
Newer and improved logic and UI/UX.*

---

## 📌 Overview

**TypeTuto** is a modern, minimalist typing tutorial and performance tracker inspired by MonkeyType.

It evaluates (in a better way than before):

* ⚡ Typing Speed (WPM)
* 🎯 Accuracy
* ✅ Correct & ❌ Incorrect characters
* 🏆 Performance Rank

The application generates randomized text (words, numbers, or quotes), provides real-time character feedback, and calculates performance metrics after each session (all in a better manner).

The goal is simple:

> Improve speed. Improve precision. Improve consistency.

---

## 🖥️ Interface & Features

### 1️⃣ Header Section

* Application title
* Instructional subtitle
* Clean dark minimalist theme

---

### 2️⃣ Navigation Controls

#### Word Mode

* Words
* Numbers
* Quotes

#### Language Mode

* English
* Filipino

#### Time Mode

* 120s
* 60s
* 30s
* 15s

#### Special Mode

* Crazy Mode (variation gameplay behavior)

---

### 3️⃣ Typing Area

#### ⏳ Timer

* Countdown based on selected time mode
* Starts automatically on first key press

#### 📝 Text Display

* 3 lines visible at a time
* 15 words per line
* Dynamically updates as user progresses

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
│       │           ├── NavbarPanel.java
│       │           └── HeaderPanel.java
│       │
│       └── resources/
│           ├── icons/
│           │   └── logo.png
│           ├── fonts/
│           │   ├── JetBrainsMono-VariableFont_wght.ttf
│           │   └── MontSerrat-VariableFont_wght.ttf
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

**HeaderPanel.java**

* Title and subtitle

**NavbarPanel.java**

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

All files are loaded using `getResourceAsStream()` for portability.

---

## ⚙️ Setup & Execution

### Requirements

* JDK 17+ (recommended for stability)
* Maven 3.9+

---

### 🔧 Build the Project

From the root directory:

```
mvn clean package
```

After building:

```
target/typeTuto-1.0-shaded.jar
```

---

### ▶️ Run the Program

```
java -jar target/typeTuto-1.0-shaded.jar
```

The JAR file includes all dependencies and runs independently.

---

## 🎯 Design Principles

* Minimalist UI
* Clear separation of concerns (MVC)
* Portable Maven-based architecture
* Resource-based file loading
* Scalable and maintainable structure

---

## 🚀 Future Improvements

* Persistent leaderboard system
* User accounts
* Difficulty scaling
* Performance history tracking
* Sound effects & animations
* Graph-based performance analytics

