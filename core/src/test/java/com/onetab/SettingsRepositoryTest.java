package com.onetab;

import com.onetab.config.GameSettings;
import com.onetab.config.SettingsRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class SettingsRepositoryTest {
    @Test
    void roundTripsSettingsJson() {
        SettingsRepository repository = new SettingsRepository();
        GameSettings settings = new GameSettings();
        settings.mouseSensitivity = 0.27f;
        settings.invertY = true;
        settings.masterVolume = 0.33f;
        settings.debugOverlay = true;
        settings.fullscreen = true;

        String json = repository.toJson(settings);
        GameSettings restored = repository.fromJson(json);

        assertEquals(0.27f, restored.mouseSensitivity, 0.0001f);
        assertTrue(restored.invertY);
        assertEquals(0.33f, restored.masterVolume, 0.0001f);
        assertTrue(restored.debugOverlay);
        assertTrue(restored.fullscreen);
    }
}
