package typeTutor.model;

/**
 * Immutable stats snapshot returned by the model layer.
 */
public class TypingStats {
    // Final computed metrics for one finished game session.
    private final double wpm;
    private final int correctCharacters;
    private final int wrongCharacters;

    /**
     * Stores computed stats values.
     */
    public TypingStats(double wpm, int correctCharacters, int wrongCharacters) {
        this.wpm = wpm;
        this.correctCharacters = correctCharacters;
        this.wrongCharacters = wrongCharacters;
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
}
