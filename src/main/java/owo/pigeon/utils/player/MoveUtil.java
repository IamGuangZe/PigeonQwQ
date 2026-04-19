package owo.pigeon.utils.player;

import net.minecraft.client.input.Input;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import owo.pigeon.mixin.accessors.IAccessorInput;
import owo.pigeon.utils.KeybindUtil;

import static owo.pigeon.Pigeon.mc;

public class MoveUtil {

    // Ported from: https://github.com/60124808866/OpenMyau/blob/main/src/main/java/myau/util/MoveUtil.java

    public static boolean isMoving() {
        boolean forward = KeybindUtil.isPressed(mc.options.forwardKey);
        boolean back = KeybindUtil.isPressed(mc.options.backKey);
        boolean left = KeybindUtil.isPressed(mc.options.leftKey);
        boolean right = KeybindUtil.isPressed(mc.options.rightKey);
        return forward != back || left != right;
    }

    public static int getForwardValue() {
        int value = 0;
        if (KeybindUtil.isPressed(mc.options.forwardKey)) value++;
        if (KeybindUtil.isPressed(mc.options.backKey)) value--;
        return value;
    }

    public static int getStrafeValue() {
        int value = 0;
        if (KeybindUtil.isPressed(mc.options.leftKey)) value++;
        if (KeybindUtil.isPressed(mc.options.rightKey)) value--;
        return value;
    }

    public static float getMoveYaw() {
        Vec2f movementInput = mc.player.input.getMovementInput();
        return adjustYaw(
                mc.player.getYaw(),
                movementInput.y,  // forward
                movementInput.x   // sideways
        );
    }

    public static float adjustYaw(float yaw, float forward, float strafe) {
        if (forward < 0.0f) {
            yaw += 180.0f;
        }
        if (strafe != 0.0f) {
            float multiplier = forward == 0.0f ? 1.0f : 0.5f * Math.signum(forward);
            yaw += -90.0f * multiplier * Math.signum(strafe);
        }
        return MathHelper.wrapDegrees(yaw);
    }

    public static float getDirectionYaw() {
        if (getSpeed() == 0.0) {
            return MathHelper.wrapDegrees(mc.player.getYaw());
        }
        return MathHelper.wrapDegrees(
                (float) Math.toDegrees(Math.atan2(mc.player.getVelocity().z, mc.player.getVelocity().x)) - 90.0f
        );
    }

    public static double getBaseMoveSpeed() {
        double baseSpeed = 0.28015;
        if (getSpeedTime() > 0) {
            baseSpeed = 0.28015 * (1.0 + 0.15 * getSpeedLevel());
        }
        return baseSpeed;
    }

    public static double getBaseJumpHigh(int speedLevel) {
        double jumpHeight = 0.452;
        if (speedLevel == 1) {
            jumpHeight = 0.4972;
        } else if (speedLevel >= 2) {
            jumpHeight *= 1.2;
        }
        return jumpHeight;
    }

    public static double getJumpMotion() {
        int speedLevel = 0;
        if (getSpeedTime() > 0) {
            speedLevel = getSpeedLevel();
        }
        return getBaseJumpHigh(speedLevel);
    }

    public static double getSpeed() {
        return getSpeed(mc.player.getVelocity().x, mc.player.getVelocity().z);
    }

    public static double getSpeed(double motionX, double motionZ) {
        return Math.hypot(motionX, motionZ);
    }

    public static void setSpeed(double speed) {
        setSpeed(speed, getDirectionYaw());
    }

    public static void setSpeed(double speed, float yaw) {
        mc.player.setVelocity(
                -Math.sin(Math.toRadians(yaw)) * speed,
                mc.player.getVelocity().y,
                Math.cos(Math.toRadians(yaw)) * speed
        );
    }

    public static void addSpeed(double speed, float yaw) {
        mc.player.addVelocity(
                -Math.sin(Math.toRadians(yaw)) * speed,
                0.0,
                Math.cos(Math.toRadians(yaw)) * speed
        );
    }

    public static int getSpeedLevel() {
        if (mc.player.hasStatusEffect(StatusEffects.SPEED)) {
            return mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier() + 1;
        }
        return 0;
    }

    public static int getSpeedTime() {
        if (mc.player.hasStatusEffect(StatusEffects.SPEED)) {
            return mc.player.getStatusEffect(StatusEffects.SPEED).getDuration();
        }
        return 0;
    }

    public static float getAllowedHorizontalDistance() {
        BlockPos blockPos = BlockPos.ofFloored(
                mc.player.getX(),
                mc.player.getBoundingBox().minY - 1.0,
                mc.player.getZ()
        );
        float slipperiness = mc.world.getBlockState(blockPos).getBlock().getSlipperiness() * 0.91f;
        return mc.player.getMovementSpeed() * (0.16277136f / (slipperiness * slipperiness * slipperiness));
    }

    public static double[] predictMovement() {
        float strafeInput = (float) getStrafeValue() * 0.98f;
        float forwardInput = (float) getForwardValue() * 0.98f;
        float inputMagnitude = strafeInput * strafeInput + forwardInput * forwardInput;
        if (inputMagnitude >= 1.0E-4f) {
            inputMagnitude = MathHelper.sqrt(inputMagnitude);
            if (inputMagnitude < 1.0f) {
                inputMagnitude = 1.0f;
            }
            inputMagnitude = getAllowedHorizontalDistance() / inputMagnitude;
            float sinYaw = MathHelper.sin(mc.player.getYaw() * (float) Math.PI / 180.0f);
            float cosYaw = MathHelper.cos(mc.player.getYaw() * (float) Math.PI / 180.0f);
            strafeInput *= inputMagnitude;
            forwardInput *= inputMagnitude;
            return new double[]{strafeInput * cosYaw - forwardInput * sinYaw, forwardInput * cosYaw + strafeInput * sinYaw};
        }
        return new double[]{0.0, 0.0};
    }

    public static void fixStrafe(float targetYaw) {
        float angle = MathHelper.wrapDegrees(
                adjustYaw(mc.player.getYaw(), getForwardValue(), getStrafeValue()) - targetYaw + 22.5f
        );

        float forward;
        float strafe;

        switch ((int) (angle + 180.0f) / 45 % 8) {
            case 0:
                forward = -1.0f;
                strafe = 0.0f;
                break;
            case 1:
                forward = -1.0f;
                strafe = 1.0f;
                break;
            case 2:
                forward = 0.0f;
                strafe = 1.0f;
                break;
            case 3:
                forward = 1.0f;
                strafe = 1.0f;
                break;
            case 4:
                forward = 1.0f;
                strafe = 0.0f;
                break;
            case 5:
                forward = 1.0f;
                strafe = -1.0f;
                break;
            case 6:
                forward = 0.0f;
                strafe = -1.0f;
                break;
            case 7:
                forward = -1.0f;
                strafe = -1.0f;
                break;
            default:
                forward = 0.0f;
                strafe = 0.0f;
        }

        if (mc.player.isSneaking()) {
            forward *= 0.3f;
            strafe *= 0.3f;
        }

        Input input = mc.player.input;
        input.playerInput = new PlayerInput(
                forward > 0.0f,
                forward < 0.0f,
                strafe > 0.0f,
                strafe < 0.0f,
                input.playerInput.jump(),
                input.playerInput.sneak(),
                input.playerInput.sprint()
        );

        ((IAccessorInput) input).pigeon$setMovementVector(new Vec2f(strafe, forward));
    }
}
