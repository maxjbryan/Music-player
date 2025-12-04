// Controls playing/pausing songs.

import javax.sound.sampled.*;
import java.io.File;

public class AudioController {
    // Keep reference to the clip so it doesn't get garbage collected
    private Clip currentClip;
    public boolean clipPaused = false;

    public Clip getcurrentClip() {
        return currentClip;
    }

    public void playSound(String filePath) {
        try {
            if (filePath.endsWith(".wav")) {
                // Close previous clip if exists
                if (currentClip != null && currentClip.isOpen()) {
                    currentClip.close();
                }

                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(filePath));
                currentClip = AudioSystem.getClip();
                currentClip.open(audioInputStream);

                // Add a listener to know when the clip finishes
                currentClip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        System.out.println("Audio finished playing");
                    }
                });

                currentClip.start();
                System.out.println("Audio started playing");
                clipPaused = false;
                // Keep the program alive while audio plays
                Thread.sleep(currentClip.getMicrosecondLength() / 1000);

            } else {
                System.out.println("Unsupported audio format. Please use .wav files.");
            }
        } catch (Exception ex) {
            System.out.println("Error playing sound: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void pause() {
        if (currentClip != null) {
            currentClip.stop();

            if (!clipPaused) {
                clipPaused = true;
            }
        }
    }

    public void resume() {
        if (currentClip != null) {
            currentClip.start();
            if (clipPaused) {
                clipPaused = false;
            }
        }
    }

    /**
     * Sets the playback position in microseconds
     */
    public void setPosition(long microseconds) {
        if (currentClip != null && currentClip.isOpen()) {
            long maxPosition = currentClip.getMicrosecondLength();

            // Ensure position is within valid range
            if (microseconds < 0) {
                microseconds = 0;
            } else if (microseconds > maxPosition) {
                microseconds = maxPosition;
            }

            boolean wasRunning = currentClip.isRunning();
            currentClip.setMicrosecondPosition(microseconds);

            // Resume playback if it was playing
            if (wasRunning && !currentClip.isRunning()) {
                currentClip.start();
            }
        }
    }

    //https://stackoverflow.com/questions/40514910/set-volume-of-java-clip
    //converts logorithmic decibal scale to linear for simplicity.
    //range of 0.0 to like 2.0.

    public float getVolume() {
        if (currentClip != null && currentClip.isOpen()) {
            FloatControl gainControl = (FloatControl) currentClip.getControl(FloatControl.Type.MASTER_GAIN);
            return (float) Math.pow(10f, gainControl.getValue() / 20f);
        }
        return 0f;
    }

    public void setVolume(float volume) {
        if (currentClip != null && currentClip.isOpen()) {
            if (volume < 0f || volume > 2f)
                throw new IllegalArgumentException("Volume not valid: " + volume);
            FloatControl gainControl = (FloatControl) currentClip.getControl(FloatControl.Type.MASTER_GAIN);
            gainControl.setValue(20f * (float) Math.log10(volume));
        }
    }
}