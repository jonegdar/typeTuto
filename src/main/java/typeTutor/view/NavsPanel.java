package typeTutor.view;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;

public class NavsPanel extends JPanel {
    public interface ModeChangeListener {
        void onModeChanged(String wordMode, String language, String timeMode, boolean crazyModeEnabled);
    }

    private static final int CORNER_RADIUS = 10;
    private static final Color ACTIVE_COLOR = new Color(255, 192, 90);
    private static final Color INACTIVE_COLOR = Color.WHITE;

    private final JPanel navRow;
    private final JPanel wordModeNav;
    private final JPanel languageNav;
    private final JPanel timeModeNav;
    private final JPanel crazyModeNav;

    private final JButton wordsButton;
    private final JButton numbersButton;
    private final JButton quotesButton;

    private final JButton engButton;
    private final JButton filButton;

    private final JButton time120Button;
    private final JButton time60Button;
    private final JButton time30Button;
    private final JButton time15Button;

    private final JButton crazyButton;

    private String selectedWordMode = "Words";
    private String selectedLanguage = "Eng";
    private String selectedTimeMode = "60s";
    private boolean crazyModeEnabled = false;

    private ModeChangeListener modeChangeListener;

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

        crazyModeNav = createNavbarPanel();
        crazyButton = createBorderlessButton("");
        crazyButton.setIcon(loadCrazyModeIcon());
        crazyButton.setHorizontalAlignment(SwingConstants.CENTER);
        crazyButton.addActionListener(e -> onCrazyModeToggled());
        crazyModeNav.add(crazyButton);

        navRow.add(wordModeNav);
        navRow.add(languageNav);
        navRow.add(timeModeNav);
        navRow.add(crazyModeNav);
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

    public void setModeChangeListener(ModeChangeListener listener) {
        this.modeChangeListener = listener;
        notifyModeChanged();
    }

    private void onWordModeSelected(String mode) {
        if (!selectedWordMode.equals(mode)) {
            selectedWordMode = mode;
            updateHighlighting();
            notifyModeChanged();
        }
    }

    private void onLanguageSelected(String language) {
        if (!selectedLanguage.equals(language)) {
            selectedLanguage = language;
            updateHighlighting();
            notifyModeChanged();
        }
    }

    private void onTimeModeSelected(String timeMode) {
        if (!selectedTimeMode.equals(timeMode)) {
            selectedTimeMode = timeMode;
            updateHighlighting();
            notifyModeChanged();
        }
    }

    private void onCrazyModeToggled() {
        crazyModeEnabled = !crazyModeEnabled;
        updateHighlighting();
        notifyModeChanged();
    }

    private void notifyModeChanged() {
        if (modeChangeListener != null) {
            modeChangeListener.onModeChanged(selectedWordMode, selectedLanguage, selectedTimeMode, crazyModeEnabled);
        }
    }

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

        applyRoundedPanelBorder(crazyModeNav, crazyModeEnabled ? ACTIVE_COLOR : INACTIVE_COLOR);
    }

    private void setButtonColor(JButton button, boolean active) {
        button.setForeground(active ? ACTIVE_COLOR : INACTIVE_COLOR);
    }

    private JPanel createNavbarPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        panel.setOpaque(false);
        applyRoundedPanelBorder(panel, INACTIVE_COLOR);
        return panel;
    }

    private void applyRoundedPanelBorder(JPanel panel, Color color) {
        panel.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(color, 1, CORNER_RADIUS),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
    }

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

    private Icon loadCrazyModeIcon() {
        URL iconUrl = getClass().getResource("/icons/crazyMode.png");
        if (iconUrl == null) {
            return UIManager.getIcon("OptionPane.warningIcon");
        }

        ImageIcon rawIcon = new ImageIcon(iconUrl);
        Image scaled = rawIcon.getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private static class RoundedBorder extends AbstractBorder {
        private final Color color;
        private final int thickness;
        private final int radius;

        RoundedBorder(Color color, int thickness, int radius) {
            this.color = color;
            this.thickness = thickness;
            this.radius = radius;
        }

        @Override
        public Insets getBorderInsets(java.awt.Component c) {
            return new Insets(thickness, thickness, thickness, thickness);
        }

        @Override
        public Insets getBorderInsets(java.awt.Component c, Insets insets) {
            insets.set(thickness, thickness, thickness, thickness);
            return insets;
        }

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
