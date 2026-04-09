package com.onetab.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.onetab.config.GameSettings;
import com.onetab.player.Player;
import com.onetab.weapons.WeaponState;
import com.onetab.weapons.WeaponSystem;
import com.onetab.world.GameWorld;

public final class HudRenderer {
    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final BitmapFont font = new BitmapFont();
    private final BitmapFont largeFont = new BitmapFont();

    public HudRenderer() {
        font.getData().setScale(1.2f);
        largeFont.getData().setScale(2.1f);
    }

    public void render(GameWorld world, WeaponSystem weapons, boolean paused, int pauseIndex, GameSettings settings) {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        WeaponState current = weapons.current();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(weapons.hitMarker() > 0f ? Color.YELLOW : Color.WHITE);
        float cx = width * 0.5f;
        float cy = height * 0.5f;
        shapes.line(cx - 10f, cy, cx - 3f, cy);
        shapes.line(cx + 3f, cy, cx + 10f, cy);
        shapes.line(cx, cy - 10f, cx, cy - 3f);
        shapes.line(cx, cy + 3f, cx, cy + 10f);
        shapes.end();

        batch.begin();
        largeFont.draw(batch, "ONE TAB", 24f, height - 24f);
        font.draw(batch, "Health: " + Math.round(world.player.health), 24f, height - 80f);
        font.draw(batch, current.definition.displayName + "  " + current.ammoInMagazine + "/" + current.reserveAmmo, 24f, height - 112f);
        font.draw(batch, "Enemies: " + world.enemies.size, 24f, height - 144f);
        font.draw(batch, String.format("Time: %.1fs", world.elapsedSeconds), 24f, height - 176f);

        if (settings.debugOverlay) {
            font.draw(batch, String.format("FPS: %d  Pos: %.1f %.1f %.1f", Gdx.graphics.getFramesPerSecond(), world.player.position.x, world.player.position.y, world.player.position.z), 24f, 36f);
        }

        if (world.player.dead) {
            largeFont.draw(batch, "YOU ARE DOWN", width * 0.5f - 160f, height * 0.6f);
            font.draw(batch, "Press ENTER to restart", width * 0.5f - 110f, height * 0.6f - 42f);
        } else if (world.victory) {
            largeFont.draw(batch, "SECTOR CLEARED", width * 0.5f - 170f, height * 0.6f);
            font.draw(batch, "Press ENTER to restart", width * 0.5f - 110f, height * 0.6f - 42f);
        }

        if (paused) {
            font.draw(batch, "Paused", width * 0.5f - 32f, height * 0.7f);
            drawOption("Resume", 0, pauseIndex, width, height * 0.62f);
            drawOption(String.format("Sensitivity: %.2f", settings.mouseSensitivity), 1, pauseIndex, width, height * 0.56f);
            drawOption("Invert Y: " + (settings.invertY ? "On" : "Off"), 2, pauseIndex, width, height * 0.50f);
            drawOption(String.format("Volume: %.0f%%", settings.masterVolume * 100f), 3, pauseIndex, width, height * 0.44f);
            drawOption("Fullscreen: " + (settings.fullscreen ? "On" : "Off"), 4, pauseIndex, width, height * 0.38f);
            drawOption("Save & Resume", 5, pauseIndex, width, height * 0.32f);
        }
        batch.end();

        if (world.player.hitFlash > 0f) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(1f, 0f, 0f, 0.18f * world.player.hitFlash);
            shapes.rect(0f, 0f, width, height);
            shapes.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }
    }

    private void drawOption(String label, int index, int selectedIndex, int width, float y) {
        font.setColor(index == selectedIndex ? Color.YELLOW : Color.WHITE);
        font.draw(batch, label, width * 0.5f - 120f, y);
        font.setColor(Color.WHITE);
    }

    public void dispose() {
        batch.dispose();
        shapes.dispose();
        font.dispose();
        largeFont.dispose();
    }
}
