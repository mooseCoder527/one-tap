package com.onetab.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.onetab.enemy.Enemy;
import com.onetab.enemy.EnemyKind;
import com.onetab.world.BoxCollider;
import com.onetab.world.GameWorld;
import com.onetab.world.ImpactEvent;
import com.onetab.world.Projectile;

public final class WorldRenderer {
    private final ModelBatch modelBatch = new ModelBatch();
    private final Environment environment = new Environment();
    private final Model floorModel;
    private final Model wallModel;
    private final Model crateModel;
    private final Model sideWallModel;
    private final Model rusherModel;
    private final Model sentryModel;
    private final Model projectileModel;
    private final Model impactModel;
    private final Array<ModelInstance> staticWorld = new Array<>();

    public WorldRenderer(GameWorld world) {
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.8f, 0.8f, 0.85f, 1f));
        environment.add(new DirectionalLight().set(1f, 1f, 1f, -0.4f, -0.9f, -0.2f));

        ModelBuilder builder = new ModelBuilder();
        floorModel = builder.createBox(28f, 0.2f, 48f,
            new Material(ColorAttribute.createDiffuse(new Color(0.22f, 0.24f, 0.28f, 1f))),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        wallModel = builder.createBox(28f, 4f, 0.4f,
            new Material(ColorAttribute.createDiffuse(new Color(0.17f, 0.19f, 0.22f, 1f))),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        crateModel = builder.createBox(1f, 1.5f, 1f,
            new Material(ColorAttribute.createDiffuse(new Color(0.38f, 0.32f, 0.24f, 1f))),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        rusherModel = builder.createCapsule(0.55f, 1.65f, 12,
            new Material(ColorAttribute.createDiffuse(Color.SCARLET)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        sentryModel = builder.createCylinder(0.9f, 1.8f, 0.9f, 16,
            new Material(ColorAttribute.createDiffuse(Color.CYAN)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        projectileModel = builder.createSphere(0.22f, 0.22f, 0.22f, 10, 10,
            new Material(ColorAttribute.createDiffuse(Color.CYAN)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        impactModel = builder.createSphere(0.35f, 0.35f, 0.35f, 8, 8,
            new Material(ColorAttribute.createDiffuse(Color.WHITE), new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 0.7f)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

        staticWorld.add(new ModelInstance(floorModel, 0f, -0.1f, -10f));
        staticWorld.add(new ModelInstance(wallModel, 0f, 2f, -34f));
        staticWorld.add(new ModelInstance(wallModel, 0f, 2f, 14f));
        sideWallModel = builder.createBox(0.4f, 4f, 48f,
            new Material(ColorAttribute.createDiffuse(new Color(0.17f, 0.19f, 0.22f, 1f))),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        staticWorld.add(new ModelInstance(sideWallModel, -14f, 2f, -10f));
        staticWorld.add(new ModelInstance(sideWallModel, 14f, 2f, -10f));
        for (BoxCollider box : world.obstacles) {
            ModelInstance instance = new ModelInstance(crateModel);
            instance.transform.setToTranslation(box.center());
            instance.transform.scale(box.halfExtents().x * 2f, box.halfExtents().y * 2f, box.halfExtents().z * 2f);
            staticWorld.add(instance);
        }
    }

    public void render(GameWorld world, PerspectiveCamera camera) {
        modelBatch.begin(camera);
        for (ModelInstance instance : staticWorld) {
            modelBatch.render(instance, environment);
        }
        for (Enemy enemy : world.enemies) {
            Model model = enemy.kind == EnemyKind.RUSHER ? rusherModel : sentryModel;
            ModelInstance instance = new ModelInstance(model);
            instance.transform.setToTranslation(enemy.position.x, enemy.kind == EnemyKind.RUSHER ? 0.85f : 0.9f, enemy.position.z);
            modelBatch.render(instance, environment);
        }
        for (Projectile projectile : world.hostileProjectiles) {
            ModelInstance instance = new ModelInstance(projectileModel, projectile.position.x, projectile.position.y, projectile.position.z);
            modelBatch.render(instance, environment);
        }
        for (ImpactEvent impact : world.impacts) {
            ModelInstance instance = new ModelInstance(impactModel);
            instance.transform.setToTranslation(impact.position).scale(impact.size, impact.size, impact.size);
            instance.materials.first().set(ColorAttribute.createDiffuse(impact.color));
            modelBatch.render(instance, environment);
        }
        modelBatch.end();
    }

    public void dispose() {
        modelBatch.dispose();
        floorModel.dispose();
        wallModel.dispose();
        crateModel.dispose();
        sideWallModel.dispose();
        rusherModel.dispose();
        sentryModel.dispose();
        projectileModel.dispose();
        impactModel.dispose();
    }
}
