package com.onetab.rendering;

import com.badlogic.gdx.Gdx;
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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.onetab.enemy.Enemy;
import com.onetab.enemy.EnemyKind;
import com.onetab.player.Player;
import com.onetab.weapons.WeaponSystem;
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
    private final Model projectileModel;
    private final Model impactModel;
    private final Array<ModelInstance> staticWorld = new Array<>();
    private final CharacterArtAssets artAssets;
    private final ModelInstance rusherInstance;
    private final ModelInstance sentryInstance;
    private final ModelInstance armsInstance;
    private final ModelInstance pistolInstance;
    private final ModelInstance rifleInstance;
    private final ModelInstance shotgunInstance;
    private final PerspectiveCamera viewModelCamera = new PerspectiveCamera(60f, 1280f, 720f);
    private final Matrix4 tempMatrix = new Matrix4();
    private final Vector3 temp = new Vector3();
    private final Vector3 tempRight = new Vector3();
    private final Vector3 tempUp = new Vector3();
    private final Vector3 tempForward = new Vector3();



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
        projectileModel = builder.createSphere(0.22f, 0.22f, 0.22f, 10, 10,
            new Material(ColorAttribute.createDiffuse(Color.CYAN)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        impactModel = builder.createSphere(0.35f, 0.35f, 0.35f, 8, 8,
            new Material(ColorAttribute.createDiffuse(Color.WHITE), new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 0.7f)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

        artAssets = new CharacterArtAssets();
        rusherInstance = new ModelInstance(artAssets.rusherEnemyModel);
        sentryInstance = new ModelInstance(artAssets.sentryEnemyModel);
        armsInstance = new ModelInstance(artAssets.playerArmsModel);
        pistolInstance = new ModelInstance(artAssets.pistolModel);
        rifleInstance = new ModelInstance(artAssets.rifleModel);
        shotgunInstance = new ModelInstance(artAssets.shotgunModel);

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

    public void render(GameWorld world, PerspectiveCamera camera, WeaponSystem weapons) {
        modelBatch.begin(camera);
        for (ModelInstance instance : staticWorld) {
            modelBatch.render(instance, environment);
        }
        for (Enemy enemy : world.enemies) {
            ModelInstance instance = enemy.kind == EnemyKind.RUSHER ? rusherInstance : sentryInstance;
            configureEnemyInstance(enemy, instance);
            applyTransform(instance,enemy.position.x , enemy.position.y , enemy.position.z , 0.30f);
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

        Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
        renderViewModel(world.player, camera, weapons);
    }

    private void configureEnemyInstance(Enemy enemy, ModelInstance instance) {
        float motion = enemy.visualTime;
        float bob = enemy.alive ? MathUtils.sin(motion * (enemy.moving() ? 9f : 3.4f)) * (enemy.moving() ? 0.06f : 0.02f) : 0f;
        float tilt = enemy.moving() ? MathUtils.sin(motion * 9f) * 4f : 0f;
        instance.transform.idt();
        instance.transform.translate(enemy.position.x, bob, enemy.position.z);
        instance.transform.rotate(Vector3.Y, enemy.facingYaw);
        instance.transform.rotate(Vector3.Z, tilt);
    }

    private void renderViewModel(Player player, PerspectiveCamera worldCamera, WeaponSystem weapons) {
        viewModelCamera.viewportWidth = worldCamera.viewportWidth;
        viewModelCamera.viewportHeight = worldCamera.viewportHeight;
        viewModelCamera.near = 0.01f;
        viewModelCamera.far = 12f;
        viewModelCamera.position.set(worldCamera.position);
        viewModelCamera.direction.set(worldCamera.direction);
        viewModelCamera.up.set(worldCamera.up);
        viewModelCamera.update(true);

        float bobX = MathUtils.sin(player.cameraBobTime) * 0.035f;
        float bobY = Math.abs(MathUtils.cos(player.cameraBobTime * 2f)) * 0.025f;
        float swayYaw = MathUtils.sin(player.cameraBobTime * 0.5f) * 1.8f;
        float swayRoll = MathUtils.cos(player.cameraBobTime * 0.55f) * 1.3f;
        float recoil = weapons.recoilVisual();

        Vector3 anchor = temp.set(viewModelCamera.position);
        tempForward.set(viewModelCamera.direction).nor();
        tempRight.set(tempForward).crs(viewModelCamera.up).nor();
        tempUp.set(viewModelCamera.up).nor();

        anchor.mulAdd(tempRight, 0.26f + bobX);
        anchor.mulAdd(tempUp, -0.30f - bobY - recoil * 0.06f);
        anchor.mulAdd(tempForward, 0.36f - recoil * 0.18f);


        armsInstance.transform.idt();
        armsInstance.transform.translate(anchor);
        alignToCamera(armsInstance.transform, tempForward, tempUp, tempRight);
        armsInstance.transform.rotate(Vector3.Y, swayYaw);
        armsInstance.transform.rotate(Vector3.Z, swayRoll + recoil * 4f);
        armsInstance.transform.scale(0.35f, 0.35f, 0.35f);


        ModelInstance weaponInstance = switch (weapons.current().definition.id) {
            case "rifle" -> rifleInstance;
            case "shotgun" -> shotgunInstance;
            default -> pistolInstance;
        };


        weaponInstance.transform.idt();
        weaponInstance.transform.translate(anchor);
        alignToCamera(weaponInstance.transform, tempForward, tempUp, tempRight);
        weaponInstance.transform.translate(0.04f, -0.05f, -0.05f);
        weaponInstance.transform.rotate(Vector3.Y, swayYaw * 1.2f);
        weaponInstance.transform.rotate(Vector3.Z, swayRoll * 0.7f + recoil * 8f);
        weaponInstance.transform.rotate(Vector3.X, -recoil * 10f);
        float weaponScale = weaponScale(weapons.current().definition.id);
        weaponInstance.transform.scale(weaponScale, weaponScale, weaponScale);

        modelBatch.begin(viewModelCamera);
        modelBatch.render(armsInstance, environment);
        modelBatch.render(weaponInstance, environment);
        modelBatch.end();
    }

    private static float weaponScale(String id) {
        return switch (id) {
            case "rifle" -> 0.58f;
            case "shotgun" -> 0.62f;
            default -> 0.54f;
        };
    }

    private static void applyTransform(ModelInstance instance,
                                       float x, float y, float z,

                                       float scale) {
        instance.transform.idt();
        instance.transform.translate(x, y, z);
        instance.transform.scale(scale, scale, scale);
    }

    private void alignToCamera(Matrix4 transform, Vector3 forward, Vector3 up, Vector3 right) {
        tempMatrix.idt();
        tempMatrix.val[Matrix4.M00] = right.x;
        tempMatrix.val[Matrix4.M10] = right.y;
        tempMatrix.val[Matrix4.M20] = right.z;
        tempMatrix.val[Matrix4.M01] = up.x;
        tempMatrix.val[Matrix4.M11] = up.y;
        tempMatrix.val[Matrix4.M21] = up.z;
        tempMatrix.val[Matrix4.M02] = -forward.x;
        tempMatrix.val[Matrix4.M12] = -forward.y;
        tempMatrix.val[Matrix4.M22] = -forward.z;
        transform.mul(tempMatrix);
    }

    public void dispose() {
        modelBatch.dispose();
        floorModel.dispose();
        wallModel.dispose();
        crateModel.dispose();
        sideWallModel.dispose();
        projectileModel.dispose();
        impactModel.dispose();
        artAssets.dispose();
    }
}
