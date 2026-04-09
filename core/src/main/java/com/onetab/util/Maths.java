package com.onetab.util;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public final class Maths {
    private Maths() {}

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static float approach(float value, float target, float delta) {
        if (value < target) return Math.min(value + delta, target);
        return Math.max(value - delta, target);
    }

    public static Vector3 forwardFromAngles(float yawDegrees, float pitchDegrees) {
        float yawRad = yawDegrees * MathUtils.degreesToRadians;
        float pitchRad = pitchDegrees * MathUtils.degreesToRadians;
        float cosPitch = MathUtils.cos(pitchRad);
        return new Vector3(
            MathUtils.sin(yawRad) * cosPitch,
            MathUtils.sin(pitchRad),
            -MathUtils.cos(yawRad) * cosPitch
        ).nor();
    }
}
