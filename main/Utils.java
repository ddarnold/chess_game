package main;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.swing.*;
import java.awt.*;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import static main.Constants.*;

public class Utils {

    private static Font baseFont;
    private static Color lightColor, darkColor;

    static Font loadCustomFont(String path, float size) {
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

    static Font deriveFont(float size, int style) {
        if (baseFont != null) {
            return baseFont.deriveFont(style, size); // Create a derived font with custom size and style
        }
        return new Font("Serif", style, (int) size); // Fallback
    }

    static String readJson(String key) {
        try (FileReader reader = new FileReader(PREFERENCES_PATH)) {
            // Parse the JSON and fetch the value for the specified key
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            return jsonObject.has(key) ? jsonObject.get(key).getAsString() : null;
        } catch (IOException e) {
            throw new RuntimeException("Error reading the JSON file", e);
        }
    }

    static void writeJson(String key, String value) {
        try (FileReader reader = new FileReader(PREFERENCES_PATH)) {
            // Parse the JSON into an object
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();

            // Update the value
            jsonObject.addProperty(key, value);

            // Write the updated JSON back to the file
            try (FileWriter writer = new FileWriter(PREFERENCES_PATH)) {
                writer.write(jsonObject.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error updating the JSON file", e);
        }
    }

    static void applyTheme(String theme) {
        if (theme.equals(GREEN_THEME)) {
            lightColor = new Color(238, 238, 210);
            darkColor = new Color(118, 150, 86);
        } else if (theme.equals(BROWN_THEME)) {
            lightColor = new Color(210, 165, 125);
            darkColor = new Color(175, 115, 70);
        }
    }

    static Color getLightColor() {
        return lightColor;
    }

    static Color getDarkColor() {
        return darkColor;
    }

    static RoundedButton createRoundedButton(String text) {
        RoundedButton button = new RoundedButton(text);
        button.setFont(Utils.deriveFont(25, Font.PLAIN));
        button.setBackground(Color.DARK_GRAY);
        button.setForeground(Color.WHITE);
        button.setCornerRadius(40);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    static void returnToHome(JFrame parentWindow, GamePanel gamePanel) {
        if (gamePanel != null) {
            gamePanel.stopGame();
        }

        parentWindow.getContentPane().removeAll();

        Menu menu = new Menu(parentWindow);
        Header header = new Header(parentWindow);
        parentWindow.add(header, BorderLayout.NORTH);
        parentWindow.add(menu);

        parentWindow.revalidate();
        parentWindow.repaint();
    }


    static void createHomeButton(JFrame parentWindow) {
        // Add Home button to the bottom-right
        RoundedButton homeButton = createRoundedButton("Home");
        homeButton.addActionListener(e -> returnToHome(parentWindow, null)); // Action for the button
        homeButton.setPreferredSize(new Dimension(200, 50)); // Fixed size for the button
        homeButton.setMargin(new Insets(0, 0, 20, 20));

        // Panel for the button to position it at bottom-right
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.BLACK); // Make the panel's background match
        buttonPanel.add(homeButton);

        parentWindow.add(buttonPanel, BorderLayout.SOUTH); // Add panel to the bottom
    }

}
