package typeTutor.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.geom.RoundRectangle2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

/**
 * Explains the game's scoring and ranking mechanics.
 */
public class ScoringInfoDialog extends JDialog {
    private static final int CORNER_RADIUS = 40;

    /**
     * Builds the scoring information dialog.
     */
    public ScoringInfoDialog(Frame owner) {
        super(owner, "TypeTuto - Scoring", true);
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
     * Shows the dialog.
     */
    public void showInfo() {
        JPanel content = buildContent();
        int ownerWidth = getOwner() != null ? getOwner().getWidth() : 1200;
        content.setPreferredSize(new Dimension(Math.min(460, Math.round(ownerWidth * 0.38f)), content.getPreferredSize().height));
        setContentPane(content);
        pack();
        applyRoundedShape();
        setLocationRelativeTo(getOwner());
        setVisible(true);
    }

    /**
     * Builds the content panel.
     */
    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setBackground(new Color(22, 22, 30));
        content.setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));

        JButton closeButton = new JButton("X");
        closeButton.setFocusPainted(false);
        closeButton.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        closeButton.setBackground(new Color(170, 55, 55));
        closeButton.setForeground(Color.WHITE);
        closeButton.addActionListener(e -> dispose());

        JLabel title = new JLabel("Scoring & Ranking", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(AppFonts.uiBold(21f));

        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);
        titleBar.add(title, BorderLayout.CENTER);
        titleBar.add(closeButton, BorderLayout.EAST);

        JLabel body = new JLabel(
                "<html><div style='text-align:left; line-height:1.35;'>" +
                        "<b>WPM</b> = (correct / 5) / minutes<br>" +
                        "<b>Accuracy</b> = correct / (correct + wrong)<br>" +
                        "<b>Score</b> = WPM × accuracy^1.7 − (wrong × 0.5) + bonuses" +
                        "</div></html>");
        body.setForeground(new Color(230, 230, 235));
        body.setFont(AppFonts.uiRegular(13f));

        JPanel legend = new JPanel(new GridLayout(0, 3, 10, 6));
        legend.setOpaque(false);
        addLegendRow(legend, "S+ (Elite)", "#6ee7b7", "85+");
        addLegendRow(legend, "S (Expert)", "#57e389", "75–84");
        addLegendRow(legend, "A (Advanced)", "#8be9fd", "65–74");
        addLegendRow(legend, "B (Intermediate)", "#ffc05a", "55–64");
        addLegendRow(legend, "C (Beginner)", "#ffb86c", "45–54");
        addLegendRow(legend, "D", "#ff7a59", "35–44");
        addLegendRow(legend, "F", "#ff5555", "<35");

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(body, BorderLayout.NORTH);
        center.add(legend, BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(center);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getViewport().setBackground(content.getBackground());

        JButton okButton = new JButton("OK");
        okButton.setFont(AppFonts.uiBold(14f));
        okButton.setFocusPainted(false);
        okButton.setBackground(new Color(255, 192, 90));
        okButton.setForeground(new Color(22, 22, 30));
        okButton.addActionListener(e -> dispose());

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.add(okButton);

        content.add(titleBar, BorderLayout.NORTH);
        content.add(scroll, BorderLayout.CENTER);
        content.add(bottom, BorderLayout.SOUTH);
        return content;
    }

    /**
     * Adds one legend row.
     */
    private void addLegendRow(JPanel legend, String label, String colorHex, String threshold) {
        JLabel name = new JLabel(label);
        name.setForeground(Color.WHITE);
        name.setFont(AppFonts.uiRegular(13f));
        JLabel swatch = new JLabel("<html><span style='color:" + colorHex + ";'>&#9632;</span></html>");
        swatch.setFont(AppFonts.uiBold(14f));
        JLabel score = new JLabel(threshold);
        score.setForeground(new Color(230, 230, 235));
        score.setFont(AppFonts.uiRegular(13f));
        legend.add(swatch);
        legend.add(name);
        legend.add(score);
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
