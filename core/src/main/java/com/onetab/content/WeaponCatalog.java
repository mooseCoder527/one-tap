package com.onetab.content;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;

public final class WeaponCatalog {
    private final ObjectMap<String, WeaponDefinition> byId = new ObjectMap<>();

    public WeaponCatalog() {
        Json json = new Json();
        WeaponDefinition[] definitions = json.fromJson(WeaponDefinition[].class,
            Gdx.files.internal("config/weapons.json"));
        for (WeaponDefinition definition : definitions) {
            byId.put(definition.id, definition);
        }
    }

    public WeaponDefinition get(String id) {
        WeaponDefinition definition = byId.get(id);
        if (definition == null) {
            throw new IllegalArgumentException("Missing weapon definition: " + id);
        }
        return definition;
    }

    public Array<WeaponDefinition> ordered(String... ids) {
        Array<WeaponDefinition> ordered = new Array<>();
        for (String id : ids) {
            ordered.add(get(id));
        }
        return ordered;
    }
}
