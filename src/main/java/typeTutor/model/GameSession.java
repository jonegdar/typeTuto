package typeTutor.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class GameSession {
    public static final String DEFAULT_WORD_MODE = "Words";
    public static final String DEFAULT_LANGUAGE = "Eng";
    public static final String DEFAULT_TIME_MODE = "60s";

    private final TextGenerator textGenerator;

    private String wordMode;
    private String language;
    private String timeMode;
    private boolean crazyModeEnabled;

    private boolean gameRunning;
    private boolean timerStarted;
    private long startTimeNanos;
    private int totalSeconds;

    private List<List<String>> generatedTriplets;
    private int tripletIndex;

    private String currentTargetText;
    private final List<CharacterState> typedCharacters;
    private int cursorIndex;
    private int correctCharacters;
    private int wrongCharacters;

    public GameSession() {
        this.textGenerator = new TextGenerator();
        this.wordMode = DEFAULT_WORD_MODE;
        this.language = DEFAULT_LANGUAGE;
        this.timeMode = DEFAULT_TIME_MODE;
        this.crazyModeEnabled = false;

        this.generatedTriplets = Collections.emptyList();
        this.typedCharacters = new ArrayList<>();
        resetForCurrentOptions();
    }

    public void setWordMode(String wordMode) {
        this.wordMode = normalizeWordMode(wordMode);
    }

    public void setLanguage(String language) {
        this.language = normalizeLanguage(language);
    }

    public void setTimeMode(String timeMode) {
        this.timeMode = normalizeTimeMode(timeMode);
    }

    public void setCrazyModeEnabled(boolean enabled) {
        this.crazyModeEnabled = enabled;
    }

    public String getWordMode() {
        return wordMode;
    }

    public String getLanguage() {
        return language;
    }

    public String getTimeMode() {
        return timeMode;
    }

    public boolean isCrazyModeEnabled() {
        return crazyModeEnabled;
    }

    public void applyNavbarOptions(String wordMode, String language, String timeMode, boolean crazyModeEnabled) {
        setWordMode(wordMode);
        setLanguage(language);
        setTimeMode(timeMode);
        setCrazyModeEnabled(crazyModeEnabled);
        resetForCurrentOptions();
    }

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

    public int getRemainingSeconds() {
        if (!timerStarted) {
            return totalSeconds;
        }

        long elapsedNanos = System.nanoTime() - startTimeNanos;
        int elapsedSeconds = (int) (elapsedNanos / 1_000_000_000L);
        int remaining = totalSeconds - elapsedSeconds;
        return Math.max(remaining, 0);
    }

    public boolean isGameRunning() {
        if (!gameRunning) {
            return false;
        }

        if (getRemainingSeconds() <= 0) {
            gameRunning = false;
        }
        return gameRunning;
    }

    public List<String> getCurrentTripletRows() {
        if (generatedTriplets.isEmpty() || tripletIndex >= generatedTriplets.size()) {
            return Collections.emptyList();
        }
        return generatedTriplets.get(tripletIndex);
    }

    public String getCurrentTargetText() {
        return currentTargetText;
    }

    public int getCursorIndex() {
        return cursorIndex;
    }

    public int getCorrectCharacters() {
        return correctCharacters;
    }

    public int getWrongCharacters() {
        return wrongCharacters;
    }

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

    public TypingStats getTypingStats() {
        return new TypingStats(calculateWpm(), correctCharacters, wrongCharacters);
    }

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

    private String normalizeLanguage(String value) {
        if (value == null || value.isBlank()) {
            return DEFAULT_LANGUAGE;
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return normalized.startsWith("fil") ? "Fil" : "Eng";
    }

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

    private static class CharacterState {
        private final char typed;
        private final boolean correct;

        CharacterState(char typed, boolean correct) {
            this.typed = typed;
            this.correct = correct;
        }
    }

    public static class InputResult {
        private final int index;
        private final char typedChar;
        private final char expectedChar;
        private final boolean correct;
        private final boolean backspace;
        private final boolean gameStopped;
        private final boolean tripletAdvanced;

        InputResult(int index, char typedChar, char expectedChar, boolean correct, boolean backspace, boolean gameStopped,
                boolean tripletAdvanced) {
            this.index = index;
            this.typedChar = typedChar;
            this.expectedChar = expectedChar;
            this.correct = correct;
            this.backspace = backspace;
            this.gameStopped = gameStopped;
            this.tripletAdvanced = tripletAdvanced;
        }

        static InputResult gameStopped() {
            return new InputResult(-1, '\0', '\0', false, false, true, false);
        }

        static InputResult noOpBackspace() {
            return new InputResult(-1, '\0', '\0', false, true, false, false);
        }

        public int getIndex() {
            return index;
        }

        public char getTypedChar() {
            return typedChar;
        }

        public char getExpectedChar() {
            return expectedChar;
        }

        public boolean isCorrect() {
            return correct;
        }

        public boolean isBackspace() {
            return backspace;
        }

        public boolean isGameStopped() {
            return gameStopped;
        }

        public boolean isTripletAdvanced() {
            return tripletAdvanced;
        }
    }
}
