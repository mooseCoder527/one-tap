package com.onetab.enemy;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.onetab.player.Player;
import com.onetab.world.GameWorld;

public final class SentryEnemy extends Enemy {
    private float shootCooldown = 1.2f;
    private float strafeDirection = 1f;
    private float strafeTimer;

    public SentryEnemy(Vector3 spawn) {
        super(EnemyKind.SENTRY, spawn, 0.5f, 110f);
    }

    @Override
    public void update(GameWorld world, Player player, float delta) {
        tickFlash(delta);
        if (!alive) return;
        shootCooldown -= delta;
        strafeTimer -= delta;
        Vector3 toPlayer = new Vector3(player.position).sub(position);
        float distance = toPlayer.len();
        if (distance > 0.001f) {
            toPlayer.scl(1f / distance);
        }

        if (strafeTimer <= 0f) {
            strafeDirection *= -1f;
            strafeTimer = 1.4f;
        }

        if (distance > 6f) {
            Vector3 next = new Vector3(position).mulAdd(toPlayer, 1.8f * delta);
            world.resolveEnemyMovement(this, next);
        } else if (distance < 3f) {
            Vector3 next = new Vector3(position).mulAdd(toPlayer, -1.6f * delta);
            world.resolveEnemyMovement(this, next);
        } else {
            Vector3 strafe = new Vector3(-toPlayer.z, 0f, toPlayer.x).scl(strafeDirection * 1.5f * delta);
            Vector3 next = new Vector3(position).add(strafe);
            world.resolveEnemyMovement(this, next);
        }

        if (shootCooldown <= 0f && distance < 16f && world.hasLineOfSight(position, player.position)) {
            world.spawnEnemyProjectile(this, player.position, 9f, 0.12f, 0.28f);
            shootCooldown = 1.25f;
        }
    }

    @Override
    public Color tint() {
        return Color.CYAN;
    }
}
