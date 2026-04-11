package typeTutor.view;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import typeTutor.model.TypingStats;

/**
 * Bottom stats view that renders final metrics after each finished game.
 */
public class GameStatsPanel extends JPanel {
    // Shared colors for panel and text styling.
    private static final Color BG = new Color(24, 24, 24);
    private static final Color TEXT = new Color(235, 235, 235);
    private static final Color ACCENT = new Color(255, 192, 90);

    // Single rich-text label that shows all metrics in one line.
    private final JLabel statsLabel;

    /**
     * Builds stats panel UI.
     */
    public GameStatsPanel() {
        setLayout(null);
        setBackground(BG);

        statsLabel = new JLabel("", SwingConstants.CENTER);
        statsLabel.setForeground(TEXT);
        statsLabel.setFont(AppFonts.ui(18f, Font.BOLD));
        add(statsLabel);

        showWaitingState();
    }

    /**
     * Renders final game stats and computed rank score.
     */
    public void updateStats(TypingStats stats) {
        int correct = stats.getCorrectCharacters();
        int wrong = stats.getWrongCharacters();
        int totalTyped = correct + wrong;

        double accuracy = totalTyped == 0 ? 0.0 : (correct * 100.0) / totalTyped;
        // Convert WPM percentile table output (0.02..42.86) into a 0..100 score.
        double wpmPercentile = percentileForWpm(stats.getWpm());
        double wpmScore = (wpmPercentile / 42.86) * 100.0;

        // Final rank score favors balance: both speed and accuracy must be high.
        double combinedScore = Math.sqrt(wpmScore * accuracy);
        String rank = rankForPercentile(combinedScore);

        String text = String.format(
                "<html><span style='color:%s;'>WPM:</span> %.0f&nbsp;&nbsp;&nbsp;&nbsp;" +
                        "<span style='color:%s;'>Correct:</span> %d&nbsp;&nbsp;&nbsp;&nbsp;" +
                        "<span style='color:%s;'>Wrong:</span> %d&nbsp;&nbsp;&nbsp;&nbsp;" +
                        "<span style='color:%s;'>Accuracy:</span> %.1f%%&nbsp;&nbsp;&nbsp;&nbsp;" +
                        "<span style='color:%s;'>Rank:</span> %.2f%% (%s)" +
                        "</html>",
                toHex(ACCENT), stats.getWpm(),
                toHex(ACCENT), correct,
                toHex(ACCENT), wrong,
                toHex(ACCENT), accuracy,
                toHex(ACCENT), combinedScore, rank);
        statsLabel.setText(text);
    }

    /**
     * Shows placeholder state while a game has not yet ended.
     */
    public void showWaitingState() {
        String text = String.format(
                "<html><span style='color:%s;'>WPM:</span> -&nbsp;&nbsp;&nbsp;&nbsp;" +
                        "<span style='color:%s;'>Correct:</span> -&nbsp;&nbsp;&nbsp;&nbsp;" +
                        "<span style='color:%s;'>Wrong:</span> -&nbsp;&nbsp;&nbsp;&nbsp;" +
                        "<span style='color:%s;'>Accuracy:</span> -&nbsp;&nbsp;&nbsp;&nbsp;" +
                        "<span style='color:%s;'>Rank:</span> waiting for game end" +
                        "</html>",
                toHex(ACCENT), toHex(ACCENT), toHex(ACCENT), toHex(ACCENT), toHex(ACCENT));
        statsLabel.setText(text);
    }

    /**
     * Maps WPM to provided percentile table.
     */
    private double percentileForWpm(double wpm) {
        if (wpm < 25) {
            return 0.02;
        }
        if (wpm < 35) {
            return 7.16;
        }
        if (wpm < 45) {
            return 14.30;
        }
        if (wpm < 60) {
            return 21.44;
        }
        if (wpm < 80) {
            return 28.58;
        }
        if (wpm < 120) {
            return 35.72;
        }
        return 42.86;
    }

    /**
     * Maps final 0..100 score to rank label.
     */
    private String rankForPercentile(double percentile) {
        if (percentile < 16.0) {
            return "asleep";
        }
        if (percentile < 32.0) {
            return "noob";
        }
        if (percentile < 48.0) {
            return "average";
        }
        if (percentile < 64.0) {
            return "pro";
        }
        if (percentile < 80.0) {
            return "hacker";
        }
        if (percentile < 96.0) {
            return "god";
        }
        return "legend";
    }

    /**
     * Converts Color to hex string for inline HTML styles.
     */
    private String toHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * Positions the single label to fill panel width with side padding.
     */
    @Override
    public void doLayout() {
        statsLabel.setBounds(20, 0, getWidth() - 40, getHeight());
    }
}
