package com.client.negocio;

import javax.sound.sampled.*;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AudioManager {

    private static final Logger log = Logger.getLogger(AudioManager.class.getName());

    private Clip bgmClip;
    private static final float MIN_VOLUME = -80.0f;
    private static final float MAX_VOLUME = 6.0f;
    private float bgmVolume = 0f;
    private float fxVolume = 0f;

    public void setBGMVolume(float dB) {
        if (dB < MIN_VOLUME) {
            dB = MIN_VOLUME;
        } else if (dB > MAX_VOLUME) {
            dB = MAX_VOLUME;
        }
        bgmVolume = dB;
        if (bgmClip != null && bgmClip.isOpen()) {
            setVolume(bgmClip, bgmVolume);
        }
    }

    public void setFXVolume(float dB) {
        if (dB < MIN_VOLUME) {
            dB = MIN_VOLUME;
        } else if (dB > MAX_VOLUME) {
            dB = MAX_VOLUME;
        }
        fxVolume = dB;
    }

    private void setVolume(Clip clip, float dB) {
        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            gainControl.setValue(dB);
        }
    }

    public void playBGM(String resourcePath, boolean loop) {
        stopBGM();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                System.err.println("Recurso não encontrado: " + resourcePath);
                return;
            }
            BufferedInputStream bis = new BufferedInputStream(is);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bis);
            bgmClip = AudioSystem.getClip();
            bgmClip.open(audioStream);
            setVolume(bgmClip, bgmVolume);

            if (loop) {
                bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
            }
            bgmClip.start();
        } catch (Exception e) {
            log.log(Level.WARNING, "Erro ao tocar BGM: " + resourcePath, e);
        }
    }

    public void stopBGM() {
        if (bgmClip != null && bgmClip.isRunning()) {
            bgmClip.stop();
            bgmClip.close();
        }
        bgmClip = null;
    }

    public void playFX(String resourcePath) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                System.err.println("Recurso não encontrado: " + resourcePath);
                return;
            }
            BufferedInputStream bis = new BufferedInputStream(is);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bis);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            setVolume(clip, fxVolume);
            clip.start();

            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });

        } catch (Exception e) {
            log.log(Level.WARNING, "Erro ao tocar FX: " + resourcePath, e);
        }
    }

    public void playFXDelayed(String resourcePath, long delayMillis) {
        new Thread(() -> {
            try {
                Thread.sleep(delayMillis);
                playFX(resourcePath);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}