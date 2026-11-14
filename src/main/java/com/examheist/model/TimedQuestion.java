package com.examheist.model;

/**
 * Specialized Question subclass used exclusively in Vault 03 (Final Escape).
 * 
 * Demonstrates:
 * - Inheritance (extends Question)
 * - Constructor chaining using super(...)
 * - Method overriding (@Override getPoints)
 * - Runtime Polymorphism (Dynamic Method Dispatch)
 */
public class TimedQuestion extends Question {

    public TimedQuestion(String subject, int vaultNumber, String questionText, 
                         String optionA, String optionB, String optionC, String optionD, 
                         int correctAnswerIndex) {
        // Explicitly calling superclass constructor using super keyword
        super(subject, vaultNumber, questionText, optionA, optionB, optionC, optionD, correctAnswerIndex);
    }

    /**
     * Overrides getPoints() from Question to award a special 15-point bonus
     * for high-stakes final vault escape questions.
     */
    @Override
    public int getPoints() {
        return 15;
    }
}
