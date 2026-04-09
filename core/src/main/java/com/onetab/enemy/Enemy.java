package com.onetab.enemy;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.onetab.player.Player;
import com.onetab.world.GameWorld;

public abstract class Enemy {
    public final EnemyKind kind;
    public final Vector3 position = new Vector3();
    public final Vector3 velocity = new Vector3();
    public float radius;
    public float maxHealth;
    public float health;
    public boolean alive = true;
    public float flash;

    protected Enemy(EnemyKind kind, Vector3 spawn, float radius, float health) {
        this.kind = kind;
        this.position.set(spawn);
        this.radius = radius;
        this.maxHealth = health;
        this.health = health;
    }

    public abstract void update(GameWorld world, Player player, float delta);
    public abstract Color tint();

    public void damage(GameWorld world, float amount, Vector3 hitPosition) {
        if (!alive) return;
        health -= amount;
        flash = 1f;
        world.spawnImpact(hitPosition, tint(), 0.18f, 0.22f);
        if (health <= 0f) {
            alive = false;
            world.spawnImpact(position, Color.ORANGE, 0.45f, 0.6f);
        }
    }

    public void tickFlash(float delta) {
        flash = Math.max(0f, flash - delta * 3f);
    }
}
