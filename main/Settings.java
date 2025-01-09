package main;

import javax.swing.*;
import java.awt.*;

import static main.Constants.*;
import static main.Utils.*;

public class Settings extends JPanel {
    private final JFrame parentWindow;
    private final String theme;

    public Settings(JFrame parentWindow) {
        this.parentWindow = parentWindow;
        setPreferredSize(new Dimension(LAYOUT_WIDTH, LAYOUT_HEIGHT));
        setBackground(Color.BLACK);
        setLayout(new BorderLayout());
        theme = Utils.readJson(PREFERENCES_THEME);
        Graphics g = parentWindow.getGraphics();
        Graphics2D g2d = (Graphics2D) g;

        // Title with Margin
        JLabel title = new JLabel("Settings");
        title.setFont(Utils.deriveFont(80, Font.BOLD));
        title.setForeground(Color.white);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBorder(BorderFactory.createEmptyBorder(200, 100, 0, 650)); // Top, left, bottom, right
        add(title, BorderLayout.NORTH);

        // Dropdown for theme selection
        String[] themes = {GREEN_THEME_TEXT, BROWN_THEME_TEXT};
        JComboBox<String> themeSelector = new JComboBox<>(themes);

        // Set the current theme as the default selection
        if (theme != null) {
            themeSelector.setSelectedItem(theme.equals(GREEN_THEME) ? GREEN_THEME_TEXT : BROWN_THEME_TEXT);
        }

        themeSelector.addActionListener(e -> {
            String selectedTheme = (String) themeSelector.getSelectedItem();
            if (GREEN_THEME_TEXT.equals(selectedTheme)) {
                Utils.applyTheme(GREEN_THEME);
                Utils.writeJson(PREFERENCES_THEME, GREEN_THEME);
            } else if (BROWN_THEME_TEXT.equals(selectedTheme)) {
                //Utils.applyTheme(parentWindow, "Dark");
                Utils.writeJson(PREFERENCES_THEME, BROWN_THEME);
            }
        });

        // Add the dropdown to the panel
        JPanel dropdownPanel = new JPanel();
        dropdownPanel.setBackground(Color.BLACK);
        dropdownPanel.setBorder(BorderFactory.createEmptyBorder(50, 100, 50, 650));
        dropdownPanel.add(themeSelector);
        add(dropdownPanel, BorderLayout.CENTER);


        int BUTTON_WIDTH = 200;
        int BUTTON_HEIGHT = 50;

        // Button position
        int buttonX = LAYOUT_WIDTH - BUTTON_WIDTH - 10;
        int buttonY = LAYOUT_HEIGHT - BUTTON_HEIGHT - 40;
        g2d.setFont(Utils.deriveFont(25, Font.PLAIN));
        // Set color and draw filled rounded rectangle
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRoundRect(buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT, 20, 20);

        // Draw the text centered on the button
        g2d.setColor(Color.WHITE);
        String buttonText = "Home";
        FontMetrics metrics = g2d.getFontMetrics();
        int textX = buttonX + (BUTTON_WIDTH - metrics.stringWidth(buttonText)) / 2;
        int textY = buttonY + ((BUTTON_HEIGHT - metrics.getHeight()) / 2) + metrics.getAscent();
        g2d.drawString(buttonText, textX, textY);
    }

}
