package typeTutor.view;

import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Main window for the application.
 * This view only owns layout and component composition.
 */
public class MainFrame extends JFrame {
    // Top heading view, mode selector view, typing view, and bottom stats view.
    private final Headings headerPanel;
    private final NavsPanel navPanel;
    private final TypingPanel typingPanel;
    private final GameStatsPanel statsPanel;

    /**
     * Builds the frame and all child views.
     */
    public MainFrame() {
        headerPanel = new Headings();
        navPanel = new NavsPanel();
        typingPanel = new TypingPanel();
        statsPanel = new GameStatsPanel();

        initializeFrame();
        layoutComponents();
    }

    /**
     * Configures frame-level settings.
     */
    private void initializeFrame() {
        setTitle("TypeTuto");
        setSize(1200, 648);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setWindowIcon();
    }

    /**
     * Loads and applies frame icon from classpath resources.
     */
    private void setWindowIcon() {
        URL iconUrl = getClass().getResource("/icons/logo.png");
        if (iconUrl == null) {
            return;
        }

        Image image = new ImageIcon(iconUrl).getImage();
        setIconImage(image);
    }

    /**
     * Adds child views and keeps percentage layout responsive.
     */
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

    /**
     * Applies fixed vertical proportions:
     * heading 20%, nav 10%, typing 50%, stats 20%.
     */
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

    /**
     * Exposes nav view for controller binding.
     */
    public NavsPanel getNavPanel() {
        return navPanel;
    }

    /**
     * Exposes typing view for controller binding.
     */
    public TypingPanel getTypingPanel() {
        return typingPanel;
    }

    /**
     * Exposes stats view for controller binding.
     */
    public GameStatsPanel getStatsPanel() {
        return statsPanel;
    }
}
