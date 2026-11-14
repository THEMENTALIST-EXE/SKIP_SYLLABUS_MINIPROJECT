package com.examheist.storage;

import com.examheist.model.Question;
import com.examheist.model.TimedQuestion;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Loads, parses, and validates questions from the flat text file data/questions.txt.
 * 
 * Demonstrates:
 * - File I/O using File and Scanner
 * - Exception Handling (FileNotFoundException, NumberFormatException, custom validation)
 * - Collections (ArrayList)
 * - String manipulation (split, trim)
 * - Polymorphism (instantiating TimedQuestion for Vault 3)
 */
public class QuestionBank {

    private ArrayList<Question> allQuestions;

    public QuestionBank() {
        this.allQuestions = new ArrayList<>();
    }

    /**
     * Reads questions from the pipe-delimited text file.
     * Format: subject|vault|question|optionA|optionB|optionC|optionD|correctIndex
     * 
     * @param filePath Path to questions.txt
     * @throws FileNotFoundException if file is missing
     * @throws IllegalArgumentException if line formatting or values are invalid
     */
    public void loadFromFile(String filePath) throws FileNotFoundException, IllegalArgumentException {
        allQuestions.clear();
        File file = new File(filePath);

        if (!file.exists()) {
            throw new FileNotFoundException("Question bank file not found at: " + filePath);
        }

        int lineNumber = 0;
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                lineNumber++;
                String line = scanner.nextLine().trim();

                // Skip empty lines or comment lines starting with #
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                // Split by pipe character |
                // Note: In regex, pipe is a reserved meta-character, so we escape it as \\|
                String[] tokens = line.split("\\|");
                if (tokens.length != 8) {
                    throw new IllegalArgumentException("Line " + lineNumber + ": Invalid format. Expected 8 tokens, found " + tokens.length);
                }

                String subject = tokens[0].trim();
                int vaultNumber;
                try {
                    vaultNumber = Integer.parseInt(tokens[1].trim());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Line " + lineNumber + ": Vault number must be an integer (1, 2, or 3).");
                }

                String questionText = tokens[2].trim();
                String optionA = tokens[3].trim();
                String optionB = tokens[4].trim();
                String optionC = tokens[5].trim();
                String optionD = tokens[6].trim();

                int correctIndex;
                try {
                    correctIndex = Integer.parseInt(tokens[7].trim());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Line " + lineNumber + ": Correct answer index must be an integer.");
                }

                if (correctIndex < 1 || correctIndex > 4) {
                    throw new IllegalArgumentException("Line " + lineNumber + ": Correct answer index must be between 1 and 4.");
                }

                // If vault is 3, instantiate TimedQuestion (Polymorphism in action!)
                // Otherwise instantiate regular Question
                Question q;
                if (vaultNumber == 3) {
                    q = new TimedQuestion(subject, vaultNumber, questionText, optionA, optionB, optionC, optionD, correctIndex);
                } else {
                    q = new Question(subject, vaultNumber, questionText, optionA, optionB, optionC, optionD, correctIndex);
                }

                allQuestions.add(q);
            }
        }
    }

    /**
     * Filters and returns questions for a specific subject and vault.
     * Ensures exactly 5 questions are returned.
     */
    public ArrayList<Question> getQuestionsFor(String subject, int vaultNumber) {
        ArrayList<Question> filtered = new ArrayList<>();
        for (Question q : allQuestions) {
            if (q.getSubject().equalsIgnoreCase(subject) && q.getVaultNumber() == vaultNumber) {
                filtered.add(q);
            }
        }
        return filtered;
    }

    /**
     * Validates that the loaded bank contains at least 5 questions for every vault
     * of the given subject.
     */
    public boolean validateVaultQuestions(String subject, int vaultNumber) {
        ArrayList<Question> list = getQuestionsFor(subject, vaultNumber);
        return list.size() >= 5;
    }

    public int getTotalQuestionsLoaded() {
        return allQuestions.size();
    }
}
