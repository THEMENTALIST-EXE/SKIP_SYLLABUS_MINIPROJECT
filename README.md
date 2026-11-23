# THE VAULTBREAK HEIST
> **Tagline:** *Crack the Questions. Escape the Exam.*

A polished educational escape-room quiz desktop game built in Java and JavaFX for the second-year B.Tech Object-Oriented Programming (OOP) curriculum.

---

## 1. Project Overview & Problem Statement

### Overview
**Skip the Syllabus: Exam Heist** puts the student in the shoes of an operative locked inside an automated Central Examination Vault. To escape the facility before an automated lockdown triggers, the player must breach three consecutive security vaults by solving technical questions under time pressure.

### Problem Statement
Traditional computer-based quiz systems are often dry, unengaging multiple-choice test portals. **Exam Heist** reimagines the examination assessment paradigm into a gamified, cyber-heist narrative without compromising code clarity or introducing bloated web/enterprise frameworks. It delivers an engaging visual experience while maintaining strict adherence to undergraduate Core Java syllabus fundamentals.

---

## 2. Objectives

- **Educational:** Reinforce core principles of Java Programming and Data Structures through timed challenges.
- **Pedagogical:** Visibly demonstrate core Java OOP concepts (Classes, Objects, Encapsulation, Inheritance, Method Overriding, Polymorphism, Interfaces, Collections, and File I/O).
- **Explainability:** Ensure every line of code is clear, concise, and defensible in an undergraduate oral viva evaluation.
- **Zero-Bloat Architecture:** Implement a responsive JavaFX GUI without FXML, web runtimes, or heavyweight ORMs.

---

## 3. Technologies Used

- **Programming Language:** Java (JDK 21+ / JDK 27 tested and verified)
- **GUI Framework:** JavaFX 21 (Controls, Graphics, Base, Animation Timeline)
- **Styling:** Standard JavaFX CSS (`style.css`)
- **Build System:** Apache Maven 3.9+ (includes self-contained Maven Wrapper scripts)
- **Storage:** Local flat text files (`data/questions.txt`, `data/scores.txt`)
- **Testing:** JUnit 5 for automated verification of core game rules

---

## 4. Key Features

1. **Cyber-Heist UI Theme:** Dark navy futuristic palette (`#0b0f19`), electric cyan accents, progress indicators, and status badges.
2. **Two Technical Mission Tracks:**
   - **Java Programming:** OOP Principles, Memory, Access Modifiers, Collections, Exceptions.
   - **Data Structures:** Stacks, Queues, Binary Trees, Search Algorithms, Sorting Complexity.
3. **Three Progressively Harder Vaults:**
   - **Vault 01 — Foundation:** 5 questions, 90-second timer, +10 points / -5 penalty. (Clearance: ≥ 3 correct).
   - **Vault 02 — Intermediate:** 5 questions, 90-second timer, +10 points / -5 penalty. (Clearance: ≥ 3 correct).
   - **Vault 03 — Final Escape:** 5 questions, 60-second high-stakes timer, +15 bonus points per correct answer (powered by `TimedQuestion` subclass).
4. **Deterministic Score Tracking:**
   - Correct Answer: +10 points (Vault 1 & 2), +15 points (Vault 3).
   - Incorrect Answer: -5 points.
   - Floor: Scores never fall below 0.
   - Attempt-isolated retry: Retrying a vault resets only the active attempt counters without corrupting previously cleared vaults or duplicating scores.
5. **Thread-Safe Countdown:** Driven by JavaFX `Timeline`, ensuring UI updates run safely on the JavaFX Application Thread without `Thread.sleep()`.
6. **Local Score Persistence:** Appends every attempt and mission completion to `data/scores.txt` with timestamp, agent callsign, subject, score, and outcome.

---

## 5. Source Code Structure

```text
SKIP_SYLLABUS_MINIPROJECT/
│
├── .mvn/                           # Self-contained Maven distribution
│   └── apache-maven-3.9.6/
├── mvn.cmd                         # Windows batch launcher
├── mvnw.cmd                        # Windows Maven Wrapper
├── pom.xml                         # Maven dependencies & JavaFX plugin config
│
├── data/
│   ├── questions.txt               # Pipe-delimited question bank (30 verified Qs)
│   └── scores.txt                  # Appended player score dossier
│
├── src/
│   ├── main/
│   │   ├── java/com/examheist/
│   │   │   ├── MainApp.java        # JavaFX Application (UI screens & Timeline)
│   │   │   │
│   │   │   ├── model/
│   │   │   │   ├── Player.java         # Encapsulated player model
│   │   │   │   ├── Question.java       # Base Question entity (+10 points)
│   │   │   │   ├── TimedQuestion.java  # Subclass (+15 bonus points, polymorphism)
│   │   │   │   └── Scorable.java       # Interface for scorable entities
│   │   │   │
│   │   │   ├── game/
│   │   │   │   └── GameSession.java    # Implements Scorable, rules & state
│   │   │   │
│   │   │   └── storage/
│   │   │       ├── QuestionBank.java   # File I/O, parsing, validation
│   │   │       └── ScoreManager.java   # FileWriter score persistence
│   │   │
│   │   └── resources/
│   │       └── style.css           # Cyber-heist JavaFX styling
│   │
│   └── test/
│       └── java/com/examheist/
│           └── GameMechanicsTest.java # 8 automated JUnit 5 tests
│
├── README.md                       # Project documentation
└── VIVA_GUIDE.md                   # Comprehensive viva explanation & Q&A
```

---

## 6. Setup & Execution Instructions (Windows)

### Prerequisites
- JDK 21 LTS or newer (tested on Java 21 and Java 27). Verify with:
  ```powershell
  java -version
  ```

### Build & Run
Open PowerShell in the project root directory:

1. **Run using Maven Wrapper (Recommended, works immediately):**
   ```powershell
   .\mvnw.cmd clean javafx:run
   ```

2. **Or run using system Maven:**
   ```powershell
   mvn clean javafx:run
   ```

3. **Execute Automated JUnit Tests:**
   ```powershell
   .\mvnw.cmd test
   ```

---

## 7. Gameplay Walkthrough

1. **Screen 1 — Welcome & Callsign:** Enter your Agent Callsign (name) and click **Initialize Heist Sequence**. Empty names are blocked with a validation warning.
2. **Screen 2 — Mission Track:** Select either **Java Programming** or **Data Structures**.
3. **Screen 3 — Vault Corridor:** View the 3 vaults. Vault 01 is unlocked. Click **Breach Vault 01**.
4. **Screen 4 — Quiz Challenge:** 
   - View the active question, timer, and score.
   - Select an option (A, B, C, or D) and click **Submit Answer**.
   - Input is immediately locked to prevent duplicate submissions.
   - Correct answers award points (+10, or +15 in Vault 3); wrong answers deduct -5.
   - Click **Next Node** until all 5 questions are answered.
   - If timer expires, emergency lockdown triggers and fails the attempt.
5. **Screen 5 — Results & Debrief:** 
   - Shows performance dossier (score, correct/incorrect, vaults cleared).
   - If you scored ≥ 3/5, the next vault is unlocked!
   - If you scored < 3/5, click **Retry Vault** to re-attempt without resetting previously cleared vaults.
   - Results are automatically appended to `data/scores.txt`.

---

## 8. Limitations & Future Improvements

### Current Limitations
- Operates locally using flat text files without remote networking or cloud sync.
- Questions are multiple-choice with 4 options per question.

### Future Improvements
- Add sound effects for countdown tick and door breaches using `javafx.scene.media.AudioClip`.
- Add an interactive coding challenge node in Vault 03 where players complete a code snippet.
- Add leaderboard visualization displaying the top 10 fastest escape times from `scores.txt`.
