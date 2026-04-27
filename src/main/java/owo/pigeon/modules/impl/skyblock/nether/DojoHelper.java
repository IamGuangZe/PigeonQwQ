package owo.pigeon.modules.impl.skyblock.nether;

import net.engio.mbassy.listener.Handler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import owo.pigeon.event.events.DoAttackEvent;
import owo.pigeon.event.events.RenderEvent;
import owo.pigeon.event.events.TickEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.utils.KeybindUtil;
import owo.pigeon.utils.WorldUtil;
import owo.pigeon.utils.chat.ChatUtil;
import owo.pigeon.utils.hypixel.skyblock.DojoUtil;
import owo.pigeon.utils.player.PlayerUtil;
import owo.pigeon.utils.player.RotationUtil;
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

    private static final int BLOCK_LIFE_TIME = 6550;
    private static final float H_SPEED = 45.0f;
    private static final float V_SPEED = 30.0f;
    private static final double ARROW_GRAVITY = 0.05;
    private static final double ARROW_DRAG = 0.99;
    private static final double MAX_ARROW_VELOCITY = 3.0;

    private final Set<BlockPos> limeWoolBlocks = ConcurrentHashMap.newKeySet();
    private final List<BlockPos> blockOrder = new ArrayList<>();
    private final Map<BlockPos, Long> endTimes = new HashMap<>();
    private BlockPos currentTarget;
    private BlockPos lockedTarget;
    private boolean hasRelease;

    @Override
    public void onEnable() {
        if (mc.worldRenderer != null) mc.worldRenderer.reload();
    }

    @Handler
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (WorldUtil.nullCheck()) return;

        if (event instanceof TickEvent.ClientTickEvent.Pre) {
            if (mastery.getValue() && DojoUtil.isDojoChallenge(DojoUtil.Dojo.Mastery)) {
                if (mc.player.isUsingItem()) {
                    BlockPos releaseTarget = lockedTarget != null ? lockedTarget : currentTarget;
                    Long endTime = endTimes.get(releaseTarget);
                    long now = System.currentTimeMillis();

                    if (endTime != null && now >= endTime) {
                        KeybindUtil.setPressed(mc.options.useKey, false);
                        hasRelease = true;
                        ChatUtil.sendDebugMessage(this.name, "Releasing bow — endTime reached at " + now);
                    }
                }
            }
        }

        if (event instanceof TickEvent.ClientTickEvent.Post) {
            if (mastery.getValue() && DojoUtil.isDojoChallenge(DojoUtil.Dojo.Mastery)) {
                if (hasRelease) {
                    KeybindUtil.resetPressed(mc.options.useKey);
                    hasRelease = false;
                    lockedTarget = null;
                }
            }
        }
    }

    @Handler
    public void onRender3D(RenderEvent.Render3DEvent event) {
        if (force.getValue() && DojoUtil.isDojoChallenge(DojoUtil.Dojo.Force)) {
            for (Entity entity : mc.world.getEntities()) {
                if (entity instanceof ZombieEntity zombie &&
                        zombie.getEquippedStack(EquipmentSlot.HEAD).isOf(Items.LEATHER_HELMET))
                    entity.discard();
            }
        }

        if (mastery.getValue() && DojoUtil.isDojoChallenge(DojoUtil.Dojo.Mastery)) {

            limeWoolBlocks.removeIf(pos ->
                    !mc.world.getBlockState(pos).isOf(Blocks.LIME_WOOL) &&
                            !mc.world.getBlockState(pos).isOf(Blocks.YELLOW_WOOL) &&
                            !mc.world.getBlockState(pos).isOf(Blocks.RED_WOOL)
            );

            for (BlockPos pos : limeWoolBlocks) {
                if (!endTimes.containsKey(pos)) {
                    blockOrder.add(pos);
                    long travelTime = (long) (Math.sqrt(mc.player.getBlockPos().getSquaredDistance(pos)) * 1000.0 / 60.0);
                    long endTime = System.currentTimeMillis() + BLOCK_LIFE_TIME - ServerUtil.getCurrentPing() - travelTime;
                    endTimes.put(pos, endTime);
                    ChatUtil.sendDebugMessage(this.name, "LIME_WOOL detected at " + pos.toShortString() +
                            " | endTime in " + (endTime - System.currentTimeMillis()) + "ms" +
                            " | travelTime=" + travelTime + "ms | ping=" + ServerUtil.getCurrentPing() + "ms");
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

            if (lockedTarget != null && !KeybindUtil.isPressed(mc.options.useKey) && !hasRelease) {
                lockedTarget = null;
                currentTarget = blockOrder.isEmpty() ? null : blockOrder.getFirst();
            }

            BlockPos aimTarget = lockedTarget != null ? lockedTarget : currentTarget;
            if (aimTarget != null && isHoldingBow() && KeybindUtil.isPressed(mc.options.useKey)) {
                if (lockedTarget == null) {
                    lockedTarget = currentTarget;
                    aimTarget = lockedTarget;
                }

                Vec3d eyes = mc.player.getCameraPosVec(event.getDelta());
                Vec3d target = aimTarget.toCenterPos();
                double diffX = target.x - eyes.x;
                double diffZ = target.z - eyes.z;

                float targetYaw = (float) Math.toDegrees(Math.atan2(-diffX, diffZ));
                float targetPitch = calculateBallisticPitch(eyes, aimTarget, getArrowVelocity());

                float currentYaw = mc.player.getYaw();
                float currentPitch = mc.player.getPitch();

                float newYaw = RotationUtil.towardsLinear(currentYaw, targetYaw, H_SPEED, event.getDelta());
                float newPitch = RotationUtil.towardsLinear(currentPitch, targetPitch, V_SPEED, event.getDelta());

                newYaw = RotationUtil.normalizeRotation(currentYaw, newYaw);
                newPitch = RotationUtil.normalizeRotation(currentPitch, newPitch);

                mc.player.setYaw(newYaw);
                mc.player.setPitch(MathHelper.clamp(newPitch, -90f, 90f));
            }
        }
    }

    @Handler
    public void onRenderBlock(RenderEvent.RenderBlockEvent event) {
        if (!mastery.getValue()) return;
        if (!DojoUtil.isDojoChallenge(DojoUtil.Dojo.Mastery)) return;

        if (event.getState().isOf(Blocks.LIME_WOOL)) {
            limeWoolBlocks.add(event.getPos().toImmutable());
        }
    }

    @Handler
    public void onDoAttack(DoAttackEvent event) {
        if (discipline.getValue() && DojoUtil.isDojoChallenge(DojoUtil.Dojo.Discipline)) {
            if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.ENTITY) {
                Entity target = ((EntityHitResult) mc.crosshairTarget).getEntity();

                if (target instanceof ZombieEntity zombie) {
                    ItemStack helmet = zombie.getEquippedStack(EquipmentSlot.HEAD);
                    if (helmet.isOf(Items.LEATHER_HELMET)) {
                        PlayerUtil.switchItemSlot(0);
                    } else if (helmet.isOf(Items.IRON_HELMET)) {
                        PlayerUtil.switchItemSlot(1);
                    } else if (helmet.isOf(Items.GOLDEN_HELMET)) {
                        PlayerUtil.switchItemSlot(2);
                    } else if (helmet.isOf(Items.DIAMOND_HELMET)) {
                        PlayerUtil.switchItemSlot(3);
                    }
                }
            }
        }
    }

    @Override
    public void onDisable() {
        limeWoolBlocks.clear();
        blockOrder.clear();
        endTimes.clear();
        currentTarget = null;
        lockedTarget = null;
        hasRelease = false;
        KeybindUtil.resetPressed(mc.options.useKey);
    }

    private float calculateBallisticPitch(Vec3d eyes, BlockPos target, double velocity) {
        Vec3d targetCenter = target.toCenterPos();
        double dx = targetCenter.x - eyes.x;
        double dy = targetCenter.y - eyes.y;
        double dz = targetCenter.z - eyes.z;
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
            float charge = (float) mc.player.getItemUseTime() / 20.0f;
            float f = (charge * charge + charge * 2.0f) / 3.0f;
            return Math.min(f, 1.0f) * MAX_ARROW_VELOCITY;
        }
        return MAX_ARROW_VELOCITY;
    }

    private boolean isHoldingBow() {
        return mc.player.getMainHandStack().isOf(Items.BOW);
    }
}
