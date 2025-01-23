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
    private JLabel whiteTimerLabel;
    private JLabel blackTimerLabel;
    private GamePanel gamePanel;
    private boolean tenSecondsWarning_White;
    private boolean tenSecondsWarning_Black;
    private SoundPlayer alert;

    public ChessTimer(JLabel whiteTimerLabel, JLabel blackTimerLabel, int startingTime, int increment, GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        this.whiteTimerLabel = whiteTimerLabel;
        this.blackTimerLabel = blackTimerLabel;
        this.whiteTime = startingTime * 60;
        this.blackTime = startingTime * 60;
        this.increment = increment;
        tenSecondsWarning_White = false;
        tenSecondsWarning_Black = false;

        alert = new SoundPlayer("/res/audio/tenseconds.wav");


        // Timer for white player
        whiteTimer = new Timer(1000, (ActionEvent e) -> {
            if (whiteTime < 11 && !tenSecondsWarning_White) {
                tenSecondsWarning_White = true;
                alert.play();
            }


            if (whiteTime > 0) {
                whiteTime--;
                updateTimerDisplay();
            } else {
                stopTimers(); // Stop when time runs out
            }
        });

        // Timer for black player
        blackTimer = new Timer(1000, (ActionEvent e) -> {
            if (blackTime < 11 && !tenSecondsWarning_Black) {
                tenSecondsWarning_Black = true;
                alert.play();
            }

            if (blackTime > 0) {
                blackTime--;
                updateTimerDisplay();
            } else {
                stopTimers(); // Stop when time runs out
            }
        });

        // Initial display
        updateTimerDisplay();
        highlightActiveTimer();
    }

    // Switch turn and start the timer for the other player
    public void switchTurn() {
        // Apply increment to the player who just finished their turn
        if (whiteTurn) {
            whiteTime += increment;
            if (whiteTime > 10) tenSecondsWarning_White = false;
        } else {
            blackTime += increment;
            if (blackTime > 10) tenSecondsWarning_Black = false;
        }

        // Swap active player
        whiteTurn = !whiteTurn;

        // Start the correct timer
        if (whiteTurn) {
            whiteTimer.start();
            blackTimer.stop();
        } else {
            blackTimer.start();
            whiteTimer.stop();
        }

        highlightActiveTimer();
        updateTimerDisplay();
    }

    // Update the displayed time on the UI
    private void updateTimerDisplay() {
        whiteTimerLabel.setText(formatTime(whiteTime));
        blackTimerLabel.setText(formatTime(blackTime));
    }

    // Highlight the active timer
    private void highlightActiveTimer() {
        if (whiteTurn) {
            whiteTimerLabel.setFont(Utils.deriveFont(20, Font.BOLD));
            blackTimerLabel.setFont(Utils.deriveFont(20, Font.PLAIN));
            whiteTimerLabel.setForeground(Color.GREEN);
            blackTimerLabel.setForeground(Color.WHITE);
        } else {
            blackTimerLabel.setFont(Utils.deriveFont(20, Font.BOLD));
            whiteTimerLabel.setFont(Utils.deriveFont(20, Font.PLAIN));
            blackTimerLabel.setForeground(Color.GREEN);
            whiteTimerLabel.setForeground(Color.WHITE);
        }
    }

    // Convert time to MM:SS format
    private String formatTime(int timeInSeconds) {
        int minutes = timeInSeconds / 60;
        int seconds = timeInSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    // Method to stop both timers
    public void stopTimers() {
        whiteTimer.stop();
        blackTimer.stop();
        gamePanel.setGameOverTimer();

    }
}
