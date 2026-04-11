package typeTutor.controller;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Timer;

import typeTutor.model.GameSession;
import typeTutor.model.TypingStats;
import typeTutor.view.GameStatsPanel;
import typeTutor.view.MainFrame;
import typeTutor.view.NavsPanel;
import typeTutor.view.TypingPanel;

/**
 * Main presenter/controller in MVP.
 * Owns all interaction logic between model and views.
 */
public class MainController {
    // References to the views that render data and emit UI events.
    private final MainFrame mainFrame;
    private final TypingPanel typingPanel;
    private final NavsPanel navsPanel;
    private final GameStatsPanel statsPanel;

    // Core model for game rules and progress.
    private final GameSession gameSession;

    // Timer used to refresh countdown independently from key presses.
    private final Timer countdownTimer;

    // Controller-managed render buffers for currently visible triplet text.
    private List<String> currentRows;
    private String currentTargetText;
    private char[] visibleChars;
    private int[] stateByChar;

    /**
     * Creates controller, binds events, and initializes first session view.
     */
    public MainController(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.typingPanel = mainFrame.getTypingPanel();
        this.navsPanel = mainFrame.getNavPanel();
        this.statsPanel = mainFrame.getStatsPanel();
        this.gameSession = new GameSession();

        this.currentRows = new ArrayList<>();
        this.currentTargetText = "";
        this.visibleChars = new char[0];
        this.stateByChar = new int[0];
        this.countdownTimer = new Timer(100, e -> onCountdownTick());

        bindViewEvents();
        loadCurrentTripletFromSession();
        typingPanel.setTimerSeconds(gameSession.getRemainingSeconds());
        statsPanel.showWaitingState();
    }

    /**
     * Exposes managed frame for bootstrap code.
     */
    public MainFrame getMainFrame() {
        return mainFrame;
    }

    /**
     * Subscribes to mode and typing events emitted by views.
     */
    private void bindViewEvents() {
        navsPanel.setModeChangeListener((wordMode, language, timeMode) -> onModesChanged(wordMode, language, timeMode));

        typingPanel.setInputListener(new TypingPanel.InputListener() {
            @Override
            public void onCharacterTyped(char value) {
                onCharacterTypedByUser(value);
            }

            @Override
            public void onBackspace() {
                onBackspacePressed();
            }

            @Override
            public void onTypingAreaFocused() {
                typingPanel.focusTypingArea();
            }
        });
    }

    /**
     * Handles navbar mode changes and restarts session using selected options.
     */
    private void onModesChanged(String wordMode, String language, String timeMode) {
        countdownTimer.stop();
        gameSession.applyNavbarOptions(wordMode, language, timeMode);
        loadCurrentTripletFromSession();
        typingPanel.setTimerSeconds(gameSession.getRemainingSeconds());
        statsPanel.showWaitingState();
        typingPanel.focusTypingArea();
    }

    /**
     * Handles typed characters and advances game state.
     */
    private void onCharacterTypedByUser(char typedChar) {
        GameSession.InputResult result = gameSession.processTypedCharacter(typedChar);
        if (result.isGameStopped()) {
            finishSessionAndReset();
            return;
        }

        if (!countdownTimer.isRunning()) {
            countdownTimer.start();
        }

        if (result.isTripletAdvanced()) {
            loadCurrentTripletFromSession();
            typingPanel.setTimerSeconds(gameSession.getRemainingSeconds());
            return;
        }

        int index = result.getIndex();
        if (index >= 0 && index < visibleChars.length) {
            visibleChars[index] = result.getTypedChar();
            stateByChar[index] = result.isCorrect() ? TypingPanel.CORRECT_STATE : TypingPanel.WRONG_STATE;
        }

        renderTypingState();

        if (!gameSession.isGameRunning()) {
            finishSessionAndReset();
        }
    }

    /**
     * Handles backspace and restores previous source character in rendered text.
     */
    private void onBackspacePressed() {
        GameSession.InputResult result = gameSession.processBackspace();
        if (result.isGameStopped()) {
            return;
        }

        int index = result.getIndex();
        if (index >= 0 && index < visibleChars.length) {
            visibleChars[index] = currentTargetText.charAt(index);
            stateByChar[index] = TypingPanel.DEFAULT_STATE;
        }

        renderTypingState();
    }

    /**
     * Updates timer display; finalizes session when countdown reaches zero.
     */
    private void onCountdownTick() {
        typingPanel.setTimerSeconds(gameSession.getRemainingSeconds());
        if (!gameSession.isGameRunning()) {
            countdownTimer.stop();
            finishSessionAndReset();
        }
    }

    /**
     * Captures final stats, displays them, then starts a fresh session with same modes.
     */
    private void finishSessionAndReset() {
        countdownTimer.stop();
        TypingStats finalStats = gameSession.getTypingStats();
        statsPanel.updateStats(finalStats);

        gameSession.resetForCurrentOptions();
        loadCurrentTripletFromSession();
        typingPanel.setTimerSeconds(gameSession.getRemainingSeconds());
        typingPanel.focusTypingArea();
    }

    /**
     * Loads active triplet from model and resets render buffers.
     */
    private void loadCurrentTripletFromSession() {
        List<String> rows = gameSession.getCurrentTripletRows();
        currentRows = new ArrayList<>(TypingPanel.ROW_COUNT);
        for (int i = 0; i < TypingPanel.ROW_COUNT; i++) {
            currentRows.add(i < rows.size() ? rows.get(i) : "");
        }

        currentTargetText = gameSession.getCurrentTargetText();
        visibleChars = currentTargetText.toCharArray();
        stateByChar = new int[currentTargetText.length()];
        renderTypingState();
    }

    /**
     * Pushes current controller-managed display state into typing view.
     */
    private void renderTypingState() {
        typingPanel.renderState(
                currentRows,
                currentTargetText,
                visibleChars,
                stateByChar,
                gameSession.getCursorIndex(),
                gameSession.isGameRunning());
        typingPanel.setTimerSeconds(gameSession.getRemainingSeconds());
    }
}
