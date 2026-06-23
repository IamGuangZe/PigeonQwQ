package owo.pigeon.modules.impl.skyblock.mining;

import net.engio.mbassy.listener.Handler;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import owo.pigeon.event.events.ClientTickEvent;
import owo.pigeon.event.events.RenderEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.*;
import owo.pigeon.utils.KeybindUtil;
import owo.pigeon.utils.WorldUtil;
import owo.pigeon.utils.chat.ChatUtil;
import owo.pigeon.utils.player.RotationUtil;
import owo.pigeon.utils.render.RenderUtil;

import java.awt.*;
import java.util.*;
import java.util.List;

import static owo.pigeon.Pigeon.isDebug;
import static owo.pigeon.Pigeon.mc;

public class LegitNuker extends Module {
    public LegitNuker() {
        super("LegitNuker", Category.MINING);
    }

    // 该模块全部代码由 GLM-5-Turbo 模型完成

    public enum MithrilSort {
        NONE, HARDNESS_ASC, HARDNESS_DESC
    }

    public EnableSetting stopInGui = setting("stop-in-gui", true, v -> true);
    public EnableSetting mineBelowFeet = setting("mine-below-feet", true, v -> true);
    public EnableSetting keepPress = setting("keep-press", false, v -> true);
    public IntSetting switchDelay = setting("switch-delay", 1, 0, 20, "ticks", v -> true);
    public FloatSetting boxInset = setting("box-inset", 0.1f, 0.0f, 0.2f, v -> true);
    public IntSetting timeout = setting("timeout", 5, 1, 60, "s", v -> true);
    public ExpandSetting rotation = setting("rotation", v -> true);
    public IntSetting horizontalSpeed = setting("horizontal-speed", 45, 1, 100, "%", v -> rotation.getValue());
    public IntSetting verticalSpeed = setting("vertical-speed", 30, 1, 100, "%", v -> rotation.getValue());
    public ExpandSetting blockPresets = setting("block-presets", v -> true);
    public EnableSetting mithril = setting("mithril", true, v -> blockPresets.getValue());
    public EnableSetting titanium = setting("titanium", true, v -> blockPresets.getValue());
    public EnableSetting diamondBlock = setting("diamond-block", false, v -> blockPresets.getValue());
    public EnableSetting coalBlock = setting("coal-block", false, v -> blockPresets.getValue());
    public EnableSetting emeraldBlock = setting("emerald-block", false, v -> blockPresets.getValue());
    public EnableSetting ironBlock = setting("iron-block", false, v -> blockPresets.getValue());
    public EnableSetting goldBlock = setting("gold-block", false, v -> blockPresets.getValue());
    public EnableSetting redstoneBlock = setting("redstone-block", false, v -> blockPresets.getValue());
    public EnableSetting lapisBlock = setting("lapis-block", false, v -> blockPresets.getValue());
    public EnableSetting quartzBlock = setting("quartz-block", false, v -> blockPresets.getValue());
    public ModeSetting<MithrilSort> mithrilSort = setting("mithril-sort", MithrilSort.HARDNESS_ASC, v -> mithril.isVisible() && mithril.getValue());
    public EnableSetting prioritizeTitanium = setting("prioritize-titanium", false, v -> titanium.isVisible() && titanium.getValue());

    private static final Set<Block> MITHRIL_BLOCKS = Set.of(
            Blocks.CYAN_TERRACOTTA, Blocks.GRAY_WOOL, Blocks.DARK_PRISMARINE,
            Blocks.PRISMARINE_BRICKS, Blocks.PRISMARINE, Blocks.LIGHT_BLUE_WOOL
    );
    private static final List<Block> MITHRIL_BLOCKS_ORDERED = List.of(
            Blocks.CYAN_TERRACOTTA, Blocks.GRAY_WOOL, Blocks.DARK_PRISMARINE,
            Blocks.PRISMARINE_BRICKS, Blocks.PRISMARINE, Blocks.LIGHT_BLUE_WOOL
    );
    private static final Set<Block> TITANIUM_BLOCKS = Set.of(Blocks.POLISHED_DIORITE);
    private static final Set<Block> GENERIC_TARGET_BLOCKS = Set.of(
            Blocks.DIAMOND_BLOCK, Blocks.COAL_BLOCK, Blocks.EMERALD_BLOCK,
            Blocks.IRON_BLOCK, Blocks.GOLD_BLOCK, Blocks.REDSTONE_BLOCK,
            Blocks.LAPIS_BLOCK, Blocks.QUARTZ_BLOCK
    );

    private double getReachDistance() {
        return mc.player.blockInteractionRange();
    }

    private BlockPos currentTarget = null;
    private BlockPos wasTarget = null;
    private int switchDelayCounter = 0;
    private Vec3 aimPoint = null;
    private final List<Vec3> failedAimPoints = new ArrayList<>();
    private int timeoutTimer = 0;
    private BlockPos miningStartTarget = null;
    private final Map<BlockPos, Integer> ignoredPositions = new HashMap<>();
    private int mineCounter = 0;

    @Handler
    public void onTick(ClientTickEvent event) {
        if (WorldUtil.nullCheck()) return;

        if (event instanceof ClientTickEvent.Pre) {
            if (stopInGui.getValue() && mc.screen instanceof AbstractContainerScreen) {
                currentTarget = null;
                aimPoint = null;
                failedAimPoints.clear();
                if (!keepPress.getValue()) KeybindUtil.resetPressed(mc.options.keyAttack);
                return;
            }

            cleanupIgnoredPositions();

            if (miningStartTarget != null) {
                if (!isTargetBlock(mc.level.getBlockState(miningStartTarget))) {
                    mineCounter++;
                    evictStaleIgnored();
                    timeoutTimer = 0;
                    miningStartTarget = null;
                    currentTarget = null;
                } else {
                    timeoutTimer++;
                    if (timeoutTimer > timeout.getValue() * 20) {
                        ChatUtil.sendDebugMessage("LegitNuker",
                                String.format("Timeout reached for %s, skipping...",
                                        miningStartTarget.toShortString()));
                        ignoredPositions.put(miningStartTarget, mineCounter);
                        timeoutTimer = 0;
                        miningStartTarget = null;
                        currentTarget = null;
                        wasTarget = null;
                        aimPoint = null;
                        failedAimPoints.clear();
                        switchDelayCounter = 0;
                        if (!keepPress.getValue()) KeybindUtil.resetPressed(mc.options.keyAttack);
                    }
                }
            }

            if (currentTarget == null) {
                currentTarget = lookupTarget();
            }

            if (currentTarget == null) {
                wasTarget = null;
                aimPoint = null;
                failedAimPoints.clear();
                if (!keepPress.getValue()) KeybindUtil.resetPressed(mc.options.keyAttack);
                return;
            }

            if (!currentTarget.equals(miningStartTarget)) {
                timeoutTimer = 0;
                miningStartTarget = currentTarget;
            }
        }

        if (event instanceof ClientTickEvent.Post) {
            if (keepPress.getValue()) {
                if (currentTarget != null) {
                    KeybindUtil.setPressed(mc.options.keyAttack, true);
                    wasTarget = currentTarget;
                } else {
                    KeybindUtil.resetPressed(mc.options.keyAttack);
                }
                return;
            }

            if (currentTarget == null) return;

            if (wasTarget != null && !currentTarget.equals(wasTarget)) {
                if (switchDelayCounter < switchDelay.getValue()) {
                    switchDelayCounter++;
                    KeybindUtil.resetPressed(mc.options.keyAttack);
                    return;
                }
            }
            switchDelayCounter = 0;

            double reach = getReachDistance();
            HitResult hitResult = mc.player.pick(reach, 1.0f, false);
            if (hitResult.getType() != HitResult.Type.BLOCK) {
                KeybindUtil.resetPressed(mc.options.keyAttack);
                return;
            }

            BlockHitResult blockHit = (BlockHitResult) hitResult;
            if (!blockHit.getBlockPos().equals(currentTarget)) {
                KeybindUtil.resetPressed(mc.options.keyAttack);
                return;
            }

            KeybindUtil.setPressed(mc.options.keyAttack, true);
            wasTarget = currentTarget;
        }
    }

    @Handler
    public void onRender3D(RenderEvent.Render3DEvent event) {
        if (currentTarget == null) return;

        RenderUtil.drawESP(event.getMatrix(), currentTarget, new Color(0x44FF4444, true), RenderUtil.ESPMode.BOTH, false);

        if (isDebug()) {
            for (Vec3 failed : failedAimPoints) {
                double s = 0.04;
                AABB failedBox = new AABB(failed.x - s, failed.y - s, failed.z - s,
                        failed.x + s, failed.y + s, failed.z + s);
                RenderUtil.drawESP(event.getMatrix(), failedBox, new Color(0x88FF0000, true), RenderUtil.ESPMode.BOTH, false);
            }

            if (aimPoint != null) {
                double s = 0.06;
                AABB aimBox = new AABB(aimPoint.x - s, aimPoint.y - s, aimPoint.z - s,
                        aimPoint.x + s, aimPoint.y + s, aimPoint.z + s);
                RenderUtil.drawESP(event.getMatrix(), aimBox, new Color(0x44FFFF00, true), RenderUtil.ESPMode.BOTH, false);

                Vec3 eyes = mc.player.getEyePosition();
                RenderUtil.draw3DLine(event.getMatrix(), eyes, aimPoint, new Color(0x6600FFFF, true), 1.5);
            }
        }

        rotateToward(currentTarget, event.getDelta());
    }

    @Override
    public void onDisable() {
        currentTarget = null;
        wasTarget = null;
        aimPoint = null;
        failedAimPoints.clear();
        switchDelayCounter = 0;
        timeoutTimer = 0;
        miningStartTarget = null;
        mineCounter = 0;
        ignoredPositions.clear();
        if (mc.options != null) {
            KeybindUtil.resetPressed(mc.options.keyAttack);
        }
    }

    private void cleanupIgnoredPositions() {
        Iterator<Map.Entry<BlockPos, Integer>> it = ignoredPositions.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<BlockPos, Integer> entry = it.next();
            BlockState state = mc.level.getBlockState(entry.getKey());
            if (!isTargetBlock(state) || mineCounter - entry.getValue() >= 3) {
                it.remove();
            }
        }
    }

    private void evictStaleIgnored() {
        if (mineCounter < 3) return;
        ignoredPositions.entrySet().removeIf(e -> mineCounter - e.getValue() >= 3);
    }

    private BlockPos lookupTarget() {
        double reach = getReachDistance();
        double rangeSq = reach * reach;
        Vec3 eyes = mc.player.getEyePosition();

        List<BlockPos> candidates = new ArrayList<>();
        int radius = (int) Math.ceil(reach);
        BlockPos playerPos = mc.player.blockPosition();

        for (int x = playerPos.getX() - radius; x <= playerPos.getX() + radius; x++) {
            for (int y = playerPos.getY() - radius; y <= playerPos.getY() + radius; y++) {
                for (int z = playerPos.getZ() - radius; z <= playerPos.getZ() + radius; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = mc.level.getBlockState(pos);

                    if (state.isAir()) continue;
                    if (!isTargetBlock(state)) continue;
                    if (state.getDestroySpeed(mc.level, pos) < 0) continue;

                    if (ignoredPositions.containsKey(pos)) continue;

                    Vec3 closestPoint = getClosestPointToShape(pos, state, eyes);
                    if (closestPoint == null || closestPoint.distanceToSqr(eyes) > rangeSq) continue;

                    if (!canBeMined(eyes, pos, state, reach)) continue;

                    if (!mineBelowFeet.getValue() && pos.getY() < playerPos.getY()) continue;

                    candidates.add(pos);
                }
            }
        }

        if (candidates.isEmpty()) return null;

        sortCandidates(candidates);

        if (wasTarget != null && candidates.contains(wasTarget) && !ignoredPositions.containsKey(wasTarget)) {
            return wasTarget;
        }

        return candidates.get(0);
    }

    private void sortCandidates(List<BlockPos> candidates) {
        if (candidates.size() <= 1) return;

        boolean priorTitanium = titanium.getValue() && prioritizeTitanium.getValue();
        boolean hasMithril = mithril.getValue();
        boolean mithrilSortActive = hasMithril && mithrilSort.getValue() != MithrilSort.NONE;
        Vec3 eyes = mc.player.getEyePosition();
        float currentYaw = mc.player.getYRot();
        float currentPitch = mc.player.getXRot();

        Map<BlockPos, Float> rotDiffs = new HashMap<>(candidates.size());
        Map<BlockPos, Block> typeCache = new HashMap<>(candidates.size());
        for (BlockPos pos : candidates) {
            BlockState state = mc.level.getBlockState(pos);
            typeCache.put(pos, state.getBlock());
            rotDiffs.put(pos, getRotationDiffToBlock(pos, state, eyes, currentYaw, currentPitch));
        }

        List<BlockPos> titanium = new ArrayList<>();
        List<BlockPos> mithril = new ArrayList<>();
        List<BlockPos> others = new ArrayList<>();
        for (BlockPos pos : candidates) {
            Block block = typeCache.get(pos);
            if (TITANIUM_BLOCKS.contains(block)) {
                titanium.add(pos);
            } else if (MITHRIL_BLOCKS.contains(block)) {
                mithril.add(pos);
            } else {
                others.add(pos);
            }
        }

        Comparator<BlockPos> byRot = Comparator.comparing(rotDiffs::get);
        titanium.sort(byRot);
        others.sort(byRot);

        if (mithrilSortActive) {
            mithril.sort((a, b) -> {
                int indexA = MITHRIL_BLOCKS_ORDERED.indexOf(typeCache.get(a));
                int indexB = MITHRIL_BLOCKS_ORDERED.indexOf(typeCache.get(b));
                if (indexA != indexB) {
                    return mithrilSort.getValue() == MithrilSort.HARDNESS_ASC
                            ? Integer.compare(indexA, indexB)
                            : Integer.compare(indexB, indexA);
                }
                return Float.compare(rotDiffs.get(a), rotDiffs.get(b));
            });
        } else {
            mithril.sort(byRot);
        }

        candidates.clear();

        if (priorTitanium) {
            candidates.addAll(titanium);
            candidates.addAll(mithril);
        } else {
            int mi = 0, ti = 0;
            while (mi < mithril.size() && ti < titanium.size()) {
                if (rotDiffs.get(mithril.get(mi)) <= rotDiffs.get(titanium.get(ti))) {
                    candidates.add(mithril.get(mi++));
                } else {
                    candidates.add(titanium.get(ti++));
                }
            }
            while (mi < mithril.size()) candidates.add(mithril.get(mi++));
            while (ti < titanium.size()) candidates.add(titanium.get(ti++));
        }

        candidates.addAll(others);
    }

    private float getRotationDiffToBlock(BlockPos pos, BlockState state, Vec3 eyes, float currentYaw, float currentPitch) {
        Vec3 aimPt = getClosestPointToShape(pos, state, eyes);
        if (aimPt == null) aimPt = pos.getCenter();

        Vec3 diff = aimPt.subtract(eyes);
        float targetYaw = (float) Math.toDegrees(Math.atan2(-diff.x, diff.z));
        double hDist = Math.sqrt(diff.x * diff.x + diff.z * diff.z);
        float targetPitch = (float) Math.toDegrees(Math.atan2(-diff.y, hDist));

        float yawDiff = Math.abs(Mth.wrapDegrees(targetYaw - currentYaw));
        float pitchDiff = Math.abs(Mth.wrapDegrees(targetPitch - currentPitch));
        return yawDiff + pitchDiff;
    }

    private boolean isTargetBlock(BlockState state) {
        Block block = state.getBlock();
        if (mithril.getValue() && MITHRIL_BLOCKS.contains(block)) return true;
        if (titanium.getValue() && TITANIUM_BLOCKS.contains(block)) return true;
        if (GENERIC_TARGET_BLOCKS.contains(block)) {
            if (state.is(Blocks.DIAMOND_BLOCK)) return diamondBlock.getValue();
            if (state.is(Blocks.COAL_BLOCK)) return coalBlock.getValue();
            if (state.is(Blocks.EMERALD_BLOCK)) return emeraldBlock.getValue();
            if (state.is(Blocks.IRON_BLOCK)) return ironBlock.getValue();
            if (state.is(Blocks.GOLD_BLOCK)) return goldBlock.getValue();
            if (state.is(Blocks.REDSTONE_BLOCK)) return redstoneBlock.getValue();
            if (state.is(Blocks.LAPIS_BLOCK)) return lapisBlock.getValue();
            if (state.is(Blocks.QUARTZ_BLOCK)) return quartzBlock.getValue();
            return false;
        }
        return false;
    }

    private Vec3 getClosestPointToShape(BlockPos pos, BlockState state, Vec3 eyes) {
        VoxelShape shape = state.getShape(mc.level, pos);
        if (shape.isEmpty()) return null;

        final Vec3[] closest = {null};
        final double[] minDistSq = {Double.MAX_VALUE};

        double inset = boxInset.getValue();
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            double iMinX = minX + inset, iMaxX = maxX - inset;
            double iMinY = minY + inset, iMaxY = maxY - inset;
            double iMinZ = minZ + inset, iMaxZ = maxZ - inset;
            if (iMinX >= iMaxX || iMinY >= iMaxY || iMinZ >= iMaxZ) return;

            double cx = Mth.clamp(eyes.x, pos.getX() + iMinX, pos.getX() + iMaxX);
            double cy = Mth.clamp(eyes.y, pos.getY() + iMinY, pos.getY() + iMaxY);
            double cz = Mth.clamp(eyes.z, pos.getZ() + iMinZ, pos.getZ() + iMaxZ);
            double distSq = eyes.distanceToSqr(cx, cy, cz);
            if (distSq < minDistSq[0]) {
                minDistSq[0] = distSq;
                closest[0] = new Vec3(cx, cy, cz);
            }
        });

        return closest[0];
    }

    private boolean canBeMined(Vec3 eyes, BlockPos pos, BlockState state, double reach) {
        double inset = boxInset.getValue();
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.relative(dir);
            BlockState neighborState = mc.level.getBlockState(neighbor);
            if (neighborState.canOcclude()) continue;

            if (!isFaceFacingPlayer(pos, dir, eyes)) continue;

            double[] offsets = {0.2, 0.5, 0.8};
            for (double u : offsets) {
                for (double v : offsets) {
                    Vec3 target = getPointOnFace(pos, dir, u, v, inset);
                    if (eyes.distanceToSqr(target) > reach * reach) continue;
                    if (isVisiblePoint(eyes, target, pos)) return true;
                }
            }
        }
        return false;
    }

    private Vec3 getPointOnFace(BlockPos pos, Direction dir, double u, double v, double inset) {
        double x = pos.getX(), y = pos.getY(), z = pos.getZ();
        double mu = inset + u * (1.0 - 2.0 * inset);
        double mv = inset + v * (1.0 - 2.0 * inset);
        return switch (dir) {
            case UP -> new Vec3(x + mu, y + 1.0, z + mv);
            case DOWN -> new Vec3(x + mu, y, z + mv);
            case NORTH -> new Vec3(x + mu, y + mv, z);
            case SOUTH -> new Vec3(x + mu, y + mv, z + 1.0);
            case WEST -> new Vec3(x, y + mu, z + mv);
            case EAST -> new Vec3(x + 1, y + mu, z + mv);
        };
    }

    private boolean isFaceFacingPlayer(BlockPos pos, Direction dir, Vec3 eyes) {
        double faceX = pos.getX() + 0.5 + dir.getUnitVec3i().getX() * 0.5;
        double faceY = pos.getY() + 0.5 + dir.getUnitVec3i().getY() * 0.5;
        double faceZ = pos.getZ() + 0.5 + dir.getUnitVec3i().getZ() * 0.5;
        double dx = faceX - eyes.x, dy = faceY - eyes.y, dz = faceZ - eyes.z;
        return dx * dir.getUnitVec3i().getX() + dy * dir.getUnitVec3i().getY() + dz * dir.getUnitVec3i().getZ() < 0;
    }

    private boolean isVisiblePoint(Vec3 eyes, Vec3 target, BlockPos expectedBlock) {
        Vec3 dir = target.subtract(eyes);
        double dist = dir.length();
        if (dist < 1e-4) return true;
        Vec3 adjusted = eyes.add(dir.normalize().scale(Math.max(dist - 0.005, 0.0)));

        BlockHitResult hitResult = mc.level.clip(
                new ClipContext(eyes, adjusted, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, mc.player)
        );
        if (hitResult.getType() == HitResult.Type.MISS) return true;
        boolean pass = hitResult.getBlockPos().equals(expectedBlock);
        if (!pass) {
            ChatUtil.sendDebugMessage("LegitNuker",
                    String.format("occluded: expect %s hit %s",
                            expectedBlock.toShortString(), hitResult.getBlockPos().toShortString()));
        }
        return pass;
    }

    private boolean validateRaytrace(Vec3 target, BlockPos pos, double reach) {
        Vec3 eyes = mc.player.getEyePosition();
        Vec3 diff = target.subtract(eyes);
        float yaw = (float) Math.toDegrees(Math.atan2(-diff.x, diff.z));
        double hDist = Math.sqrt(diff.x * diff.x + diff.z * diff.z);
        float pitch = (float) Math.toDegrees(Math.atan2(-diff.y, hDist));
        HitResult hit = RotationUtil.rayTrace(yaw, pitch, reach, 1.0f);
        if (hit.getType() != HitResult.Type.BLOCK) return false;
        return ((BlockHitResult) hit).getBlockPos().equals(pos);
    }

    private Vec3 findBestAimPoint(Vec3 eyes, BlockPos pos, BlockState state) {
        VoxelShape shape = state.getShape(mc.level, pos);
        if (shape.isEmpty()) return null;

        double reach = getReachDistance();
        float currentYaw = mc.player.getYRot();
        float currentPitch = mc.player.getXRot();

        Vec3 bestVisible = null;
        float bestVisibleDiff = Float.MAX_VALUE;
        Vec3 bestInvisible = null;
        float bestInvisibleDiff = Float.MAX_VALUE;

        if (isDebug()) failedAimPoints.clear();

        double inset = boxInset.getValue();
        final List<AABB> boxes = new ArrayList<>();
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            AABB box = new AABB(
                    pos.getX() + minX, pos.getY() + minY, pos.getZ() + minZ,
                    pos.getX() + maxX, pos.getY() + maxY, pos.getZ() + maxZ
            );
            AABB contracted = box.deflate(inset);
            if (contracted.minX >= contracted.maxX
                    || contracted.minY >= contracted.maxY
                    || contracted.minZ >= contracted.maxZ) {
                boxes.add(box);
            } else {
                boxes.add(contracted);
            }
        });

        for (AABB box : boxes) {
            Vec3 lookDir = Vec3.directionFromRotation(currentPitch, currentYaw);
            Vec3 lookEnd = eyes.add(lookDir.scale(reach * 2));
            Optional<Vec3> crosshairHit = box.clip(eyes, lookEnd);
            if (crosshairHit.isPresent()) {
                Vec3 hitPoint = crosshairHit.get();
                if (isVisiblePoint(eyes, hitPoint, pos) && eyes.distanceTo(hitPoint) <= reach) {
                    if (validateRaytrace(hitPoint, pos, reach)) {
                        return hitPoint;
                    }
                    if (isDebug()) failedAimPoints.add(hitPoint);
                }
            }

            List<Vec3> candidates = new ArrayList<>();

            candidates.add(new Vec3(
                    Mth.clamp(eyes.x, box.minX, box.maxX),
                    Mth.clamp(eyes.y, box.minY, box.maxY),
                    Mth.clamp(eyes.z, box.minZ, box.maxZ)
            ));

            for (Direction dir : Direction.values()) {
                BlockPos neighbor = pos.relative(dir);
                BlockState neighborState = mc.level.getBlockState(neighbor);
                if (neighborState.canOcclude()) continue;
                if (!isFaceFacingPlayer(pos, dir, eyes)) continue;

                double[] offsets = {0.1, 0.25, 0.4, 0.5, 0.6, 0.75, 0.9};
                for (double u : offsets) {
                    for (double v : offsets) {
                        candidates.add(getPointOnFace(pos, dir, u, v, inset));
                    }
                }

                double[] edgeOffsets = {0.05, 0.5, 0.95};
                for (double u : edgeOffsets) {
                    candidates.add(getPointOnFace(pos, dir, u, 0.05, inset));
                    candidates.add(getPointOnFace(pos, dir, u, 0.95, inset));
                    candidates.add(getPointOnFace(pos, dir, 0.05, u, inset));
                    candidates.add(getPointOnFace(pos, dir, 0.95, u, inset));
                }
            }

            for (Vec3 candidate : candidates) {
                Vec3 toCandidate = candidate.subtract(eyes);
                Vec3 rayEnd = eyes.add(toCandidate.scale(2.0));
                Optional<Vec3> exactHit = box.clip(eyes, rayEnd);
                if (exactHit.isEmpty()) continue;

                Vec3 hitPoint = exactHit.get();
                double dist = eyes.distanceTo(hitPoint);
                if (dist > reach) continue;

                boolean visible = isVisiblePoint(eyes, hitPoint, pos);

                float rotDiff = getRotationDifference(
                        hitPoint.subtract(eyes), currentYaw, currentPitch);

                if (visible) {
                    if (validateRaytrace(hitPoint, pos, reach)) {
                        if (rotDiff < bestVisibleDiff) {
                            bestVisibleDiff = rotDiff;
                            bestVisible = hitPoint;
                        }
                    } else {
                        if (isDebug()) failedAimPoints.add(hitPoint);
                    }
                } else {
                    if (rotDiff < bestInvisibleDiff) {
                        bestInvisibleDiff = rotDiff;
                        bestInvisible = hitPoint;
                    }
                }
            }
        }

        if (bestVisible == null) {
            bestVisible = findEdgeVisiblePoint(eyes, pos, state, reach, currentYaw, currentPitch);
            if (bestVisible != null) {
                ChatUtil.sendDebugMessage("LegitNuker", "fallback: edge visible point");
            }
        }

        Vec3 result = bestVisible;
        if (result != null) {
            ChatUtil.sendDebugMessage("LegitNuker",
                    String.format("aim: %.2f %.2f %.2f (vis)", result.x, result.y, result.z));
        } else if (bestInvisible != null) {
            result = bestInvisible;
            ChatUtil.sendDebugMessage("LegitNuker",
                    String.format("aim: %.2f %.2f %.2f (invis-fallback)", result.x, result.y, result.z));
        } else {
            ChatUtil.sendDebugMessage("LegitNuker", String.format("aim: NONE for %s", pos.toShortString()));
        }
        return result;
    }

    private Vec3 findEdgeVisiblePoint(Vec3 eyes, BlockPos pos, BlockState state, double reach, float currentYaw, float currentPitch) {
        VoxelShape shape = state.getShape(mc.level, pos);
        if (shape.isEmpty()) return null;

        final List<AABB> rawBoxes = new ArrayList<>();
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            AABB box = new AABB(
                    pos.getX() + minX, pos.getY() + minY, pos.getZ() + minZ,
                    pos.getX() + maxX, pos.getY() + maxY, pos.getZ() + maxZ
            );
            AABB minContracted = box.deflate(0.01);
            if (minContracted.minX >= minContracted.maxX
                    || minContracted.minY >= minContracted.maxY
                    || minContracted.minZ >= minContracted.maxZ) {
                rawBoxes.add(box);
            } else {
                rawBoxes.add(minContracted);
            }
        });

        Vec3 bestEdge = null;
        float bestEdgeDiff = Float.MAX_VALUE;

        for (AABB box : rawBoxes) {
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = pos.relative(dir);
                BlockState neighborState = mc.level.getBlockState(neighbor);
                if (neighborState.canOcclude()) continue;
                if (!isFaceFacingPlayer(pos, dir, eyes)) continue;

                double[] edgeOffsets = {0.05, 0.25, 0.5, 0.75, 0.95};
                for (double u : edgeOffsets) {
                    for (double v : edgeOffsets) {
                        Vec3 pt = getPointOnFace(pos, dir, u, v, 0.01);
                        double dist = eyes.distanceTo(pt);
                        if (dist > reach) continue;

                        if (!isVisiblePoint(eyes, pt, pos)) continue;

                        if (!validateRaytrace(pt, pos, reach)) continue;

                        float rotDiff = getRotationDifference(pt.subtract(eyes), currentYaw, currentPitch);
                        if (rotDiff < bestEdgeDiff) {
                            bestEdgeDiff = rotDiff;
                            bestEdge = pt;
                        }
                    }
                }
            }
        }

        return bestEdge;
    }

    private float getRotationDifference(Vec3 diff, float currentYaw, float currentPitch) {
        float targetYaw = (float) Math.toDegrees(Math.atan2(-diff.x, diff.z));
        double hDist = Math.sqrt(diff.x * diff.x + diff.z * diff.z);
        float targetPitch = (float) Math.toDegrees(Math.atan2(-diff.y, hDist));

        float yawDiff = Math.abs(Mth.wrapDegrees(targetYaw - currentYaw));
        float pitchDiff = Math.abs(Mth.wrapDegrees(targetPitch - currentPitch));
        return yawDiff + pitchDiff;
    }

    private void rotateToward(BlockPos pos, float delta) {
        Vec3 eyes = mc.player.getEyePosition();
        BlockState state = mc.level.getBlockState(pos);

        Vec3 aimTarget = findBestAimPoint(eyes, pos, state);
        if (aimTarget == null) return;
        aimPoint = aimTarget;

        Vec3 diff = aimTarget.subtract(eyes);

        float targetYaw = (float) Math.toDegrees(Math.atan2(-diff.x, diff.z));
        double hDist = Math.sqrt(diff.x * diff.x + diff.z * diff.z);
        float targetPitch = (float) Math.toDegrees(Math.atan2(-diff.y, hDist));

        float currentYaw = mc.player.getYRot();
        float currentPitch = mc.player.getXRot();

        float hSpeed = (float) horizontalSpeed.getValue();
        float vSpeed = (float) verticalSpeed.getValue();
        float newYaw = RotationUtil.towardsLinear(currentYaw, targetYaw, hSpeed, delta);
        float newPitch = RotationUtil.towardsLinear(currentPitch, targetPitch, vSpeed, delta);

        newYaw = RotationUtil.normalizeRotation(currentYaw, newYaw);
        newPitch = RotationUtil.normalizeRotation(currentPitch, newPitch);

        mc.player.setYRot(newYaw);
        mc.player.setXRot(Mth.clamp(newPitch, -90f, 90f));
    }
}
