package typeTutor.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Lightweight warning dialog shown shortly before auto-lock.
 */
public class InactivityWarningDialog extends JDialog {
    private final JLabel countdownLabel;

    /**
     * Creates a non-modal countdown warning dialog.
     */
    public InactivityWarningDialog(Frame owner) {
        super(owner, "TypeTuto - Inactivity Warning", Dialog.ModalityType.MODELESS);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setResizable(false);

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBackground(new Color(24, 24, 32));
        content.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        JLabel titleLabel = new JLabel("Session will lock soon", SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(AppFonts.ui(18f, Font.BOLD));

        countdownLabel = new JLabel("", SwingConstants.CENTER);
        countdownLabel.setForeground(new Color(255, 210, 90));
        countdownLabel.setFont(AppFonts.ui(15f, Font.BOLD));

        JLabel detailLabel = new JLabel("Move the mouse or type to stay active.", SwingConstants.CENTER);
        detailLabel.setForeground(new Color(195, 195, 210));
        detailLabel.setFont(AppFonts.ui(13f, Font.PLAIN));

        content.add(titleLabel, BorderLayout.NORTH);
        content.add(countdownLabel, BorderLayout.CENTER);
        content.add(detailLabel, BorderLayout.SOUTH);
        setContentPane(content);
        pack();
    }

    /**
     * Updates countdown text and shows the dialog near the owner frame.
     */
    public void showCountdown(int secondsRemaining) {
        countdownLabel.setText("Auto-lock in " + secondsRemaining + "s");
        repositionRelativeToOwner();
        if (!isVisible()) {
            setVisible(true);
        }
    }

    /**
     * Hides the warning when the session is active again.
     */
    public void hideWarning() {
        setVisible(false);
    }

    /**
     * Repositions the dialog relative to its owner.
     */
    private void repositionRelativeToOwner() {
        Window owner = getOwner();
        if (owner == null) {
            return;
        }

        int x = owner.getX() + owner.getWidth() - getWidth() - 28;
        int y = owner.getY() + 28;
        setLocation(Math.max(x, 0), Math.max(y, 0));
    }
}
