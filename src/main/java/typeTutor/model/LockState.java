package typeTutor.model;

import java.util.Random;

/**
 * Stores current auto-lock state and generated confirmation PIN.
 */
public class LockState {
    private static final int PIN_BOUND = 10_000;

    private final Random pinGenerator;
    private boolean locked;
    private String expectedPin;

    /**
     * Creates unlocked state with its own PIN generator.
     */
    public LockState() {
        this.pinGenerator = new Random();
        this.locked = false;
        this.expectedPin = null;
    }

    /**
     * Locks the session and generates a new four-digit PIN.
     */
    public void lockWithNewPin() {
        locked = true;
        expectedPin = String.format("%04d", pinGenerator.nextInt(PIN_BOUND));
    }

    /**
     * Unlocks the session and clears the active PIN.
     */
    public void unlock() {
        locked = false;
        expectedPin = null;
    }

    /**
     * Returns whether the session is currently locked.
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * Returns the currently active confirmation PIN.
     */
    public String getExpectedPin() {
        return expectedPin;
    }

    /**
     * Returns whether the provided PIN matches the active one.
     */
    public boolean matchesPin(String enteredPin) {
        return locked && expectedPin != null && expectedPin.equals(enteredPin);
    }
}
