package typeTutor.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.swing.Timer;

/**
 * Core game model.
 * Contains typing rules, timer lifecycle, triplet progression, and raw counters.
 */
public class GameSession {
    private static final int STATE_DEFAULT = 0;
    private static final int STATE_CORRECT = 1;
    private static final int STATE_WRONG = 2;
    // Default mode values used for initial app launch.
    public static final String DEFAULT_WORD_MODE = "Words";
    public static final String DEFAULT_LANGUAGE = "Eng";
    public static final String DEFAULT_TIME_MODE = "60s";

    // Dependency that generates text rows based on selected modes.
    private final TextGenerator textGenerator;

    // Active mode selections for current and future resets.
    private String wordMode;
    private String language;
    private String timeMode;

    // Timer/runtime state flags and base duration.
    private boolean gameRunning;
    private boolean timerStarted;
    private boolean timerPaused;
    private int totalSeconds;
    private int remainingSeconds;
    private final Timer sessionTimer;

    // Generated line stream and current active line index.
    private List<String> generatedLines;
    private int currentLineIndex;

    // Quotes mode state: one quote distributed across 3 rows.
    private List<String> quotePool;
    private int quoteIndex;
    private List<String> quoteRows;

    // Active target text and typed progress counters.
    private String currentTargetText;
    private final List<CharacterState> typedCharacters;
    private int cursorIndex;
    private int correctCharacters;
    private int wrongCharacters;
    private int completedWords;
    private int completedLines;
    private char[] lastCompletedTypedChars;
    private int[] lastCompletedCharStates;

    /**
     * Creates session with default modes and generated content.
     */
    public GameSession() {
        this.textGenerator = new TextGenerator();
        this.wordMode = DEFAULT_WORD_MODE;
        this.language = DEFAULT_LANGUAGE;
        this.timeMode = DEFAULT_TIME_MODE;
        this.sessionTimer = new Timer(1000, e -> onSessionTimerTick());

        this.generatedLines = Collections.emptyList();
        this.quotePool = Collections.emptyList();
        this.quoteRows = List.of("", "", "");
        this.typedCharacters = new ArrayList<>();
        resetForCurrentOptions();
    }

    /**
     * Updates word mode with normalization.
     */
    public void setWordMode(String wordMode) {
        this.wordMode = normalizeWordMode(wordMode);
    }

    /**
     * Updates language mode with normalization.
     */
    public void setLanguage(String language) {
        this.language = normalizeLanguage(language);
    }

    /**
     * Updates time mode with normalization.
     */
    public void setTimeMode(String timeMode) {
        this.timeMode = normalizeTimeMode(timeMode);
    }

    /**
     * Returns selected word mode.
     */
    public String getWordMode() {
        return wordMode;
    }

    /**
     * Returns selected language mode.
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Returns selected time mode.
     */
    public String getTimeMode() {
        return timeMode;
    }

    /**
     * Applies navbar selections and fully resets game state.
     */
    public void applyNavbarOptions(String wordMode, String language, String timeMode) {
        setWordMode(wordMode);
        setLanguage(language);
        setTimeMode(timeMode);
        resetForCurrentOptions();
    }

    /**
     * Regenerates text and clears progress counters for a fresh run.
     */
    public void resetForCurrentOptions() {
        this.totalSeconds = parseTimeModeSeconds(timeMode);
        List<String> flattened = flattenTriplets(textGenerator.generateTriplets(wordMode, language, totalSeconds));
        boolean quotesMode = isQuotesMode();
        if (quotesMode) {
            this.quotePool = flattened;
            this.quoteIndex = 0;
            setNextQuoteFromPool();
            this.generatedLines = Collections.emptyList();
            this.currentLineIndex = 0;
        } else {
            this.generatedLines = flattened;
            this.currentLineIndex = 0;
            this.quotePool = Collections.emptyList();
            this.quoteIndex = 0;
            this.quoteRows = List.of("", "", "");
            ensureLineBuffer();
            this.currentTargetText = getActiveLine();
        }
        this.lastCompletedTypedChars = null;
        this.lastCompletedCharStates = null;

        this.gameRunning = true;
        this.timerStarted = false;
        this.timerPaused = false;
        this.remainingSeconds = totalSeconds;
        this.sessionTimer.stop();

        this.typedCharacters.clear();
        this.cursorIndex = 0;
        this.correctCharacters = 0;
        this.wrongCharacters = 0;
        this.completedWords = 0;
        this.completedLines = 0;
    }

    /**
     * Returns remaining countdown seconds.
     */
    public int getRemainingSeconds() {
        return Math.max(remainingSeconds, 0);
    }

    /**
     * Returns whether game is still active; expires automatically at zero time.
     */
    public boolean isGameRunning() {
        if (!gameRunning) {
            return false;
        }

        if (getRemainingSeconds() <= 0) {
            gameRunning = false;
        }
        return gameRunning;
    }

    /**
     * Returns current 3-row triplet from generated session data.
     */
    public List<String> getCurrentTripletRows() {
        if (isQuotesMode()) {
            return new ArrayList<>(quoteRows);
        }

        ensureLineBuffer();
        if (generatedLines.isEmpty() || currentLineIndex >= generatedLines.size()) {
            return Collections.emptyList();
        }

        String active = getActiveLine();
        String next = getNextLine();
        List<String> rows = new ArrayList<>(3);
        rows.add("");
        rows.add(active);
        rows.add(next);
        return rows;
    }

    /**
     * Returns flattened target text for active triplet.
     */
    public String getCurrentTargetText() {
        return currentTargetText;
    }

    /**
     * Returns the last completed line's typed characters for rendering.
     */
    public char[] getLastCompletedTypedChars() {
        return lastCompletedTypedChars == null ? null : lastCompletedTypedChars.clone();
    }

    /**
     * Returns the last completed line's per-character states for rendering.
     */
    public int[] getLastCompletedCharStates() {
        return lastCompletedCharStates == null ? null : lastCompletedCharStates.clone();
    }

    /**
     * Returns current typing cursor position.
     */
    public int getCursorIndex() {
        return cursorIndex;
    }

    /**
     * Returns accumulated correct character count.
     */
    public int getCorrectCharacters() {
        return correctCharacters;
    }

    /**
     * Returns accumulated wrong character count.
     */
    public int getWrongCharacters() {
        return wrongCharacters;
    }

    /**
     * Returns number of completed words.
     */
    public int getCompletedWords() {
        return completedWords;
    }

    /**
     * Returns number of completed lines.
     */
    public int getCompletedLines() {
        return completedLines;
    }

    /**
     * Applies one typed character according to game rules.
     */
    public InputResult processTypedCharacter(char typedChar) {
        if (!isGameRunning()) {
            return InputResult.gameStopped();
        }

        if (!timerStarted) {
            timerStarted = true;
            timerPaused = false;
            sessionTimer.start();
        }

        if (currentTargetText.isEmpty()) {
            gameRunning = false;
            return InputResult.gameStopped();
        }

        char expectedChar = currentTargetText.charAt(cursorIndex);
        boolean correct = (typedChar == expectedChar);
        if (correct) {
            correctCharacters++;
        } else {
            wrongCharacters++;
        }

        typedCharacters.add(new CharacterState(typedChar, correct));
        int previousIndex = cursorIndex;
        cursorIndex++;

        boolean advancedTriplet = false;
        if (cursorIndex >= currentTargetText.length()) {
            advancedTriplet = isQuotesMode()
                    ? registerCompletedQuoteAndAdvance()
                    : registerCompletedLineAndAdvance();
        }

        return new InputResult(previousIndex, typedChar, expectedChar, correct, false, false, advancedTriplet);
    }

    /**
     * Processes backspace and reverts cursor/last typed slot.
     * Wrong count intentionally does not decrease after undo, as requested.
     */
    public InputResult processBackspace() {
        if (!isGameRunning() || cursorIndex <= 0 || typedCharacters.isEmpty()) {
            return InputResult.noOpBackspace();
        }

        cursorIndex--;
        CharacterState removed = typedCharacters.remove(typedCharacters.size() - 1);
        if (removed.correct) {
            correctCharacters = Math.max(0, correctCharacters - 1);
        }

        return new InputResult(cursorIndex, '\0', currentTargetText.charAt(cursorIndex), false, true, false, false);
    }

    /**
     * Produces immutable stats snapshot for UI.
     */
    public TypingStats getTypingStats() {
        return new TypingStats(
                calculateWpm(),
                correctCharacters,
                wrongCharacters,
                completedWords,
                completedLines,
                timeMode,
                language,
                wordMode);
    }

    /**
     * Pauses the active countdown without resetting progress.
     */
    public void pauseTimer() {
        if (!timerStarted || timerPaused || !gameRunning) {
            return;
        }

        timerPaused = true;
        sessionTimer.stop();
    }

    /**
     * Resumes a previously paused countdown.
     */
    public void resumeTimer() {
        if (!timerPaused) {
            return;
        }

        timerPaused = false;
        if (gameRunning && remainingSeconds > 0) {
            sessionTimer.start();
        }
    }

    /**
     * Calculates WPM using conventional 5 chars per word.
     */
    private double calculateWpm() {
        if (!timerStarted) {
            return 0.0;
        }

        int elapsedSeconds = totalSeconds - getRemainingSeconds();
        if (elapsedSeconds <= 0) {
            return 0.0;
        }

        double elapsedMinutes = elapsedSeconds / 60.0;
        return (correctCharacters / 5.0) / elapsedMinutes;
    }

    /**
     * Converts generated triplets into one linear list of rows.
     */
    private List<String> flattenTriplets(List<List<String>> triplets) {
        List<String> lines = new ArrayList<>();
        for (List<String> triplet : triplets) {
            lines.addAll(triplet);
        }
        return lines;
    }

    /**
     * Appends more generated lines when the visible window is about to run out.
     */
    private void ensureLineBuffer() {
        int requiredIndex = currentLineIndex + 1;
        while (requiredIndex >= generatedLines.size()) {
            generatedLines.addAll(flattenTriplets(textGenerator.generateTriplets(wordMode, language, totalSeconds)));
        }
    }

    /**
     * Returns the currently active line.
     */
    private String getActiveLine() {
        if (generatedLines.isEmpty() || currentLineIndex >= generatedLines.size()) {
            return "";
        }
        return generatedLines.get(currentLineIndex);
    }

    /**
     * Returns the next line below the active line.
     */
    private String getNextLine() {
        int index = currentLineIndex + 1;
        if (generatedLines.isEmpty() || index < 0 || index >= generatedLines.size()) {
            return "";
        }
        return generatedLines.get(index);
    }

    /**
     * Registers a completed line and shifts the active line window upward by one.
     */
    private boolean registerCompletedLineAndAdvance() {
        String completedLineText = getActiveLine();
        if (completedLineText == null) {
            return false;
        }

        completedLines++;
        completedWords += countWords(completedLineText);
        captureCompletedLineSnapshot(completedLineText);

        currentLineIndex += 1;
        ensureLineBuffer();
        currentTargetText = getActiveLine();
        typedCharacters.clear();
        cursorIndex = 0;
        return true;
    }

    private boolean registerCompletedQuoteAndAdvance() {
        // Count the whole quote as 3 visible rows worth of progress.
        String quoteText = String.join(" ", quoteRows).trim();
        completedWords += countWords(quoteText);
        completedLines += 3;
        lastCompletedTypedChars = null;
        lastCompletedCharStates = null;

        quoteIndex += 1;
        setNextQuoteFromPool();
        typedCharacters.clear();
        cursorIndex = 0;
        return true;
    }

    private void setNextQuoteFromPool() {
        if (quotePool == null || quotePool.isEmpty()) {
            quotePool = flattenTriplets(textGenerator.generateTriplets(wordMode, language, totalSeconds));
            quoteIndex = 0;
        }

        if (quoteIndex >= quotePool.size()) {
            quotePool = flattenTriplets(textGenerator.generateTriplets(wordMode, language, totalSeconds));
            quoteIndex = 0;
        }

        String quote = quotePool.get(quoteIndex);
        quoteRows = splitQuoteIntoRows(quote);
        currentTargetText = joinTripletRows(quoteRows);
    }

    private List<String> splitQuoteIntoRows(String quote) {
        if (quote == null || quote.isBlank()) {
            return List.of("", "", "");
        }

        String[] words = quote.trim().split("\\s+");
        StringBuilder r1 = new StringBuilder();
        StringBuilder r2 = new StringBuilder();
        StringBuilder r3 = new StringBuilder();

        int totalLen = quote.length();
        int target = Math.max(20, totalLen / 3);
        int which = 0;
        for (String w : words) {
            StringBuilder row = which == 0 ? r1 : which == 1 ? r2 : r3;
            if (row.length() > 0) {
                row.append(' ');
            }
            row.append(w);

            if (which < 2 && row.length() >= target) {
                which++;
            }
        }

        return List.of(r1.toString(), r2.toString(), r3.toString());
    }

    private String joinTripletRows(List<String> rows) {
        if (rows == null || rows.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < rows.size(); i++) {
            if (i > 0) {
                builder.append(' ');
            }
            builder.append(rows.get(i));
        }
        return builder.toString();
    }

    private void captureCompletedLineSnapshot(String completedLineText) {
        if (isQuotesMode()) {
            lastCompletedTypedChars = null;
            lastCompletedCharStates = null;
            return;
        }

        int length = completedLineText.length();
        if (typedCharacters.size() < length) {
            // Defensive: if something is off, render the expected line in default gray.
            lastCompletedTypedChars = completedLineText.toCharArray();
            lastCompletedCharStates = new int[length];
            return;
        }

        lastCompletedTypedChars = new char[length];
        lastCompletedCharStates = new int[length];
        for (int i = 0; i < length; i++) {
            CharacterState state = typedCharacters.get(i);
            lastCompletedTypedChars[i] = state.typed;
            lastCompletedCharStates[i] = state.correct ? STATE_CORRECT : STATE_WRONG;
        }
    }

    /**
     * Counts whitespace-separated words in one completed line.
     */
    private int countWords(String line) {
        if (line == null || line.isBlank()) {
            return 0;
        }
        return line.trim().split("\\s+").length;
    }

    public boolean isQuotesMode() {
        return "Quotes".equalsIgnoreCase(wordMode);
    }

    /**
     * Advances the countdown by one second.
     */
    private void onSessionTimerTick() {
        if (!gameRunning || timerPaused) {
            sessionTimer.stop();
            return;
        }

        remainingSeconds = Math.max(remainingSeconds - 1, 0);
        if (remainingSeconds <= 0) {
            gameRunning = false;
            sessionTimer.stop();
        }
    }

    /**
     * Converts time mode labels (e.g. "60s") to integer seconds.
     */
    private int parseTimeModeSeconds(String timeMode) {
        if (timeMode == null || timeMode.isBlank()) {
            throw new IllegalArgumentException("timeMode cannot be blank");
        }

        String trimmed = timeMode.trim().toLowerCase(Locale.ROOT);
        if (!trimmed.endsWith("s")) {
            throw new IllegalArgumentException("timeMode must end with 's', got: " + timeMode);
        }

        try {
            return Integer.parseInt(trimmed.substring(0, trimmed.length() - 1));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid timeMode: " + timeMode, ex);
        }
    }

    /**
     * Normalizes word mode input into expected display token.
     */
    private String normalizeWordMode(String value) {
        if (value == null || value.isBlank()) {
            return DEFAULT_WORD_MODE;
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "numbers" -> "Numbers";
            case "quotes" -> "Quotes";
            default -> "Words";
        };
    }

    /**
     * Normalizes language input into expected display token.
     */
    private String normalizeLanguage(String value) {
        if (value == null || value.isBlank()) {
            return DEFAULT_LANGUAGE;
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return normalized.startsWith("fil") ? "Fil" : "Eng";
    }

    /**
     * Normalizes time mode input into expected display token.
     */
    private String normalizeTimeMode(String value) {
        if (value == null || value.isBlank()) {
            return DEFAULT_TIME_MODE;
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "120s" -> "120s";
            case "60s" -> "60s";
            case "30s" -> "30s";
            case "15s" -> "15s";
            default -> DEFAULT_TIME_MODE;
        };
    }

    /**
     * Tracks each typed input and whether it matched target.
     */
    private static class CharacterState {
        // Raw typed character and correctness marker.
        private final char typed;
        private final boolean correct;

        /**
         * Creates one typed-character state record.
         */
        CharacterState(char typed, boolean correct) {
            this.typed = typed;
            this.correct = correct;
        }
    }

    /**
     * Immutable outcome of one input action.
     */
    public static class InputResult {
        // Cursor index, typed/expected chars, and action metadata.
        private final int index;
        private final char typedChar;
        private final char expectedChar;
        private final boolean correct;
        private final boolean backspace;
        private final boolean gameStopped;
        private final boolean tripletAdvanced;

        /**
         * Builds full input result payload.
         */
        InputResult(
                int index,
                char typedChar,
                char expectedChar,
                boolean correct,
                boolean backspace,
                boolean gameStopped,
                boolean tripletAdvanced) {
            this.index = index;
            this.typedChar = typedChar;
            this.expectedChar = expectedChar;
            this.correct = correct;
            this.backspace = backspace;
            this.gameStopped = gameStopped;
            this.tripletAdvanced = tripletAdvanced;
        }

        /**
         * Factory for stopped-session result.
         */
        static InputResult gameStopped() {
            return new InputResult(-1, '\0', '\0', false, false, true, false);
        }

        /**
         * Factory for ignored/no-op backspace result.
         */
        static InputResult noOpBackspace() {
            return new InputResult(-1, '\0', '\0', false, true, false, false);
        }

        /**
         * Returns affected index.
         */
        public int getIndex() {
            return index;
        }

        /**
         * Returns character typed by user.
         */
        public char getTypedChar() {
            return typedChar;
        }

        /**
         * Returns expected target character for index.
         */
        public char getExpectedChar() {
            return expectedChar;
        }

        /**
         * Returns if typed character matched expected character.
         */
        public boolean isCorrect() {
            return correct;
        }

        /**
         * Returns whether this result was caused by backspace.
         */
        public boolean isBackspace() {
            return backspace;
        }

        /**
         * Returns whether game is already stopped.
         */
        public boolean isGameStopped() {
            return gameStopped;
        }

        /**
         * Returns whether this input completed one triplet and advanced.
         */
        public boolean isTripletAdvanced() {
            return tripletAdvanced;
        }
    }
}
