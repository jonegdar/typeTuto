package typeTutor.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

/**
 * Modal lock dialog that requires a correct PIN before typing can resume.
 */
public class LockDialog extends JDialog {
    private static final int CORNER_RADIUS = 40;
    /**
     * Outcome of one lock-dialog interaction.
     */
    public enum DialogAction {
        CHECK_PIN,
        CLOSE_APP,
        TIMEOUT
    }

    /**
     * Result payload for one dialog submission.
     */
    public static class DialogResult {
        private final DialogAction action;
        private final String enteredPin;

        /**
         * Creates one dialog result.
         */
        public DialogResult(DialogAction action, String enteredPin) {
            this.action = action;
            this.enteredPin = enteredPin;
        }

        /**
         * Returns the selected action.
         */
        public DialogAction getAction() {
            return action;
        }

        /**
         * Returns the entered PIN when applicable.
         */
        public String getEnteredPin() {
            return enteredPin;
        }
    }

    private static final int RESPONSE_TIMEOUT_SECONDS = 10;

    private JLabel pinLabel;
    private JLabel helperLabel;
    private JTextField inputField;
    private DialogAction dialogAction;
    private Timer responseTimer;
    private int responseTimeLeft;

    /**
     * Creates the lock dialog UI once and reuses it for each lock cycle.
     */
    public LockDialog(Frame owner) {
        super(owner, "TypeTuto - Session Check", true);
        setUndecorated(true);
        setAlwaysOnTop(true);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setResizable(false);
        setContentPane(buildContent());
        pack();
        setBackground(new Color(0, 0, 0, 0));
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                applyRoundedShape();
            }
        });
        applyRoundedShape();
    }

    /**
     * Shows the dialog and returns the chosen action plus any entered PIN.
     */
    public DialogResult promptForPin(String expectedPin) {
        pinLabel.setText(expectedPin);
        inputField.setText("");
        dialogAction = null;
        responseTimeLeft = RESPONSE_TIMEOUT_SECONDS;
        updateHelperText();
        startResponseTimer();
        pack();
        applyRoundedShape();
        setLocationRelativeTo(getOwner());
        setVisible(true);
        stopResponseTimer();
        return new DialogResult(dialogAction, inputField.getText().trim());
    }

    /**
     * Shows an error for an incorrect PIN attempt.
     */
    public void showWrongPinMessage() {
        JOptionPane.showMessageDialog(
                getOwner(),
                "Wrong PIN. Please try again.",
                "TypeTuto - Incorrect PIN",
                JOptionPane.ERROR_MESSAGE,
                loadDialogIcon());
    }

    /**
     * Builds dialog layout and actions.
     */
    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(new Color(18, 18, 24));
        content.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel cardPanel = new JPanel();
        cardPanel.setBackground(new Color(22, 22, 30));
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(76, 76, 102), 2),
                new EmptyBorder(22, 22, 18, 22)));
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Session Check");
        titleLabel.setFont(AppFonts.uiExtraBold(21f));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Enter the PIN shown below to continue typing.");
        subtitleLabel.setFont(AppFonts.uiRegular(13f));
        subtitleLabel.setForeground(new Color(205, 205, 220));
        subtitleLabel.setAlignmentX(CENTER_ALIGNMENT);
        subtitleLabel.setBorder(BorderFactory.createEmptyBorder(7, 0, 14, 0));

        helperLabel = new JLabel("", SwingConstants.CENTER);
        helperLabel.setFont(AppFonts.uiRegular(11f));
        helperLabel.setForeground(new Color(160, 160, 180));
        helperLabel.setAlignmentX(CENTER_ALIGNMENT);

        pinLabel = new JLabel("", SwingConstants.CENTER);
        pinLabel.setFont(AppFonts.monoExtraBold(30f));
        pinLabel.setForeground(new Color(255, 210, 90));
        pinLabel.setOpaque(true);
        pinLabel.setBackground(new Color(32, 32, 48));
        pinLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(76, 76, 102), 2),
                BorderFactory.createEmptyBorder(12, 22, 12, 22)));
        pinLabel.setAlignmentX(CENTER_ALIGNMENT);

        inputField = new JTextField();
        inputField.setMaximumSize(new Dimension(280, 40));
        inputField.setPreferredSize(new Dimension(280, 40));
        inputField.setFont(AppFonts.monoRegular(18f));
        inputField.setHorizontalAlignment(JTextField.CENTER);
        inputField.setAlignmentX(CENTER_ALIGNMENT);
        inputField.setBackground(new Color(242, 242, 246));
        inputField.setForeground(new Color(20, 20, 24));
        inputField.setCaretColor(new Color(20, 20, 24));
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 192, 90), 2),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)));
        inputField.addActionListener(e -> checkPinAndClose());

        JPanel buttons = new JPanel(new GridLayout(1, 2, 10, 0));
        buttons.setOpaque(false);
        buttons.setAlignmentX(CENTER_ALIGNMENT);
        buttons.setMaximumSize(new Dimension(280, 40));
        buttons.setPreferredSize(new Dimension(280, 40));

        JButton closeButton = createButton("Close App");
        closeButton.addActionListener(e -> closeAppAndClose());
        closeButton.setBackground(new Color(176, 56, 56));

        JButton checkPinButton = createButton("Check PIN");
        checkPinButton.addActionListener(e -> checkPinAndClose());
        checkPinButton.setBackground(new Color(255, 192, 90));
        checkPinButton.setForeground(new Color(22, 22, 30));

        buttons.add(closeButton);
        buttons.add(checkPinButton);

        cardPanel.add(titleLabel);
        cardPanel.add(subtitleLabel);
        cardPanel.add(helperLabel);
        cardPanel.add(Box.createVerticalStrut(14));
        cardPanel.add(pinLabel);
        cardPanel.add(Box.createVerticalStrut(14));
        cardPanel.add(inputField);
        cardPanel.add(Box.createVerticalStrut(14));
        cardPanel.add(buttons);
        content.add(cardPanel, BorderLayout.CENTER);
        return content;
    }

    /**
     * Creates a consistent button style for the lock dialog.
     */
    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setFont(AppFonts.uiRegular(13f));
        button.setOpaque(true);
        button.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setBackground(new Color(58, 58, 74));
        button.setForeground(Color.WHITE);
        return button;
    }

    /**
     * Stores the intent to check the PIN and closes the modal dialog.
     */
    private void checkPinAndClose() {
        dialogAction = DialogAction.CHECK_PIN;
        setVisible(false);
    }

    /**
     * Stores the intent to close the app and closes the modal dialog.
     */
    private void closeAppAndClose() {
        dialogAction = DialogAction.CLOSE_APP;
        setVisible(false);
    }

    /**
     * Starts the 10-second response countdown.
     */
    private void startResponseTimer() {
        stopResponseTimer();
        responseTimer = new Timer(1000, e -> onResponseTimerTick());
        responseTimer.start();
    }

    /**
     * Stops the response countdown if active.
     */
    private void stopResponseTimer() {
        if (responseTimer != null) {
            responseTimer.stop();
            responseTimer = null;
        }
    }

    /**
     * Updates helper text with the remaining auto-close time.
     */
    private void updateHelperText() {
        helperLabel.setText("No response in " + responseTimeLeft + "s will close the app.");
    }

    /**
     * Counts down to auto-close when the dialog is ignored.
     */
    private void onResponseTimerTick() {
        responseTimeLeft--;
        updateHelperText();
        if (responseTimeLeft <= 0) {
            dialogAction = DialogAction.TIMEOUT;
            setVisible(false);
        }
    }

    /**
     * Loads a scaled app icon for lock-related message dialogs.
     */
    private ImageIcon loadDialogIcon() {
        URL iconUrl = getClass().getResource("/icons/logo.png");
        if (iconUrl == null) {
            return null;
        }
        Image image = new ImageIcon(iconUrl).getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH);
        return new ImageIcon(image);
    }

    /**
     * Updates the rounded clipping for the dialog window.
     */
    private void applyRoundedShape() {
        if (getWidth() <= 0 || getHeight() <= 0) {
            return;
        }
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), CORNER_RADIUS, CORNER_RADIUS));
    }
}
