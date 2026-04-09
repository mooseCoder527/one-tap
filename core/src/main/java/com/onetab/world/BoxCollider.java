package com.onetab.world;

import com.badlogic.gdx.math.Vector3;

public record BoxCollider(Vector3 center, Vector3 halfExtents) {
    public boolean containsXZ(float x, float z, float radius) {
        return x + radius > center.x - halfExtents.x &&
            x - radius < center.x + halfExtents.x &&
            z + radius > center.z - halfExtents.z &&
            z - radius < center.z + halfExtents.z;
    }
}
