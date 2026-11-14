package com.examheist.storage;

import com.examheist.game.GameSession;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Manages persisting heist results to a local flat text file data/scores.txt.
 * 
 * Demonstrates:
 * - File and Directory Creation
 * - Character Streams (FileWriter in append mode)
 * - Exception Handling (IOException, try-catch-finally or try-with-resources)
 */
public class ScoreManager {

    private static final String DEFAULT_SCORE_FILE = "data/scores.txt";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Appends a completed or attempted heist record to scores.txt.
     * 
     * @param session The active game session containing player stats
     * @param outcome "ESCAPED" or "CAPTURED"
     * @return true if successfully saved, false if an error occurred
     */
    public boolean saveScore(GameSession session, String outcome) {
        return saveScore(session, outcome, DEFAULT_SCORE_FILE);
    }

    public boolean saveScore(GameSession session, String outcome, String targetFilePath) {
        if (session == null) {
            return false;
        }

        File file = new File(targetFilePath);

        // Ensure parent directory (data/) exists
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        // Use FileWriter with append = true so past records are never overwritten
        try (FileWriter fw = new FileWriter(file, true);
             PrintWriter pw = new PrintWriter(fw)) {

            String timestamp = LocalDateTime.now().format(FORMATTER);
            String playerName = session.getPlayer().getName();
            String subject = session.getPlayer().getSelectedSubject();
            int score = session.getScore();
            int correct = session.getTotalCorrectAnswers();
            int incorrect = session.getTotalIncorrectAnswers();
            int vaultsCleared = session.getVaultCountCleared();

            // Format: Timestamp | Player | Subject | Score | Correct | Incorrect | Vaults | Outcome
            String record = String.format("%s | %-12s | %-16s | Score: %-4d | Correct: %-2d | Wrong: %-2d | Vaults: %d/3 | %s",
                    timestamp, playerName, subject, score, correct, incorrect, vaultsCleared, outcome);

            pw.println(record);
            return true;

        } catch (IOException e) {
            System.err.println("Error saving score to " + targetFilePath + ": " + e.getMessage());
            return false;
        }
    }
}
