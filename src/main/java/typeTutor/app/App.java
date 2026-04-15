package typeTutor.app;

import javax.swing.SwingUtilities;
import javax.swing.JDialog;
import javax.swing.JFrame;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;

import typeTutor.controller.MainController;
import typeTutor.view.AppFonts;
import typeTutor.view.MainFrame;

/**
 * +
 * 
 * Application entry point.
 * Bootstraps fonts, frame, and the main controller.
 */
public class App {
    /**
     * Starts Swing UI on EDT and wires MVC/MVP components.
     */
    public static void main(String[] args) {
        AppFonts.init();

        SwingUtilities.invokeLater(() -> {
            // Enable FlatLaf window decorations (custom title bar) for a more native-feeling experience
            // while still allowing us to place custom macOS-style traffic-light buttons.
            System.setProperty("flatlaf.useWindowDecorations", "true");
            JFrame.setDefaultLookAndFeelDecorated(true);
            JDialog.setDefaultLookAndFeelDecorated(true);
            FlatLaf.setUseNativeWindowDecorations(false);
            FlatDarkLaf.setup();
            MainFrame frame = new MainFrame();
            MainController controller = new MainController(frame);
            controller.getMainFrame().setVisible(true);
        });
    }
}
