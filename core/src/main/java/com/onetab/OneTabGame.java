package com.onetab;

import com.badlogic.gdx.Game;
import com.onetab.config.GameSettings;
import com.onetab.config.SettingsRepository;
import com.onetab.screens.GameplayScreen;

public final class OneTabGame extends Game {
    private final SettingsRepository settingsRepository;
    private GameSettings settings;

    public OneTabGame() {
        this.settingsRepository = new SettingsRepository();
    }

    @Override
    public void create() {
        this.settings = settingsRepository.load();
        setScreen(new GameplayScreen(this));
    }

    public GameSettings settings() {
        return settings;
    }

    public void saveSettings() {
        settingsRepository.save(settings);
    }
}
