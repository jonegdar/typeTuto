package typeTutor.view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Typing view. This class does not contain game rules.
 * It only renders state and forwards user input to the controller.
 */
public class TypingPanel extends JPanel {
    // Row count and color states for rendering typed characters.
    public static final int ROW_COUNT = 3;
    public static final int DEFAULT_STATE = 0;
    public static final int CORRECT_STATE = 1;
    public static final int WRONG_STATE = 2;

    // Timer label and row containers for typing text.
    private final JLabel timerLabel;
    private final JPanel wordRowsContainer;
    private final AlphaLabel[] rowLabels;
    private final JButton restartButton;

    // Current render model pushed by the controller.
    private List<String> currentRows;
    private String currentTargetText;
    private char[] shownChars;
    private int[] charStates;
    private int cursorIndex;
    private boolean gameRunning;
    private boolean multiRowTyping;
    private char[] previousTypedChars;
    private int[] previousCharStates;

    // Callback interface used by controller for key/click events.
    public interface InputListener {
        void onCharacterTyped(char value);
        void onBackspace();
        void onTypingAreaFocused();
        void onRestartRequested();
    }

    // Listener assigned by controller.
    private InputListener inputListener;

    /**
     * Builds typing UI components and event wiring.
     */
    public TypingPanel() {
        setBackground(new Color(31, 31, 31));
        setLayout(null);
        setFocusable(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));

        timerLabel = new JLabel("", SwingConstants.CENTER);
        timerLabel.setForeground(new Color(255, 192, 90));
        timerLabel.setFont(AppFonts.ui(30f, Font.BOLD));
        add(timerLabel);

        wordRowsContainer = new JPanel(new GridLayout(ROW_COUNT, 1, 0, 26));
        wordRowsContainer.setOpaque(false);
        add(wordRowsContainer);

        restartButton = new JButton();
        restartButton.setFocusPainted(false);
        restartButton.setBorderPainted(false);
        restartButton.setContentAreaFilled(false);
        restartButton.setOpaque(false);
        restartButton.setBorder(null);
        restartButton.setMargin(new Insets(0, 0, 0, 0));
        restartButton.setIcon(loadScaledIcon("/icons/restart.png", 22, 22));
        restartButton.addActionListener(e -> {
            if (inputListener != null) {
                inputListener.onRestartRequested();
            }
        });
        add(restartButton);

        rowLabels = new AlphaLabel[ROW_COUNT];
        for (int i = 0; i < ROW_COUNT; i++) {
            AlphaLabel row = new AlphaLabel("");
            row.setForeground(Color.GRAY);
            row.setFont(AppFonts.mono(19f, Font.PLAIN));
            row.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
            row.setAlpha(i == 1 ? 1f : 0.3f);
            rowLabels[i] = row;
            wordRowsContainer.add(row);
        }

        // Initial empty render model until controller provides first session state.
        currentRows = new ArrayList<>();
        currentTargetText = "";
        shownChars = new char[0];
        charStates = new int[0];
        cursorIndex = 0;
        gameRunning = true;
        multiRowTyping = false;
        previousTypedChars = null;
        previousCharStates = null;

        setupInputHandlers();
        setTimerSeconds(60);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                layoutPanelContent();
            }
        });
    }

    /**
     * Assigns controller callback target.
     */
    public void setInputListener(InputListener listener) {
        this.inputListener = listener;
    }

    /**
     * Updates only the timer text.
     */
    public void setTimerSeconds(int seconds) {
        timerLabel.setText(seconds + "s");
    }

    /**
     * Applies a full render state from controller and repaints the rows.
     */
    public void renderState(
            List<String> rows,
            String targetText,
            char[] visibleChars,
            int[] states,
            int cursor,
            boolean running,
            char[] previousChars,
            int[] previousStates,
            boolean multiRowTyping) {
        currentRows = new ArrayList<>(rows);
        currentTargetText = targetText;
        shownChars = visibleChars.clone();
        charStates = states.clone();
        cursorIndex = cursor;
        gameRunning = running;
        previousTypedChars = previousChars == null ? null : previousChars.clone();
        previousCharStates = previousStates == null ? null : previousStates.clone();
        this.multiRowTyping = multiRowTyping;
        renderRows();
    }

    /**
     * Ensures key events go to this view.
     */
    public void focusTypingArea() {
        requestFocusInWindow();
        renderRows();
    }

    /**
     * Wires click and keyboard forwarding to the assigned controller callback.
     */
    private void setupInputHandlers() {
        MouseAdapter clickFocus = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                focusTypingArea();
                if (inputListener != null) {
                    inputListener.onTypingAreaFocused();
                }
            }
        };
        addMouseListener(clickFocus);
        wordRowsContainer.addMouseListener(clickFocus);
        for (JLabel rowLabel : rowLabels) {
            rowLabel.addMouseListener(clickFocus);
        }

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                renderRows();
            }

            @Override
            public void focusLost(FocusEvent e) {
                renderRows();
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && inputListener != null) {
                    inputListener.onBackspace();
                    e.consume();
                }
            }

            @Override
            public void keyTyped(KeyEvent e) {
                if (inputListener == null) {
                    return;
                }
                char typed = e.getKeyChar();
                if (Character.isISOControl(typed)) {
                    return;
                }
                inputListener.onCharacterTyped(typed);
                e.consume();
            }
        });
    }

    /**
     * Renders the three rows with caret and per-character colors.
     */
    private void renderRows() {
        boolean showCaret = isFocusOwner() && gameRunning;

        for (int rowIndex = 0; rowIndex < ROW_COUNT; rowIndex++) {
            String rowText = rowIndex < currentRows.size() ? currentRows.get(rowIndex) : "";
            int rowLength = rowText.length();
            rowLabels[rowIndex].setAlpha(multiRowTyping ? 1f : (rowIndex == 1 ? 1f : 0.3f));

            StringBuilder html = new StringBuilder("<html><div style='text-align:center;'>");

            boolean isActiveRow = multiRowTyping || (rowIndex == 1);
            boolean isPreviousRow = (rowIndex == 0);
            char[] renderChars = null;
            int[] renderStates = null;
            if (isPreviousRow && previousTypedChars != null && previousCharStates != null) {
                renderChars = previousTypedChars;
                renderStates = previousCharStates;
                rowLength = renderChars.length;
            }

            int rowStart = 0;
            if (multiRowTyping) {
                rowStart = computeRowStarts(currentRows)[rowIndex];
            }
            for (int localIndex = 0; localIndex < rowLength; localIndex++) {
                int thisIndex = multiRowTyping ? (rowStart + localIndex) : localIndex;
                if (isActiveRow && showCaret && cursorIndex == thisIndex) {
                    html.append("<span style='color:#ffc05a;'>|</span>");
                }

                if (!isActiveRow) {
                    if (isPreviousRow && renderChars != null && renderStates != null) {
                        char c = renderChars[localIndex];
                        String color = colorForStateFromArray(renderStates, localIndex);
                        html.append("<span style='color:").append(color).append(";'>")
                                .append(escapeHtml(c))
                                .append("</span>");
                    } else {
                        html.append("<span style='color:#9b9b9b;'>")
                                .append(escapeHtml(rowText.charAt(localIndex)))
                                .append("</span>");
                    }
                    continue;
                }

                char currentChar = thisIndex < shownChars.length ? shownChars[thisIndex] : rowText.charAt(localIndex);
                String color = colorForState(thisIndex);
                html.append("<span style='color:").append(color).append(";'>")
                        .append(escapeHtml(currentChar))
                        .append("</span>");
            }

            int endIndex = multiRowTyping ? (rowStart + rowLength) : rowLength;
            if (isActiveRow && showCaret && cursorIndex == endIndex) {
                html.append("<span style='color:#ffc05a;'>|</span>");
            }

            html.append("</div></html>");
            rowLabels[rowIndex].setText(html.toString());
        }
    }

    private int[] computeRowStarts(List<String> rows) {
        int[] starts = new int[ROW_COUNT];
        int running = 0;
        for (int i = 0; i < ROW_COUNT; i++) {
            starts[i] = running;
            String row = i < rows.size() ? rows.get(i) : "";
            running += row.length();
            if (i < ROW_COUNT - 1) {
                running += 1;
            }
        }
        return starts;
    }

    /**
     * Maps state value to display color.
     */
    private String colorForState(int index) {
        if (index < 0 || index >= charStates.length) {
            return "#9b9b9b";
        }

        return switch (charStates[index]) {
            case CORRECT_STATE -> "#57e389";
            case WRONG_STATE -> "#ff6b6b";
            default -> "#9b9b9b";
        };
    }

    private String colorForStateFromArray(int[] states, int index) {
        if (states == null || index < 0 || index >= states.length) {
            return "#9b9b9b";
        }
        return switch (states[index]) {
            case CORRECT_STATE -> "#57e389";
            case WRONG_STATE -> "#ff6b6b";
            default -> "#9b9b9b";
        };
    }

    /**
     * Escapes special characters for HTML JLabel rendering.
     */
    private String escapeHtml(char value) {
        if (value == '<') {
            return "&lt;";
        }
        if (value == '>') {
            return "&gt;";
        }
        if (value == '&') {
            return "&amp;";
        }
        if (value == ' ') {
            return "&nbsp;";
        }
        return Character.toString(value);
    }

    /**
     * Resizes timer and row region according to panel size.
     */
    public void layoutPanelContent() {
        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        int timerHeight = Math.max(36, Math.round(height * 0.20f));
        timerLabel.setBounds(0, 8, width, timerHeight);

        int rowsWidth = Math.round(width * 0.90f);
        int rowsHeight = Math.round(height * 0.50f);
        int rowsX = (width - rowsWidth) / 2;
        int rowsY = timerHeight + Math.round(height * 0.06f);

        wordRowsContainer.setBounds(rowsX, rowsY, rowsWidth, rowsHeight);

        int buttonWidth = 32;
        int buttonHeight = 32;
        int buttonX = (width - buttonWidth) / 2;
        int buttonY = rowsY + rowsHeight + 10;
        restartButton.setBounds(buttonX, buttonY, buttonWidth, buttonHeight);
    }

    private ImageIcon loadScaledIcon(String resourcePath, int width, int height) {
        URL iconUrl = getClass().getResource(resourcePath);
        if (iconUrl == null) {
            return null;
        }
        Image image = new ImageIcon(iconUrl).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(image);
    }

    /**
     * JLabel that can paint itself with adjustable opacity.
     */
    private static class AlphaLabel extends JLabel {
        private float alpha = 1f;

        AlphaLabel(String text) {
            super(text, SwingConstants.CENTER);
            setOpaque(false);
        }

        void setAlpha(float alpha) {
            this.alpha = Math.max(0f, Math.min(1f, alpha));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setComposite(AlphaComposite.SrcOver.derive(alpha));
            super.paintComponent(g2);
            g2.dispose();
        }
    }
}
