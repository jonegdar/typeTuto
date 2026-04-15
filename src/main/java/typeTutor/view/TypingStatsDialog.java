package typeTutor.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Dimension;
import java.awt.geom.RoundRectangle2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import typeTutor.model.TypingStats;

/**
 * Modal dialog that presents final typing stats when a timed session ends.
 */
public class TypingStatsDialog extends JDialog {
    private static final int CORNER_RADIUS = 40;

    /**
     * Creates the stats dialog shell.
     */
    public TypingStatsDialog(Frame owner) {
        super(owner, "TypeTuto - Session Stats", true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                applyRoundedShape();
            }
        });
    }

    /**
     * Populates and shows the stats dialog for one completed session.
     */
    public void showStats(TypingStats stats) {
        JPanel content = buildContent(stats);
        int ownerWidth = getOwner() != null ? getOwner().getWidth() : 1200;
        content.setPreferredSize(new Dimension(Math.min(720, Math.round(ownerWidth * 0.60f)), content.getPreferredSize().height));
        setContentPane(content);
        pack();
        applyRoundedShape();
        setLocationRelativeTo(getOwner());
        setVisible(true);
    }

    /**
     * Builds the dialog content from the final stats snapshot.
     */
    private JPanel buildContent(TypingStats stats) {
        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setBackground(new Color(22, 22, 30));
        content.setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));

        JButton closeButton = new JButton("X");
        closeButton.setFocusPainted(false);
        closeButton.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        closeButton.setBackground(new Color(170, 55, 55));
        closeButton.setForeground(Color.WHITE);
        closeButton.addActionListener(e -> dispose());

        JLabel titleLabel = new JLabel("Your Stats", SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(AppFonts.uiExtraBold(21f));

        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);
        titleBar.add(titleLabel, BorderLayout.CENTER);
        titleBar.add(closeButton, BorderLayout.EAST);

        JLabel sessionLabel = new JLabel(
                "<html><div style='text-align:center;'>"
                        + "<span style='color:#ffc05a;'>Time</span> <span style='color:#ffffff;'>" + stats.getTimeMode() + "</span>"
                        + "&nbsp;&nbsp;&nbsp;&nbsp;"
                        + "<span style='color:#ffc05a;'>Language</span> <span style='color:#ffffff;'>" + stats.getLanguage() + "</span>"
                        + "&nbsp;&nbsp;&nbsp;&nbsp;"
                        + "<span style='color:#ffc05a;'>Word Type</span> <span style='color:#ffffff;'>" + stats.getWordMode() + "</span>"
                        + "</div></html>",
                SwingConstants.CENTER);
        sessionLabel.setForeground(new Color(230, 230, 235));
        sessionLabel.setFont(AppFonts.uiRegular(12f));

        JPanel metricsPanel = new JPanel(new BorderLayout());
        metricsPanel.setOpaque(true);
        metricsPanel.setBackground(new Color(32, 32, 46));
        metricsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 192, 90), 2),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)));

        int correct = stats.getCorrectCharacters();
        int wrong = stats.getWrongCharacters();

        JLabel metricsLabel = new JLabel(
                "<html><div style='text-align:center;'>"
                        + "<span style='color:#ffc05a;'>WPM</span><br><span style='font-size:28px; color:#ffffff;'>"
                        + String.format("%.0f", stats.getWpm())
                        + "</span><br><br>"
                        + "<span style='color:#ffc05a;'>Correct</span> <span style='color:#ffffff;'>" + correct + "</span>"
                        + "&nbsp;&nbsp;&nbsp;&nbsp;"
                        + "<span style='color:#ffc05a;'>Wrong</span> <span style='color:#ffffff;'>" + wrong + "</span>"
                        + "&nbsp;&nbsp;&nbsp;&nbsp;"
                        + "<span style='color:#ffc05a;'>Accuracy</span> <span style='color:#ffffff;'>"
                        + String.format("%.1f%%", stats.getAccuracyPercent())
                        + "</span>"
                        + "&nbsp;&nbsp;&nbsp;&nbsp;"
                        + "<span style='color:#ffc05a;'>Score</span> <span style='color:#ffffff;'>"
                        + String.format("%.1f", stats.getFinalScore())
                        + "</span>"
                        + "&nbsp;&nbsp;&nbsp;&nbsp;"
                        + "<span style='color:#ffc05a;'>Rank</span> <span style='color:" + stats.getRankColorHex() + ";'>"
                        + stats.getRank()
                        + "</span><br><br>"
                        + "<span style='color:#ffc05a;'>Words</span> <span style='color:#ffffff;'>"
                        + stats.getCompletedWords()
                        + "</span>"
                        + "&nbsp;&nbsp;&nbsp;&nbsp;"
                        + "<span style='color:#ffc05a;'>Lines</span> <span style='color:#ffffff;'>"
                        + stats.getCompletedLines()
                        + "</span></div></html>",
                SwingConstants.CENTER);
        metricsLabel.setFont(AppFonts.uiRegular(15f));
        metricsLabel.setForeground(Color.WHITE);
        metricsPanel.add(metricsLabel, BorderLayout.CENTER);

        JLabel hintLabel = new JLabel("Press OK to begin a fresh session.", SwingConstants.CENTER);
        hintLabel.setForeground(new Color(198, 198, 214));
        hintLabel.setFont(AppFonts.uiRegular(12f));

        JButton okButton = new JButton("OK");
        okButton.setFocusPainted(false);
        okButton.setFont(AppFonts.uiExtraBold(14f));
        okButton.setBackground(new Color(255, 192, 90));
        okButton.setForeground(new Color(22, 22, 30));
        okButton.setBorder(BorderFactory.createEmptyBorder(11, 30, 11, 30));
        okButton.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(okButton);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        hintLabel.setAlignmentX(JPanel.CENTER_ALIGNMENT);
        okButton.setAlignmentX(JPanel.CENTER_ALIGNMENT);
        bottomPanel.add(hintLabel);
        bottomPanel.add(Box.createVerticalStrut(12));
        bottomPanel.add(okButton);

        content.add(titleBar, BorderLayout.NORTH);
        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setOpaque(false);
        center.add(sessionLabel, BorderLayout.NORTH);
        center.add(metricsPanel, BorderLayout.CENTER);
        content.add(center, BorderLayout.CENTER);
        content.add(bottomPanel, BorderLayout.SOUTH);
        return content;
    }

    /**
     * Updates the rounded clipping for the dialog window.
     */
    private void applyRoundedShape() {
        if (getWidth() <= 0 || getHeight() <= 0) {
            return;
        }
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), CORNER_RADIUS, CORNER_RADIUS));
    }
}
