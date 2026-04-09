package com.onetab.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.onetab.enemy.Enemy;
import com.onetab.enemy.RusherEnemy;
import com.onetab.enemy.SentryEnemy;
import com.onetab.player.Player;

public final class GameWorld {
    public final Player player = new Player();
    public final Array<Enemy> enemies = new Array<>();
    public final Array<BoxCollider> obstacles = new Array<>();
    public final Array<Projectile> hostileProjectiles = new Array<>();
    public final Array<ImpactEvent> impacts = new Array<>();
    public final PerspectiveCamera camera = new PerspectiveCamera(75f, 1280f, 720f);
    private final Vector3 cachedAim = new Vector3();
    public boolean victory;
    public float elapsedSeconds;

    public GameWorld() {
        camera.near = 0.05f;
        camera.far = 120f;
        buildMap();
        reset();
    }

    public void reset() {
        player.reset();
        enemies.clear();
        hostileProjectiles.clear();
        impacts.clear();
        victory = false;
        elapsedSeconds = 0f;
        enemies.add(new RusherEnemy(new Vector3(-7f, 0f, -4f)));
        enemies.add(new RusherEnemy(new Vector3(7f, 0f, -6f)));
        enemies.add(new RusherEnemy(new Vector3(0f, 0f, -14f)));
        enemies.add(new SentryEnemy(new Vector3(-8f, 0f, -20f)));
        enemies.add(new SentryEnemy(new Vector3(8f, 0f, -22f)));
        player.updateCamera(camera);
    }

    private void buildMap() {
        obstacles.add(new BoxCollider(new Vector3(0f, 0.75f, -10f), new Vector3(1.6f, 0.75f, 1.6f)));
        obstacles.add(new BoxCollider(new Vector3(-5f, 0.75f, -16f), new Vector3(1.4f, 0.75f, 1.4f)));
        obstacles.add(new BoxCollider(new Vector3(5f, 0.75f, -18f), new Vector3(1.4f, 0.75f, 1.4f)));
        obstacles.add(new BoxCollider(new Vector3(-10f, 1f, -25f), new Vector3(2f, 1f, 2f)));
        obstacles.add(new BoxCollider(new Vector3(10f, 1f, -26f), new Vector3(2f, 1f, 2f)));
    }

    public void update(float delta) {
        elapsedSeconds += delta;
        player.updateMovement(this, delta);
        for (Enemy enemy : enemies) {
            enemy.update(this, player, delta);
        }
        updateProjectiles(delta);
        updateImpacts(delta);
        for (int i = enemies.size - 1; i >= 0; i--) {
            if (!enemies.get(i).alive) {
                enemies.removeIndex(i);
            }
        }
        victory = enemies.size == 0;
        player.updateCamera(camera);
    }

    public void resolvePlayerMovement(Player player, Vector3 next) {
        float minX = -13.5f, maxX = 13.5f, minZ = -32f, maxZ = 13.5f;
        next.x = MathUtils.clamp(next.x, minX, maxX);
        next.z = MathUtils.clamp(next.z, minZ, maxZ);

        for (BoxCollider box : obstacles) {
            if (box.containsXZ(next.x, next.z, player.radius)) {
                if (!box.containsXZ(next.x, player.position.z, player.radius)) {
                    player.position.x = next.x;
                }
                if (!box.containsXZ(player.position.x, next.z, player.radius)) {
                    player.position.z = next.z;
                }
                resolveY(player, next);
                return;
            }
        }
        player.position.x = next.x;
        player.position.z = next.z;
        resolveY(player, next);
    }

    private void resolveY(Player player, Vector3 next) {
        if (next.y <= 0f) {
            player.position.y = 0f;
            player.velocity.y = 0f;
            player.grounded = true;
        } else {
            player.position.y = next.y;
            player.grounded = false;
        }
    }

    public void resolveEnemyMovement(Enemy enemy, Vector3 next) {
        next.x = MathUtils.clamp(next.x, -13.5f, 13.5f);
        next.z = MathUtils.clamp(next.z, -32f, 13.5f);
        for (BoxCollider box : obstacles) {
            if (box.containsXZ(next.x, next.z, enemy.radius)) {
                return;
            }
        }
        enemy.position.set(next.x, 0f, next.z);
    }

    private void updateProjectiles(float delta) {
        for (int i = hostileProjectiles.size - 1; i >= 0; i--) {
            Projectile projectile = hostileProjectiles.get(i);
            projectile.ttl -= delta;
            projectile.position.mulAdd(projectile.velocity, delta);
            if (projectile.ttl <= 0f) {
                hostileProjectiles.removeIndex(i);
                continue;
            }
            if (projectile.position.dst2(player.position.x, player.position.y + player.currentHeight * 0.6f, player.position.z) <= 0.7f * 0.7f) {
                player.damage(projectile.damage);
                spawnImpact(projectile.position, Color.CYAN, 0.18f, 0.18f);
                hostileProjectiles.removeIndex(i);
            }
        }
    }

    private void updateImpacts(float delta) {
        for (int i = impacts.size - 1; i >= 0; i--) {
            ImpactEvent impact = impacts.get(i);
            impact.ttl -= delta;
            if (impact.ttl <= 0f) impacts.removeIndex(i);
        }
    }

    public Vector3 playerAimDirection() {
        return cachedAim.set(camera.direction).nor();
    }

    public boolean fireHitscan(Vector3 origin, Vector3 direction, float range, float damage) {
        Enemy bestEnemy = null;
        float bestDistance = range;
        Vector3 hitPoint = new Vector3(origin).mulAdd(direction, range);
        for (Enemy enemy : enemies) {
            if (!enemy.alive) continue;
            float t = intersectSphere(origin, direction, enemy.position.x, enemy.position.y + 0.7f, enemy.position.z, enemy.radius + 0.25f);
            if (t >= 0f && t < bestDistance) {
                bestDistance = t;
                bestEnemy = enemy;
                hitPoint.set(origin).mulAdd(direction, t);
            }
        }
        float wallDistance = raycastWalls(origin, direction, range);
        if (wallDistance < bestDistance) {
            hitPoint.set(origin).mulAdd(direction, wallDistance);
            spawnImpact(hitPoint, Color.GRAY, 0.12f, 0.11f);
            return false;
        }
        if (bestEnemy != null) {
            bestEnemy.damage(this, damage, hitPoint);
            return true;
        }
        return false;
    }

    private float intersectSphere(Vector3 origin, Vector3 direction, float cx, float cy, float cz, float radius) {
        Vector3 oc = new Vector3(origin.x - cx, origin.y - cy, origin.z - cz);
        float b = 2f * oc.dot(direction);
        float c = oc.len2() - radius * radius;
        float discriminant = b * b - 4f * c;
        if (discriminant < 0f) return -1f;
        float sqrt = (float) Math.sqrt(discriminant);
        float t1 = (-b - sqrt) * 0.5f;
        float t2 = (-b + sqrt) * 0.5f;
        if (t1 >= 0f) return t1;
        return t2 >= 0f ? t2 : -1f;
    }

    private float raycastWalls(Vector3 origin, Vector3 direction, float range) {
        float best = range;
        if (direction.z != 0f) {
            float t = (-34f - origin.z) / direction.z;
            if (t > 0f && t < best) best = t;
        }
        for (BoxCollider box : obstacles) {
            float t = rayAabb(origin, direction, box.center(), box.halfExtents());
            if (t >= 0f && t < best) best = t;
        }
        return best;
    }

    private float rayAabb(Vector3 origin, Vector3 dir, Vector3 center, Vector3 half) {
        float minX = center.x - half.x, maxX = center.x + half.x;
        float minY = 0f, maxY = center.y + half.y * 2f;
        float minZ = center.z - half.z, maxZ = center.z + half.z;
        float tmin = 0f, tmax = Float.MAX_VALUE;

        if (!slab(origin.x, dir.x, minX, maxX, holder)) return -1f;
        tmin = Math.max(tmin, holder[0]); tmax = Math.min(tmax, holder[1]);
        if (!slab(origin.y, dir.y, minY, maxY, holder)) return -1f;
        tmin = Math.max(tmin, holder[0]); tmax = Math.min(tmax, holder[1]);
        if (!slab(origin.z, dir.z, minZ, maxZ, holder)) return -1f;
        tmin = Math.max(tmin, holder[0]); tmax = Math.min(tmax, holder[1]);
        if (tmax >= tmin) return tmin;
        return -1f;
    }

    private final float[] holder = new float[2];
    private boolean slab(float origin, float dir, float min, float max, float[] out) {
        if (Math.abs(dir) < 0.0001f) {
            if (origin < min || origin > max) return false;
            out[0] = 0f; out[1] = Float.MAX_VALUE;
            return true;
        }
        float inv = 1f / dir;
        float t1 = (min - origin) * inv;
        float t2 = (max - origin) * inv;
        out[0] = Math.min(t1, t2);
        out[1] = Math.max(t1, t2);
        return true;
    }

    public boolean hasLineOfSight(Vector3 from, Vector3 to) {
        Vector3 dir = new Vector3(to).sub(from);
        float dist = dir.len();
        if (dist < 0.001f) return true;
        dir.scl(1f / dist);
        return raycastWalls(new Vector3(from).add(0f, 0.9f, 0f), dir, dist) >= dist - 0.1f;
    }

    public void spawnEnemyProjectile(Enemy enemy, Vector3 target, float speed, float damage, float radius) {
        Projectile projectile = new Projectile();
        projectile.position.set(enemy.position).add(0f, 0.85f, 0f);
        projectile.velocity.set(target).add(0f, 0.8f, 0f).sub(projectile.position).nor().scl(speed);
        projectile.damage = damage;
        projectile.ttl = 3f;
        projectile.radius = radius;
        projectile.hostile = true;
        hostileProjectiles.add(projectile);
    }

    public void spawnImpact(Vector3 position, Color color, float ttl, float size) {
        ImpactEvent impact = new ImpactEvent().set(position, color, ttl, size);
        impacts.add(impact);
    }
}
