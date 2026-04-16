package com.onetab.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.utils.Disposable;

public final class CharacterArtAssets implements Disposable {
    public final Model playerArmsModel;
    public final Model pistolModel;
    public final Model rifleModel;
    public final Model shotgunModel;
    public final Model rusherEnemyModel;
    public final Model sentryEnemyModel;

    public CharacterArtAssets() {
        ObjLoader loader = new ObjLoader();
        playerArmsModel = tint(loader.loadModel(Gdx.files.internal("models/player/arms.obj")), new Color(0.92f, 0.80f, 0.66f, 1f));
        pistolModel = tint(loader.loadModel(Gdx.files.internal("models/weapons/pistol.obj")), new Color(0.16f, 0.18f, 0.22f, 1f));
        rifleModel = tint(loader.loadModel(Gdx.files.internal("models/weapons/rifle.obj")), new Color(0.22f, 0.26f, 0.30f, 1f));
        shotgunModel = tint(loader.loadModel(Gdx.files.internal("models/weapons/shotgun.obj")), new Color(0.34f, 0.24f, 0.16f, 1f));
        rusherEnemyModel = tint(loader.loadModel(Gdx.files.internal("models/enemies/rusher.obj")), new Color(0.84f, 0.18f, 0.24f, 1f));
        sentryEnemyModel = tint(loader.loadModel(Gdx.files.internal("models/enemies/sentry.obj")), new Color(0.20f, 0.78f, 0.90f, 1f));
    }

    private static Model tint(Model model, Color color) {
        if (model.materials.isEmpty()) {
            model.materials.add(new Material(ColorAttribute.createDiffuse(color)));
            return model;
        }
        for (Material material : model.materials) {
            material.set(ColorAttribute.createDiffuse(color));
        }
        return model;
    }

    @Override
    public void dispose() {
        playerArmsModel.dispose();
        pistolModel.dispose();
        rifleModel.dispose();
        shotgunModel.dispose();
        rusherEnemyModel.dispose();
        sentryEnemyModel.dispose();
    }
}
