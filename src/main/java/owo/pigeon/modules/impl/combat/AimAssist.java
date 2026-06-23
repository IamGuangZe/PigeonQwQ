package owo.pigeon.modules.impl.combat;

import net.engio.mbassy.listener.Handler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
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
        if (mc.screen != null) return;

        boolean attacking = KeybindUtil.isPressed(mc.options.keyAttack);
        boolean lookingAtBlock = mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.BLOCK;

        if (attacking) {
            lastPressTime = System.currentTimeMillis();
        }

        if (!attacking && lookingAtBlock) {
            return;
        }

        if (!attacking && System.currentTimeMillis() - lastPressTime > 350L) {
            return;
        }

        List<Player> targets = mc.level.players()
                .stream()
                .filter(this::isValidTarget)
                .sorted(this::compareTargets)
                .collect(Collectors.toList());

        if (targets.isEmpty()) return;

        if (targets.stream().anyMatch(this::isInReach)) {
            targets.removeIf(player -> !isInReach(player));
        }

        Player target = targets.getFirst();

        if (RotationUtil.distanceToEntity(target) <= 0.0) return;

        float collisionBorderSize = target.getPickRadius();
        float[] targetRotation = RotationUtil.getRotationsToBox(
                target.getBoundingBox().inflate(collisionBorderSize),
                mc.player.getYRot(),
                mc.player.getXRot(),
                180.0f,
                0.0f
        );

        float currentYaw = mc.player.getYRot();
        float currentPitch = mc.player.getXRot();
        float hSpeed = (float) horizontalSpeed.getValue();
        float vSpeed = (float) verticalSpeed.getValue();

        float delta = event.getDelta();
        float newYaw = smoothRotation(currentYaw, targetRotation[0], hSpeed, delta);
        float newPitch = smoothRotation(currentPitch, targetRotation[1], vSpeed, delta);

        newYaw = RotationUtil.normalizeRotation(currentYaw, newYaw);
        newPitch = RotationUtil.normalizeRotation(currentPitch, newPitch);

        mc.player.setYRot(newYaw);
        mc.player.setXRot(newPitch);
    }

    private float smoothRotation(float current, float target, float speed, float delta) {
        return switch (smoothMode.getValue()) {
            case LINEAR -> RotationUtil.towardsLinear(current, target, speed, delta);
            case SIGMOID ->
                    RotationUtil.towardsSigmoid(current, target, speed, steepness.getValue(), midpoint.getValue(), delta);
            case INTERPOLATION ->
                    RotationUtil.towardsInterpolation(current, target, speed, (float) directionChange.getValue(), midpoint.getValue(), delta);
        };
    }

    private boolean isValidTarget(Player player) {
        if (player == mc.player || player == mc.player.getVehicle() || player.isDeadOrDying() || player.isSpectator())
            return false;
        if (RotationUtil.distanceToEntity(player) > this.range.getValue()) return false;
        if (RotationUtil.angleToEntity(player) > this.fov.getValue()) return false;
        if (!this.invisibles.getValue() && player.isInvisible()) return false;
        if (this.botCheck.getValue() && !PlayerUtil.hasUUID(player)) return false;
        return this.throughWalls.getValue() || RotationUtil.rayTrace(player).getType() != HitResult.Type.BLOCK;
    }

    private boolean isInReach(Player player) {
        return RotationUtil.distanceToEntity(player) <= 3.0;
    }

    private int compareTargets(Player e1, Player e2) {
        return switch (this.sort.getValue()) {
            case FOV -> Float.compare(RotationUtil.angleToEntity(e1), RotationUtil.angleToEntity(e2));
            case HEALTH -> Float.compare(e1.getHealth(), e2.getHealth());
            default -> Double.compare(RotationUtil.distanceToEntity(e1), RotationUtil.distanceToEntity(e2));
        };
    }
}
