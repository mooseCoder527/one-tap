package com.onetab.config;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

public final class SettingsRepository {
    private static final String SETTINGS_PATH = "one-tab/settings.json";
    private final Json json;

    public SettingsRepository() {
        json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        json.setUsePrototypes(false);
    }

    public GameSettings load() {
        try {
            if (Gdx.files == null) {
                return new GameSettings();
            }
            FileHandle file = Gdx.files.local(SETTINGS_PATH);
            if (!file.exists()) {
                return new GameSettings();
            }
            return json.fromJson(GameSettings.class, file);
        } catch (Exception ex) {
            return new GameSettings();
        }
    }

    public void save(GameSettings settings) {
        if (Gdx.files == null) {
            return;
        }
        FileHandle file = Gdx.files.local(SETTINGS_PATH);
        file.parent().mkdirs();
        file.writeString(json.prettyPrint(settings), false, "UTF-8");
    }

    public String toJson(GameSettings settings) {
        return json.toJson(settings);
    }

    public GameSettings fromJson(String raw) {
        return json.fromJson(GameSettings.class, raw);
    }
}
