package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class TimeSettingsDialog extends JDialog {
    private final JTextField timeField;
    private final JTextField incrementField;
    private boolean confirmed = false;

    private int time;
    private int increment;

    public TimeSettingsDialog(JFrame parent) {
        super(parent, "Set Timer Settings", true); // Modal dialog
        setLayout(new GridLayout(3, 2, 10, 10));

        // Time input field
        add(new JLabel("Starting Time (minutes):"));
        timeField = new JTextField("5"); // Default 5 minutes (300 seconds)
        add(timeField);

        // Increment input field
        add(new JLabel("Increment per Turn (seconds):"));
        incrementField = new JTextField("10"); // Default increment 10 seconds
        add(incrementField);

        // Buttons
        JButton okButton = new JButton("OK");
        okButton.addActionListener(this::onOkButtonClick);
        add(okButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> setVisible(false));
        add(cancelButton);

        setSize(700, 150);
        setLocationRelativeTo(parent); // Center dialog relative to the parent
    }

    private void onOkButtonClick(ActionEvent e) {
        try {
            // Try to parse the values entered by the user
            time = Integer.parseInt(timeField.getText());
            increment = Integer.parseInt(incrementField.getText());
            if (time <= 0 || increment < 0) {
                JOptionPane.showMessageDialog(this, "Please enter valid positive values.");
            } else {
                confirmed = true;
                setVisible(false); // Close the dialog
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid number format. Please enter valid integers.");
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public int getTime() {
        return time;
    }

    public int getIncrement() {
        return increment;
    }
}

