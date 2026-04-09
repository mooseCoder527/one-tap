package com.onetab.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.onetab.config.GameSettings;
import com.onetab.util.Maths;
import com.onetab.world.GameWorld;

public final class Player {
    public final Vector3 position = new Vector3(0f, 1.15f, 10f);
    public final Vector3 velocity = new Vector3();
    public float yaw = 180f;
    public float pitch = 0f;
    public float radius = 0.35f;
    public float standingHeight = 1.15f;
    public float crouchHeight = 0.8f;
    public float currentHeight = 1.15f;
    public int maxHealth = 100;
    public float health = 100f;
    public float hitFlash;
    public float cameraBobTime;
    public boolean grounded = true;
    public boolean dead;

    public void reset() {
        position.set(0f, 1.15f, 10f);
        velocity.setZero();
        yaw = 180f;
        pitch = 0f;
        currentHeight = standingHeight;
        health = maxHealth;
        hitFlash = 0f;
        grounded = true;
        dead = false;
    }

    public void updateLook(GameSettings settings) {
        float deltaX = Gdx.input.getDeltaX();
        float deltaY = Gdx.input.getDeltaY();
        yaw += deltaX * settings.mouseSensitivity;
        float yFactor = settings.invertY ? 1f : -1f;
        pitch = MathUtils.clamp(pitch + deltaY * settings.mouseSensitivity * yFactor, -85f, 85f);
    }

    public void updateMovement(GameWorld world, float delta) {
        boolean crouching = Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT);
        float targetHeight = crouching ? crouchHeight : standingHeight;
        currentHeight = Maths.approach(currentHeight, targetHeight, 4f * delta);

        Vector3 forward = Maths.forwardFromAngles(yaw, 0f);
        Vector3 right = new Vector3(forward.z, 0f, -forward.x).nor();
        Vector3 move = new Vector3();
        if (Gdx.input.isKeyPressed(Input.Keys.W)) move.add(forward);
        if (Gdx.input.isKeyPressed(Input.Keys.S)) move.sub(forward);
        if (Gdx.input.isKeyPressed(Input.Keys.D)) move.add(right);
        if (Gdx.input.isKeyPressed(Input.Keys.A)) move.sub(right);
        if (!move.isZero()) move.nor();

        boolean sprinting = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && !crouching;
        float speed = crouching ? 3.2f : sprinting ? 7.8f : 5.4f;
        velocity.x = move.x * speed;
        velocity.z = move.z * speed;

        if (grounded && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            velocity.y = 6.2f;
            grounded = false;
        }
        velocity.y -= 20f * delta;

        Vector3 next = new Vector3(position).mulAdd(velocity, delta);
        world.resolvePlayerMovement(this, next);
        if (move.len2() > 0.01f && grounded) {
            cameraBobTime += delta * (sprinting ? 12f : 8f);
        }
        hitFlash = Math.max(0f, hitFlash - delta * 2.2f);
    }

    public void updateCamera(PerspectiveCamera camera) {
        float bob = grounded ? MathUtils.sin(cameraBobTime) * 0.04f : 0f;
        Vector3 eye = new Vector3(position.x, position.y + currentHeight + bob, position.z);
        camera.position.set(eye);
        camera.direction.set(Maths.forwardFromAngles(yaw, pitch));
        camera.up.set(Vector3.Y);
        camera.update(true);
    }

    public void damage(float amount) {
        if (dead) return;
        health -= amount;
        hitFlash = 1f;
        if (health <= 0f) {
            health = 0f;
            dead = true;
        }
    }
}
