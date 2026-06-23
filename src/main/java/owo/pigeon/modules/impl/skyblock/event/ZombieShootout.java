package owo.pigeon.modules.impl.skyblock.event;

import net.engio.mbassy.listener.Handler;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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
    private Vec3 targetAimPos;
    private final Map<UUID, TargetData> targetDataMap = new HashMap<>();
    private int shotCooldown;
    private UUID skippedTargetId;

    private static class TargetData {
        Vec3 pos1;
        Vec3 pos2;
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
            targetAimPos = new Vec3(litLamp.getX() + 0.5, litLamp.getY() + 0.8, litLamp.getZ() + 0.5);
        } else {
            Zombie bestZombie = findBestZombie();
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
                if (currentTarget instanceof Zombie zombie) {
                    skippedTargetId = zombie.getUUID();
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
            ItemStack stack = mc.player.getInventory().getItem(dartSlot);
            if (!stack.isEmpty()) {
                String name = ColorUtil.removeColor(stack.getHoverName().getString());
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
            BlockState state = mc.level.getBlockState(pos);
            if (state.is(Blocks.REDSTONE_LAMP) && state.getValue(RedstoneLampBlock.LIT)) {
                return pos;
            }
        }
        return null;
    }

    private Zombie findBestZombie() {
        Vec3 playerPos = mc.player.getEyePosition();
        double radius = 32.0;
        AABB searchBox = new AABB(
                playerPos.x - radius, playerPos.y - radius, playerPos.z - radius,
                playerPos.x + radius, playerPos.y + radius, playerPos.z + radius
        );

        List<Zombie> zombies = mc.level.getEntitiesOfClass(Zombie.class, searchBox, e -> true);

        Zombie best = null;
        int bestPriority = -1;
        double bestDist = Double.MAX_VALUE;

        Zombie bestSkipped = null;
        int bestSkippedPriority = -1;
        double bestSkippedDist = Double.MAX_VALUE;

        for (Zombie zombie : zombies) {
            ItemStack head = zombie.getItemBySlot(EquipmentSlot.HEAD);
            if (head.isEmpty()) continue;

            int priority = helmetPriority(head);
            if (priority <= 0) continue;

            updateTargetData(zombie);

            double dist = playerPos.distanceTo(zombie.getEyePosition());

            if (assumeHit.getValue() && zombie.getUUID().equals(skippedTargetId)) {
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
        if (stack.is(Items.DIAMOND_HELMET)) return 4;
        if (stack.is(Items.GOLDEN_HELMET)) return 3;
        if (stack.is(Items.IRON_HELMET)) return 2;
        if (stack.is(Items.LEATHER_HELMET)) return 1;
        return 0;
    }

    private void updateTargetData(Zombie zombie) {
        UUID uuid = zombie.getUUID();
        TargetData data = targetDataMap.computeIfAbsent(uuid, k -> new TargetData());
        Vec3 headPos = zombie.getEyePosition().add(0, 0.3, 0);
        long currentTick = mc.level.getGameTime();
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

    private Vec3 computeAimPos(Zombie zombie) {
        UUID uuid = zombie.getUUID();
        TargetData data = targetDataMap.get(uuid);

        if (data != null && data.hasPos2) {
            Vec3 movement = data.pos2.subtract(data.pos1);
            double factor = latency.getValue() / 100.0;
            Vec3 offset = movement.scale(factor);
            return data.pos2.add(offset);
        }
        return null;
    }

    private void smoothAimAt(Vec3 targetPos, RenderEvent.Render3DEvent event) {
        Vec3 eyePos = mc.player.getEyePosition();
        double dx = targetPos.x - eyePos.x;
        double dy = targetPos.y - eyePos.y;
        double dz = targetPos.z - eyePos.z;
        double hDist = Math.sqrt(dx * dx + dz * dz);

        float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float targetPitch = (float) Math.toDegrees(Math.atan2(-dy, hDist));

        float currentYaw = mc.player.getYRot();
        float currentPitch = mc.player.getXRot();
        float delta = event.getDelta();

        float horizontalSpeedVal = (float) horizontalSpeed.getValue();
        float verticalSpeedVal = (float) verticalSpeed.getValue();

        float newYaw = RotationUtil.towardsInterpolation(currentYaw, targetYaw, horizontalSpeedVal, 50.0f, midpoint.getValue(), delta);
        float newPitch = RotationUtil.towardsInterpolation(currentPitch, targetPitch, verticalSpeedVal, 50.0f, midpoint.getValue(), delta);

        newYaw = RotationUtil.normalizeRotation(currentYaw, newYaw);
        newPitch = RotationUtil.normalizeRotation(currentPitch, newPitch);

        mc.player.setYRot(newYaw);
        mc.player.setXRot(Mth.clamp(newPitch, -90f, 90f));
    }

    private boolean isOnTarget(Vec3 targetPos) {
        Vec3 eyePos = mc.player.getEyePosition();
        double dx = targetPos.x - eyePos.x;
        double dy = targetPos.y - eyePos.y;
        double dz = targetPos.z - eyePos.z;
        double hDist = Math.sqrt(dx * dx + dz * dz);

        float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float targetPitch = (float) Math.toDegrees(Math.atan2(-dy, hDist));

        float yawDiff = Math.abs(Mth.wrapDegrees(mc.player.getYRot() - targetYaw));
        float pitchDiff = Math.abs(Mth.wrapDegrees(mc.player.getXRot() - targetPitch));

        return yawDiff < aimThreshold.getValue() && pitchDiff < aimThreshold.getValue();
    }
}
