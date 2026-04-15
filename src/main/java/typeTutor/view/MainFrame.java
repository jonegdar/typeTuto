package typeTutor.view;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.formdev.flatlaf.FlatClientProperties;

import typeTutor.model.SessionHistoryTracker.Entry;
import typeTutor.model.TypingStats;

/**
 * Main window for the application.
 * Owns layout and view composition only.
 */
public class MainFrame extends JFrame {
    private static final Color APP_BG = new Color(31, 31, 31);
    private static final int TITLE_BAR_HEIGHT = 34;
    private static final int WINDOW_CORNER_RADIUS = 26;

    private final Headings headerPanel;
    private final NavsPanel navPanel;
    private final TypingPanel typingPanel;
    private final GameStatsPanel statsPanel;
    private final JPanel contentPanel;
    private final DimOverlayPane dimOverlayPane;
    private final JPanel titleBar;
    private final JPanel titleDragArea;
    private final JPanel trafficLights;
    private final JButton closeButton;
    private final JButton minimizeButton;
    private final JButton maximizeButton;

    private InactivityOverlayDialog inactivityOverlayDialog;
    private TypingStatsDialog typingStatsDialog;
    private ScoringInfoDialog scoringInfoDialog;
    private SessionHistoryDialog sessionHistoryDialog;
    private List<Entry> sessionHistoryEntries;

    public MainFrame() {
        headerPanel = new Headings();
        navPanel = new NavsPanel();
        typingPanel = new TypingPanel();
        statsPanel = new GameStatsPanel();
        contentPanel = new JPanel(null);
        dimOverlayPane = new DimOverlayPane();

        titleBar = new JPanel(null);
        titleBar.setOpaque(true);
        titleBar.setBackground(APP_BG);

        trafficLights = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 7, 8));
        trafficLights.setOpaque(false);
        closeButton = new TrafficLightButton(new Color(255, 95, 87));
        minimizeButton = new TrafficLightButton(new Color(255, 189, 46));
        maximizeButton = new TrafficLightButton(new Color(40, 201, 64));
        trafficLights.add(closeButton);
        trafficLights.add(minimizeButton);
        trafficLights.add(maximizeButton);

        titleDragArea = new JPanel(new BorderLayout());
        titleDragArea.setOpaque(false);
        titleDragArea.putClientProperty(FlatClientProperties.COMPONENT_TITLE_BAR_CAPTION, Boolean.TRUE);
        titleBar.putClientProperty(FlatClientProperties.COMPONENT_TITLE_BAR_CAPTION, Boolean.TRUE);
        sessionHistoryEntries = new ArrayList<>();

        initializeFrame();
        initializeDialogs();
        layoutComponents();
    }

    private void initializeFrame() {
        setUndecorated(true);
        setResizable(true);
        setTitle("TypeTuto");
        setSize(1200, 648);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setWindowIcon();

        // FlatLaf window decorations with full-window content lets us draw our own traffic lights
        // while keeping native-feeling drag/maximize behavior.
        getRootPane().putClientProperty(FlatClientProperties.USE_WINDOW_DECORATIONS, Boolean.TRUE);
        getRootPane().putClientProperty(FlatClientProperties.FULL_WINDOW_CONTENT, Boolean.TRUE);
        getRootPane().putClientProperty(FlatClientProperties.TITLE_BAR_SHOW_TITLE, Boolean.FALSE);
        getRootPane().putClientProperty(FlatClientProperties.TITLE_BAR_SHOW_ICON, Boolean.FALSE);
        getRootPane().putClientProperty(FlatClientProperties.TITLE_BAR_SHOW_CLOSE, Boolean.FALSE);
        getRootPane().putClientProperty(FlatClientProperties.TITLE_BAR_SHOW_MAXIMIZE, Boolean.FALSE);
        getRootPane().putClientProperty(FlatClientProperties.TITLE_BAR_SHOW_ICONIFFY, Boolean.FALSE);

        contentPanel.setBackground(APP_BG);
        contentPanel.setOpaque(true);
        setContentPane(contentPanel);

        setGlassPane(dimOverlayPane);
        dimOverlayPane.setVisible(false);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                applyWindowShape();
            }
        });
        SwingUtilities.invokeLater(this::applyWindowShape);
    }

    private void applyWindowShape() {
        if ((getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH) {
            setShape(null);
            return;
        }
        if (getWidth() > 0 && getHeight() > 0) {
            setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(),
                    WINDOW_CORNER_RADIUS, WINDOW_CORNER_RADIUS));
        }
    }

    private void initializeDialogs() {
        inactivityOverlayDialog = new InactivityOverlayDialog(this);
        typingStatsDialog = new TypingStatsDialog(this);
        scoringInfoDialog = new ScoringInfoDialog(this);
        sessionHistoryDialog = new SessionHistoryDialog(this);
    }

    private void layoutComponents() {
        contentPanel.add(titleBar);
        contentPanel.add(headerPanel);
        contentPanel.add(navPanel);
        contentPanel.add(typingPanel);
        contentPanel.add(statsPanel);

        closeButton.addActionListener(e -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));
        minimizeButton.addActionListener(e -> setState(JFrame.ICONIFIED));
        maximizeButton.addActionListener(e -> toggleMaximizeRestore());

        titleBar.add(trafficLights);
        titleBar.add(titleDragArea);
        navPanel.setUtilityActions(this::showSessionHistoryDialog, this::showScoringInfoDialog);

        MouseAdapter dblClickMaximize = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    toggleMaximizeRestore();
                }
            }
        };
        titleBar.addMouseListener(dblClickMaximize);
        titleDragArea.addMouseListener(dblClickMaximize);

        contentPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                layoutByPercentages();
            }
        });

        SwingUtilities.invokeLater(this::layoutByPercentages);
    }

    private void layoutByPercentages() {
        int width = contentPanel.getWidth();
        int height = contentPanel.getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        titleBar.setBounds(0, 0, width, TITLE_BAR_HEIGHT);
        trafficLights.setBounds(16, 6, 90, TITLE_BAR_HEIGHT);
        titleDragArea.setBounds(0, 0, width, TITLE_BAR_HEIGHT);

        int usableHeight = height - TITLE_BAR_HEIGHT;
        int headingHeight = Math.round(usableHeight * 0.20f);
        int navHeight = Math.round(usableHeight * 0.10f);
        int typingHeight = Math.round(usableHeight * 0.60f);
        int statsHeight = usableHeight - headingHeight - navHeight - typingHeight;

        int currentY = 0;
        currentY += TITLE_BAR_HEIGHT;
        headerPanel.setBounds(0, currentY, width, headingHeight);
        currentY += headingHeight;

        navPanel.setBounds(0, currentY, width, navHeight);
        currentY += navHeight;

        typingPanel.setBounds(0, currentY, width, typingHeight);
        currentY += typingHeight;

        statsPanel.setBounds(0, currentY, width, statsHeight);
    }

    private void setWindowIcon() {
        URL iconUrl = getClass().getResource("/icons/logo.png");
        if (iconUrl == null) {
            return;
        }
        Image image = new ImageIcon(iconUrl).getImage();
        setIconImage(image);
    }

    private void toggleMaximizeRestore() {
        if ((getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH) {
            setExtendedState(JFrame.NORMAL);
            applyWindowShape();
        } else {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
            applyWindowShape();
        }
    }

    public NavsPanel getNavPanel() {
        return navPanel;
    }

    public TypingPanel getTypingPanel() {
        return typingPanel;
    }

    public GameStatsPanel getStatsPanel() {
        return statsPanel;
    }

    public void setSessionHistoryEntries(List<Entry> entries) {
        sessionHistoryEntries = new ArrayList<>(entries);
    }

    public void showTypingStatsDialog(TypingStats stats) {
        typingStatsDialog.showStats(stats);
    }

    public void showScoringInfoDialog() {
        scoringInfoDialog.showInfo();
    }

    public void showSessionHistoryDialog() {
        sessionHistoryDialog.showHistory(sessionHistoryEntries);
    }

    public void showInactivityCountdown(int secondsRemaining) {
        inactivityOverlayDialog.showCountdown(secondsRemaining);
    }

    public void hideInactivityCountdown() {
        inactivityOverlayDialog.hideOverlay();
    }

    public void setBlurStrength(float strength) {
        dimOverlayPane.setOverlayStrength(strength);
    }

    public void setBlurVisible(boolean visible) {
        if (visible) {
            dimOverlayPane.refreshBlurSnapshot(getContentPane());
        }
        dimOverlayPane.setVisible(visible);
    }

    public void setDistractionFreeMode(boolean enabled) {
        float alpha = enabled ? 0f : 1f;
        setPanelAlpha(headerPanel, alpha);
        setPanelAlpha(navPanel, alpha);
        setPanelAlpha(statsPanel, alpha);
        repaint();
    }

    private void setPanelAlpha(JPanel panel, float alpha) {
        if (panel instanceof SupportsAlpha alphaPanel) {
            alphaPanel.setAlpha(alpha);
        } else {
            panel.setVisible(alpha > 0f);
        }
    }

    public interface SupportsAlpha {
        void setAlpha(float alpha);
    }

    private static class DimOverlayPane extends JPanel {
        private float overlayStrength = 0.45f;
        private BufferedImage blurredBackground;

        DimOverlayPane() {
            setLayout(new BorderLayout());
            setOpaque(false);
            setVisible(false);
        }

        void setOverlayStrength(float strength) {
            overlayStrength = Math.max(0f, Math.min(strength, 1f));
            repaint();
        }

        void refreshBlurSnapshot(Container contentPane) {
            int width = contentPane.getWidth();
            int height = contentPane.getHeight();
            if (width <= 0 || height <= 0) {
                blurredBackground = null;
                return;
            }

            BufferedImage source = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = source.createGraphics();
            contentPane.paint(g2);
            g2.dispose();

            int scaledWidth = Math.max(1, width / 7);
            int scaledHeight = Math.max(1, height / 7);
            BufferedImage scaled = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D small = scaled.createGraphics();
            small.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            small.drawImage(source, 0, 0, scaledWidth, scaledHeight, null);
            small.dispose();

            BufferedImage expanded = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D large = expanded.createGraphics();
            large.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            large.drawImage(scaled, 0, 0, width, height, null);
            large.dispose();
            blurredBackground = expanded;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            if (blurredBackground != null) {
                g2.drawImage(blurredBackground, 0, 0, null);
            }
            g2.setComposite(AlphaComposite.SrcOver.derive(overlayStrength));
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class TrafficLightButton extends JButton {
        private final Color fill;

        TrafficLightButton(Color fill) {
            this.fill = fill;
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setPreferredSize(new java.awt.Dimension(12, 12));
            setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
            g2.setColor(new Color(0, 0, 0, 55));
            g2.setStroke(new BasicStroke(1f));
            g2.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
            g2.dispose();
        }
    }

}
