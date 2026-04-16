package com.onetab.enemy;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.onetab.player.Player;
import com.onetab.world.GameWorld;

public final class RusherEnemy extends Enemy {
    private float attackCooldown;

    public RusherEnemy(Vector3 spawn) {
        super(EnemyKind.RUSHER, spawn, 0.45f, 80f);
    }

    @Override
    public void update(GameWorld world, Player player, float delta) {
        tickFlash(delta);
        if (!alive) return;
        attackCooldown = Math.max(0f, attackCooldown - delta);
        Vector3 toPlayer = new Vector3(player.position).sub(position);
        float distance = toPlayer.len();
        if (distance > 0.001f) {
            toPlayer.scl(1f / distance);
            faceTowards(toPlayer, delta, 540f);
        }
        if (distance > 1.35f) {
            velocity.set(toPlayer).scl(3.4f);
            Vector3 next = new Vector3(position).mulAdd(velocity, delta);
            world.resolveEnemyMovement(this, next);
        } else if (attackCooldown <= 0f) {
            velocity.setZero();
            player.damage(12f);
            attackCooldown = 0.85f;
        } else {
            velocity.setZero();
        }
    }

    @Override
    public Color tint() {
        return Color.SCARLET;
    }
}
