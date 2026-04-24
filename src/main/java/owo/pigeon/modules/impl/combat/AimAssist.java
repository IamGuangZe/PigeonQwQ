package owo.pigeon.modules.impl.combat;

import net.engio.mbassy.listener.Handler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.HitResult;
import owo.pigeon.event.events.RenderEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.FloatSetting;
import owo.pigeon.settings.IntSetting;
import owo.pigeon.settings.ModeSetting;
import owo.pigeon.utils.KeybindUtil;
import owo.pigeon.utils.player.PlayerUtil;
import owo.pigeon.utils.player.RotationUtil;

import java.util.List;
import java.util.stream.Collectors;

import static owo.pigeon.Pigeon.mc;

public class AimAssist extends Module {
    public AimAssist() {
        super("AimAssist", Category.COMBAT);
    }

    // Reference: https://github.com/CCBlueX/LiquidBounce (AngleSmooth)
    // Reference: https://github.com/60124808866/OpenMyau (AimAssist)

    public enum Sort {
        DISTANCE, FOV, HEALTH
    }

    public enum SmoothMode {
        LINEAR, SIGMOID, INTERPOLATION
    }

    public ModeSetting<Sort> sort = setting("sort", Sort.DISTANCE, v -> true);
    public ModeSetting<SmoothMode> smoothMode = setting("smooth-mode", SmoothMode.INTERPOLATION, v -> true);
    public IntSetting horizontalSpeed = setting("horizontal-speed", 30, 0, 100, "%", v -> true);
    public IntSetting verticalSpeed = setting("vertical-speed", 20, 0, 100, "%", v -> true);
    public FloatSetting midpoint = setting("midpoint", 0.3f, 0.0f, 1.0f, v -> smoothMode.getValue() == SmoothMode.SIGMOID || smoothMode.getValue() == SmoothMode.INTERPOLATION);
    public FloatSetting steepness = setting("steepness", 5.0f, 0.0f, 20.0f, v -> smoothMode.getValue() == SmoothMode.SIGMOID);
    public IntSetting directionChange = setting("direction-change", 50, 0, 100, "%", v -> smoothMode.getValue() == SmoothMode.INTERPOLATION);
    public FloatSetting range = setting("range", 4.5f, 3.0f, 8.0f, v -> true);
    public IntSetting fov = setting("fov", 90, 30, 360, v -> true);
    public EnableSetting throughWalls = setting("through-walls", false, v -> true);
    public EnableSetting invisibles = setting("invisibles", false, v -> true);
    public EnableSetting botCheck = setting("bot-check", true, v -> true);

    private long lastPressTime = 0;

    @Handler
    public void onRender3D(RenderEvent.Render3DEvent event) {
        if (mc.currentScreen != null) return;

        boolean attacking = KeybindUtil.isPressed(mc.options.attackKey);
        boolean lookingAtBlock = mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.BLOCK;

        if (attacking) {
            lastPressTime = System.currentTimeMillis();
        }

        if (!attacking && lookingAtBlock) {
            return;
        }

        if (!attacking && System.currentTimeMillis() - lastPressTime > 350L) {
            return;
        }

        List<PlayerEntity> targets = mc.world.getPlayers()
                .stream()
                .filter(this::isValidTarget)
                .sorted(this::compareTargets)
                .collect(Collectors.toList());

        if (targets.isEmpty()) return;

        if (targets.stream().anyMatch(this::isInReach)) {
            targets.removeIf(player -> !isInReach(player));
        }

        PlayerEntity target = targets.getFirst();

        if (RotationUtil.distanceToEntity(target) <= 0.0) return;

        float collisionBorderSize = target.getTargetingMargin();
        float[] targetRotation = RotationUtil.getRotationsToBox(
                target.getBoundingBox().expand(collisionBorderSize),
                mc.player.getYaw(),
                mc.player.getPitch(),
                180.0f,
                0.0f
        );

        float currentYaw = mc.player.getYaw();
        float currentPitch = mc.player.getPitch();
        float hSpeed = (float) horizontalSpeed.getValue();
        float vSpeed = (float) verticalSpeed.getValue();

        float delta = event.getDelta();
        float newYaw = smoothRotation(currentYaw, targetRotation[0], hSpeed, delta);
        float newPitch = smoothRotation(currentPitch, targetRotation[1], vSpeed, delta);

        newYaw = RotationUtil.normalizeRotation(currentYaw, newYaw);
        newPitch = RotationUtil.normalizeRotation(currentPitch, newPitch);

        mc.player.setYaw(newYaw);
        mc.player.setPitch(newPitch);
    }

    private float smoothRotation(float current, float target, float speed, float delta) {
        return switch (smoothMode.getValue()) {
            case LINEAR -> RotationUtil.towardsLinear(current, target, speed, delta);
            case SIGMOID -> RotationUtil.towardsSigmoid(current, target, speed, steepness.getValue(), midpoint.getValue(), delta);
            case INTERPOLATION -> RotationUtil.towardsInterpolation(current, target, speed, (float) directionChange.getValue(), midpoint.getValue(), delta);
        };
    }

    private boolean isValidTarget(PlayerEntity player) {
        if (player == mc.player || player == mc.player.getVehicle() || player.isDead() || player.isSpectator()) return false;
        if (RotationUtil.distanceToEntity(player) > this.range.getValue()) return false;
        if (RotationUtil.angleToEntity(player) > this.fov.getValue()) return false;
        if (!this.invisibles.getValue() && player.isInvisible()) return false;
        if (this.botCheck.getValue() && !PlayerUtil.hasUUID(player)) return false;
        if (!this.throughWalls.getValue() && RotationUtil.rayTrace(player).getType() == HitResult.Type.BLOCK)
            return false;

        return true;
    }

    private boolean isInReach(PlayerEntity player) {
        return RotationUtil.distanceToEntity(player) <= 3.0;
    }

    private int compareTargets(PlayerEntity e1, PlayerEntity e2) {
        return switch (this.sort.getValue()) {
            case FOV -> Float.compare(RotationUtil.angleToEntity(e1), RotationUtil.angleToEntity(e2));
            case HEALTH -> Float.compare(e1.getHealth(), e2.getHealth());
            default -> Double.compare(RotationUtil.distanceToEntity(e1), RotationUtil.distanceToEntity(e2));
        };
    }
}
