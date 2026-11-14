# VIVA PREPARATION & CODE ARCHITECTURE GUIDE
## SKIP THE SYLLABUS: EXAM HEIST

This document maps every Java Object-Oriented Programming (OOP) concept directly to the actual source files and methods implemented in **Skip The Syllabus: Exam Heist**. Use this guide to prepare for your classroom demonstration and oral viva evaluation.

---

## 1. MAPPING OOP CONCEPTS TO SOURCE CODE

### 1.1 Classes and Objects
- **Where Used:**
  - `Player.java`: Blueprint for a player object (`new Player(enteredName, "Java")` in `MainApp.java`).
  - `Question.java` & `TimedQuestion.java`: Blueprints representing individual quiz security nodes.
  - `GameSession.java`: Core engine object encapsulating the active heist rules and state.
- **Why It Matters:** Classes provide the templates (attributes + methods), while objects are the dynamic, state-bearing instances instantiated in memory during the game.

### 1.2 Constructors & Constructor Chaining
- **Where Used:**
  - `Player(String name, String selectedSubject)` initializes the player state.
  - `Question(...)` initializes question text, options, and the correct answer index.
  - `TimedQuestion(...)` in `TimedQuestion.java`:
    ```java
    public TimedQuestion(String subject, int vaultNumber, String questionText, 
                         String optionA, String optionB, String optionC, String optionD, 
                         int correctAnswerIndex) {
        super(subject, vaultNumber, questionText, optionA, optionB, optionC, optionD, correctAnswerIndex);
    }
    ```
- **Why It Matters:** Constructors guarantee that an object cannot exist in an uninitialized, invalid state. The `super(...)` call demonstrates constructor chaining, ensuring superclass fields are initialized before subclass logic runs.

### 1.3 Encapsulation & Data Hiding
- **Where Used:**
  - All model classes (`Player.java`, `Question.java`, `GameSession.java`) declare fields as `private`.
  - Public getter methods (e.g., `getName()`, `getScore()`, `getCurrentQuestion()`) provide controlled, read-only access.
  - Internal scoring variables (`currentAttemptScore`, `completedVaultScores`) cannot be modified directly from outside classes or UI event handlers.
- **Why It Matters:** Encapsulation protects object integrity, prevents unauthorized external tampering with game state, and cleanly decouples internal storage representations from external consumers.

### 1.4 Inheritance (`extends`)
- **Where Used:**
  - `TimedQuestion extends Question` in `TimedQuestion.java`.
  - `MainApp extends Application` in `MainApp.java`.
- **Why It Matters:** `TimedQuestion` inherits all fields and behaviors (`isCorrect()`, `getQuestionText()`, `getOptions()`) from `Question` without duplicating code, while adding specialized behavior for Vault 03.

### 1.5 Method Overriding (`@Override`)
- **Where Used:**
  - In `Question.java`:
    ```java
    public int getPoints() {
        return 10;
    }
    ```
  - In `TimedQuestion.java`:
    ```java
    @Override
    public int getPoints() {
        return 15; // Bonus points for Vault 3 high-stakes escape!
    }
    ```
- **Why It Matters:** Method overriding allows a subclass to provide a specific implementation of a method defined in its superclass with the exact same method signature.

### 1.6 Runtime Polymorphism (Dynamic Method Dispatch)
- **Where Used:**
  - In `GameSession.submitAnswer()`:
    ```java
    Question current = getCurrentQuestion();
    if (correct) {
        int earnedPoints = current.getPoints(); // Polymorphic method call
        currentAttemptScore += earnedPoints;
    }
    ```
- **Why It Matters:** The variable `current` is of reference type `Question`. For Vault 1 and Vault 2, it refers to a `Question` object and returns 10. For Vault 3, it refers to a `TimedQuestion` object and dynamically invokes `TimedQuestion.getPoints()`, returning 15. The Java Virtual Machine (JVM) resolves which method to execute at runtime based on the actual object instance, not the reference type.

### 1.7 Interfaces (`implements`)
- **Where Used:**
  - `Scorable.java`: Defines the contract:
    ```java
    public interface Scorable {
        int getScore();
    }
    ```
  - `GameSession.java`: Implements `Scorable`:
    ```java
    public class GameSession implements Scorable {
        @Override
        public int getScore() { ... }
    }
    ```
- **Why It Matters:** Interfaces establish an abstract contract without dictating implementation. Any system component needing a score can accept a `Scorable` reference, decoupling scoring consumers from specific session details.

### 1.8 Collections Framework (`ArrayList`)
- **Where Used:**
  - In `QuestionBank.java`: `ArrayList<Question> allQuestions = new ArrayList<>();`
  - In `GameSession.java`: `ArrayList<Question> currentVaultQuestions;`
- **Why It Matters:** `ArrayList` provides dynamic resizing, contiguous indexed access ($O(1)$ lookup via `get(i)`), type safety via generics (`<Question>`), and eliminates the need for manual array reallocation.

### 1.9 File I/O & Streams
- **Where Used:**
  - **Reading (`QuestionBank.java`):** Uses `File` and `Scanner` to read `data/questions.txt` line by line.
  - **Writing (`ScoreManager.java`):** Uses `FileWriter` with `append = true` wrapped in `PrintWriter` to write records to `data/scores.txt`.
- **Why It Matters:** Enables offline persistence of both configuration/questions and cumulative player match dossiers across application restarts.

### 1.10 Exception Handling (`try-catch-finally` / `try-with-resources`)
- **Where Used:**
  - `QuestionBank.loadFromFile()` handles `FileNotFoundException` and `NumberFormatException`, re-throwing clean `IllegalArgumentException` with the offending line number if data is malformed.
  - `ScoreManager.saveScore()` handles `IOException` gracefully if the disk is write-protected or unavailable.
  - Both use Java's modern **try-with-resources** syntax (`try (Scanner s = ...)`, `try (FileWriter fw = ...)`), which guarantees that file handles are safely closed even if an unexpected exception occurs.

### 1.11 Package Architecture
- **Where Used:**
  - `com.examheist`: Root application entry point (`MainApp.java`).
  - `com.examheist.model`: Domain data models (`Player`, `Question`, `TimedQuestion`, `Scorable`).
  - `com.examheist.game`: Business logic and session state (`GameSession`).
  - `com.examheist.storage`: Persistence and file handling (`QuestionBank`, `ScoreManager`).
- **Why It Matters:** Follows standard Java package naming conventions to prevent namespace collisions, establish clear access boundaries, and make the codebase intuitive to navigate.

### 1.12 GUI Communication with Business Logic
- **Where Used:**
  - `MainApp.java` acts purely as the presentation layer. It constructs the screens and listens for button events.
  - When the user selects an answer and clicks **Submit**, `MainApp` does **not** calculate points directly. Instead, it delegates to `gameSession.submitAnswer(chosenIndex)` and simply renders the resulting boolean and score on screen.
- **Why It Matters:** Enforces the Single Responsibility Principle and Separation of Concerns. If game rules or scoring change, only `GameSession.java` needs modification—the UI code remains untouched.

---

## 2. 15 LIKELY VIVA QUESTIONS & ANSWERS

### Q1: Why did you choose JavaFX instead of Swing or AWT?
> **Answer:** JavaFX is the modern standard for desktop applications in Java. It provides clean separation of structure and presentation through CSS, responsive layout containers (`BorderPane`, `VBox`, `HBox`), and thread-safe animation mechanisms like `Timeline`.

### Q2: What is the purpose of constructor chaining and where is it in your project?
> **Answer:** Constructor chaining occurs when a subclass constructor invokes its superclass constructor using `super()`. In `TimedQuestion.java`, `super(subject, vaultNumber, ...)` is called as the first statement to ensure all superclass properties of `Question` are initialized before subclass execution.

### Q3: Explain where runtime polymorphism is demonstrated in your code.
> **Answer:** In `GameSession.java`, inside `submitAnswer()`, we call `current.getPoints()` on a variable of type `Question`. For Vault 1 and 2 questions, it calls `Question.getPoints()` returning 10. For Vault 3, the object is an instance of `TimedQuestion`, so Java dynamically dispatches the call to `TimedQuestion.getPoints()`, returning 15.

### Q4: Why did you use an interface (`Scorable`) instead of an abstract class?
> **Answer:** `Scorable` defines a pure capability (`int getScore()`) rather than an identity. By using an interface, any future class (like a `TeamSession` or `BonusRound`) can implement `Scorable` regardless of where it sits in the class inheritance hierarchy.

### Q5: How do you prevent duplicate answer submissions in JavaFX?
> **Answer:** As soon as the player clicks **Submit Answer**, `MainApp` disables all `RadioButton` choices in the options container using `node.setDisable(true)` and hides the submit button, preventing any second click before the player moves to the next question.

### Q6: How does the countdown timer work without crashing the JavaFX thread?
> **Answer:** We used `javafx.animation.Timeline` with a 1-second `KeyFrame` cycle. `Timeline` events run directly on the JavaFX Application Thread, eliminating cross-thread concurrency hazards and avoiding `Thread.sleep()`, which would freeze the user interface.

### Q7: What happens if a player fails a vault and retries it?
> **Answer:** `gameSession.retryCurrentVault()` resets only the active attempt counters (`currentQuestionIndex = 0`, `currentAttemptScore = 0`, correct/incorrect counts). It does not lock previously cleared vaults, and cumulative score calculation remains strictly deterministic.

### Q8: How does your file reading handle incorrect formatting in `questions.txt`?
> **Answer:** `QuestionBank.java` reads the file line by line using `Scanner`. It splits each line using `line.split("\\|")`. If the token count is not 8, or if the vault or correct index cannot be parsed as an integer, it throws an `IllegalArgumentException` indicating the exact line number.

### Q9: Why did you use `try-with-resources` in `ScoreManager.java`?
> **Answer:** `try-with-resources` automatically invokes the `close()` method on any object implementing `AutoCloseable` (like `FileWriter` and `Scanner`) at the end of the block, even if an uncaught exception is thrown, preventing operating system file handle leaks.

### Q10: Why did you escape the pipe symbol as `\\|` in `String.split()`?
> **Answer:** In Java, `String.split()` takes a regular expression. The pipe symbol `|` is a regex meta-character representing logical OR. To split by a literal pipe, it must be escaped as `\\|`.

### Q11: How is score persistence handled without overwriting past history?
> **Answer:** `ScoreManager.java` uses `new FileWriter(file, true)`. Passing `true` as the second argument opens the file in append mode, writing new heist records to the bottom of `data/scores.txt` without deleting past entries.

### Q12: Why is the floor of the score clamped to zero?
> **Answer:** In `GameSession.java`, when a wrong answer is submitted, we write:
> `currentAttemptScore = Math.max(0, currentAttemptScore - 5);`
> This prevents a streak of wrong answers from driving the score into negative numbers.

### Q13: What is the difference between method overloading and method overriding in your project?
> **Answer:** Method overloading is compile-time polymorphism where methods share the same name with different parameters. Method overriding is runtime polymorphism where `TimedQuestion` provides its own implementation of `getPoints()` with the identical signature as in `Question`.

### Q14: How does `MainApp` switch between screens?
> **Answer:** The primary window uses a `BorderPane rootLayout`. Each screen construction method (`showWelcomeScreen`, `showSubjectSelectionScreen`, etc.) builds a clean container (such as a `StackPane` or `VBox`) and sets it as the center via `rootLayout.setCenter(...)`. This enables instantaneous, flicker-free screen transitions without creating multiple windows or heavy stages.

### Q15: If `questions.txt` is missing, does the application crash?
> **Answer:** No. `QuestionBank.loadFromFile()` throws a `FileNotFoundException`, which is caught in `MainApp.start()`. It alerts the user via a friendly JavaFX `Alert` dialog explaining that the question file is missing, rather than crashing with an unhandled stack trace.
