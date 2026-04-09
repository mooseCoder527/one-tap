package com.onetab.config;

public final class GameSettings {
    public float mouseSensitivity = 0.18f;
    public boolean invertY = false;
    public float masterVolume = 0.7f;
    public boolean debugOverlay = false;
    public boolean fullscreen = false;

    public GameSettings copy() {
        GameSettings copy = new GameSettings();
        copy.mouseSensitivity = mouseSensitivity;
        copy.invertY = invertY;
        copy.masterVolume = masterVolume;
        copy.debugOverlay = debugOverlay;
        copy.fullscreen = fullscreen;
        return copy;
    }
}
