package main;

import javax.swing.*;
import java.awt.*;

import static main.Constants.*;

public class Settings extends JPanel {

    public Settings(Main parentWindow) {
        setBackground(Color.BLACK);
        setLayout(new BorderLayout());
        String theme = JsonHandler.readJson(PREFERENCES_THEME);

        // Title with Margin
        JLabel title = new JLabel("Settings");
        title.setFont(Utils.deriveFont(80, Font.BOLD));
        title.setForeground(Color.white);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBorder(BorderFactory.createEmptyBorder(200, 100, 0, 650)); // Top, left, bottom, right
        add(title, BorderLayout.NORTH);

        // Dropdown for theme selection
        String[] themes = {GREEN_THEME_TEXT, BROWN_THEME_TEXT};
        JPanel dropdownPanel = getDropdownPanel(themes, theme);
        add(dropdownPanel, BorderLayout.CENTER);

        Utils.createMenuButton(parentWindow, this);
    }

    private static JPanel getDropdownPanel(String[] themes, String theme) {
        JComboBox<String> themeSelector = new JComboBox<>(themes);

        // Set the current theme as the default selection
        if (theme != null) {
            themeSelector.setSelectedItem(theme.equals(GREEN_THEME) ? GREEN_THEME_TEXT : BROWN_THEME_TEXT);
        }

        themeSelector.addActionListener(e -> {
            String selectedTheme = (String) themeSelector.getSelectedItem();
            if (GREEN_THEME_TEXT.equals(selectedTheme)) {
                Utils.applyTheme(GREEN_THEME);
                JsonHandler.writeJson(PREFERENCES_THEME, GREEN_THEME);
            } else if (BROWN_THEME_TEXT.equals(selectedTheme)) {
                Utils.applyTheme(BROWN_THEME);
                JsonHandler.writeJson(PREFERENCES_THEME, BROWN_THEME);
            }
        });

        // Add the dropdown to the panel
        JPanel dropdownPanel = new JPanel();
        dropdownPanel.setBackground(Color.BLACK);
        dropdownPanel.setBorder(BorderFactory.createEmptyBorder(50, 0, 0, 650));
        dropdownPanel.add(themeSelector);
        return dropdownPanel;
    }
}
