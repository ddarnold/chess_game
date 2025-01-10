package main;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Objects;

import static main.Constants.*;

public class Main extends JFrame {

    private JPanel currentPanel;

    public Main(){
        //Window Configuration
        setUndecorated(true);
        setShape(new RoundRectangle2D.Double(0,0, LAYOUT_WIDTH,LAYOUT_HEIGHT,15,15));
        setSize(LAYOUT_WIDTH, LAYOUT_HEIGHT);
        setResizable(false);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        //Add Custom Header
        Header header = new Header(this);
        add(header, BorderLayout.NORTH);

        //Switch to Menu
        switchToPanel(new Menu(this));

    }

    public static void main(String[] args) {

        Utils.loadAndSetCustomFont();
        Utils.applyTheme(Objects.requireNonNull(JsonHandler.readJson(PREFERENCES_THEME)));

        SwingUtilities.invokeLater(() -> new Main().setVisible(true));

    }

    public void switchToPanel(JPanel newPanel) {
        if (currentPanel != null) {
            remove(currentPanel); // Remove the current panel
        }
        currentPanel = newPanel;
        add(currentPanel);       // Add the new panel
        revalidate();            // Refresh the frame
        repaint();
    }

}
