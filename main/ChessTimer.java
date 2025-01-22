package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ChessTimer {
    private Timer whiteTimer, blackTimer;
    private int whiteTime;
    private int blackTime;
    private int increment;
    private boolean whiteTurn = true;
    private JLabel timerLabel;

    public ChessTimer(JLabel timerLabel, int startingTime, int increment) {
        this.timerLabel = timerLabel;
        this.whiteTime = startingTime*60;
        this.blackTime = startingTime*60;
        this.increment = increment;

        // Timer for white player
        whiteTimer = new Timer(1000, (ActionEvent e) -> {
            if (whiteTime > 0 && whiteTurn) {
                whiteTime--;
                updateTimerDisplay();
            }
        });

        // Timer for black player
        blackTimer = new Timer(1000, (ActionEvent e) -> {
            if (blackTime > 0 && !whiteTurn) {
                blackTime--;
                updateTimerDisplay();
            }
        });
    }

    // Switch turn and start the timer for the other player
    public void switchTurn() {
        whiteTurn = !whiteTurn;
        if (whiteTurn) {
            whiteTimer.start();
            blackTimer.stop();
        } else {
            blackTimer.start();
            whiteTimer.stop();
        }

        // Add increment after each turn
        if (whiteTurn) {
            whiteTime += increment;
        } else {
            blackTime += increment;
        }
    }

    // Update the displayed time on the UI
    private void updateTimerDisplay() {
        int minutes, seconds;

        if (whiteTurn) {
            minutes = whiteTime / 60;
            seconds = whiteTime % 60;
        } else {
            minutes = blackTime / 60;
            seconds = blackTime % 60;
        }

        // Display the time in MM:SS format
        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
    }

    // Method to stop both timers
    public void stopTimers() {
        whiteTimer.stop();
        blackTimer.stop();
    }
}
