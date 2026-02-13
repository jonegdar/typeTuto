package typeTutor.view;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.InputStream;

/**
 * Central font loader for all UI files.
 */
public final class AppFonts {
    // Base font files loaded once from classpath resources.
    private static Font montserratBase;
    private static Font jetBrainsBase;

    /**
     * Utility class; no public constructor.
     */
    private AppFonts() {
    }

    /**
     * Loads and registers custom fonts once.
     */
    public static void init() {
        if (montserratBase != null) {
            return;
        }

        try (InputStream montserrat = AppFonts.class.getResourceAsStream("/fonts/Montserrat-VariableFont_wght.ttf");
                InputStream jetBrains = AppFonts.class.getResourceAsStream("/fonts/JetBrainsMono-VariableFont_wght.ttf")) {

            montserratBase = Font.createFont(Font.TRUETYPE_FONT, montserrat);
            jetBrainsBase = Font.createFont(Font.TRUETYPE_FONT, jetBrains);

            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(montserratBase);
            ge.registerFont(jetBrainsBase);
        } catch (Exception e) {
            montserratBase = new Font("SansSerif", Font.PLAIN, 14);
            jetBrainsBase = new Font("Monospaced", Font.PLAIN, 14);
        }
    }

    /**
     * Returns styled UI font from Montserrat family.
     */
    public static Font ui(float size, int style) {
        return montserratBase.deriveFont(style, size);
    }

    /**
     * Returns styled monospace font from JetBrains Mono family.
     */
    public static Font mono(float size, int style) {
        return jetBrainsBase.deriveFont(style, size);
    }
}
