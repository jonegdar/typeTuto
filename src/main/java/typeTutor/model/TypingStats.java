package typeTutor.model;

public class TypingStats {
    private final double wpm;
    private final int correctCharacters;
    private final int wrongCharacters;

    public TypingStats(double wpm, int correctCharacters, int wrongCharacters) {
        this.wpm = wpm;
        this.correctCharacters = correctCharacters;
        this.wrongCharacters = wrongCharacters;
    }

    public double getWpm() {
        return wpm;
    }

    public int getCorrectCharacters() {
        return correctCharacters;
    }

    public int getWrongCharacters() {
        return wrongCharacters;
    }
}
