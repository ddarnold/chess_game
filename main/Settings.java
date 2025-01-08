package main;

import javax.swing.*;
import java.awt.*;

import static main.Constants.LAYOUT_HEIGHT;
import static main.Constants.LAYOUT_WIDTH;

public class Settings extends JPanel {
    private final JFrame parentWindow;

    public Settings(JFrame parentWindow) {
        this.parentWindow = parentWindow;
        setPreferredSize(new Dimension(LAYOUT_WIDTH, LAYOUT_HEIGHT));
        setBackground(Color.BLACK);
        setLayout(new BorderLayout());
    }
}
