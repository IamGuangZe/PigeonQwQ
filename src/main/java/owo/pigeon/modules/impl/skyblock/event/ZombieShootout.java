package owo.pigeon.modules.impl.skyblock.event;

import net.engio.mbassy.listener.Handler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneLampBlock;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import owo.pigeon.event.events.ClientTickEvent;
import owo.pigeon.event.events.RenderEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.FloatSetting;
import owo.pigeon.settings.IntSetting;
import owo.pigeon.utils.ColorUtil;
import owo.pigeon.utils.ItemUtil;
import owo.pigeon.utils.ScoreBoardUtil;
import owo.pigeon.utils.WorldUtil;
import owo.pigeon.utils.player.PlayerUtil;
import owo.pigeon.utils.player.RotationUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static owo.pigeon.Pigeon.mc;

public class ZombieShootout extends Module {

    public ZombieShootout() {
        super("ZombieShootout", Category.EVENT);
    }

    private static final List<BlockPos> LAMP_POSITIONS = List.of(
            new BlockPos(-96, 76, 61),
            new BlockPos(-99, 77, 62),
            new BlockPos(-102, 75, 62),
            new BlockPos(-106, 77, 61),
            new BlockPos(-109, 75, 60),
            new BlockPos(-112, 76, 58),
            new BlockPos(-115, 77, 55),
            new BlockPos(-117, 76, 52),
            new BlockPos(-118, 76, 49),
            new BlockPos(-119, 75, 45),
            new BlockPos(-119, 77, 42),
            new BlockPos(-118, 76, 39)
    );

    public IntSetting horizontalSpeed = setting("horizontal-speed", 40, 10, 100, "%", v -> true);
    public IntSetting verticalSpeed = setting("vertical-speed", 30, 10, 100, "%", v -> true);
    public FloatSetting midpoint = setting("midpoint", 0.3f, 0.0f, 1.0f, v -> true);
    public IntSetting latency = setting("latency", 150, 50, 300, "ms", v -> true);
    public FloatSetting aimThreshold = setting("aim-threshold", 1.3f, 0.5f, 5.0f, "°", v -> true);
    public IntSetting sampleInterval = setting("sample-interval", 6, 1, 10, "ticks", v -> true);
    public IntSetting shotInterval = setting("shot-interval", 10, 1, 20, "ticks", v -> true);
    public EnableSetting assumeHit = setting("assume-hit", false, v -> true);

    private int dartSlot = -1;
    private Object currentTarget;
    private Vec3d targetAimPos;
    private final Map<UUID, TargetData> targetDataMap = new HashMap<>();
    private int shotCooldown;
    private UUID skippedTargetId;

    private static class TargetData {
        Vec3d pos1;
        Vec3d pos2;
        long tick1;
        boolean hasPos1;
        boolean hasPos2;
    }

    @Handler
    public void onTickPre(ClientTickEvent.Pre event) {
        if (shotCooldown > 0) shotCooldown--;
    }

    @Handler
    public void onRender3D(RenderEvent.Render3DEvent event) {
        if (WorldUtil.nullCheck()) return;
        if (!isInZombieShootout()) {
            currentTarget = null;
            targetAimPos = null;
            dartSlot = -1;
            return;
        }

        findDartSlot();

        BlockPos litLamp = findLitLamp();
        if (litLamp != null) {
            currentTarget = litLamp;
            targetAimPos = new Vec3d(litLamp.getX() + 0.5, litLamp.getY() + 0.8, litLamp.getZ() + 0.5);
        } else {
            ZombieEntity bestZombie = findBestZombie();
            if (bestZombie != null) {
                currentTarget = bestZombie;
                targetAimPos = computeAimPos(bestZombie);
            } else {
                currentTarget = null;
                targetAimPos = null;
            }
        }

        if (targetAimPos == null) return;

        smoothAimAt(targetAimPos, event);

        if (dartSlot < 0) return;
        if (shotCooldown > 0) return;

        if (isOnTarget(targetAimPos)) {
            PlayerUtil.switchUseItem(dartSlot, PlayerUtil.RightClickMode.MOUSE);
            shotCooldown = shotInterval.getValue();

            if (assumeHit.getValue()) {
                if (currentTarget instanceof ZombieEntity zombie) {
                    skippedTargetId = zombie.getUuid();
                } else {
                    skippedTargetId = null;
                }
            }
        }
    }

    @Override
    public void onDisable() {
        dartSlot = -1;
        currentTarget = null;
        targetAimPos = null;
        targetDataMap.clear();
        shotCooldown = 0;
        skippedTargetId = null;
    }

    private boolean isInZombieShootout() {
        List<String> lines = ScoreBoardUtil.getSidebarLines();
        for (String line : lines) {
            String stripped = ColorUtil.removeColor(line);
            if (stripped != null && stripped.contains("Zombie Shootout")) {
                return true;
            }
        }
        return false;
    }

    private void findDartSlot() {
        if (dartSlot >= 0) {
            ItemStack stack = mc.player.getInventory().getStack(dartSlot);
            if (!stack.isEmpty()) {
                String name = ColorUtil.removeColor(stack.getName().getString());
                if (name != null && name.toLowerCase().contains("carnival dart tube")) {
                    if (mc.player.getInventory().getSelectedSlot() != dartSlot) {
                        PlayerUtil.switchItemSlot(dartSlot);
                    }
                    return;
                }
            }
            dartSlot = -1;
        }
        int slot = ItemUtil.getSlotFromItemName("carnival dart tube", true);
        if (slot >= 0) {
            dartSlot = slot;
            PlayerUtil.switchItemSlot(slot);
        }
    }

    private BlockPos findLitLamp() {
        for (BlockPos pos : LAMP_POSITIONS) {
            BlockState state = mc.world.getBlockState(pos);
            if (state.isOf(Blocks.REDSTONE_LAMP) && state.get(RedstoneLampBlock.LIT)) {
                return pos;
            }
        }
        return null;
    }

    private ZombieEntity findBestZombie() {
        Vec3d playerPos = mc.player.getEyePos();
        double radius = 32.0;
        Box searchBox = new Box(
                playerPos.x - radius, playerPos.y - radius, playerPos.z - radius,
                playerPos.x + radius, playerPos.y + radius, playerPos.z + radius
        );

        List<ZombieEntity> zombies = mc.world.getEntitiesByClass(ZombieEntity.class, searchBox, e -> true);

        ZombieEntity best = null;
        int bestPriority = -1;
        double bestDist = Double.MAX_VALUE;

        ZombieEntity bestSkipped = null;
        int bestSkippedPriority = -1;
        double bestSkippedDist = Double.MAX_VALUE;

        for (ZombieEntity zombie : zombies) {
            ItemStack head = zombie.getEquippedStack(EquipmentSlot.HEAD);
            if (head.isEmpty()) continue;

            int priority = helmetPriority(head);
            if (priority <= 0) continue;

            updateTargetData(zombie);

            double dist = playerPos.distanceTo(zombie.getEyePos());

            if (assumeHit.getValue() && zombie.getUuid().equals(skippedTargetId)) {
                if (priority > bestSkippedPriority || (priority == bestSkippedPriority && dist < bestSkippedDist)) {
                    bestSkippedPriority = priority;
                    bestSkippedDist = dist;
                    bestSkipped = zombie;
                }
                continue;
            }

            if (priority > bestPriority || (priority == bestPriority && dist < bestDist)) {
                bestPriority = priority;
                bestDist = dist;
                best = zombie;
            }
        }

        if (best != null) return best;
        return bestSkipped;
    }

    private int helmetPriority(ItemStack stack) {
        if (stack.isOf(Items.DIAMOND_HELMET)) return 4;
        if (stack.isOf(Items.GOLDEN_HELMET)) return 3;
        if (stack.isOf(Items.IRON_HELMET)) return 2;
        if (stack.isOf(Items.LEATHER_HELMET)) return 1;
        return 0;
    }

    private void updateTargetData(ZombieEntity zombie) {
        UUID uuid = zombie.getUuid();
        TargetData data = targetDataMap.computeIfAbsent(uuid, k -> new TargetData());
        Vec3d headPos = zombie.getEyePos().add(0, 0.3, 0);
        long currentTick = mc.world.getTime();
        int interval = sampleInterval.getValue();

        if (!data.hasPos1) {
            data.pos1 = headPos;
            data.tick1 = currentTick;
            data.hasPos1 = true;
        } else if (!data.hasPos2 && currentTick - data.tick1 >= interval) {
            data.pos2 = headPos;
            data.hasPos2 = true;
        } else if (data.hasPos2) {
            if (currentTick - data.tick1 >= interval) {
                data.pos1 = data.pos2;
                data.pos2 = headPos;
                data.tick1 = currentTick;
            }
            if (currentTick - data.tick1 > 20) {
                data.hasPos1 = false;
                data.hasPos2 = false;
            }
        }
    }

    private Vec3d computeAimPos(ZombieEntity zombie) {
        UUID uuid = zombie.getUuid();
        TargetData data = targetDataMap.get(uuid);

        if (data != null && data.hasPos2) {
            Vec3d movement = data.pos2.subtract(data.pos1);
            double factor = latency.getValue() / 100.0;
            Vec3d offset = movement.multiply(factor);
            return data.pos2.add(offset);
        }
        return null;
    }

    private void smoothAimAt(Vec3d targetPos, RenderEvent.Render3DEvent event) {
        Vec3d eyePos = mc.player.getEyePos();
        double dx = targetPos.x - eyePos.x;
        double dy = targetPos.y - eyePos.y;
        double dz = targetPos.z - eyePos.z;
        double hDist = Math.sqrt(dx * dx + dz * dz);

        float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float targetPitch = (float) Math.toDegrees(Math.atan2(-dy, hDist));

        float currentYaw = mc.player.getYaw();
        float currentPitch = mc.player.getPitch();
        float delta = event.getDelta();

        float horizontalSpeedVal = (float) horizontalSpeed.getValue();
        float verticalSpeedVal = (float) verticalSpeed.getValue();

        float newYaw = RotationUtil.towardsInterpolation(currentYaw, targetYaw, horizontalSpeedVal, 50.0f, midpoint.getValue(), delta);
        float newPitch = RotationUtil.towardsInterpolation(currentPitch, targetPitch, verticalSpeedVal, 50.0f, midpoint.getValue(), delta);

        newYaw = RotationUtil.normalizeRotation(currentYaw, newYaw);
        newPitch = RotationUtil.normalizeRotation(currentPitch, newPitch);

        mc.player.setYaw(newYaw);
        mc.player.setPitch(MathHelper.clamp(newPitch, -90f, 90f));
    }

    private boolean isOnTarget(Vec3d targetPos) {
        Vec3d eyePos = mc.player.getEyePos();
        double dx = targetPos.x - eyePos.x;
        double dy = targetPos.y - eyePos.y;
        double dz = targetPos.z - eyePos.z;
        double hDist = Math.sqrt(dx * dx + dz * dz);

        float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float targetPitch = (float) Math.toDegrees(Math.atan2(-dy, hDist));

        float yawDiff = Math.abs(MathHelper.wrapDegrees(mc.player.getYaw() - targetYaw));
        float pitchDiff = Math.abs(MathHelper.wrapDegrees(mc.player.getPitch() - targetPitch));

        return yawDiff < aimThreshold.getValue() && pitchDiff < aimThreshold.getValue();
    }
}
