package com.examheist.game;

import com.examheist.model.Player;
import com.examheist.model.Question;
import com.examheist.model.Scorable;

import java.util.ArrayList;

/**
 * Manages the state, rules, and scoring for an entire heist session.
 * 
 * Demonstrates:
 * - Interface implementation (implements Scorable)
 * - Polymorphism (dynamic dispatch when calling q.getPoints())
 * - Encapsulation of core game rules separate from JavaFX UI logic
 * - Arrays and Collections
 */
public class GameSession implements Scorable {

    private Player player;
    private int currentVaultNumber;
    private int currentQuestionIndex;

    // Active attempt metrics for the current vault
    private int currentAttemptScore;
    private int currentAttemptCorrect;
    private int currentAttemptIncorrect;

    // Track state for vaults 1, 2, and 3 (index 1 to 3)
    private boolean[] vaultUnlocked = new boolean[4];
    private boolean[] vaultCleared = new boolean[4];
    private int[] completedVaultScores = new int[4];

    // Cumulative answer stats
    private int totalCorrectAnswers;
    private int totalIncorrectAnswers;

    private ArrayList<Question> currentVaultQuestions;

    public static final int PASSING_THRESHOLD = 3; // Minimum correct answers required to clear a vault

    public GameSession(Player player) {
        this.player = player;
        this.currentVaultNumber = 1;
        this.currentQuestionIndex = 0;
        this.currentAttemptScore = 0;
        this.currentAttemptCorrect = 0;
        this.currentAttemptIncorrect = 0;
        this.totalCorrectAnswers = 0;
        this.totalIncorrectAnswers = 0;

        // Vault 1 is unlocked by default; Vaults 2 and 3 are locked initially
        vaultUnlocked[1] = true;
        vaultUnlocked[2] = false;
        vaultUnlocked[3] = false;

        vaultCleared[1] = false;
        vaultCleared[2] = false;
        vaultCleared[3] = false;

        this.currentVaultQuestions = new ArrayList<>();
    }

    /**
     * Prepares a vault attempt with its question set.
     * Resets attempt-specific counters without corrupting previously unlocked vaults.
     */
    public void startVault(int vaultNumber, ArrayList<Question> questions) {
        this.currentVaultNumber = vaultNumber;
        this.currentVaultQuestions = questions;
        this.currentQuestionIndex = 0;
        this.currentAttemptScore = 0;
        this.currentAttemptCorrect = 0;
        this.currentAttemptIncorrect = 0;
    }

    public Question getCurrentQuestion() {
        if (currentVaultQuestions != null && currentQuestionIndex < currentVaultQuestions.size()) {
            return currentVaultQuestions.get(currentQuestionIndex);
        }
        return null;
    }

    public boolean hasMoreQuestions() {
        return currentVaultQuestions != null && (currentQuestionIndex < currentVaultQuestions.size() - 1);
    }

    public void nextQuestion() {
        if (currentQuestionIndex < currentVaultQuestions.size() - 1) {
            currentQuestionIndex++;
        }
    }

    /**
     * Submits an answer choice (1 to 4).
     * Calculates score and updates correct/incorrect counters.
     * 
     * Demonstrates Polymorphism:
     * q.getPoints() invokes Question.getPoints() (+10) for Vault 1 & 2,
     * or TimedQuestion.getPoints() (+15) for Vault 3 dynamically at runtime!
     */
    public boolean submitAnswer(int selectedOptionIndex) {
        Question current = getCurrentQuestion();
        if (current == null) {
            return false;
        }

        boolean correct = current.isCorrect(selectedOptionIndex);
        if (correct) {
            int earnedPoints = current.getPoints(); // Polymorphic method call
            currentAttemptScore += earnedPoints;
            currentAttemptCorrect++;
        } else {
            // Apply penalty; score cannot fall below zero
            currentAttemptScore = Math.max(0, currentAttemptScore - 5);
            currentAttemptIncorrect++;
        }
        return correct;
    }

    /**
     * Called when all 5 questions of a vault are finished or when time runs out.
     * Evaluates whether the player met the passing threshold (at least 3 correct).
     * 
     * @return true if vault is cleared; false if player must retry.
     */
    public boolean finalizeVaultAttempt() {
        boolean passed = (currentAttemptCorrect >= PASSING_THRESHOLD);

        if (passed) {
            vaultCleared[currentVaultNumber] = true;
            completedVaultScores[currentVaultNumber] = currentAttemptScore;

            // Unlock next vault in sequence
            if (currentVaultNumber == 1) {
                vaultUnlocked[2] = true;
            } else if (currentVaultNumber == 2) {
                vaultUnlocked[3] = true;
            }

            totalCorrectAnswers += currentAttemptCorrect;
            totalIncorrectAnswers += currentAttemptIncorrect;
        }
        return passed;
    }

    /**
     * Resets the active vault attempt for a retry without affecting unlocked status.
     */
    public void retryCurrentVault() {
        this.currentQuestionIndex = 0;
        this.currentAttemptScore = 0;
        this.currentAttemptCorrect = 0;
        this.currentAttemptIncorrect = 0;
    }

    /**
     * Implementation of Scorable interface.
     * Calculates deterministic total score from all completed vaults plus current attempt.
     */
    @Override
    public int getScore() {
        int total = 0;
        for (int i = 1; i <= 3; i++) {
            total += completedVaultScores[i];
        }
        // If current vault is not yet cleared, add active attempt score
        if (!vaultCleared[currentVaultNumber]) {
            total += currentAttemptScore;
        }
        return total;
    }

    public int getVaultCountCleared() {
        int count = 0;
        for (int i = 1; i <= 3; i++) {
            if (vaultCleared[i]) {
                count++;
            }
        }
        return count;
    }

    public boolean isMissionAccomplished() {
        return vaultCleared[3];
    }

    // Getters and helper methods
    public Player getPlayer() {
        return player;
    }

    public int getCurrentVaultNumber() {
        return currentVaultNumber;
    }

    public int getCurrentQuestionIndex() {
        return currentQuestionIndex;
    }

    public int getCurrentAttemptScore() {
        return currentAttemptScore;
    }

    public int getCurrentAttemptCorrect() {
        return currentAttemptCorrect;
    }

    public int getCurrentAttemptIncorrect() {
        return currentAttemptIncorrect;
    }

    public int getTotalCorrectAnswers() {
        return totalCorrectAnswers + (vaultCleared[currentVaultNumber] ? 0 : currentAttemptCorrect);
    }

    public int getTotalIncorrectAnswers() {
        return totalIncorrectAnswers + (vaultCleared[currentVaultNumber] ? 0 : currentAttemptIncorrect);
    }

    public boolean isVaultUnlocked(int vaultNumber) {
        if (vaultNumber >= 1 && vaultNumber <= 3) {
            return vaultUnlocked[vaultNumber];
        }
        return false;
    }

    public boolean isVaultCleared(int vaultNumber) {
        if (vaultNumber >= 1 && vaultNumber <= 3) {
            return vaultCleared[vaultNumber];
        }
        return false;
    }

    public int getTotalQuestionsInCurrentVault() {
        return (currentVaultQuestions != null) ? currentVaultQuestions.size() : 0;
    }
}
