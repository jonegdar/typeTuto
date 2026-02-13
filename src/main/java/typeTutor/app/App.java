package typeTutor.app;

import javax.swing.SwingUtilities;

import typeTutor.controller.MainController;
import typeTutor.view.AppFonts;
import typeTutor.view.MainFrame;

/**
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
            MainFrame frame = new MainFrame();
            MainController controller = new MainController(frame);
            controller.getMainFrame().setVisible(true);
        });
    }
}
