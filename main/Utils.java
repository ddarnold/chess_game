package main;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

public class Utils {

    private static Font baseFont;

    public static Font loadCustomFont(String path, float size) {
        try {
            // Load the base font only once
            if (baseFont == null) {
                InputStream is = Utils.class.getResourceAsStream(path);
                if (is == null) {
                    throw new IOException("Font file not found: " + path);
                }
                baseFont = Font.createFont(Font.TRUETYPE_FONT, is);
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(baseFont); // Register the font
            }
            return baseFont.deriveFont(size); // Create a derived font with the given size
        } catch (FontFormatException | IOException e) {
            return new Font("Serif", Font.PLAIN, 24); // Fallback font
        }
    }

    public static Font deriveFont(float size, int style) {
        if (baseFont != null) {
            return baseFont.deriveFont(style, size); // Create a derived font with custom size and style
        }
        return new Font("Serif", style, (int) size); // Fallback
    }
}
