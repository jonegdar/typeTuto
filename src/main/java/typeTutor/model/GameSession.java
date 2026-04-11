package typeTutor.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Core game model.
 * Contains typing rules, timer lifecycle, triplet progression, and raw counters.
 */
public class GameSession {
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
    private long startTimeNanos;
    private int totalSeconds;

    // Generated triplet session data and current triplet index.
    private List<List<String>> generatedTriplets;
    private int tripletIndex;

    // Active flattened target text and typed progress counters.
    private String currentTargetText;
    private final List<CharacterState> typedCharacters;
    private int cursorIndex;
    private int correctCharacters;
    private int wrongCharacters;

    /**
     * Creates session with default modes and generated content.
     */
    public GameSession() {
        this.textGenerator = new TextGenerator();
        this.wordMode = DEFAULT_WORD_MODE;
        this.language = DEFAULT_LANGUAGE;
        this.timeMode = DEFAULT_TIME_MODE;

        this.generatedTriplets = Collections.emptyList();
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
        this.generatedTriplets = textGenerator.generateTriplets(wordMode, language, totalSeconds);
        this.tripletIndex = 0;
        this.currentTargetText = joinTripletRows(getCurrentTripletRows());

        this.gameRunning = true;
        this.timerStarted = false;
        this.startTimeNanos = 0L;

        this.typedCharacters.clear();
        this.cursorIndex = 0;
        this.correctCharacters = 0;
        this.wrongCharacters = 0;
    }

    /**
     * Returns remaining countdown seconds.
     */
    public int getRemainingSeconds() {
        if (!timerStarted) {
            return totalSeconds;
        }

        long elapsedNanos = System.nanoTime() - startTimeNanos;
        int elapsedSeconds = (int) (elapsedNanos / 1_000_000_000L);
        int remaining = totalSeconds - elapsedSeconds;
        return Math.max(remaining, 0);
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
        if (generatedTriplets.isEmpty() || tripletIndex >= generatedTriplets.size()) {
            return Collections.emptyList();
        }
        return generatedTriplets.get(tripletIndex);
    }

    /**
     * Returns flattened target text for active triplet.
     */
    public String getCurrentTargetText() {
        return currentTargetText;
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
     * Applies one typed character according to game rules.
     */
    public InputResult processTypedCharacter(char typedChar) {
        if (!isGameRunning()) {
            return InputResult.gameStopped();
        }

        if (!timerStarted) {
            timerStarted = true;
            startTimeNanos = System.nanoTime();
        }

        if (cursorIndex >= currentTargetText.length()) {
            if (!moveToNextTriplet()) {
                gameRunning = false;
                return InputResult.gameStopped();
            }
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
            advancedTriplet = moveToNextTriplet();
            if (!advancedTriplet) {
                gameRunning = false;
            }
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
        return new TypingStats(calculateWpm(), correctCharacters, wrongCharacters);
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
     * Moves to next triplet when available.
     */
    private boolean moveToNextTriplet() {
        if (tripletIndex + 1 >= generatedTriplets.size()) {
            return false;
        }

        tripletIndex++;
        currentTargetText = joinTripletRows(getCurrentTripletRows());
        typedCharacters.clear();
        cursorIndex = 0;
        return true;
    }

    /**
     * Joins 3 row strings into one flat target stream separated by spaces.
     */
    private String joinTripletRows(List<String> rows) {
        if (rows.isEmpty()) {
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
