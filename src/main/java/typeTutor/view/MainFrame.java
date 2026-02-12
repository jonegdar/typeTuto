package typeTutor.view;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class MainFrame extends JFrame {
    private Headings headerPanel;
    private NavsPanel navPanel;
    private TypingPanel typingPanel;
    private GameStatsPanel statsPanel;

    public MainFrame() {
        initializeFrame();
        initializeComponents();
        layoutComponents();
    }

    private void initializeFrame() {
        setTitle("TypeTuto");
        setSize(1200, 648);
        setLocationRelativeTo(null); // center
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
    }

    private void initializeComponents() {
        headerPanel = new Headings();
        navPanel = new NavsPanel();
        typingPanel = new TypingPanel();
        statsPanel = new GameStatsPanel();

        navPanel.setModeChangeListener((wordMode, language, timeMode, crazyModeEnabled) -> {
            typingPanel.applySessionOptions(wordMode, language, timeMode, crazyModeEnabled);
            statsPanel.showWaitingState();
        });
        typingPanel.setStatsUpdateListener(stats -> statsPanel.updateStats(stats));
    }

    private void layoutComponents() {
        add(headerPanel);
        add(navPanel);
        add(typingPanel);
        add(statsPanel);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                layoutByPercentages();
            }
        });

        SwingUtilities.invokeLater(this::layoutByPercentages);
    }

    private void layoutByPercentages() {
        int width = getContentPane().getWidth();
        int height = getContentPane().getHeight();

        if (width <= 0 || height <= 0) {
            return;
        }

        int headingHeight = Math.round(height * 0.20f);
        int navHeight = Math.round(height * 0.10f);
        int typingHeight = Math.round(height * 0.50f);
        int statsHeight = height - headingHeight - navHeight - typingHeight;

        int currentY = 0;
        headerPanel.setBounds(0, currentY, width, headingHeight);
        currentY += headingHeight;

        navPanel.setBounds(0, currentY, width, navHeight);
        currentY += navHeight;

        typingPanel.setBounds(0, currentY, width, typingHeight);
        currentY += typingHeight;

        statsPanel.setBounds(0, currentY, width, statsHeight);
    }
}
