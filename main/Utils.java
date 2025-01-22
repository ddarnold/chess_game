package main;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

import static main.Constants.*;

public class Utils {

    private static Font baseFont;
    private static Color lightColor, darkColor;


    static void loadAndSetCustomFont() {
        try {
            // Load the base font only once
            if (baseFont == null) {
                InputStream is = Utils.class.getResourceAsStream("/res/font/vidaloka.ttf");
                if (is == null) {
                    throw new IOException("Font file not found: " + "/res/font/vidaloka.ttf");
                }
                baseFont = Font.createFont(Font.TRUETYPE_FONT, is);
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(baseFont); // Register the font
            }
            Font font = baseFont.deriveFont((float) 24.0);

            UIManager.put("Label.font", font);
            UIManager.put("Button.font", font);
            UIManager.put("TextField.font", font);
            UIManager.put("TextArea.font", font);
            UIManager.put("ComboBox.font", font);
            UIManager.put("CheckBox.font", font);
            UIManager.put("RadioButton.font", font);

        } catch (FontFormatException | IOException ignored) {
        }
    }

    static Font deriveFont(float size, int style) {
        if (baseFont != null) {
            return baseFont.deriveFont(style, size); // Create a derived font with custom size and style
        }
        return new Font("Serif", style, (int) size); // Fallback
    }

    static void applyTheme(String theme) {
        if (theme.equals(GREEN_THEME)) {
            lightColor = new Color(238, 238, 210);
            darkColor = new Color(118, 150, 86);
        } else if (theme.equals(BROWN_THEME)) {
            lightColor = new Color(210, 165, 125);
            darkColor = new Color(175, 115, 70);
        }else if (theme.equals(BLUE_THEME)) {
            lightColor = new Color(160, 207, 234);
            darkColor = new Color(93, 140, 214);
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

    public static void createMenuButton(Main parentWindow, JPanel mainPanel) {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.BLACK);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        RoundedButton homeButton = createRoundedButton("Menu");
        homeButton.setMinimumSize(new Dimension(200, 50));
        homeButton.addActionListener(e ->
                parentWindow.switchToPanel(new Menu(parentWindow))
        );
        buttonPanel.add(homeButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

}
