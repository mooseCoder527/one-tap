package com.onetab.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import com.onetab.OneTabGame;
import com.onetab.content.WeaponCatalog;
import com.onetab.config.GameSettings;
import com.onetab.rendering.WorldRenderer;
import com.onetab.ui.HudRenderer;
import com.onetab.weapons.WeaponSystem;
import com.onetab.world.GameWorld;

public final class GameplayScreen extends ScreenAdapter {
    private static final float FIXED_STEP = 1f / 120f;

    private final OneTabGame game;
    private final GameWorld world;
    private final WorldRenderer renderer;
    private final HudRenderer hud;
    private final WeaponSystem weapons;
    private float accumulator;
    private boolean paused;
    private int pauseIndex;

    public GameplayScreen(OneTabGame game) {
        this.game = game;
        this.world = new GameWorld();
        this.renderer = new WorldRenderer(world);
        this.hud = new HudRenderer();
        this.weapons = new WeaponSystem(new WeaponCatalog());
        this.weapons.reset();
        if (game.settings().fullscreen) {
            applyDisplayMode();
        }
        applyMouseCapture();
    }

    @Override
    public void render(float delta) {
        handleGlobalInput();
        float frameDelta = Math.min(0.1f, delta);
        if (!paused && !world.player.dead && !world.victory) {
            accumulator += frameDelta;
            while (accumulator >= FIXED_STEP) {
                step(FIXED_STEP);
                accumulator -= FIXED_STEP;
            }
        }

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0.03f, 0.03f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        renderer.render(world, world.camera, weapons);
        hud.render(world, weapons, paused, pauseIndex, game.settings());
    }

    private void step(float delta) {
        world.player.updateLook(game.settings());
        world.update(delta);
        weapons.update(world.player, world, delta);
    }

    private void handleGlobalInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            paused = !paused;
            applyMouseCapture();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F3)) {
            game.settings().debugOverlay = !game.settings().debugOverlay;
            game.saveSettings();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            game.settings().fullscreen = !game.settings().fullscreen;
            applyDisplayMode();
            game.saveSettings();
        }
        if (world.player.dead || world.victory) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                world.reset();
                weapons.reset();
                paused = false;
                applyMouseCapture();
            }
            return;
        }
        if (paused) {
            handlePauseMenu();
        }
    }

    private void handlePauseMenu() {
        GameSettings settings = game.settings();
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) pauseIndex = (pauseIndex + 5) % 6;
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) pauseIndex = (pauseIndex + 1) % 6;
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) mutateOption(settings, -1);
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) mutateOption(settings, 1);
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            if (pauseIndex == 0) {
                paused = false;
                applyMouseCapture();
            } else if (pauseIndex == 5) {
                game.saveSettings();
                paused = false;
                applyMouseCapture();
            }
        }
    }

    private void mutateOption(GameSettings settings, int direction) {
        switch (pauseIndex) {
            case 1 -> settings.mouseSensitivity = MathUtils.clamp(settings.mouseSensitivity + (0.01f * direction), 0.05f, 0.5f);
            case 2 -> settings.invertY = !settings.invertY;
            case 3 -> settings.masterVolume = MathUtils.clamp(settings.masterVolume + (0.05f * direction), 0f, 1f);
            case 4 -> {
                settings.fullscreen = !settings.fullscreen;
                applyDisplayMode();
            }
            default -> {
            }
        }
    }

    private void applyMouseCapture() {
        boolean captured = !paused && !world.player.dead && !world.victory;
        Gdx.input.setCursorCatched(captured);
    }

    private void applyDisplayMode() {
        if (game.settings().fullscreen) {
            var mode = Gdx.graphics.getDisplayMode();
            Gdx.graphics.setFullscreenMode(mode);
        } else {
            Gdx.graphics.setWindowedMode(1280, 720);
        }
    }

    @Override
    public void resize(int width, int height) {
        PerspectiveCamera camera = world.camera;
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update(true);
    }

    @Override
    public void dispose() {
        renderer.dispose();
        hud.dispose();
        game.saveSettings();
    }
}
