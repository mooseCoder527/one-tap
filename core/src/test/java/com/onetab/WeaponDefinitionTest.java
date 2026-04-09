package com.onetab;

import com.badlogic.gdx.utils.Json;
import com.onetab.content.WeaponDefinition;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

final class WeaponDefinitionTest {
    @Test
    void parsesWeaponConfig() {
        Json json = new Json();
        try (var stream = getClass().getClassLoader().getResourceAsStream("config/weapons.json")) {
            assertNotNull(stream);
            WeaponDefinition[] definitions = json.fromJson(WeaponDefinition[].class,
                new InputStreamReader(stream, StandardCharsets.UTF_8));
            assertEquals(3, definitions.length);
            assertEquals("pistol", definitions[0].id);
            assertEquals("shotgun", definitions[2].id);
        } catch (Exception exception) {
            fail(exception);
        }
    }
}
