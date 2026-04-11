package typeTutor.view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
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
    private final JLabel[] rowLabels;

    // Current render model pushed by the controller.
    private List<String> currentRows;
    private String currentTargetText;
    private char[] shownChars;
    private int[] charStates;
    private int cursorIndex;
    private boolean gameRunning;

    // Callback interface used by controller for key/click events.
    public interface InputListener {
        void onCharacterTyped(char value);
        void onBackspace();
        void onTypingAreaFocused();
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

        wordRowsContainer = new JPanel(new GridLayout(ROW_COUNT, 1, 0, 12));
        wordRowsContainer.setOpaque(false);
        add(wordRowsContainer);

        rowLabels = new JLabel[ROW_COUNT];
        for (int i = 0; i < ROW_COUNT; i++) {
            JLabel row = new JLabel("", SwingConstants.CENTER);
            row.setForeground(Color.GRAY);
            row.setFont(AppFonts.mono(19f, Font.PLAIN));
            row.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
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
    public void renderState(List<String> rows, String targetText, char[] visibleChars, int[] states, int cursor, boolean running) {
        currentRows = new ArrayList<>(rows);
        currentTargetText = targetText;
        shownChars = visibleChars.clone();
        charStates = states.clone();
        cursorIndex = cursor;
        gameRunning = running;
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
        int[] rowStarts = computeRowStarts(currentRows);
        boolean showCaret = isFocusOwner() && gameRunning;

        for (int rowIndex = 0; rowIndex < ROW_COUNT; rowIndex++) {
            String rowText = rowIndex < currentRows.size() ? currentRows.get(rowIndex) : "";
            int rowStart = rowIndex < rowStarts.length ? rowStarts[rowIndex] : 0;
            int rowLength = rowText.length();

            StringBuilder html = new StringBuilder("<html><div style='text-align:center;'>");

            for (int localIndex = 0; localIndex < rowLength; localIndex++) {
                int globalIndex = rowStart + localIndex;
                if (showCaret && cursorIndex == globalIndex) {
                    html.append("<span style='color:#ffc05a;'>|</span>");
                }

                char currentChar = globalIndex < shownChars.length ? shownChars[globalIndex] : rowText.charAt(localIndex);
                String color = colorForState(globalIndex);
                html.append("<span style='color:").append(color).append(";'>")
                        .append(escapeHtml(currentChar))
                        .append("</span>");
            }

            if (showCaret && cursorIndex == rowStart + rowLength) {
                html.append("<span style='color:#ffc05a;'>|</span>");
            }

            if (rowLength == 0 && showCaret && cursorIndex == rowStart) {
                html.append("<span style='color:#ffc05a;'>|</span>");
            }

            html.append("</div></html>");
            rowLabels[rowIndex].setText(html.toString());
        }
    }

    /**
     * Converts row text lengths to global start indexes.
     */
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
    private String colorForState(int globalIndex) {
        if (globalIndex < 0 || globalIndex >= charStates.length) {
            return "#9b9b9b";
        }

        return switch (charStates[globalIndex]) {
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
        int rowsY = timerHeight + Math.round(height * 0.05f);

        wordRowsContainer.setBounds(rowsX, rowsY, rowsWidth, rowsHeight);
    }
}
