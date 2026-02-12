package typeTutor.app;

import javax.swing.SwingUtilities;

import typeTutor.view.AppFonts;
import typeTutor.view.MainFrame;

public class App {
    public static void main(String[] args) {
        AppFonts.init();
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
            }
        });

        System.out.println("Hello, World!");
    }
}
