package owo.pigeon.modules.impl.skyblock.nether;

import net.engio.mbassy.listener.Handler;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import owo.pigeon.Pigeon;
import owo.pigeon.event.events.ClientTickEvent;
import owo.pigeon.event.events.DoAttackEvent;
import owo.pigeon.event.events.RenderEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.IntSetting;
import owo.pigeon.utils.KeybindUtil;
import owo.pigeon.utils.WorldUtil;
import owo.pigeon.utils.chat.ChatUtil;
import owo.pigeon.utils.hypixel.skyblock.DojoUtil;
import owo.pigeon.utils.player.PlayerUtil;
import owo.pigeon.utils.player.RotationUtil;
import owo.pigeon.utils.render.RenderUtil;
import owo.pigeon.utils.world.ServerUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static owo.pigeon.Pigeon.mc;

public class DojoHelper extends Module {
    public DojoHelper() {
        super("DojoHelper", Category.NETHER);
    }

    public EnableSetting force = setting("force", true, v -> true);
    public EnableSetting mastery = setting("mastery", true, v -> true);
    public EnableSetting discipline = setting("discipline", true, v -> true);
    public EnableSetting control = setting("control", true, v -> true);
    public IntSetting pingOffset = setting("ping-offset", 0, 0, 500, "ms", v -> true);

    private static final int BLOCK_LIFE_TIME = 6550;
    private static final float H_SPEED = 45.0f;
    private static final float V_SPEED = 30.0f;
    private static final double ARROW_GRAVITY = 0.05;
    private static final double ARROW_DRAG = 0.99;
    private static final double MAX_ARROW_VELOCITY = 3.0;
    private static final int CONTROL_UPDATE_INTERVAL = 150;
    private static final int CONTROL_PREDICT_TICKS = 3;

    private final Set<BlockPos> limeWoolBlocks = ConcurrentHashMap.newKeySet();
    private final List<BlockPos> blockOrder = new ArrayList<>();
    private final Map<BlockPos, Long> endTimes = new HashMap<>();
    private BlockPos currentTarget;
    private BlockPos lockedTarget;
    private boolean hasRelease;

    private WitherSkeleton controlWither;
    private Vec3 controlLastPos;
    private long controlLastUpdate;
    private Vec3 controlPingOffset;
    private Vec3 controlLastPingOffset;
    private int controlTickCounter;

    @Override
    public void onEnable() {
        if (mc.levelRenderer != null) mc.levelRenderer.allChanged();
    }

    @Handler
    public void onClientTick(ClientTickEvent event) {
        if (WorldUtil.nullCheck()) return;

        if (event instanceof ClientTickEvent.Pre) {
            if (mastery.getValue() && DojoUtil.isDojoChallenge(DojoUtil.Dojo.Mastery)) {
                if (mc.player.isUsingItem()) {
                    BlockPos releaseTarget = lockedTarget != null ? lockedTarget : currentTarget;
                    Long endTime = endTimes.get(releaseTarget);
                    long now = System.currentTimeMillis();

                    if (endTime != null && now >= endTime) {
                        KeybindUtil.setPressed(mc.options.keyUse, false);
                        hasRelease = true;
                        ChatUtil.sendDebugMessage(this.name, "Releasing bow — endTime reached at " + now);
                    }
                }
            }

            if (control.getValue() && DojoUtil.isDojoChallenge(DojoUtil.Dojo.Control)) {
                updateControlWither();
            }
        }

        if (event instanceof ClientTickEvent.Post) {
            if (mastery.getValue() && DojoUtil.isDojoChallenge(DojoUtil.Dojo.Mastery)) {
                if (hasRelease) {
                    KeybindUtil.resetPressed(mc.options.keyUse);
                    hasRelease = false;
                    lockedTarget = null;
                }
            }
        }
    }

    @Handler
    public void onRender3D(RenderEvent.Render3DEvent event) {
        if (WorldUtil.nullCheck()) return;

        if (force.getValue() && DojoUtil.isDojoChallenge(DojoUtil.Dojo.Force)) {
            for (Entity entity : mc.level.entitiesForRendering()) {
                if (entity instanceof Zombie zombie &&
                        zombie.getItemBySlot(EquipmentSlot.HEAD).is(Items.LEATHER_HELMET))
                    entity.discard();
            }
        }

        if (mastery.getValue() && DojoUtil.isDojoChallenge(DojoUtil.Dojo.Mastery)) {
            handleMastery(event);
        }

        if (control.getValue() && DojoUtil.isDojoChallenge(DojoUtil.Dojo.Control)) {
            handleControl(event);
        }
    }

    @Handler
    public void onRenderBlock(RenderEvent.RenderBlockEvent event) {
        if (!mastery.getValue()) return;
        if (!DojoUtil.isDojoChallenge(DojoUtil.Dojo.Mastery)) return;

        if (event.getState().is(Blocks.LIME_WOOL)) {
            limeWoolBlocks.add(event.getPos().immutable());
        }
    }

    @Handler
    public void onDoAttack(DoAttackEvent event) {
        if (discipline.getValue() && DojoUtil.isDojoChallenge(DojoUtil.Dojo.Discipline)) {
            if (mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.ENTITY) {
                Entity target = ((EntityHitResult) mc.hitResult).getEntity();

                if (target instanceof Zombie zombie) {
                    ItemStack helmet = zombie.getItemBySlot(EquipmentSlot.HEAD);
                    if (helmet.is(Items.LEATHER_HELMET)) {
                        PlayerUtil.switchItemSlot(0);
                    } else if (helmet.is(Items.IRON_HELMET)) {
                        PlayerUtil.switchItemSlot(1);
                    } else if (helmet.is(Items.GOLDEN_HELMET)) {
                        PlayerUtil.switchItemSlot(2);
                    } else if (helmet.is(Items.DIAMOND_HELMET)) {
                        PlayerUtil.switchItemSlot(3);
                    }
                }
            }
        }
    }

    private void handleMastery(RenderEvent.Render3DEvent event) {
        limeWoolBlocks.removeIf(pos ->
                !mc.level.getBlockState(pos).is(Blocks.LIME_WOOL) &&
                        !mc.level.getBlockState(pos).is(Blocks.YELLOW_WOOL) &&
                        !mc.level.getBlockState(pos).is(Blocks.RED_WOOL)
        );

        for (BlockPos pos : limeWoolBlocks) {
            if (!endTimes.containsKey(pos)) {
                blockOrder.add(pos);
                long travelTime = (long) (mc.player.position().distanceTo(pos.getCenter()) * 1000.0 / 60.0);
                long effectivePing = ServerUtil.getCurrentPing() + pingOffset.getValue();
                long endTime = System.currentTimeMillis() + BLOCK_LIFE_TIME - effectivePing - travelTime;
                endTimes.put(pos, endTime);
                ChatUtil.sendDebugMessage(this.name, "LIME_WOOL detected at " + pos.toShortString() +
                        " | endTime in " + (endTime - System.currentTimeMillis()) + "ms" +
                        " | travelTime=" + travelTime + "ms | ping=" + effectivePing + "ms" +
                        " (base=" + ServerUtil.getCurrentPing() + " + offset=" + pingOffset.getValue() + ")");
            }
        }

        long currentTime = System.currentTimeMillis();
        blockOrder.removeIf(pos -> {
            if (pos.equals(lockedTarget)) return false;

            if (!limeWoolBlocks.contains(pos)) {
                endTimes.remove(pos);
                if (pos.equals(currentTarget)) currentTarget = null;
                return true;
            }
            Long endTime = endTimes.get(pos);
            if (endTime == null || endTime <= currentTime) {
                endTimes.remove(pos);
                if (pos.equals(currentTarget)) currentTarget = null;
                return true;
            }
            return false;
        });

        if (lockedTarget != null && !limeWoolBlocks.contains(lockedTarget)) {
            endTimes.remove(lockedTarget);
            blockOrder.remove(lockedTarget);
            if (lockedTarget.equals(currentTarget)) currentTarget = null;
            lockedTarget = null;
        }

        if (lockedTarget == null) {
            currentTarget = blockOrder.isEmpty() ? null : blockOrder.getFirst();
        }

        if (lockedTarget != null && !KeybindUtil.isPressed(mc.options.keyUse) && !hasRelease) {
            lockedTarget = null;
            currentTarget = blockOrder.isEmpty() ? null : blockOrder.getFirst();
        }

        BlockPos aimTarget = lockedTarget != null ? lockedTarget : currentTarget;
        if (aimTarget != null && isHoldingBow() && KeybindUtil.isPressed(mc.options.keyUse)) {
            if (lockedTarget == null) {
                lockedTarget = currentTarget;
                aimTarget = lockedTarget;
            }

            Vec3 eyes = mc.player.getEyePosition(event.getDelta());
            Vec3 target = aimTarget.getCenter();
            double diffX = target.x - eyes.x;
            double diffZ = target.z - eyes.z;

            float targetYaw = (float) Math.toDegrees(Math.atan2(-diffX, diffZ));
            float targetPitch = calculateBallisticPitch(eyes, target, getArrowVelocity());

            applyRotation(targetYaw, targetPitch, event.getDelta());
        }
    }

    private void handleControl(RenderEvent.Render3DEvent event) {
        if (controlWither == null || !controlWither.isAlive()) {
            resetControlState();
            return;
        }

        Vec3 aimPos = getControlAimPos(event.getDelta());
        if (aimPos == null) return;

        // Debug: render aim position box (1x1x1, matching Skyblocker's target AABB)
        if (Pigeon.isDebug()) {
            AABB debugBox = new AABB(
                    aimPos.x - 0.5, aimPos.y - 0.5, aimPos.z - 0.5,
                    aimPos.x + 0.5, aimPos.y + 0.5, aimPos.z + 0.5
            );
            RenderUtil.drawBox(event.getMatrix(), debugBox, new java.awt.Color(0, 255, 128), 2.0);
        }

        Vec3 eyes = mc.player.getEyePosition(event.getDelta());
        double diffX = aimPos.x - eyes.x;
        double diffY = aimPos.y - eyes.y;
        double diffZ = aimPos.z - eyes.z;
        double hDist = Math.sqrt(diffX * diffX + diffZ * diffZ);

        if (hDist < 0.01) return;

        float targetYaw = (float) Math.toDegrees(Math.atan2(-diffX, diffZ));
        float targetPitch = (float) Math.toDegrees(Math.atan2(-diffY, hDist));

        applyRotation(targetYaw, targetPitch, event.getDelta());
    }

    private Vec3 getControlAimPos(float tickDelta) {
        if (controlPingOffset == null || controlLastPingOffset == null) {
            return controlWither.getEyePosition(tickDelta);
        }

        double updatePercent = (double) (System.currentTimeMillis() - controlLastUpdate) / CONTROL_UPDATE_INTERVAL;
        updatePercent = Mth.clamp(updatePercent, 0.0, 1.0);

        Vec3 interpolatedOffset = controlPingOffset.scale(updatePercent)
                .add(controlLastPingOffset.scale(1.0 - updatePercent));

        return controlWither.getEyePosition(tickDelta).add(interpolatedOffset);
    }

    private void updateControlWither() {
        if (controlWither != null) {
            if (!controlWither.isAlive() || !isControlTarget(controlWither) || !isInArenaRange(controlWither)) {
                String reason = !controlWither.isAlive() ? "dead"
                        : !isControlTarget(controlWither) ? "no longer a valid target (equipment changed)"
                        : "out of arena range";
                ChatUtil.sendDebugMessage(this.name, "Control: Lost target — " + reason);
                resetControlState();
            }
        }

        if (controlWither == null) {
            WitherSkeleton bestWither = null;
            double bestDist = Double.MAX_VALUE;
            Vec3 playerPos = mc.player.position();

            for (Entity entity : mc.level.entitiesForRendering()) {
                if (!isControlTarget(entity)) continue;
                WitherSkeleton wither = (WitherSkeleton) entity;
                if (!isInArenaRange(wither)) continue;

                double dist = playerPos.distanceToSqr(wither.position());
                if (dist < bestDist) {
                    bestDist = dist;
                    bestWither = wither;
                }
            }

            if (bestWither != null) {
                controlWither = bestWither;
                controlTickCounter = 0;
                ChatUtil.sendDebugMessage(this.name, "Control: Tracking WitherSkeleton at " +
                        bestWither.blockPosition().toShortString() +
                        " | hDist=" + String.format("%.1f", horizontalDistFromPlayer(bestWither)));
            }
            return;
        }

        controlTickCounter++;
        if (controlTickCounter >= CONTROL_PREDICT_TICKS) {
            controlTickCounter = 0;

            if (controlLastPos != null) {
                controlLastPingOffset = controlPingOffset;
                double ping = (ServerUtil.getCurrentPing() + pingOffset.getValue()) / 1000.0;
                Vec3 currentPos = controlWither.position();
                Vec3 movement = currentPos.subtract(controlLastPos).multiply(1, 0.1, 1);
                controlPingOffset = movement.scale(1.0 + (double) CONTROL_PREDICT_TICKS / 20.0 + ping);
            }
            controlLastPos = controlWither.position();
            controlLastUpdate = System.currentTimeMillis();
        }
    }

    private boolean isControlTarget(Entity entity) {
        if (!(entity instanceof WitherSkeleton wither)) return false;

        ItemStack helmet = wither.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.isEmpty()) return false;
        if (!(helmet.getItem() instanceof BlockItem)) return false;
        return !helmet.is(Items.PLAYER_HEAD);
    }

    private boolean isInArenaRange(Entity entity) {
        boolean inArena = DojoUtil.isInDojoChallenge(entity);
        ChatUtil.sendDebugMessage(this.name, "isInArenaRange: " +
                DojoUtil.getDojoBoundsDiagnostic(entity) +
                " | inArena=" + inArena);
        return inArena;
    }

    private double horizontalDistFromPlayer(Entity entity) {
        double dx = entity.getX() - mc.player.getX();
        double dz = entity.getZ() - mc.player.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    private void resetControlState() {
        controlWither = null;
        controlLastPos = null;
        controlPingOffset = null;
        controlLastPingOffset = null;
        controlTickCounter = 0;
    }

    private void applyRotation(float targetYaw, float targetPitch, float delta) {
        float currentYaw = mc.player.getYRot();
        float currentPitch = mc.player.getXRot();

        float newYaw = RotationUtil.towardsLinear(currentYaw, targetYaw, H_SPEED, delta);
        float newPitch = RotationUtil.towardsLinear(currentPitch, targetPitch, V_SPEED, delta);

        newYaw = RotationUtil.normalizeRotation(currentYaw, newYaw);
        newPitch = RotationUtil.normalizeRotation(currentPitch, newPitch);

        mc.player.setYRot(newYaw);
        mc.player.setXRot(Mth.clamp(newPitch, -90f, 90f));
    }

    private float calculateBallisticPitch(Vec3 eyes, Vec3 target, double velocity) {
        double dx = target.x - eyes.x;
        double dy = target.y - eyes.y;
        double dz = target.z - eyes.z;
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);

        float geometricPitch = (float) Math.toDegrees(Math.atan2(-dy, horizontalDist));

        if (horizontalDist < 1.0 || velocity < 0.3) {
            return geometricPitch;
        }

        float low = -90f;
        float high = 90f;

        for (int i = 0; i < 16; i++) {
            float mid = (low + high) / 2f;
            float midRad = (float) Math.toRadians(mid);

            double hSpeed = Math.cos(midRad) * velocity;
            double vSpeed = -Math.sin(midRad) * velocity;

            double simH = 0;
            double simV = 0;

            for (int tick = 0; tick < 200; tick++) {
                hSpeed *= ARROW_DRAG;
                vSpeed = vSpeed * ARROW_DRAG - ARROW_GRAVITY;

                simH += hSpeed;
                simV += vSpeed;

                if (simH >= horizontalDist) break;
            }

            if (simV >= dy) {
                low = mid;
            } else {
                high = mid;
            }
        }

        float result = (low + high) / 2f;

        float resultRad = (float) Math.toRadians(result);
        double verifyH = Math.cos(resultRad) * velocity;
        double verifyV = -Math.sin(resultRad) * velocity;
        double simH = 0;
        double simV = 0;

        for (int tick = 0; tick < 200; tick++) {
            verifyH *= ARROW_DRAG;
            verifyV = verifyV * ARROW_DRAG - ARROW_GRAVITY;
            simH += verifyH;
            simV += verifyV;
            if (simH >= horizontalDist) break;
        }

        if (simH < horizontalDist || Math.abs(simV - dy) > 2.0) {
            return geometricPitch;
        }

        return result;
    }

    private double getArrowVelocity() {
        if (mc.player.isUsingItem() && isHoldingBow()) {
            float charge = (float) mc.player.getTicksUsingItem() / 20.0f;
            float f = (charge * charge + charge * 2.0f) / 3.0f;
            return Math.min(f, 1.0f) * MAX_ARROW_VELOCITY;
        }
        return MAX_ARROW_VELOCITY;
    }

    private boolean isHoldingBow() {
        return mc.player.getMainHandItem().is(Items.BOW);
    }

    @Override
    public void onDisable() {
        limeWoolBlocks.clear();
        blockOrder.clear();
        endTimes.clear();
        currentTarget = null;
        lockedTarget = null;
        hasRelease = false;

        resetControlState();

        KeybindUtil.resetPressed(mc.options.keyUse);
    }
}
