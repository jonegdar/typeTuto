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
import java.awt.Window;
import java.awt.image.BufferedImage;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

/**
 * Always-on-top overlay used for the inactivity countdown.
 * This must sit above normal dialogs (info/history) but below the lock dialog.
 */
public class InactivityOverlayDialog extends JDialog {
    private static final int CORNER_RADIUS = 40;
    private static final Color SAFE_BACKGROUND = new Color(250, 250, 250);
    private static final Color SAFE_TEXT = new Color(26, 26, 26);
    private static final Color DANGER_BACKGROUND = new Color(204, 52, 52);
    private static final Color DANGER_TEXT = Color.WHITE;

    private final OverlayPane overlayPane;

    /**
     * Creates the overlay owned by the supplied window.
     */
    public InactivityOverlayDialog(Window owner) {
        super(owner);
        setUndecorated(true);
        setAlwaysOnTop(true);
        setFocusableWindowState(false);
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        setBackground(new Color(0, 0, 0, 0));
        overlayPane = new OverlayPane();
        setContentPane(overlayPane);
        pack();
    }

    /**
     * Shows the overlay with an updated countdown.
     */
    public void showCountdown(int secondsRemaining) {
        overlayPane.refreshBlurSnapshot(getOwner());
        overlayPane.setCountdown(secondsRemaining);
        syncToOwnerBounds();
        setVisible(true);
        toFront();
    }

    /**
     * Hides the overlay if visible.
     */
    public void hideOverlay() {
        setVisible(false);
    }

    /**
     * Keeps the overlay positioned and sized over the owner window.
     */
    private void syncToOwnerBounds() {
        if (getOwner() == null || !getOwner().isShowing()) {
            return;
        }
        java.awt.Point p = getOwner().getLocationOnScreen();
        setBounds(p.x, p.y, getOwner().getWidth(), getOwner().getHeight());
        overlayPane.revalidate();
        overlayPane.repaint();
    }

    /**
     * Panel that paints blurred snapshot + dim + warning pill.
     */
    private static class OverlayPane extends JPanel {
        private static final float OVERLAY_STRENGTH = 0.315f;

        private final JLabel messageLabel;
        private BufferedImage blurredBackground;
        private Color pillBackground = SAFE_BACKGROUND;

        OverlayPane() {
            setLayout(null);
            setOpaque(false);
            messageLabel = new JLabel("", SwingConstants.CENTER);
            messageLabel.setFont(AppFonts.uiRegular(28f));
            add(messageLabel);
        }

        void setCountdown(int secondsRemaining) {
            boolean danger = secondsRemaining <= 5;
            pillBackground = danger ? DANGER_BACKGROUND : SAFE_BACKGROUND;
            messageLabel.setForeground(danger ? DANGER_TEXT : SAFE_TEXT);
            messageLabel.setText("<html>Inactivity timeout in: <b>" + secondsRemaining + "s</b></html>");
        }

        void refreshBlurSnapshot(Container owner) {
            if (owner == null) {
                blurredBackground = null;
                return;
            }

            int width = owner.getWidth();
            int height = owner.getHeight();
            if (width <= 0 || height <= 0) {
                blurredBackground = null;
                return;
            }

            BufferedImage source = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = source.createGraphics();
            owner.paint(g2);
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
            Image img = scaled;
            large.drawImage(img, 0, 0, width, height, null);
            large.dispose();
            blurredBackground = expanded;
        }

        @Override
        public void doLayout() {
            int width = getWidth();
            int height = getHeight();
            int barWidth = Math.max(360, Math.round(width * 0.60f));
            int barHeight = Math.max(72, Math.round(height * 0.12f));
            int x = (width - barWidth) / 2;
            int y = (height - barHeight) / 2;
            messageLabel.setBounds(x, y, barWidth, barHeight);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            if (blurredBackground != null) {
                g2.drawImage(blurredBackground, 0, 0, null);
            }
            g2.setComposite(AlphaComposite.SrcOver.derive(OVERLAY_STRENGTH));
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Draw pill behind the label bounds.
            int x = messageLabel.getX();
            int y = messageLabel.getY();
            int w = messageLabel.getWidth();
            int h = messageLabel.getHeight();
            int arc = Math.max(h - 2, CORNER_RADIUS);
            g2.setComposite(AlphaComposite.SrcOver);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(pillBackground);
            g2.fillRoundRect(x, y, w - 1, h - 1, arc, arc);
            g2.setColor(pillBackground);
            g2.setStroke(new BasicStroke(3f));
            g2.drawRoundRect(x + 1, y + 1, w - 3, h - 3, arc, arc);
            g2.dispose();

            super.paintComponent(g);
        }
    }
}
