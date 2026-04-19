package owo.pigeon.utils.player;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class RotationUtil {

    // Ported from: https://github.com/60124808866/OpenMyau/blob/main/src/main/java/myau/util/RotationUtil.java

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static float wrapAngleDiff(float angle, float target) {
        return target + MathHelper.wrapDegrees(angle - target);
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

    public static float[] getRotationsToBox(Box boundingBox, float yaw, float pitch, float maxAngle, float smoothFactor) {
        Vec3d eyePos = mc.player.getCameraPosVec(1.0f);
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
        float yawDelta = MathHelper.wrapDegrees((float) (Math.atan2(targetZ, targetX) * 180.0 / Math.PI) - 90.0f - currentYaw);
        float pitchDelta = MathHelper.wrapDegrees((float) (-Math.atan2(targetY, horizontalDistance) * 180.0 / Math.PI) - currentPitch);
        yawDelta = Math.abs(yawDelta) <= 1.0f ? 0.0f : smoothAngle(clampAngle(yawDelta, maxAngle), smoothFactor);
        pitchDelta = Math.abs(pitchDelta) <= 1.0f ? 0.0f : smoothAngle(clampAngle(pitchDelta, maxAngle), smoothFactor);
        return new float[]{quantizeAngle(currentYaw + yawDelta), quantizeAngle(currentPitch + pitchDelta)};
    }

    public static Vec3d clampVecToBox(Vec3d vector, Box boundingBox) {
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
        return new Vec3d(coords[0], coords[1], coords[2]);
    }

    public static double distanceToEntity(Entity entity) {
        float borderSize = entity.getTargetingMargin();
        Box boundingBox = entity.getBoundingBox().expand(borderSize);
        return distanceToBox(boundingBox);
    }

    public static double distanceToEntity(Entity entity, Vec3d point) {
        float borderSize = entity.getTargetingMargin();
        Box boundingBox = entity.getBoundingBox().expand(borderSize);
        return distanceToBox(boundingBox, point);
    }

    public static double distanceToBox(Box boundingBox) {
        return distanceToBox(boundingBox, mc.player.getCameraPosVec(1.0f));
    }

    public static double distanceToBox(Box boundingBox, Vec3d point) {
        if (boundingBox.contains(point)) {
            return 0.0;
        }
        Vec3d clampedPoint = clampVecToBox(point, boundingBox);
        double deltaX = clampedPoint.x - point.x;
        double deltaY = clampedPoint.y - point.y;
        double deltaZ = clampedPoint.z - point.z;
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
    }

    public static float angleToEntity(Entity entity) {
        Vec3d eyePos = mc.player.getCameraPosVec(1.0f);
        float borderSize = entity.getTargetingMargin();
        Box boundingBox = entity.getBoundingBox().expand(borderSize);
        if (boundingBox.contains(eyePos)) {
            return 0.0f;
        }
        double deltaX = entity.getX() - eyePos.x;
        double deltaZ = entity.getZ() - eyePos.z;
        return Math.abs(MathHelper.wrapDegrees((float) (Math.atan2(deltaZ, deltaX) * 180.0 / Math.PI) - 90.0f - mc.player.getYaw())) * 2.0f;
    }

    public static float getYawBetween(double x1, double z1, double x2, double z2) {
        return MathHelper.wrapDegrees((float) (Math.atan2(z2 - z1, x2 - x1) * 180.0 / Math.PI) - 90.0f - mc.player.getYaw());
    }

    public static HitResult rayTrace(float yaw, float pitch, double distance, float partialTicks) {
        Vec3d eyePos = mc.player.getCameraPosVec(partialTicks);
        Vec3d lookVec = Vec3d.fromPolar(pitch, yaw);
        Vec3d targetPos = eyePos.add(lookVec.x * distance, lookVec.y * distance, lookVec.z * distance);
        return mc.world.raycast(new RaycastContext(eyePos, targetPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));
    }

    public static HitResult rayTrace(Entity entity) {
        Vec3d eyePos = mc.player.getCameraPosVec(1.0f);
        float borderSize = entity.getTargetingMargin();
        Vec3d targetPos = clampVecToBox(eyePos, entity.getBoundingBox().expand(borderSize));
        return mc.world.raycast(new RaycastContext(eyePos, targetPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));
    }

    public static Optional<Vec3d> rayTraceBox(Box boundingBox, float yaw, float pitch, double distance) {
        Vec3d eyePos = mc.player.getCameraPosVec(1.0f);
        Vec3d lookVec = Vec3d.fromPolar(pitch, yaw);
        Vec3d targetPos = eyePos.add(lookVec.x * distance, lookVec.y * distance, lookVec.z * distance);
        return boundingBox.raycast(eyePos, targetPos);
    }
}
