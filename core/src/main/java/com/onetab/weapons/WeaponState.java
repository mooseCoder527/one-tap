package com.onetab.weapons;

import com.onetab.content.WeaponDefinition;

public final class WeaponState {
    public final WeaponDefinition definition;
    public int ammoInMagazine;
    public int reserveAmmo;
    public float cooldown;
    public float reloadRemaining;

    public WeaponState(WeaponDefinition definition, int reserveAmmo) {
        this.definition = definition;
        this.ammoInMagazine = definition.magazineSize;
        this.reserveAmmo = reserveAmmo;
    }

    public boolean isReloading() {
        return reloadRemaining > 0f;
    }
}
