package typeTutor.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stores the history of completed typing sessions.
 */
public class SessionHistoryTracker {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final List<Entry> entries;

    /**
     * Creates an empty history tracker.
     */
    public SessionHistoryTracker() {
        this.entries = new ArrayList<>();
    }

    /**
     * Records one completed session.
     */
    public void recordSession(TypingStats stats) {
        entries.add(new Entry(
                FORMATTER.format(LocalDateTime.now()),
                stats.getWpm(),
                stats.getAccuracyPercent(),
                stats.getRank(),
                stats.getTimeMode(),
                stats.getLanguage(),
                stats.getWordMode()));
    }

    /**
     * Returns an immutable snapshot of the recorded sessions, newest first.
     */
    public List<Entry> getEntries() {
        List<Entry> copy = new ArrayList<>(entries);
        Collections.reverse(copy);
        return Collections.unmodifiableList(copy);
    }

    /**
     * Immutable history entry for one session.
     */
    public static class Entry {
        private final String timestamp;
        private final double wpm;
        private final double accuracy;
        private final String rank;
        private final String timeMode;
        private final String language;
        private final String wordMode;

        /**
         * Stores entry details.
         */
        public Entry(String timestamp, double wpm, double accuracy, String rank, String timeMode, String language, String wordMode) {
            this.timestamp = timestamp;
            this.wpm = wpm;
            this.accuracy = accuracy;
            this.rank = rank;
            this.timeMode = timeMode;
            this.language = language;
            this.wordMode = wordMode;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public double getWpm() {
            return wpm;
        }

        public double getAccuracy() {
            return accuracy;
        }

        public String getRank() {
            return rank;
        }

        public String getTimeMode() {
            return timeMode;
        }

        public String getLanguage() {
            return language;
        }

        public String getWordMode() {
            return wordMode;
        }
    }
}
