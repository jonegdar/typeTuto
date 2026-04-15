package typeTutor.controller;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;

import javax.swing.Timer;

import typeTutor.model.LockState;
import typeTutor.view.LockDialog;
import typeTutor.view.LockDialog.DialogAction;
import typeTutor.view.LockDialog.DialogResult;
import typeTutor.view.MainFrame;

/**
 * Manages inactivity countdown, warning UI, and lock/unlock flow.
 */
public class InactivityController {
    private static final int DEFAULT_TIMEOUT_SECONDS = 30;
    private static final int WARNING_THRESHOLD_SECONDS = 10;
    private static final int MOUSE_SENSITIVITY_THRESHOLD = 10;

    private final MainFrame mainFrame;
    private final LockState lockState;
    private final LockDialog lockDialog;
    private final Timer inactivityTimer;
    private final Runnable onLockAction;
    private final Runnable onUnlockAction;
    private final AWTEventListener mouseMotionListener;

    private int inactivityTimeLeft;
    private int lastMouseX = -1;
    private int lastMouseY = -1;

    /**
     * Creates controller and starts monitoring activity immediately.
     */
    public InactivityController(MainFrame mainFrame, Runnable onLockAction, Runnable onUnlockAction) {
        this.mainFrame = mainFrame;
        this.lockState = new LockState();
        this.lockDialog = new LockDialog(mainFrame);
        this.inactivityTimer = new Timer(1000, e -> onInactivityTick());
        this.onLockAction = onLockAction;
        this.onUnlockAction = onUnlockAction;
        this.mouseMotionListener = this::handleMouseMotionEvent;
        this.inactivityTimeLeft = DEFAULT_TIMEOUT_SECONDS;

        Toolkit.getDefaultToolkit().addAWTEventListener(mouseMotionListener, AWTEvent.MOUSE_MOTION_EVENT_MASK);
        resetTimer();
    }

    /**
     * Records user activity and restarts the inactivity countdown.
     */
    public void recordActivity() {
        if (lockState.isLocked()) {
            return;
        }
        resetTimer();
    }

    /**
     * Returns whether the session is currently locked.
     */
    public boolean isLocked() {
        return lockState.isLocked();
    }

    /**
     * Stops timers and unregisters listeners when the controller is no longer needed.
     */
    public void dispose() {
        inactivityTimer.stop();
        lockDialog.dispose();
        Toolkit.getDefaultToolkit().removeAWTEventListener(mouseMotionListener);
    }

    /**
     * Advances the countdown and locks the app when time runs out.
     */
    private void onInactivityTick() {
        inactivityTimeLeft--;
        updateWarningState();
        if (inactivityTimeLeft <= 0) {
            lockApplication();
        }
    }

    /**
     * Syncs warning dialog and frame overlay with remaining inactivity time.
     */
    private void updateWarningState() {
        if (inactivityTimeLeft <= WARNING_THRESHOLD_SECONDS) {
            mainFrame.showInactivityCountdown(inactivityTimeLeft);
            return;
        }

        mainFrame.hideInactivityCountdown();
    }

    /**
     * Resets the countdown to its full duration.
     */
    private void resetTimer() {
        inactivityTimeLeft = DEFAULT_TIMEOUT_SECONDS;
        mainFrame.hideInactivityCountdown();
        inactivityTimer.restart();
    }

    /**
     * Locks the session, shows the generated PIN, and waits until unlocked.
     */
    private void lockApplication() {
        if (lockState.isLocked()) {
            return;
        }

        lockState.lockWithNewPin();
        inactivityTimer.stop();
        mainFrame.hideInactivityCountdown();
        mainFrame.setBlurStrength(0.665f);
        mainFrame.setBlurVisible(true);

        if (onLockAction != null) {
            onLockAction.run();
        }

        while (lockState.isLocked()) {
            DialogResult result = lockDialog.promptForPin(lockState.getExpectedPin());
            if (result.getAction() == DialogAction.CLOSE_APP || result.getAction() == DialogAction.TIMEOUT) {
                closeApplication();
                return;
            }

            if (result.getAction() == DialogAction.CHECK_PIN
                    && lockState.matchesPin(result.getEnteredPin())) {
                lockState.unlock();
                mainFrame.setBlurVisible(false);
                if (onUnlockAction != null) {
                    onUnlockAction.run();
                }
                resetTimer();
                return;
            }

            lockDialog.showWrongPinMessage();
        }
    }

    /**
     * Closes the application from the lock flow.
     */
    private void closeApplication() {
        inactivityTimer.stop();
        mainFrame.dispose();
        System.exit(0);
    }

    /**
     * Treats meaningful mouse movement as activity.
     */
    private void handleMouseMotionEvent(AWTEvent event) {
        if (!(event instanceof MouseEvent mouseEvent)) {
            return;
        }
        if (mouseEvent.getID() != MouseEvent.MOUSE_MOVED && mouseEvent.getID() != MouseEvent.MOUSE_DRAGGED) {
            return;
        }

        int x = mouseEvent.getXOnScreen();
        int y = mouseEvent.getYOnScreen();
        if (lastMouseX == -1 || lastMouseY == -1) {
            lastMouseX = x;
            lastMouseY = y;
            recordActivity();
            return;
        }

        double distance = Math.hypot(x - lastMouseX, y - lastMouseY);
        if (distance > MOUSE_SENSITIVITY_THRESHOLD) {
            lastMouseX = x;
            lastMouseY = y;
            recordActivity();
        }
    }
}
