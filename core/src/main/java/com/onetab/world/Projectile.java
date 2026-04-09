package com.onetab.world;

import com.badlogic.gdx.math.Vector3;

public final class Projectile {
    public final Vector3 position = new Vector3();
    public final Vector3 velocity = new Vector3();
    public float radius;
    public float damage;
    public float ttl;
    public boolean hostile;
}
