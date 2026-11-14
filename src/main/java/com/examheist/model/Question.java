package com.examheist.model;

/**
 * Base Question class representing a single multiple-choice question.
 * 
 * Demonstrates:
 * - Encapsulation (data hiding with private fields and public methods)
 * - Methods and Constructors
 * - Reusability as superclass for TimedQuestion
 */
public class Question {
    private String subject;
    private int vaultNumber;
    private String questionText;
    private String[] options;
    private int correctAnswerIndex; // 1-based index (1 to 4)

    public Question(String subject, int vaultNumber, String questionText, 
                    String optionA, String optionB, String optionC, String optionD, 
                    int correctAnswerIndex) {
        this.subject = subject;
        this.vaultNumber = vaultNumber;
        this.questionText = questionText;
        this.options = new String[] { optionA, optionB, optionC, optionD };
        this.correctAnswerIndex = correctAnswerIndex;
    }

    // Checks if the chosen option (1 to 4) matches the correct answer
    public boolean isCorrect(int selectedAnswerIndex) {
        return this.correctAnswerIndex == selectedAnswerIndex;
    }

    // Base points for answering correctly in Vault 1 and Vault 2
    public int getPoints() {
        return 10;
    }

    public String getSubject() {
        return subject;
    }

    public int getVaultNumber() {
        return vaultNumber;
    }

    public String getQuestionText() {
        return questionText;
    }

    public String[] getOptions() {
        return options;
    }

    public int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }

    // Returns the actual text of the correct option for player feedback
    public String getCorrectAnswerText() {
        if (correctAnswerIndex >= 1 && correctAnswerIndex <= options.length) {
            return options[correctAnswerIndex - 1];
        }
        return "";
    }
}
