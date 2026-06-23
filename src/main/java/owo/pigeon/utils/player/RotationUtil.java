package owo.pigeon.utils.player;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static owo.pigeon.Pigeon.mc;

// Reference: https://github.com/CCBlueX/LiquidBounce (AngleSmooth)
// Reference: https://github.com/60124808866/OpenMyau (RotationUtil)

public class RotationUtil {
    public static float angleDifference(float a, float b) {
        return Mth.wrapDegrees(a - b);
    }

    public static float wrapAngleDiff(float angle, float target) {
        return target + Mth.wrapDegrees(angle - target);
    }

    public static float clampAngle(float angle, float maxAngle) {
        maxAngle = Math.max(0.0f, Math.min(180.0f, maxAngle));
        if (angle > maxAngle) {
            angle = maxAngle;
        } else if (angle < -maxAngle) {
            angle = -maxAngle;
        }
        return angle;
    }

    public static float smoothAngle(float angle, float smoothFactor) {
        return angle * (0.5f + 0.5f * (1.0f - Math.max(0.0f, Math.min(1.0f, smoothFactor + ThreadLocalRandom.current().nextFloat(-0.1f, 0.1f)))));
    }

    public static float quantizeAngle(float angle) {
        return (float) ((double) angle - (double) angle % 0.0096);
    }

    public static float gcd() {
        double sensitivity = mc.options.sensitivity().get();
        double gcdSens = sensitivity * 0.6 + 0.2;
        return (float) (gcdSens * gcdSens * gcdSens * 8.0 * 0.15);
    }

    public static float normalizeRotation(float current, float target) {
        float gcd = gcd();
        if (gcd <= 0.0f) return target;
        float diff = target - current;
        return current + Math.round(diff / gcd) * gcd;
    }

    public static float towardsLinear(float current, float target, float speed, float delta) {
        float diff = angleDifference(target, current);
        if (Math.abs(diff) < 0.01f) return current;
        float factor = speed / 100.0f;
        float adjusted = 1.0f - (float) Math.pow(1.0 - factor, delta);
        return current + diff * adjusted;
    }

    public static float towardsSigmoid(float current, float target, float turnSpeed, float steepness, float midpoint, float delta) {
        float diff = angleDifference(target, current);
        float absDiff = Math.abs(diff);
        if (absDiff < 0.01f) return current;
        float scaledDiff = absDiff / 120.0f;
        float sigmoid = (float) (1.0 / (1.0 + Math.exp(-steepness * (scaledDiff - midpoint))));
        float factor = Math.min(sigmoid * turnSpeed / 100.0f, 1.0f);
        float adjusted = 1.0f - (float) Math.pow(1.0 - factor, delta);
        return current + diff * adjusted;
    }

    public static float towardsInterpolation(float current, float target, float turnSpeed, float directionChangeFactor, float midpoint, float delta) {
        float diff = angleDifference(target, current);
        float absDiff = Math.abs(diff);
        if (absDiff < 0.01f) return current;
        float t = Math.min(absDiff / 180.0f, 1.0f);
        float factor;
        if (t > midpoint) {
            float bezier = quadraticBezier(0.05f, 1.0f, 1.0f - t);
            factor = bezier * turnSpeed / 100.0f;
        } else {
            float sigmoid = (float) (1.0 / (1.0 + Math.exp(-0.5 * (t - 0.3))));
            factor = sigmoid * (turnSpeed + directionChangeFactor) / 100.0f;
        }
        factor = Math.min(factor, 1.0f);
        float adjusted = 1.0f - (float) Math.pow(1.0 - factor, delta);
        return current + diff * adjusted;
    }

    private static float quadraticBezier(float start, float end, float t) {
        float control = 1.0f;
        float oneMinusT = 1.0f - t;
        return oneMinusT * oneMinusT * start + 2.0f * oneMinusT * t * control + t * t * end;
    }

    public static float[] getRotationsToBox(AABB boundingBox, float yaw, float pitch, float maxAngle, float smoothFactor) {
        Vec3 eyePos = mc.player.getEyePosition(1.0f);
        double minTargetY = boundingBox.minY + 0.05 * (boundingBox.maxY - boundingBox.minY);
        double maxTargetY = boundingBox.minY + 0.75 * (boundingBox.maxY - boundingBox.minY);
        double deltaX = (boundingBox.minX + boundingBox.maxX) / 2.0 - eyePos.x;
        double deltaY = eyePos.y >= maxTargetY ? maxTargetY - eyePos.y : (eyePos.y <= minTargetY ? minTargetY - eyePos.y : 0.0);
        double deltaZ = (boundingBox.minZ + boundingBox.maxZ) / 2.0 - eyePos.z;
        return getRotations(deltaX, deltaY, deltaZ, yaw, pitch, maxAngle, smoothFactor);
    }

    public static float[] getRotationsTo(double targetX, double targetY, double targetZ, float currentYaw, float currentPitch) {
        return getRotations(targetX, targetY, targetZ, currentYaw, currentPitch, 180.0f, 0.0f);
    }

    public static float[] getRotations(double targetX, double targetY, double targetZ, float currentYaw, float currentPitch, float maxAngle, float smoothFactor) {
        double horizontalDistance = Math.sqrt(targetX * targetX + targetZ * targetZ);
        float yawDelta = Mth.wrapDegrees((float) (Math.atan2(targetZ, targetX) * 180.0 / Math.PI) - 90.0f - currentYaw);
        float pitchDelta = Mth.wrapDegrees((float) (-Math.atan2(targetY, horizontalDistance) * 180.0 / Math.PI) - currentPitch);
        yawDelta = Math.abs(yawDelta) <= 1.0f ? 0.0f : smoothAngle(clampAngle(yawDelta, maxAngle), smoothFactor);
        pitchDelta = Math.abs(pitchDelta) <= 1.0f ? 0.0f : smoothAngle(clampAngle(pitchDelta, maxAngle), smoothFactor);
        return new float[]{quantizeAngle(currentYaw + yawDelta), quantizeAngle(currentPitch + pitchDelta)};
    }

    public static Vec3 clampVecToBox(Vec3 vector, AABB boundingBox) {
        double[] coords = new double[]{vector.x, vector.y, vector.z};
        double[] minCoords = new double[]{boundingBox.minX, boundingBox.minY, boundingBox.minZ};
        double[] maxCoords = new double[]{boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ};
        for (int i = 0; i < 3; ++i) {
            if (coords[i] > maxCoords[i]) {
                coords[i] = maxCoords[i];
                continue;
            }
            if (!(coords[i] < minCoords[i])) continue;
            coords[i] = minCoords[i];
        }
        return new Vec3(coords[0], coords[1], coords[2]);
    }

    public static double distanceToEntity(Entity entity) {
        float borderSize = entity.getPickRadius();
        AABB boundingBox = entity.getBoundingBox().inflate(borderSize);
        return distanceToBox(boundingBox);
    }

    public static double distanceToEntity(Entity entity, Vec3 point) {
        float borderSize = entity.getPickRadius();
        AABB boundingBox = entity.getBoundingBox().inflate(borderSize);
        return distanceToBox(boundingBox, point);
    }

    public static double distanceToBox(AABB boundingBox) {
        return distanceToBox(boundingBox, mc.player.getEyePosition(1.0f));
    }

    public static double distanceToBox(AABB boundingBox, Vec3 point) {
        if (boundingBox.contains(point)) {
            return 0.0;
        }
        Vec3 clampedPoint = clampVecToBox(point, boundingBox);
        double deltaX = clampedPoint.x - point.x;
        double deltaY = clampedPoint.y - point.y;
        double deltaZ = clampedPoint.z - point.z;
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
    }

    public static float angleToEntity(Entity entity) {
        Vec3 eyePos = mc.player.getEyePosition(1.0f);
        float borderSize = entity.getPickRadius();
        AABB boundingBox = entity.getBoundingBox().inflate(borderSize);
        if (boundingBox.contains(eyePos)) {
            return 0.0f;
        }
        double deltaX = entity.getX() - eyePos.x;
        double deltaZ = entity.getZ() - eyePos.z;
        return Math.abs(Mth.wrapDegrees((float) (Math.atan2(deltaZ, deltaX) * 180.0 / Math.PI) - 90.0f - mc.player.getYRot())) * 2.0f;
    }

    public static float getYawBetween(double x1, double z1, double x2, double z2) {
        return Mth.wrapDegrees((float) (Math.atan2(z2 - z1, x2 - x1) * 180.0 / Math.PI) - 90.0f - mc.player.getYRot());
    }

    public static HitResult rayTrace(float yaw, float pitch, double distance, float partialTicks) {
        Vec3 eyePos = mc.player.getEyePosition(partialTicks);
        Vec3 lookVec = Vec3.directionFromRotation(pitch, yaw);
        Vec3 targetPos = eyePos.add(lookVec.x * distance, lookVec.y * distance, lookVec.z * distance);
        return mc.level.clip(new ClipContext(eyePos, targetPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player));
    }

    public static HitResult rayTrace(Entity entity) {
        Vec3 eyePos = mc.player.getEyePosition(1.0f);
        float borderSize = entity.getPickRadius();
        Vec3 targetPos = clampVecToBox(eyePos, entity.getBoundingBox().inflate(borderSize));
        return mc.level.clip(new ClipContext(eyePos, targetPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player));
    }

    public static Optional<Vec3> rayTraceBox(AABB boundingBox, float yaw, float pitch, double distance) {
        Vec3 eyePos = mc.player.getEyePosition(1.0f);
        Vec3 lookVec = Vec3.directionFromRotation(pitch, yaw);
        Vec3 targetPos = eyePos.add(lookVec.x * distance, lookVec.y * distance, lookVec.z * distance);
        return boundingBox.clip(eyePos, targetPos);
    }
}
