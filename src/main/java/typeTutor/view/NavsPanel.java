package typeTutor.view;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.AbstractBorder;

/**
 * Navbar view that exposes word/language/time selections.
 * The controller listens to mode changes via callback.
 */
public class NavsPanel extends JPanel {
    /**
     * Callback contract for mode changes.
     */
    public interface ModeChangeListener {
        void onModeChanged(String wordMode, String language, String timeMode);
    }

    // Style constants for active/inactive mode appearance.
    private static final int CORNER_RADIUS = 10;
    private static final Color ACTIVE_COLOR = new Color(255, 192, 90);
    private static final Color INACTIVE_COLOR = Color.WHITE;

    // Main row and grouped nav containers.
    private final JPanel navRow;
    private final JPanel wordModeNav;
    private final JPanel languageNav;
    private final JPanel timeModeNav;

    // Word mode buttons.
    private final JButton wordsButton;
    private final JButton numbersButton;
    private final JButton quotesButton;

    // Language buttons.
    private final JButton engButton;
    private final JButton filButton;

    // Time mode buttons.
    private final JButton time120Button;
    private final JButton time60Button;
    private final JButton time30Button;
    private final JButton time15Button;

    // Current selected modes and listener.
    private String selectedWordMode = "Words";
    private String selectedLanguage = "Eng";
    private String selectedTimeMode = "60s";
    private ModeChangeListener modeChangeListener;

    /**
     * Builds nav groups, action handlers, and responsive bounds behavior.
     */
    public NavsPanel() {
        setBackground(new Color(31, 31, 31));
        setLayout(null);

        navRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 4));
        navRow.setOpaque(false);

        wordModeNav = createNavbarPanel();
        wordsButton = createBorderlessButton("Words");
        numbersButton = createBorderlessButton("Numbers");
        quotesButton = createBorderlessButton("Quotes");
        wordsButton.addActionListener(e -> onWordModeSelected("Words"));
        numbersButton.addActionListener(e -> onWordModeSelected("Numbers"));
        quotesButton.addActionListener(e -> onWordModeSelected("Quotes"));
        wordModeNav.add(wordsButton);
        wordModeNav.add(numbersButton);
        wordModeNav.add(quotesButton);

        languageNav = createNavbarPanel();
        engButton = createBorderlessButton("Eng");
        filButton = createBorderlessButton("Fil");
        engButton.addActionListener(e -> onLanguageSelected("Eng"));
        filButton.addActionListener(e -> onLanguageSelected("Fil"));
        languageNav.add(engButton);
        languageNav.add(filButton);

        timeModeNav = createNavbarPanel();
        time120Button = createBorderlessButton("120s");
        time60Button = createBorderlessButton("60s");
        time30Button = createBorderlessButton("30s");
        time15Button = createBorderlessButton("15s");
        time120Button.addActionListener(e -> onTimeModeSelected("120s"));
        time60Button.addActionListener(e -> onTimeModeSelected("60s"));
        time30Button.addActionListener(e -> onTimeModeSelected("30s"));
        time15Button.addActionListener(e -> onTimeModeSelected("15s"));
        timeModeNav.add(time120Button);
        timeModeNav.add(time60Button);
        timeModeNav.add(time30Button);
        timeModeNav.add(time15Button);

        navRow.add(wordModeNav);
        navRow.add(languageNav);
        navRow.add(timeModeNav);
        add(navRow);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                layoutNavRow();
            }
        });

        updateHighlighting();
        layoutNavRow();
    }

    /**
     * Assigns external listener and emits current initial mode state.
     */
    public void setModeChangeListener(ModeChangeListener listener) {
        this.modeChangeListener = listener;
        notifyModeChanged();
    }

    /**
     * Handles word mode click.
     */
    private void onWordModeSelected(String mode) {
        if (!selectedWordMode.equals(mode)) {
            selectedWordMode = mode;
            updateHighlighting();
            notifyModeChanged();
        }
    }

    /**
     * Handles language mode click.
     */
    private void onLanguageSelected(String language) {
        if (!selectedLanguage.equals(language)) {
            selectedLanguage = language;
            updateHighlighting();
            notifyModeChanged();
        }
    }

    /**
     * Handles time mode click.
     */
    private void onTimeModeSelected(String timeMode) {
        if (!selectedTimeMode.equals(timeMode)) {
            selectedTimeMode = timeMode;
            updateHighlighting();
            notifyModeChanged();
        }
    }

    /**
     * Emits active modes to controller.
     */
    private void notifyModeChanged() {
        if (modeChangeListener != null) {
            modeChangeListener.onModeChanged(selectedWordMode, selectedLanguage, selectedTimeMode);
        }
    }

    /**
     * Colors active buttons yellow and inactive buttons white.
     */
    private void updateHighlighting() {
        setButtonColor(wordsButton, selectedWordMode.equals("Words"));
        setButtonColor(numbersButton, selectedWordMode.equals("Numbers"));
        setButtonColor(quotesButton, selectedWordMode.equals("Quotes"));

        setButtonColor(engButton, selectedLanguage.equals("Eng"));
        setButtonColor(filButton, selectedLanguage.equals("Fil"));

        setButtonColor(time120Button, selectedTimeMode.equals("120s"));
        setButtonColor(time60Button, selectedTimeMode.equals("60s"));
        setButtonColor(time30Button, selectedTimeMode.equals("30s"));
        setButtonColor(time15Button, selectedTimeMode.equals("15s"));
    }

    /**
     * Applies foreground color to button by active state.
     */
    private void setButtonColor(JButton button, boolean active) {
        button.setForeground(active ? ACTIVE_COLOR : INACTIVE_COLOR);
    }

    /**
     * Creates one rounded group panel.
     */
    private JPanel createNavbarPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        panel.setOpaque(false);
        applyRoundedPanelBorder(panel, INACTIVE_COLOR);
        return panel;
    }

    /**
     * Applies rounded outline to panel.
     */
    private void applyRoundedPanelBorder(JPanel panel, Color color) {
        panel.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(color, 1, CORNER_RADIUS),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
    }

    /**
     * Creates consistent nav mode button style.
     */
    private JButton createBorderlessButton(String text) {
        JButton button = new JButton(text);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setForeground(INACTIVE_COLOR);
        button.setMargin(new Insets(2, 8, 2, 8));
        button.setFont(AppFonts.ui(17f, Font.BOLD));
        return button;
    }

    /**
     * Keeps nav content centered inside panel with width/height caps.
     */
    private void layoutNavRow() {
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        if (panelWidth <= 0 || panelHeight <= 0) {
            return;
        }

        int rowHeight = Math.round(panelHeight * 0.80f);
        int rowWidth = Math.round(panelWidth * 0.85f);
        int x = (panelWidth - rowWidth) / 2;
        int y = (panelHeight - rowHeight) / 2;
        navRow.setBounds(x, y, rowWidth, rowHeight);
        navRow.revalidate();
        navRow.repaint();
    }

    /**
     * Custom rounded border used by nav group containers.
     */
    private static class RoundedBorder extends AbstractBorder {
        // Border color and geometry values.
        private final Color color;
        private final int thickness;
        private final int radius;

        /**
         * Stores border properties.
         */
        RoundedBorder(Color color, int thickness, int radius) {
            this.color = color;
            this.thickness = thickness;
            this.radius = radius;
        }

        /**
         * Returns border insets for layout calculations.
         */
        @Override
        public Insets getBorderInsets(java.awt.Component c) {
            return new Insets(thickness, thickness, thickness, thickness);
        }

        /**
         * Writes border insets into provided object.
         */
        @Override
        public Insets getBorderInsets(java.awt.Component c, Insets insets) {
            insets.set(thickness, thickness, thickness, thickness);
            return insets;
        }

        /**
         * Paints anti-aliased rounded border.
         */
        @Override
        public void paintBorder(java.awt.Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new java.awt.BasicStroke(thickness));
            int arc = radius * 2;
            int offset = thickness / 2;
            g2.drawRoundRect(x + offset, y + offset, width - thickness, height - thickness, arc, arc);
            g2.dispose();
        }
    }
}
