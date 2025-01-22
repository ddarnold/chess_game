package main;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.InputStream;

public class SoundPlayer {
    private Clip clip;

    public SoundPlayer(String soundFile) {
        try {
            InputStream is = Utils.class.getResourceAsStream(soundFile);
            if (is == null) {
                throw new IOException("Sound file not found: " + soundFile);
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(is);
            clip = AudioSystem.getClip();
            clip.open(audioStream);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void play() {
        if (clip != null) {
            if (clip.isRunning()) {
                clip.stop();  // Stop if it's already playing
            }
            clip.setFramePosition(0);  // Reset to the beginning
            clip.start();
        }
    }
}

