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
import javax.swing.Timer;

import typeTutor.model.GameSession;
import typeTutor.model.TypingStats;

public class TypingPanel extends JPanel {
    private static final int ROW_COUNT = 3;
    private static final int DEFAULT_STATE = 0;
    private static final int CORRECT_STATE = 1;
    private static final int WRONG_STATE = 2;

    private final JLabel timerLabel;
    private final JPanel wordRowsContainer;
    private final JLabel[] rowLabels;

    private final GameSession gameSession;
    private final Timer uiTimer;
    private StatsUpdateListener statsUpdateListener;

    private List<String> currentRows;
    private String currentTargetText;
    private char[] shownChars;
    private int[] charStates;

    public interface StatsUpdateListener {
        void onStatsUpdated(TypingStats stats);
    }

    public TypingPanel() {
        setBackground(new Color(31, 31, 31));
        setLayout(null);
        setFocusable(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));

        gameSession = new GameSession();

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

        setupInputHandlers();
        loadCurrentTripletFromSession();
        updateTimerLabel();
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                layoutPanelContent();
            }
        });

        uiTimer = new Timer(100, e -> {
            updateTimerLabel();
            if (!gameSession.isGameRunning()) {
                ((Timer) e.getSource()).stop();
                finishSessionAndReset();
            }
        });
    }

    public void setStatsUpdateListener(StatsUpdateListener listener) {
        this.statsUpdateListener = listener;
    }

    public void applySessionOptions(String wordMode, String language, String timeMode, boolean crazyModeEnabled) {
        gameSession.applyNavbarOptions(wordMode, language, timeMode, crazyModeEnabled);
        loadCurrentTripletFromSession();
        updateTimerLabel();
        uiTimer.stop();
    }

    private void setupInputHandlers() {
        MouseAdapter clickFocus = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                renderRows();
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
                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    handleBackspace();
                    e.consume();
                }
            }

            @Override
            public void keyTyped(KeyEvent e) {
                char typed = e.getKeyChar();
                if (Character.isISOControl(typed)) {
                    return;
                }
                handleTypedCharacter(typed);
                e.consume();
            }
        });
    }

    private void handleTypedCharacter(char typed) {
        GameSession.InputResult result = gameSession.processTypedCharacter(typed);
        if (result.isGameStopped()) {
            uiTimer.stop();
            finishSessionAndReset();
            return;
        }

        if (!uiTimer.isRunning()) {
            uiTimer.start();
        }

        if (result.isTripletAdvanced()) {
            loadCurrentTripletFromSession();
            updateTimerLabel();
            return;
        }

        int index = result.getIndex();
        if (index >= 0 && index < shownChars.length) {
            shownChars[index] = result.getTypedChar();
            charStates[index] = result.isCorrect() ? CORRECT_STATE : WRONG_STATE;
        }

        updateTimerLabel();
        renderRows();

        if (!gameSession.isGameRunning()) {
            uiTimer.stop();
            finishSessionAndReset();
        }
    }

    private void handleBackspace() {
        GameSession.InputResult result = gameSession.processBackspace();
        if (result.isGameStopped()) {
            uiTimer.stop();
            return;
        }

        int index = result.getIndex();
        if (index >= 0 && index < shownChars.length) {
            shownChars[index] = currentTargetText.charAt(index);
            charStates[index] = DEFAULT_STATE;
        }

        renderRows();
    }

    private void loadCurrentTripletFromSession() {
        List<String> sourceRows = gameSession.getCurrentTripletRows();
        currentRows = new ArrayList<>(ROW_COUNT);
        for (int i = 0; i < ROW_COUNT; i++) {
            String rowText = i < sourceRows.size() ? sourceRows.get(i) : "";
            currentRows.add(rowText);
        }

        currentTargetText = gameSession.getCurrentTargetText();
        shownChars = currentTargetText.toCharArray();
        charStates = new int[currentTargetText.length()];
        renderRows();
    }

    private void updateTimerLabel() {
        timerLabel.setText(gameSession.getRemainingSeconds() + "s");
    }

    private void renderRows() {
        int[] rowStarts = computeRowStarts(currentRows);
        int cursor = gameSession.getCursorIndex();
        boolean showCaret = isFocusOwner() && gameSession.isGameRunning();

        for (int rowIndex = 0; rowIndex < ROW_COUNT; rowIndex++) {
            String rowText = currentRows.get(rowIndex);
            int rowStart = rowStarts[rowIndex];
            int rowLength = rowText.length();

            StringBuilder html = new StringBuilder("<html><div style='text-align:center;'>");

            for (int localIndex = 0; localIndex < rowLength; localIndex++) {
                int globalIndex = rowStart + localIndex;
                if (showCaret && cursor == globalIndex) {
                    html.append("<span style='color:#ffc05a;'>|</span>");
                }

                char currentChar = globalIndex < shownChars.length ? shownChars[globalIndex] : rowText.charAt(localIndex);
                String color = colorForState(globalIndex);
                html.append("<span style='color:").append(color).append(";'>")
                        .append(escapeHtml(currentChar))
                        .append("</span>");
            }

            if (showCaret && cursor == rowStart + rowLength) {
                html.append("<span style='color:#ffc05a;'>|</span>");
            }

            if (rowLength == 0 && showCaret && cursor == rowStart) {
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
            running += rows.get(i).length();
            if (i < ROW_COUNT - 1) {
                running += 1;
            }
        }
        return starts;
    }

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

    private void notifyStatsUpdated() {
        if (statsUpdateListener != null) {
            statsUpdateListener.onStatsUpdated(gameSession.getTypingStats());
        }
    }

    private void finishSessionAndReset() {
        notifyStatsUpdated();
        gameSession.resetForCurrentOptions();
        loadCurrentTripletFromSession();
        updateTimerLabel();
    }
}
