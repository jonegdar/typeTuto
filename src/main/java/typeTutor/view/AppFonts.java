package typeTutor.view;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.InputStream;

public final class AppFonts {
    private static Font montserratBase;
    private static Font jetBrainsBase;

    private AppFonts() {}

    public static void init() {
        if (montserratBase != null) return;
        try (InputStream m = AppFonts.class.getResourceAsStream("/fonts/Montserrat-VariableFont_wght.ttf");
             InputStream j = AppFonts.class.getResourceAsStream("/fonts/JetBrainsMono-VariableFont_wght.ttf")) {

            montserratBase = Font.createFont(Font.TRUETYPE_FONT, m);
            jetBrainsBase = Font.createFont(Font.TRUETYPE_FONT, j);

            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(montserratBase);
            ge.registerFont(jetBrainsBase);
        } catch (Exception e) {
            montserratBase = new Font("SansSerif", Font.PLAIN, 14);
            jetBrainsBase = new Font("Monospaced", Font.PLAIN, 14);
        }
    }

    public static Font ui(float size, int style) {
        return montserratBase.deriveFont(style, size);
    }

    public static Font mono(float size, int style) {
        return jetBrainsBase.deriveFont(style, size);
    }
}
