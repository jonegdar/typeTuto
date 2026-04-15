package typeTutor.view;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.InputStream;

/**
 * Central font loader for all UI files.
 */
public final class AppFonts {
    // Base font files loaded once from classpath resources.
    private static Font montserratMediumBase;
    private static Font montserratBoldBase;
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
        if (montserratMediumBase != null && montserratBoldBase != null) {
            return;
        }

        try (InputStream montserratMedium = AppFonts.class.getResourceAsStream("/fonts/Montserrat-Medium.ttf");
                InputStream montserratBold = AppFonts.class.getResourceAsStream("/fonts/Montserrat-Bold.ttf");
                InputStream jetBrains = AppFonts.class.getResourceAsStream("/fonts/JetBrainsMono-VariableFont_wght.ttf")) {

            montserratMediumBase = Font.createFont(Font.TRUETYPE_FONT, montserratMedium);
            montserratBoldBase = Font.createFont(Font.TRUETYPE_FONT, montserratBold);
            jetBrainsBase = Font.createFont(Font.TRUETYPE_FONT, jetBrains);

            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(montserratMediumBase);
            ge.registerFont(montserratBoldBase);
            ge.registerFont(jetBrainsBase);
        } catch (Exception e) {
            montserratMediumBase = new Font("SansSerif", Font.PLAIN, 14);
            montserratBoldBase = new Font("SansSerif", Font.BOLD, 14);
            jetBrainsBase = new Font("Monospaced", Font.PLAIN, 14);
        }
    }

    /**
     * Returns styled UI font from Montserrat family.
     */
    public static Font ui(float size, int style) {
        return (style == Font.BOLD ? montserratBoldBase : montserratMediumBase).deriveFont(style, size);
    }

    /**
     * Returns Montserrat Medium 500 for normal UI text.
     */
    public static Font uiRegular(float size) {
        return montserratMediumBase.deriveFont(Font.PLAIN, size);
    }

    /**
     * Returns Montserrat Bold for headings.
     */
    public static Font uiBold(float size) {
        return montserratBoldBase.deriveFont(Font.BOLD, size);
    }

    /**
     * Returns Montserrat Bold for headings.
     */
    public static Font uiExtraBold(float size) {
        return uiBold(size);
    }

    /**
     * Returns styled monospace font from JetBrains Mono family.
     */
    public static Font mono(float size, int style) {
        return jetBrainsBase.deriveFont(style, size);
    }

    /**
     * Returns JetBrains Mono plain for normal text.
     */
    public static Font monoRegular(float size) {
        return jetBrainsBase.deriveFont(Font.PLAIN, size);
    }

    /**
     * Returns JetBrains Mono bold for emphasis.
     */
    public static Font monoExtraBold(float size) {
        return jetBrainsBase.deriveFont(Font.BOLD, size);
    }
}
