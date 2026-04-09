package com.onetab.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

public final class ImpactEvent {
    public final Vector3 position = new Vector3();
    public final Color color = new Color(Color.WHITE);
    public float ttl;
    public float size;

    public ImpactEvent set(Vector3 position, Color color, float ttl, float size) {
        this.position.set(position);
        this.color.set(color);
        this.ttl = ttl;
        this.size = size;
        return this;
    }
}
