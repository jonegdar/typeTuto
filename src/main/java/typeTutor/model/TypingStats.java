package typeTutor.model;

/**
 * Immutable stats snapshot returned by the model layer.
 */
public class TypingStats {
    private static final double ACCURACY_WEIGHT = 1.7;
    private static final double WRONG_CHAR_PENALTY = 0.5;
    private static final double HIGH_ACCURACY_BONUS_THRESHOLD = 0.98;
    private static final double HIGH_ACCURACY_BONUS = 5.0;
    private static final double PERFECT_RUN_BONUS = 10.0;

    // Final computed metrics for one finished game session.
    private final double wpm;
    private final int correctCharacters;
    private final int wrongCharacters;
    private final int completedWords;
    private final int completedLines;
    private final String timeMode;
    private final String language;
    private final String wordMode;

    /**
     * Stores computed stats values.
     */
    public TypingStats(
            double wpm,
            int correctCharacters,
            int wrongCharacters,
            int completedWords,
            int completedLines,
            String timeMode,
            String language,
            String wordMode) {
        this.wpm = wpm;
        this.correctCharacters = correctCharacters;
        this.wrongCharacters = wrongCharacters;
        this.completedWords = completedWords;
        this.completedLines = completedLines;
        this.timeMode = timeMode;
        this.language = language;
        this.wordMode = wordMode;
    }

    /**
     * Returns words-per-minute value.
     */
    public double getWpm() {
        return wpm;
    }

    /**
     * Returns number of correct characters.
     */
    public int getCorrectCharacters() {
        return correctCharacters;
    }

    /**
     * Returns number of wrong characters.
     */
    public int getWrongCharacters() {
        return wrongCharacters;
    }

    /**
     * Returns number of fully completed words.
     */
    public int getCompletedWords() {
        return completedWords;
    }

    /**
     * Returns number of fully completed lines.
     */
    public int getCompletedLines() {
        return completedLines;
    }

    /**
     * Returns the time control used for the session.
     */
    public String getTimeMode() {
        return timeMode;
    }

    /**
     * Returns the language used for the session.
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Returns the word type used for the session.
     */
    public String getWordMode() {
        return wordMode;
    }

    /**
     * Returns accuracy as a 0..1 fraction.
     */
    public double getAccuracy() {
        int totalTyped = correctCharacters + wrongCharacters;
        if (totalTyped == 0) {
            return 0.0;
        }
        return correctCharacters / (double) totalTyped;
    }

    /**
     * Returns accuracy as a percentage.
     */
    public double getAccuracyPercent() {
        return getAccuracy() * 100.0;
    }

    /**
     * Returns the precision-weighted speed score before penalties.
     */
    public double getCompositeScore() {
        return wpm * Math.pow(getAccuracy(), ACCURACY_WEIGHT);
    }

    /**
     * Returns the penalty applied for incorrect characters.
     */
    public double getPenalty() {
        return wrongCharacters * WRONG_CHAR_PENALTY;
    }

    /**
     * Returns any bonus applied for highly accurate or perfect runs.
     */
    public double getBonus() {
        double bonus = 0.0;
        if (getAccuracy() >= HIGH_ACCURACY_BONUS_THRESHOLD) {
            bonus += HIGH_ACCURACY_BONUS;
        }
        if (wrongCharacters == 0 && correctCharacters > 0) {
            bonus += PERFECT_RUN_BONUS;
        }
        return bonus;
    }

    /**
     * Returns the final score used for ranking.
     */
    public double getFinalScore() {
        return Math.max(0.0, getCompositeScore() - getPenalty() + getBonus());
    }

    /**
     * Returns the rank label derived from the final score.
     */
    public String getRank() {
        double score = getFinalScore();
        if (score >= 85.0) {
            return "S+ (Elite)";
        }
        if (score >= 75.0) {
            return "S (Expert)";
        }
        if (score >= 65.0) {
            return "A (Advanced)";
        }
        if (score >= 55.0) {
            return "B (Intermediate)";
        }
        if (score >= 45.0) {
            return "C (Beginner)";
        }
        if (score >= 35.0) {
            return "D";
        }
        return "F";
    }

    /**
     * Returns the rank accent color in hex form.
     */
    public String getRankColorHex() {
        double score = getFinalScore();
        if (score >= 85.0) {
            return "#6ee7b7";
        }
        if (score >= 75.0) {
            return "#57e389";
        }
        if (score >= 65.0) {
            return "#8be9fd";
        }
        if (score >= 55.0) {
            return "#ffc05a";
        }
        if (score >= 45.0) {
            return "#ffb86c";
        }
        if (score >= 35.0) {
            return "#ff7a59";
        }
        return "#ff5555";
    }
}
