package typeTutor.view;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import typeTutor.model.TypingStats;

/**
 * Bottom stats view that renders final metrics after each finished game.
 */
public class GameStatsPanel extends JPanel implements MainFrame.SupportsAlpha {
    // Shared colors for panel and text styling.
    private static final Color BG = new Color(24, 24, 24);
    private static final Color TEXT = new Color(235, 235, 235);
    private static final Color ACCENT = new Color(255, 192, 90);

    // Single rich-text label that shows all metrics in one line.
    private final JLabel statsLabel;
    private float alpha = 1f;

    /**
     * Builds stats panel UI.
     */
    public GameStatsPanel() {
        setLayout(null);
        setBackground(BG);

        statsLabel = new JLabel("", SwingConstants.CENTER);
        statsLabel.setForeground(TEXT);
        statsLabel.setFont(AppFonts.uiRegular(18f));
        add(statsLabel);

        showWaitingState();
    }

    /**
     * Renders final game stats and computed rank score.
     */
    public void updateStats(TypingStats stats) {
        String text = String.format(
                "<html><div style='text-align:center;'>"
                        + "<span style='color:%s;'>WPM:</span> %.0f&nbsp;&nbsp;&nbsp;&nbsp;"
                        + "<span style='color:%s;'>Correct:</span> %d&nbsp;&nbsp;&nbsp;&nbsp;"
                        + "<span style='color:%s;'>Wrong:</span> %d&nbsp;&nbsp;&nbsp;&nbsp;"
                        + "<span style='color:%s;'>Accuracy:</span> %.1f%%&nbsp;&nbsp;&nbsp;&nbsp;"
                        + "<span style='color:%s;'>Score:</span> %.1f&nbsp;&nbsp;&nbsp;&nbsp;"
                        + "<span style='color:%s;'>Rank:</span> <span style='color:%s;'>%s</span>&nbsp;&nbsp;&nbsp;&nbsp;"
                        + "<span style='color:%s;'>Words:</span> %d&nbsp;&nbsp;&nbsp;&nbsp;"
                        + "<span style='color:%s;'>Lines:</span> %d"
                        + "</div></html>",
                toHex(ACCENT), stats.getWpm(),
                toHex(ACCENT), stats.getCorrectCharacters(),
                toHex(ACCENT), stats.getWrongCharacters(),
                toHex(ACCENT), stats.getAccuracyPercent(),
                toHex(ACCENT), stats.getFinalScore(),
                toHex(ACCENT), stats.getRankColorHex(), stats.getRank(),
                toHex(ACCENT), stats.getCompletedWords(),
                toHex(ACCENT), stats.getCompletedLines());
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
                        "<span style='color:%s;'>Score:</span> -&nbsp;&nbsp;&nbsp;&nbsp;" +
                        "<span style='color:%s;'>Rank:</span> &nbsp;&nbsp;&nbsp;&nbsp;" +
                        "<span style='color:%s;'>Words:</span> -&nbsp;&nbsp;&nbsp;&nbsp;" +
                        "<span style='color:%s;'>Lines:</span> -" +
                        "</html>",
                toHex(ACCENT), toHex(ACCENT), toHex(ACCENT), toHex(ACCENT), toHex(ACCENT),
                toHex(ACCENT), toHex(ACCENT), toHex(ACCENT));
        statsLabel.setText(text);
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

    /**
     * Updates panel opacity for distraction-free mode.
     */
    @Override
    public void setAlpha(float alpha) {
        this.alpha = Math.max(0f, Math.min(alpha, 1f));
        repaint();
    }

    /**
     * Paints child labels with configurable alpha.
     */
    @Override
    protected void paintChildren(java.awt.Graphics g) {
        java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
        g2.setComposite(java.awt.AlphaComposite.SrcOver.derive(alpha));
        super.paintChildren(g2);
        g2.dispose();
    }
}
