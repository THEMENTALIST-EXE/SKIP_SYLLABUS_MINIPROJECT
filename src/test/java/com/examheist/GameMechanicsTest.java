package com.examheist;

import com.examheist.game.GameSession;
import com.examheist.model.Player;
import com.examheist.model.Question;
import com.examheist.model.TimedQuestion;
import com.examheist.storage.QuestionBank;
import com.examheist.storage.ScoreManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Automated test suite verifying gameplay mechanics, scoring rules,
 * vault unlocking conditions, score persistence, and exception handling.
 */
public class GameMechanicsTest {

    private QuestionBank questionBank;
    private Player player;
    private GameSession session;

    @BeforeEach
    void setUp() throws FileNotFoundException {
        questionBank = new QuestionBank();
        questionBank.loadFromFile("data/questions.txt");
        player = new Player("TestAgent", "Java");
        session = new GameSession(player);
    }

    @Test
    @DisplayName("Question bank should load exactly 30 questions (15 Java and 15 DSA)")
    void testQuestionBankLoad() {
        assertEquals(30, questionBank.getTotalQuestionsLoaded());

        ArrayList<Question> javaV1 = questionBank.getQuestionsFor("Java", 1);
        ArrayList<Question> javaV2 = questionBank.getQuestionsFor("Java", 2);
        ArrayList<Question> javaV3 = questionBank.getQuestionsFor("Java", 3);

        assertEquals(5, javaV1.size(), "Java Vault 1 must have exactly 5 questions");
        assertEquals(5, javaV2.size(), "Java Vault 2 must have exactly 5 questions");
        assertEquals(5, javaV3.size(), "Java Vault 3 must have exactly 5 questions");

        ArrayList<Question> dsaV1 = questionBank.getQuestionsFor("DSA", 1);
        ArrayList<Question> dsaV2 = questionBank.getQuestionsFor("DSA", 2);
        ArrayList<Question> dsaV3 = questionBank.getQuestionsFor("DSA", 3);

        assertEquals(5, dsaV1.size(), "DSA Vault 1 must have exactly 5 questions");
        assertEquals(5, dsaV2.size(), "DSA Vault 2 must have exactly 5 questions");
        assertEquals(5, dsaV3.size(), "DSA Vault 3 must have exactly 5 questions");
    }

    @Test
    @DisplayName("Vault 3 questions must be instances of TimedQuestion awarding 15 points (Polymorphism)")
    void testTimedQuestionBonusAndPolymorphism() {
        ArrayList<Question> v1Questions = questionBank.getQuestionsFor("Java", 1);
        ArrayList<Question> v3Questions = questionBank.getQuestionsFor("Java", 3);

        Question q1 = v1Questions.get(0);
        Question q3 = v3Questions.get(0);

        assertEquals(10, q1.getPoints(), "Vault 1 question must award 10 points");
        assertInstanceOf(TimedQuestion.class, q3, "Vault 3 question must be an instance of TimedQuestion");
        assertEquals(15, q3.getPoints(), "Vault 3 TimedQuestion must award 15 bonus points");
    }

    @Test
    @DisplayName("Initial vault state: Vault 1 is unlocked; Vaults 2 and 3 are locked")
    void testInitialVaultLockState() {
        assertTrue(session.isVaultUnlocked(1), "Vault 1 should be unlocked initially");
        assertFalse(session.isVaultUnlocked(2), "Vault 2 should be locked initially");
        assertFalse(session.isVaultUnlocked(3), "Vault 3 should be locked initially");
    }

    @Test
    @DisplayName("Correct answers increase score (+10) and wrong answers deduct (-5), min score 0")
    void testScoringMechanicsAndPenalty() {
        ArrayList<Question> v1 = questionBank.getQuestionsFor("Java", 1);
        session.startVault(1, v1);

        Question q1 = session.getCurrentQuestion();
        int correctIndex = q1.getCorrectAnswerIndex();

        // 1. Submit correct answer: +10 pts
        boolean isCorrect = session.submitAnswer(correctIndex);
        assertTrue(isCorrect);
        assertEquals(10, session.getCurrentAttemptScore());
        assertEquals(1, session.getCurrentAttemptCorrect());

        // 2. Submit wrong answer: -5 pts
        session.nextQuestion();
        Question q2 = session.getCurrentQuestion();
        int wrongIndexQ2 = (q2.getCorrectAnswerIndex() == 1) ? 2 : 1;
        boolean isWrong = session.submitAnswer(wrongIndexQ2);
        assertFalse(isWrong);
        assertEquals(5, session.getCurrentAttemptScore());
        assertEquals(1, session.getCurrentAttemptIncorrect());

        // 3. Score must not fall below zero
        session.nextQuestion();
        Question q3 = session.getCurrentQuestion();
        int wrongIndexQ3 = (q3.getCorrectAnswerIndex() == 1) ? 2 : 1;
        session.submitAnswer(wrongIndexQ3); // 5 - 5 = 0

        session.nextQuestion();
        Question q4 = session.getCurrentQuestion();
        int wrongIndexQ4 = (q4.getCorrectAnswerIndex() == 1) ? 2 : 1;
        session.submitAnswer(wrongIndexQ4); // 0 - 5 = clamped to 0
        assertEquals(0, session.getCurrentAttemptScore(), "Score should never drop below zero");
    }

    @Test
    @DisplayName("Passing threshold: At least 3 correct answers required to clear vault and unlock next")
    void testVaultUnlockingSequence() {
        ArrayList<Question> v1 = questionBank.getQuestionsFor("Java", 1);
        session.startVault(1, v1);

        // Answer 3 correct, 2 wrong (total 3/5)
        for (int i = 0; i < 5; i++) {
            Question q = session.getCurrentQuestion();
            if (i < 3) {
                session.submitAnswer(q.getCorrectAnswerIndex());
            } else {
                int wrong = (q.getCorrectAnswerIndex() == 1) ? 2 : 1;
                session.submitAnswer(wrong);
            }
            if (session.hasMoreQuestions()) {
                session.nextQuestion();
            }
        }

        boolean cleared = session.finalizeVaultAttempt();
        assertTrue(cleared, "Vault 1 should be cleared with 3 correct answers");
        assertTrue(session.isVaultCleared(1), "Vault 1 cleared flag must be true");
        assertTrue(session.isVaultUnlocked(2), "Vault 2 must now be unlocked");
        assertFalse(session.isVaultUnlocked(3), "Vault 3 must remain locked");

        // Now play Vault 2 and clear it
        ArrayList<Question> v2 = questionBank.getQuestionsFor("Java", 2);
        session.startVault(2, v2);
        for (int i = 0; i < 5; i++) {
            Question q = session.getCurrentQuestion();
            session.submitAnswer(q.getCorrectAnswerIndex());
            if (session.hasMoreQuestions()) {
                session.nextQuestion();
            }
        }
        boolean v2Cleared = session.finalizeVaultAttempt();
        assertTrue(v2Cleared);
        assertTrue(session.isVaultCleared(2));
        assertTrue(session.isVaultUnlocked(3), "Vault 3 must now be unlocked after Vault 2 clearance");

        // Play Vault 3 and clear it (Escape!)
        ArrayList<Question> v3 = questionBank.getQuestionsFor("Java", 3);
        session.startVault(3, v3);
        for (int i = 0; i < 5; i++) {
            Question q = session.getCurrentQuestion();
            session.submitAnswer(q.getCorrectAnswerIndex());
            if (session.hasMoreQuestions()) {
                session.nextQuestion();
            }
        }
        boolean v3Cleared = session.finalizeVaultAttempt();
        assertTrue(v3Cleared);
        assertTrue(session.isMissionAccomplished(), "All 3 vaults cleared means mission accomplished!");
    }

    @Test
    @DisplayName("Failing vault threshold (< 3 correct) requires retry and does not unlock next vault")
    void testVaultFailureAndThreshold() {
        ArrayList<Question> v1 = questionBank.getQuestionsFor("Java", 1);
        session.startVault(1, v1);

        // Answer only 2 correct (below threshold of 3)
        for (int i = 0; i < 5; i++) {
            Question q = session.getCurrentQuestion();
            if (i < 2) {
                session.submitAnswer(q.getCorrectAnswerIndex());
            } else {
                int wrong = (q.getCorrectAnswerIndex() == 1) ? 2 : 1;
                session.submitAnswer(wrong);
            }
            if (session.hasMoreQuestions()) {
                session.nextQuestion();
            }
        }

        boolean cleared = session.finalizeVaultAttempt();
        assertFalse(cleared, "Vault 1 should fail with only 2 correct answers");
        assertFalse(session.isVaultCleared(1));
        assertFalse(session.isVaultUnlocked(2), "Vault 2 should remain locked");

        // Test retry mechanism
        session.retryCurrentVault();
        assertEquals(0, session.getCurrentQuestionIndex(), "Question index must reset to 0 on retry");
        assertEquals(0, session.getCurrentAttemptScore(), "Attempt score must reset to 0 on retry");
        assertEquals(0, session.getCurrentAttemptCorrect(), "Attempt correct count must reset to 0 on retry");
        assertTrue(session.isVaultUnlocked(1), "Vault 1 must still be unlocked for retry");
    }

    @Test
    @DisplayName("Score persistence: Result must be appended to scores.txt successfully")
    void testScorePersistence() throws IOException {
        ScoreManager sm = new ScoreManager();
        File testScoreFile = new File("data/test_scores.txt");
        if (testScoreFile.exists()) {
            testScoreFile.delete();
        }

        boolean saved = sm.saveScore(session, "TEST_RUN", "data/test_scores.txt");
        assertTrue(saved, "Score should save successfully");
        assertTrue(testScoreFile.exists(), "Target score file must exist");

        String content = Files.readString(testScoreFile.toPath());
        assertTrue(content.contains("TestAgent"), "Score file must contain player callsign");
        assertTrue(content.contains("TEST_RUN"), "Score file must contain outcome");

        // Clean up test file
        testScoreFile.delete();
    }

    @Test
    @DisplayName("QuestionBank handles missing file gracefully with FileNotFoundException")
    void testMissingFileHandling() {
        QuestionBank badBank = new QuestionBank();
        assertThrows(FileNotFoundException.class, () -> {
            badBank.loadFromFile("data/non_existent_file.txt");
        });
    }
}
