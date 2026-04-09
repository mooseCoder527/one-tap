package com.onetab.weapons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.onetab.content.WeaponCatalog;
import com.onetab.content.WeaponDefinition;
import com.onetab.player.Player;
import com.onetab.world.GameWorld;

public final class WeaponSystem {
    private final Array<WeaponState> weapons;
    private int currentIndex;
    private float recoilVisual;
    private float muzzleFlash;
    private float hitMarker;

    public WeaponSystem(WeaponCatalog catalog) {
        weapons = new Array<>();
        weapons.add(new WeaponState(catalog.get("pistol"), 72));
        weapons.add(new WeaponState(catalog.get("rifle"), 150));
        weapons.add(new WeaponState(catalog.get("shotgun"), 40));
    }

    public void reset() {
        currentIndex = 0;
        recoilVisual = 0f;
        muzzleFlash = 0f;
        hitMarker = 0f;
        for (WeaponState state : weapons) {
            state.ammoInMagazine = state.definition.magazineSize;
            state.reloadRemaining = 0f;
            state.cooldown = 0f;
        }
    }

    public void update(Player player, GameWorld world, float delta) {
        WeaponState current = current();
        for (WeaponState weapon : weapons) {
            weapon.cooldown = Math.max(0f, weapon.cooldown - delta);
            if (weapon.reloadRemaining > 0f) {
                weapon.reloadRemaining = Math.max(0f, weapon.reloadRemaining - delta);
                if (weapon.reloadRemaining == 0f) {
                    int missing = weapon.definition.magazineSize - weapon.ammoInMagazine;
                    int moved = Math.min(missing, weapon.reserveAmmo);
                    weapon.ammoInMagazine += moved;
                    weapon.reserveAmmo -= moved;
                }
            }
        }
        muzzleFlash = Math.max(0f, muzzleFlash - delta * 6f);
        recoilVisual = Math.max(0f, recoilVisual - delta * 8f);
        hitMarker = Math.max(0f, hitMarker - delta * 5f);

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) currentIndex = 0;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) currentIndex = 1;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) currentIndex = 2;
        current = current();

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            startReload(current);
        }
        if (current.isReloading()) return;

        boolean firing = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        if (current.definition.id.equals("pistol") && !Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            firing = false;
        }
        if (firing) {
            tryFire(player, world, current);
        }
    }

    private void startReload(WeaponState weapon) {
        if (weapon.isReloading()) return;
        if (weapon.ammoInMagazine >= weapon.definition.magazineSize) return;
        if (weapon.reserveAmmo <= 0) return;
        weapon.reloadRemaining = weapon.definition.reloadSeconds;
    }

    private void tryFire(Player player, GameWorld world, WeaponState weapon) {
        if (weapon.cooldown > 0f || weapon.isReloading()) return;
        if (weapon.ammoInMagazine <= 0) {
            startReload(weapon);
            return;
        }
        weapon.cooldown = weapon.definition.fireIntervalSeconds;
        weapon.ammoInMagazine--;
        recoilVisual = weapon.definition.recoilKick;
        muzzleFlash = 1f;
        boolean hit = false;

        for (int i = 0; i < weapon.definition.pellets; i++) {
            Vector3 origin = player.position.cpy().add(0f, player.currentHeight, 0f);
            Vector3 dir = world.playerAimDirection();
            dir.rotate(Vector3.Y, MathUtils.random(-weapon.definition.spreadDegrees, weapon.definition.spreadDegrees));
            Vector3 sideAxis = new Vector3(dir).crs(Vector3.Y).nor();
            if (!sideAxis.isZero()) {
                dir.rotate(sideAxis, MathUtils.random(-weapon.definition.spreadDegrees * 0.65f, weapon.definition.spreadDegrees * 0.65f));
            }
            if (world.fireHitscan(origin, dir.nor(), weapon.definition.range, weapon.definition.damage / weapon.definition.pellets)) {
                hit = true;
            }
        }

        if (hit) {
            hitMarker = 1f;
        } else {
            Vector3 point = player.position.cpy().add(world.playerAimDirection().scl(weapon.definition.range));
            world.spawnImpact(point, Color.LIGHT_GRAY, 0.1f, 0.12f);
        }
    }

    public WeaponState current() {
        return weapons.get(currentIndex);
    }

    public int currentIndex() {
        return currentIndex;
    }

    public float recoilVisual() {
        return recoilVisual;
    }

    public float muzzleFlash() {
        return muzzleFlash;
    }

    public float hitMarker() {
        return hitMarker;
    }
}
