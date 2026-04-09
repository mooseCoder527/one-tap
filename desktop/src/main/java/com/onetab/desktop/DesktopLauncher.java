package com.onetab.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.onetab.OneTabGame;

public final class DesktopLauncher {
    private DesktopLauncher() {}

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("One Tab");
        config.setWindowedMode(1280, 720);
        config.useVsync(true);
        config.setForegroundFPS(120);
        config.setBackBufferConfig(8, 8, 8, 8, 16, 0, 0);
        config.setResizable(true);
        new Lwjgl3Application(new OneTabGame(), config);
    }
}
