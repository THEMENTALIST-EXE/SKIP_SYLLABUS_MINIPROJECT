package com.examheist;

import com.examheist.game.GameSession;
import com.examheist.model.Player;
import com.examheist.model.Question;
import com.examheist.storage.QuestionBank;
import com.examheist.storage.ScoreManager;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * Main JavaFX Application for "SKIP THE SYLLABUS: EXAM HEIST".
 * 
 * Demonstrates:
 * - JavaFX Application lifecycle (init, start, stop)
 * - JavaFX Layouts (BorderPane, VBox, HBox, StackPane)
 * - JavaFX Controls (Label, Button, TextField, RadioButton, ToggleGroup, ProgressBar, Alert)
 * - JavaFX Event Handling (Event-driven programming with lambda expressions)
 * - JavaFX Timeline (Thread-safe animation and countdown without Thread.sleep)
 * - Clean separation between UI Presentation and Core Model/Storage logic
 */
public class MainApp extends Application {

    private Stage primaryStage;
    private BorderPane rootLayout;

    // Core application state
    private QuestionBank questionBank;
    private ScoreManager scoreManager;
    private GameSession gameSession;

    // Active countdown timer
    private Timeline countdownTimeline;
    private int remainingSeconds;
    private int totalVaultSeconds;

    // UI references for countdown updates
    private Label timerLabel;
    private ProgressBar timerBar;

    private static final String QUESTIONS_PATH = "data/questions.txt";

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        this.questionBank = new QuestionBank();
        this.scoreManager = new ScoreManager();

        // Load question bank on startup
        try {
            questionBank.loadFromFile(QUESTIONS_PATH);
        } catch (FileNotFoundException e) {
            showErrorAlert("Question Bank Missing", 
                    "Could not locate questions file at " + QUESTIONS_PATH + ".\nPlease ensure the file exists.");
        } catch (IllegalArgumentException e) {
            showErrorAlert("Question Bank Format Error", e.getMessage());
        }

        rootLayout = new BorderPane();
        rootLayout.setStyle("-fx-background-color: #0b0f19;");

        Scene scene = new Scene(rootLayout, 1100, 720);
        
        // Attach CSS stylesheet if available
        try {
            String cssPath = getClass().getResource("/style.css") != null 
                    ? getClass().getResource("/style.css").toExternalForm() 
                    : null;
            if (cssPath != null) {
                scene.getStylesheets().add(cssPath);
            }
        } catch (Exception e) {
            System.err.println("Notice: Could not load external style.css, default styling applied.");
        }

        primaryStage.setTitle("Skip The Syllabus: Exam Heist");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(950);
        primaryStage.setMinHeight(650);

        // Clean up active timers when window closes
        primaryStage.setOnCloseRequest(e -> stopCountdown());

        // Launch initial welcome screen
        showWelcomeScreen();

        primaryStage.show();
    }

    // =========================================================================
    // SCREEN 1: WELCOME SCREEN
    // =========================================================================
    private void showWelcomeScreen() {
        stopCountdown();

        VBox contentBox = new VBox(20);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setMaxWidth(680);
        contentBox.setPadding(new Insets(30));
        contentBox.getStyleClass().add("cyber-card");

        // Heist Logo / Icon
        Label iconLabel = new Label("⚡ [ SYSTEM LOCKED ] ⚡");
        iconLabel.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 16px; -fx-font-weight: bold; -fx-letter-spacing: 2px;");

        // Title and Tagline
        Label titleLabel = new Label("SKIP THE SYLLABUS: EXAM HEIST");
        titleLabel.getStyleClass().add("title-label");

        Label taglineLabel = new Label("Crack the Questions. Escape the Exam.");
        taglineLabel.getStyleClass().add("tagline-label");

        // Scenario Description
        Label scenarioLabel = new Label(
                "You have been trapped inside the high-security Examination Vault!\n" +
                "To initiate a system override and escape, you must breach three progressively\n" +
                "harder security vaults before the automated lockdown countdown expires.\n\n" +
                "Identify yourself, Agent, to begin the breach sequence."
        );
        scenarioLabel.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 14px; -fx-text-alignment: center; -fx-line-spacing: 4px;");

        // Player Name Input
        VBox inputSection = new VBox(8);
        inputSection.setAlignment(Pos.CENTER_LEFT);
        inputSection.setMaxWidth(400);

        Label inputPrompt = new Label("ENTER AGENT CALLSIGN (PLAYER NAME):");
        inputPrompt.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 12px; -fx-font-weight: bold;");

        TextField nameField = new TextField();
        nameField.setPromptText("e.g., Cipher, Neo, Agent 007");
        nameField.getStyleClass().add("text-field");

        inputSection.getChildren().addAll(inputPrompt, nameField);

        // Start Button
        Button startButton = new Button("INITIALIZE HEIST SEQUENCE →");
        startButton.getStyleClass().add("btn-primary");
        startButton.setMaxWidth(400);

        startButton.setOnAction(e -> {
            String enteredName = nameField.getText().trim();
            if (enteredName.isEmpty()) {
                showWarningAlert("Callsign Required", "Please enter your Agent name before initiating the heist.");
                nameField.requestFocus();
                return;
            }

            // Create Player and GameSession instances
            Player player = new Player(enteredName, "Java");
            gameSession = new GameSession(player);

            showSubjectSelectionScreen();
        });

        // Trigger start on Enter key in text field
        nameField.setOnAction(e -> startButton.fire());

        contentBox.getChildren().addAll(iconLabel, titleLabel, taglineLabel, scenarioLabel, inputSection, startButton);

        StackPane centerContainer = new StackPane(contentBox);
        centerContainer.setPadding(new Insets(20));
        rootLayout.setCenter(centerContainer);
    }

    // =========================================================================
    // SCREEN 2: SUBJECT SELECTION SCREEN
    // =========================================================================
    private void showSubjectSelectionScreen() {
        stopCountdown();

        VBox mainContainer = new VBox(25);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(30));

        // Header
        Label header = new Label("SELECT MISSION TRACK");
        header.getStyleClass().add("section-header");

        Label subHeader = new Label("Choose the technical security architecture to exploit:");
        subHeader.getStyleClass().add("sub-header");

        // Track Cards container
        HBox tracksContainer = new HBox(30);
        tracksContainer.setAlignment(Pos.CENTER);

        // Track 1: Java Programming
        VBox javaCard = createSubjectCard(
                "JAVA PROGRAMMING",
                "OOP Principles, Inheritance, Polymorphism, Collections & Exception Handling",
                "15 Classified Questions Loaded",
                "Java"
        );

        // Track 2: Data Structures
        VBox dsaCard = createSubjectCard(
                "DATA STRUCTURES",
                "Stacks, Queues, Binary Trees, Search Algorithms & Sorting Complexity",
                "15 Classified Questions Loaded",
                "DSA"
        );

        tracksContainer.getChildren().addAll(javaCard, dsaCard);

        // Navigation bar
        HBox navBar = new HBox(20);
        navBar.setAlignment(Pos.CENTER);

        Button backButton = new Button("← BACK TO IDENTIFICATION");
        backButton.getStyleClass().add("btn-secondary");
        backButton.setOnAction(e -> showWelcomeScreen());

        navBar.getChildren().add(backButton);

        mainContainer.getChildren().addAll(header, subHeader, tracksContainer, navBar);
        rootLayout.setCenter(mainContainer);
    }

    private VBox createSubjectCard(String title, String description, String questionCount, String subjectKey) {
        VBox card = new VBox(15);
        card.getStyleClass().add("subject-card");
        card.setAlignment(Pos.TOP_LEFT);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #00f0ff; -fx-font-size: 18px; -fx-font-weight: bold;");

        Label descLabel = new Label(description);
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 13px; -fx-line-spacing: 3px;");

        Label countBadge = new Label(questionCount);
        countBadge.getStyleClass().add("badge");

        Button selectButton = new Button("SELECT TRACK →");
        selectButton.getStyleClass().add("btn-primary");
        selectButton.setMaxWidth(Double.MAX_VALUE);

        selectButton.setOnAction(e -> {
            gameSession.getPlayer().setSelectedSubject(subjectKey);
            showVaultSelectionScreen();
        });

        // Clicking anywhere on card triggers selection
        card.setOnMouseClicked(e -> selectButton.fire());

        card.getChildren().addAll(titleLabel, descLabel, countBadge, selectButton);
        return card;
    }

    // =========================================================================
    // SCREEN 3: VAULT SELECTION SCREEN
    // =========================================================================
    private void showVaultSelectionScreen() {
        stopCountdown();

        VBox mainContainer = new VBox(22);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(20, 30, 20, 30));

        // Top Status Header
        HBox topBar = new HBox(15);
        topBar.setAlignment(Pos.CENTER);

        String agentName = gameSession.getPlayer().getName();
        String subject = gameSession.getPlayer().getSelectedSubject();

        Label agentBadge = new Label("AGENT: " + agentName.toUpperCase());
        agentBadge.getStyleClass().add("badge");

        Label trackBadge = new Label("TRACK: " + (subject.equalsIgnoreCase("Java") ? "JAVA PROGRAMMING" : "DATA STRUCTURES"));
        trackBadge.getStyleClass().add("badge");

        Label scoreBadge = new Label("TOTAL SCORE: " + gameSession.getScore() + " PTS");
        scoreBadge.getStyleClass().add("badge-gold");

        topBar.getChildren().addAll(agentBadge, trackBadge, scoreBadge);

        Label title = new Label("SECURITY VAULT CORRIDOR");
        title.getStyleClass().add("section-header");

        Label subtitle = new Label("Breach each vault in succession. Score at least 3/5 correct to disable vault security.");
        subtitle.getStyleClass().add("sub-header");

        // Vault Cards Row
        HBox vaultRow = new HBox(25);
        vaultRow.setAlignment(Pos.CENTER);

        // Vault 1 Card
        VBox v1Card = createVaultCard(
                1,
                "VAULT 01 — FOUNDATION",
                "Core syntax & fundamental mechanics",
                90,
                10,
                gameSession.isVaultUnlocked(1),
                gameSession.isVaultCleared(1)
        );

        // Vault 2 Card
        VBox v2Card = createVaultCard(
                2,
                "VAULT 02 — INTERMEDIATE",
                "Standard algorithms & object relationships",
                90,
                10,
                gameSession.isVaultUnlocked(2),
                gameSession.isVaultCleared(2)
        );

        // Vault 3 Card
        VBox v3Card = createVaultCard(
                3,
                "VAULT 03 — FINAL ESCAPE",
                "High-stakes escape challenge with +15 bonus points!",
                60,
                15,
                gameSession.isVaultUnlocked(3),
                gameSession.isVaultCleared(3)
        );

        vaultRow.getChildren().addAll(v1Card, v2Card, v3Card);

        // Bottom Controls
        HBox bottomControls = new HBox(20);
        bottomControls.setAlignment(Pos.CENTER);

        Button switchTrackBtn = new Button("← CHANGE MISSION TRACK");
        switchTrackBtn.getStyleClass().add("btn-secondary");
        switchTrackBtn.setOnAction(e -> showSubjectSelectionScreen());

        bottomControls.getChildren().add(switchTrackBtn);

        // If all 3 vaults are cleared, provide a Victory Mission Accomplished button
        if (gameSession.isMissionAccomplished()) {
            Button victoryBtn = new Button("★ VIEW MISSION ESCAPE CERTIFICATE ★");
            victoryBtn.getStyleClass().add("btn-success");
            victoryBtn.setOnAction(e -> showResultsScreen(true, false));
            bottomControls.getChildren().add(victoryBtn);
        }

        mainContainer.getChildren().addAll(topBar, title, subtitle, vaultRow, bottomControls);
        rootLayout.setCenter(mainContainer);
    }

    private VBox createVaultCard(int vaultNum, String title, String description, int seconds, 
                                 int pointsPerQ, boolean unlocked, boolean cleared) {
        VBox card = new VBox(12);
        card.getStyleClass().add("vault-card");
        card.setAlignment(Pos.TOP_LEFT);

        // Apply visual state styling
        if (cleared) {
            card.getStyleClass().add("vault-card-cleared");
        } else if (unlocked) {
            card.getStyleClass().add("vault-card-unlocked");
        } else {
            card.getStyleClass().add("vault-card-locked");
        }

        // Status Badge
        Label statusBadge;
        if (cleared) {
            statusBadge = new Label("✔ COMPLETED");
            statusBadge.setStyle("-fx-background-color: rgba(16, 185, 129, 0.2); -fx-text-fill: #34d399; -fx-padding: 4 10 4 10; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 11px;");
        } else if (unlocked) {
            statusBadge = new Label("🔓 READY TO BREACH");
            statusBadge.setStyle("-fx-background-color: rgba(0, 240, 255, 0.2); -fx-text-fill: #00f0ff; -fx-padding: 4 10 4 10; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 11px;");
        } else {
            statusBadge = new Label("🔒 LOCKED");
            statusBadge.setStyle("-fx-background-color: rgba(100, 116, 139, 0.2); -fx-text-fill: #94a3b8; -fx-padding: 4 10 4 10; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 11px;");
        }

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label descLabel = new Label(description);
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

        // Parameters list
        VBox specs = new VBox(6);
        specs.setStyle("-fx-background-color: #0b0f19; -fx-padding: 10; -fx-background-radius: 8;");

        Label qCountLabel = new Label("• Challenge: 5 Questions");
        qCountLabel.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12px;");

        Label timerSpec = new Label("• Countdown: " + seconds + " Seconds");
        timerSpec.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12px;");

        Label pointsSpec = new Label("• Rewards: +" + pointsPerQ + " / -5 Penalty");
        pointsSpec.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 12px;");

        Label passSpec = new Label("• Clearance: ≥ 3 Correct Answers");
        passSpec.setStyle("-fx-text-fill: #fbbf24; -fx-font-size: 12px;");

        specs.getChildren().addAll(qCountLabel, timerSpec, pointsSpec, passSpec);

        // Action Button
        Button actionBtn;
        if (!unlocked) {
            actionBtn = new Button("SYSTEM LOCKED");
            actionBtn.getStyleClass().add("btn-disabled");
            actionBtn.setDisable(true);
        } else if (cleared) {
            actionBtn = new Button("RE-PLAY VAULT →");
            actionBtn.getStyleClass().add("btn-secondary");
            actionBtn.setOnAction(e -> startVaultQuiz(vaultNum, seconds));
        } else {
            actionBtn = new Button("BREACH VAULT 0" + vaultNum + " →");
            actionBtn.getStyleClass().add("btn-primary");
            actionBtn.setOnAction(e -> startVaultQuiz(vaultNum, seconds));
        }
        actionBtn.setMaxWidth(Double.MAX_VALUE);

        card.getChildren().addAll(statusBadge, titleLabel, descLabel, specs, actionBtn);
        return card;
    }

    private void startVaultQuiz(int vaultNumber, int durationSeconds) {
        String subject = gameSession.getPlayer().getSelectedSubject();
        ArrayList<Question> questions = questionBank.getQuestionsFor(subject, vaultNumber);

        if (questions == null || questions.size() < 5) {
            showErrorAlert("Insufficient Questions", 
                    "Could not find 5 valid questions for Subject: " + subject + " and Vault: " + vaultNumber);
            return;
        }

        // Initialize session attempt
        gameSession.startVault(vaultNumber, questions);
        this.totalVaultSeconds = durationSeconds;
        this.remainingSeconds = durationSeconds;

        showQuizChallengeScreen();
    }

    // =========================================================================
    // SCREEN 4: QUIZ CHALLENGE SCREEN (HEIST HUD)
    // =========================================================================
    private void showQuizChallengeScreen() {
        stopCountdown();

        VBox mainContainer = new VBox(18);
        mainContainer.setAlignment(Pos.TOP_CENTER);
        mainContainer.setPadding(new Insets(20, 40, 20, 40));

        // 1. Top HUD Bar
        HBox hudBar = new HBox(15);
        hudBar.setAlignment(Pos.CENTER_LEFT);

        Label subjectBadge = new Label(gameSession.getPlayer().getSelectedSubject().toUpperCase());
        subjectBadge.getStyleClass().add("badge");

        Label vaultBadge = new Label("VAULT 0" + gameSession.getCurrentVaultNumber());
        vaultBadge.getStyleClass().add("badge");

        Label reqBadge = new Label("PASS: ≥ 3/5 CORRECT");
        reqBadge.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label scoreLiveLabel = new Label("VAULT SCORE: " + gameSession.getCurrentAttemptScore() + " PTS");
        scoreLiveLabel.getStyleClass().add("badge-gold");

        hudBar.getChildren().addAll(subjectBadge, vaultBadge, reqBadge, spacer, scoreLiveLabel);

        // 2. Timer & Progress Bar Section
        VBox timerContainer = new VBox(6);
        timerContainer.setAlignment(Pos.CENTER);

        HBox timerLabelRow = new HBox();
        timerLabelRow.setAlignment(Pos.CENTER_LEFT);

        Label timerTitle = new Label("VAULT COUNTDOWN TIMER");
        timerTitle.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px; -fx-font-weight: bold;");

        Region timerSpacer = new Region();
        HBox.setHgrow(timerSpacer, Priority.ALWAYS);

        timerLabel = new Label("TIME REMAINING: " + remainingSeconds + "s");
        timerLabel.getStyleClass().add("timer-label");

        timerLabelRow.getChildren().addAll(timerTitle, timerSpacer, timerLabel);

        timerBar = new ProgressBar(1.0);
        timerBar.setMaxWidth(Double.MAX_VALUE);
        timerBar.setPrefHeight(10);
        timerBar.getStyleClass().add("progress-bar");

        timerContainer.getChildren().addAll(timerLabelRow, timerBar);

        // 3. Question Card
        Question currentQuestion = gameSession.getCurrentQuestion();
        int qNum = gameSession.getCurrentQuestionIndex() + 1;
        int totalQ = gameSession.getTotalQuestionsInCurrentVault();

        VBox questionCard = new VBox(16);
        questionCard.getStyleClass().add("cyber-card");
        questionCard.setAlignment(Pos.TOP_LEFT);

        // Question header
        HBox qHeaderRow = new HBox(12);
        qHeaderRow.setAlignment(Pos.CENTER_LEFT);

        Label qNumLabel = new Label("SECURITY NODE " + qNum + " OF " + totalQ);
        qNumLabel.setStyle("-fx-text-fill: #00f0ff; -fx-font-size: 13px; -fx-font-weight: bold;");

        Label pointsTag = new Label("+" + currentQuestion.getPoints() + " PTS ON SUCCESS");
        pointsTag.setStyle("-fx-text-fill: #34d399; -fx-font-size: 11px; -fx-font-weight: bold;");

        qHeaderRow.getChildren().addAll(qNumLabel, pointsTag);

        Label questionText = new Label(currentQuestion.getQuestionText());
        questionText.setWrapText(true);
        questionText.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 18px; -fx-font-weight: bold; -fx-line-spacing: 4px;");

        // Options Radio Buttons
        ToggleGroup group = new ToggleGroup();
        VBox optionsBox = new VBox(10);

        String[] options = currentQuestion.getOptions();
        char prefix = 'A';
        for (int i = 0; i < options.length; i++) {
            RadioButton rb = new RadioButton("(" + (char)(prefix + i) + ")  " + options[i]);
            rb.setUserData(i + 1); // 1-based index (1 to 4)
            rb.setToggleGroup(group);
            rb.setMaxWidth(Double.MAX_VALUE);
            rb.getStyleClass().add("option-tile");
            optionsBox.getChildren().add(rb);
        }

        // Feedback Banner (initially hidden)
        VBox feedbackBanner = new VBox(4);
        feedbackBanner.setVisible(false);
        feedbackBanner.setManaged(false);

        // Submit & Next Buttons
        HBox buttonRow = new HBox(15);
        buttonRow.setAlignment(Pos.CENTER_RIGHT);

        Button submitBtn = new Button("SUBMIT ANSWER →");
        submitBtn.getStyleClass().add("btn-primary");

        Button nextBtn = new Button(gameSession.hasMoreQuestions() ? "NEXT NODE →" : "FINALIZE VAULT ATTEMPT →");
        nextBtn.getStyleClass().add("btn-primary");
        nextBtn.setVisible(false);
        nextBtn.setManaged(false);

        submitBtn.setOnAction(e -> {
            if (group.getSelectedToggle() == null) {
                showWarningAlert("No Selection", "Please select an answer choice before submitting.");
                return;
            }

            // Disable radio buttons to prevent duplicate submissions
            for (javafx.scene.Node node : optionsBox.getChildren()) {
                node.setDisable(true);
            }
            submitBtn.setVisible(false);
            submitBtn.setManaged(false);

            int chosenIndex = (int) group.getSelectedToggle().getUserData();
            boolean correct = gameSession.submitAnswer(chosenIndex);

            // Update live score display
            scoreLiveLabel.setText("VAULT SCORE: " + gameSession.getCurrentAttemptScore() + " PTS");

            // Display Feedback banner
            feedbackBanner.getChildren().clear();
            if (correct) {
                feedbackBanner.getStyleClass().setAll("feedback-success");
                Label msg = new Label("✔ ACCESS GRANTED! +" + currentQuestion.getPoints() + " POINTS");
                msg.getStyleClass().add("feedback-success-text");
                feedbackBanner.getChildren().add(msg);
            } else {
                feedbackBanner.getStyleClass().setAll("feedback-error");
                Label msg = new Label("✖ ACCESS DENIED! -5 POINTS PENALTY");
                msg.getStyleClass().add("feedback-error-text");
                Label detail = new Label("Correct Bypass Key: (" + (char)('A' + currentQuestion.getCorrectAnswerIndex() - 1) + ") " + currentQuestion.getCorrectAnswerText());
                detail.getStyleClass().add("feedback-detail-text");
                feedbackBanner.getChildren().addAll(msg, detail);
            }
            feedbackBanner.setVisible(true);
            feedbackBanner.setManaged(true);

            // Show Next Button
            nextBtn.setVisible(true);
            nextBtn.setManaged(true);
        });

        nextBtn.setOnAction(e -> {
            if (gameSession.hasMoreQuestions()) {
                gameSession.nextQuestion();
                showQuizChallengeScreen();
            } else {
                // Completed all 5 questions
                stopCountdown();
                boolean passed = gameSession.finalizeVaultAttempt();
                showResultsScreen(passed, false);
            }
        });

        buttonRow.getChildren().addAll(submitBtn, nextBtn);
        questionCard.getChildren().addAll(qHeaderRow, questionText, optionsBox, feedbackBanner, buttonRow);

        mainContainer.getChildren().addAll(hudBar, timerContainer, questionCard);
        rootLayout.setCenter(mainContainer);

        // Start Countdown Timer
        startCountdown();
    }

    // =========================================================================
    // COUNTDOWN TIMER LOGIC (Timeline)
    // =========================================================================
    private void startCountdown() {
        stopCountdown();

        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            remainingSeconds--;

            // Update timer visuals
            if (timerLabel != null) {
                timerLabel.setText("TIME REMAINING: " + remainingSeconds + "s");
                if (remainingSeconds <= 15) {
                    timerLabel.getStyleClass().setAll("timer-label-warning");
                    if (timerBar != null) {
                        timerBar.getStyleClass().setAll("progress-bar", "progress-bar-warning");
                    }
                }
            }

            if (timerBar != null && totalVaultSeconds > 0) {
                double progress = (double) remainingSeconds / (double) totalVaultSeconds;
                timerBar.setProgress(Math.max(0.0, progress));
            }

            // Check if countdown expired
            if (remainingSeconds <= 0) {
                stopCountdown();
                handleVaultTimeout();
            }
        }));

        countdownTimeline.setCycleCount(Timeline.INDEFINITE);
        countdownTimeline.play();
    }

    private void stopCountdown() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
            countdownTimeline = null;
        }
    }

    private void handleVaultTimeout() {
        // Vault failed due to timer expiry
        gameSession.finalizeVaultAttempt();
        showResultsScreen(false, true);
    }

    // =========================================================================
    // SCREEN 5: RESULTS SCREEN
    // =========================================================================
    private void showResultsScreen(boolean passed, boolean timeout) {
        stopCountdown();

        VBox contentBox = new VBox(20);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setMaxWidth(680);
        contentBox.setPadding(new Insets(30));
        contentBox.getStyleClass().add("cyber-card");

        // Determine outcome status & banner styling
        boolean gameWon = gameSession.isMissionAccomplished();
        String outcomeCode = gameWon ? "ESCAPED" : (timeout ? "CAPTURED (TIMEOUT)" : (passed ? "VAULT CLEARED" : "CAPTURED (FAILED)"));

        // Persist score to file
        boolean saved = scoreManager.saveScore(gameSession, outcomeCode);

        Label bannerLabel;
        Label subBannerLabel;

        if (gameWon) {
            bannerLabel = new Label("★ MISSION ACCOMPLISHED: EXAM ESCAPED! ★");
            bannerLabel.setStyle("-fx-text-fill: #34d399; -fx-font-size: 24px; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, rgba(52, 211, 153, 0.5), 12, 0.4, 0, 0);");
            subBannerLabel = new Label("Congratulations Agent! You bypassed all 3 security vaults and breached the exam.");
            subBannerLabel.setStyle("-fx-text-fill: #a7f3d0; -fx-font-size: 14px;");
        } else if (timeout) {
            bannerLabel = new Label("⚠ SECURITY LOCKDOWN: TIME EXPIRED ⚠");
            bannerLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 24px; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, rgba(239, 68, 68, 0.5), 12, 0.4, 0, 0);");
            subBannerLabel = new Label("Automated vault security locked the chamber. Attempt failed.");
            subBannerLabel.setStyle("-fx-text-fill: #fca5a5; -fx-font-size: 14px;");
        } else if (passed) {
            bannerLabel = new Label("✔ VAULT 0" + gameSession.getCurrentVaultNumber() + " SECURITY OVERRIDDEN");
            bannerLabel.setStyle("-fx-text-fill: #00f0ff; -fx-font-size: 24px; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, rgba(0, 240, 255, 0.5), 12, 0.4, 0, 0);");
            subBannerLabel = new Label("Passed required clearance threshold! The next corridor is now accessible.");
            subBannerLabel.setStyle("-fx-text-fill: #bae6fd; -fx-font-size: 14px;");
        } else {
            bannerLabel = new Label("✖ BREACH ATTEMPT FAILED ✖");
            bannerLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 24px; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, rgba(239, 68, 68, 0.5), 12, 0.4, 0, 0);");
            subBannerLabel = new Label("Threshold not reached. At least 3 correct answers are required to clear this vault.");
            subBannerLabel.setStyle("-fx-text-fill: #fca5a5; -fx-font-size: 14px;");
        }

        // Dossier Card
        VBox dossier = new VBox(10);
        dossier.setStyle("-fx-background-color: #0b0f19; -fx-padding: 18; -fx-background-radius: 8; -fx-border-color: #1e293b; -fx-border-radius: 8;");
        dossier.setAlignment(Pos.CENTER_LEFT);

        Label dossierTitle = new Label("AGENT PERFORMANCE DOSSIER");
        dossierTitle.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 13px; -fx-font-weight: bold;");

        HBox r1 = createDossierRow("Agent Callsign:", gameSession.getPlayer().getName());
        HBox r2 = createDossierRow("Mission Track:", gameSession.getPlayer().getSelectedSubject().equalsIgnoreCase("Java") ? "Java Programming" : "Data Structures");
        HBox r3 = createDossierRow("Current Vault Score:", gameSession.getCurrentAttemptScore() + " Points");
        HBox r4 = createDossierRow("Total Heist Score:", gameSession.getScore() + " Points");
        HBox r5 = createDossierRow("Vault Correct Answers:", gameSession.getCurrentAttemptCorrect() + " / 5 (Threshold: 3)");
        HBox r6 = createDossierRow("Vaults Overridden:", gameSession.getVaultCountCleared() + " / 3 Vaults");

        String saveStatus = saved ? "Record saved to data/scores.txt" : "Could not write to data/scores.txt";
        Label saveLabel = new Label("• File Storage: " + saveStatus);
        saveLabel.setStyle("-fx-text-fill: " + (saved ? "#34d399" : "#f87171") + "; -fx-font-size: 12px;");

        dossier.getChildren().addAll(dossierTitle, r1, r2, r3, r4, r5, r6, saveLabel);

        // Action Buttons Row
        HBox actionsRow = new HBox(15);
        actionsRow.setAlignment(Pos.CENTER);

        // If vault failed, show Retry button
        if (!passed && !gameWon) {
            Button retryBtn = new Button("↻ RETRY VAULT 0" + gameSession.getCurrentVaultNumber());
            retryBtn.getStyleClass().add("btn-primary");
            retryBtn.setOnAction(e -> {
                gameSession.retryCurrentVault();
                startVaultQuiz(gameSession.getCurrentVaultNumber(), 
                        gameSession.getCurrentVaultNumber() == 3 ? 60 : 90);
            });
            actionsRow.getChildren().add(retryBtn);
        }

        // If passed and next vault is available
        if (passed && !gameWon && gameSession.getCurrentVaultNumber() < 3) {
            int nextVault = gameSession.getCurrentVaultNumber() + 1;
            Button nextVaultBtn = new Button("PROCEED TO VAULT 0" + nextVault + " →");
            nextVaultBtn.getStyleClass().add("btn-primary");
            nextVaultBtn.setOnAction(e -> {
                startVaultQuiz(nextVault, nextVault == 3 ? 60 : 90);
            });
            actionsRow.getChildren().add(nextVaultBtn);
        }

        // Vault Corridor Map button
        Button mapBtn = new Button("VAULT MAP");
        mapBtn.getStyleClass().add("btn-secondary");
        mapBtn.setOnAction(e -> showVaultSelectionScreen());

        // Main Menu button
        Button menuBtn = new Button("MAIN MENU");
        menuBtn.getStyleClass().add("btn-secondary");
        menuBtn.setOnAction(e -> showWelcomeScreen());

        actionsRow.getChildren().addAll(mapBtn, menuBtn);

        contentBox.getChildren().addAll(bannerLabel, subBannerLabel, dossier, actionsRow);

        StackPane centerContainer = new StackPane(contentBox);
        centerContainer.setPadding(new Insets(20));
        rootLayout.setCenter(centerContainer);
    }

    private HBox createDossierRow(String label, String value) {
        HBox row = new HBox();
        Label l = new Label(label);
        l.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px; -fx-min-width: 180;");
        Label v = new Label(value);
        v.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px; -fx-font-weight: bold;");
        row.getChildren().addAll(l, v);
        return row;
    }

    // =========================================================================
    // ALERT DIALOG HELPERS
    // =========================================================================
    private void showWarningAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Exam Heist — Warning");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Exam Heist — Error");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
