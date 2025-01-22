package main;

import javax.swing.*;
import java.awt.*;

import static main.Constants.*;

public class Settings extends JPanel {

    public Settings(Main parentWindow) {
        setBackground(Color.BLACK);
        setLayout(new BorderLayout());
        String theme = JsonHandler.readJson(PREFERENCES_THEME);
        boolean soundEnabled = Boolean.parseBoolean(JsonHandler.readJson(PREFERENCES_SOUND)); // Default to true

        // Title with Margin
        JLabel title = new JLabel("Settings");
        title.setFont(Utils.deriveFont(80, Font.BOLD));
        title.setForeground(Color.white);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBorder(BorderFactory.createEmptyBorder(200, 100, 0, 650)); // Top, left, bottom, right
        add(title, BorderLayout.NORTH);

        // Dropdown for theme selection
        String[] themes = {GREEN_THEME_TEXT, BROWN_THEME_TEXT, BLUE_THEME_TEXT};
        JPanel dropdownPanel = getDropdownPanel(themes, theme);

        // Sound Toggle Button
        JPanel soundPanel = getSoundTogglePanel(soundEnabled);

        // Center Panel for Layout
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Color.BLACK);
        centerPanel.add(dropdownPanel);
        centerPanel.add(soundPanel);

        add(centerPanel, BorderLayout.CENTER);

        Utils.createMenuButton(parentWindow, this);
    }

    private static JPanel getDropdownPanel(String[] themes, String theme) {
        JComboBox<String> themeSelector = new JComboBox<>(themes);

        // Set the current theme as the default selection
        if (theme != null) {
            switch (theme) {
                case GREEN_THEME:
                    themeSelector.setSelectedItem(GREEN_THEME_TEXT);
                    break;
                case BROWN_THEME:
                    themeSelector.setSelectedItem(BROWN_THEME_TEXT);
                    break;
                case BLUE_THEME:
                    themeSelector.setSelectedItem(BLUE_THEME_TEXT);
                    break;
            }
        }

        themeSelector.addActionListener(e -> {
            String selectedTheme = (String) themeSelector.getSelectedItem();
            if (GREEN_THEME_TEXT.equals(selectedTheme)) {
                Utils.applyTheme(GREEN_THEME);
                JsonHandler.writeJson(PREFERENCES_THEME, GREEN_THEME);
            } else if (BROWN_THEME_TEXT.equals(selectedTheme)) {
                Utils.applyTheme(BROWN_THEME);
                JsonHandler.writeJson(PREFERENCES_THEME, BROWN_THEME);
            } else if (BLUE_THEME_TEXT.equals(selectedTheme)) {
                Utils.applyTheme(BLUE_THEME);
                JsonHandler.writeJson(PREFERENCES_THEME, BLUE_THEME);
            }
        });

        // Add the dropdown to the panel
        JPanel dropdownPanel = new JPanel();
        dropdownPanel.setBackground(Color.BLACK);
        dropdownPanel.setBorder(BorderFactory.createEmptyBorder(50, 0, 0, 650));
        dropdownPanel.add(themeSelector);
        return dropdownPanel;
    }

    private static JPanel getSoundTogglePanel(boolean soundEnabled) {
        JToggleButton soundToggle = new JToggleButton("Sound: " + (soundEnabled ? "ON" : "OFF"), soundEnabled);
        soundToggle.setFont(Utils.deriveFont(20, Font.BOLD));
        soundToggle.setFocusPainted(false);
        soundToggle.setBackground(Color.GRAY);
        soundToggle.setForeground(Color.BLACK);
        soundToggle.setOpaque(true);
        soundToggle.setBorderPainted(false);

        soundToggle.addItemListener(e -> {
            boolean isEnabled = soundToggle.isSelected();
            soundToggle.setText("Sound: " + (isEnabled ? "ON" : "OFF"));
            soundToggle.setBackground(isEnabled ? Color.GRAY : Color.DARK_GRAY); // Maintain background color
            JsonHandler.writeJson(PREFERENCES_SOUND, String.valueOf(isEnabled));
        });

        JPanel soundPanel = new JPanel();
        soundPanel.setBackground(Color.BLACK);
        soundPanel.setBorder(BorderFactory.createEmptyBorder(20, 90, 400, 750));
        soundPanel.add(soundToggle);
        return soundPanel;
    }

}
