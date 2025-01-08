package main;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

import static main.Constants.*;

public class Main {

    public static void main(String[] args) {

        Font customFont = Utils.loadCustomFont("/res/font/vidaloka.ttf", 24f);
        setDefaultFont(customFont);

        // Create main window
        JFrame window = new JFrame();
        window.setUndecorated(true); // Remove default title bar
        window.setShape(new RoundRectangle2D.Double(0,0, LAYOUT_WIDTH,LAYOUT_HEIGHT,15,15));
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setLayout(new BorderLayout());

        // Custom Header Panel
        Header header = new Header(window);
        window.add(header, BorderLayout.NORTH);

        // Show menu first
        Menu menu = new Menu(window);
        window.add(menu, BorderLayout.CENTER);

        // Configure window
        window.setSize(LAYOUT_WIDTH, LAYOUT_HEIGHT);
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }

    private static void setDefaultFont(Font font) {
        UIManager.put("Label.font", font);
        UIManager.put("Button.font", font);
        UIManager.put("TextField.font", font);
        UIManager.put("TextArea.font", font);
        UIManager.put("ComboBox.font", font);
        UIManager.put("CheckBox.font", font);
        UIManager.put("RadioButton.font", font);
    }
}
