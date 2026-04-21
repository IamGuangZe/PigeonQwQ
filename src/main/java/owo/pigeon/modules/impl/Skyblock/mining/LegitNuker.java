package owo.pigeon.modules.impl.skyblock.mining;

import net.engio.mbassy.listener.Handler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.RaycastContext;
import owo.pigeon.event.events.RenderEvent;
import owo.pigeon.event.events.TickEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.*;
import owo.pigeon.utils.KeybindUtil;
import owo.pigeon.utils.WorldUtil;
import owo.pigeon.utils.player.RotationUtil;
import owo.pigeon.utils.render.RenderUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static owo.pigeon.Pigeon.mc;

public class LegitNuker extends Module {
    public LegitNuker() {
        super("LegitNuker", Category.MINING);
    }

    // Reference: https://github.com/CCBlueX/LiquidBounce

    public enum MithrilSort {
        NONE, HARDNESS_ASC, HARDNESS_DESC
    }

    public EnableSetting stopInGui = setting("stop-in-gui", true, v -> true);
    public EnableSetting mineBelowFeet = setting("mine-below-feet", true, v -> true);
    public IntSetting switchDelay = setting("switch-delay", 1, 0, 20, "ticks", v -> true);
    public FloatSetting boxInset = setting("box-inset", 0.05f, 0.0f, 0.2f, v -> true);
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

    private static final Block[] MITHRIL_BLOCKS = {
            Blocks.CYAN_TERRACOTTA, Blocks.GRAY_WOOL, Blocks.DARK_PRISMARINE,
            Blocks.PRISMARINE_BRICKS, Blocks.PRISMARINE, Blocks.LIGHT_BLUE_WOOL
    };
    private static final Block[] TITANIUM_BLOCKS = {Blocks.POLISHED_DIORITE};

    private double getReachDistance() {
        return mc.player.getBlockInteractionRange();
    }

    private BlockPos currentTarget = null;
    private BlockPos wasTarget = null;
    private int switchDelayCounter = 0;
    private Vec3d aimPoint = null;

    @Handler
    public void onTick(TickEvent.ClientTickEvent event) {
        if (WorldUtil.nullCheck()) return;

        if (event instanceof TickEvent.ClientTickEvent.Pre) {
            if (stopInGui.getValue() && mc.currentScreen instanceof HandledScreen) {
                currentTarget = null;
                aimPoint = null;
                KeybindUtil.resetPressed(mc.options.attackKey);
                return;
            }

            currentTarget = lookupTarget();

            if (currentTarget == null) {
                wasTarget = null;
                aimPoint = null;
                KeybindUtil.resetPressed(mc.options.attackKey);
                return;
            }
        }

        if (event instanceof TickEvent.ClientTickEvent.Post) {
            if (currentTarget == null) return;

            if (wasTarget != null && !currentTarget.equals(wasTarget)) {
                if (switchDelayCounter < switchDelay.getValue()) {
                    switchDelayCounter++;
                    KeybindUtil.resetPressed(mc.options.attackKey);
                    return;
                }
            }
            switchDelayCounter = 0;

            double reach = getReachDistance();
            HitResult hitResult = mc.player.raycast(reach, 1.0f, false);
            if (hitResult.getType() != HitResult.Type.BLOCK) {
                KeybindUtil.resetPressed(mc.options.attackKey);
                return;
            }

            BlockHitResult blockHit = (BlockHitResult) hitResult;
            if (!blockHit.getBlockPos().equals(currentTarget)) {
                KeybindUtil.resetPressed(mc.options.attackKey);
                return;
            }

            KeybindUtil.setPressed(mc.options.attackKey, true);
            wasTarget = currentTarget;
        }
    }

    @Handler
    public void onRender3D(RenderEvent.Render3DEvent event) {
        if (WorldUtil.nullCheck() || currentTarget == null) return;

        {
            RenderUtil.drawESP(event.getMatrix(), currentTarget, new Color(0x44FF4444, true), RenderUtil.ESPMode.BOTH, false);
            rotateToward(currentTarget, event.getDelta());
        }
    }

    @Override
    public void onDisable() {
        currentTarget = null;
        wasTarget = null;
        aimPoint = null;
        switchDelayCounter = 0;
        if (mc.options != null) {
            KeybindUtil.resetPressed(mc.options.attackKey);
        }
    }

    private BlockPos lookupTarget() {
        double reach = getReachDistance();
        double rangeSq = reach * reach;
        Vec3d eyes = mc.player.getEyePos();

        List<BlockPos> candidates = new ArrayList<>();
        int radius = (int) Math.ceil(reach);
        BlockPos playerPos = mc.player.getBlockPos();

        for (int x = playerPos.getX() - radius; x <= playerPos.getX() + radius; x++) {
            for (int y = playerPos.getY() - radius; y <= playerPos.getY() + radius; y++) {
                for (int z = playerPos.getZ() - radius; z <= playerPos.getZ() + radius; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = mc.world.getBlockState(pos);

                    if (state.isAir()) continue;
                    if (!isTargetBlock(state.getBlock())) continue;
                    if (state.getHardness(mc.world, pos) < 0) continue;

                    Vec3d closestPoint = getClosestPointToShape(pos, state, eyes);
                    if (closestPoint == null || closestPoint.squaredDistanceTo(eyes) > rangeSq) continue;

                    if (!canBeMined(eyes, pos, state)) continue;

                    if (!mineBelowFeet.getValue() && pos.getY() < playerPos.getY()) continue;

                    candidates.add(pos);
                }
            }
        }

        if (candidates.isEmpty()) return null;

        sortCandidates(candidates);

        if (wasTarget != null && candidates.contains(wasTarget)) {
            return wasTarget;
        }

        return candidates.get(0);
    }

    private void sortCandidates(List<BlockPos> candidates) {
        boolean hasTitanium = titanium.getValue();
        boolean priorTitanium = hasTitanium && prioritizeTitanium.getValue();
        boolean hasMithril = mithril.getValue();
        MithrilSort sort = mithrilSort.getValue();
        float currentYaw = mc.player.getYaw();
        float currentPitch = mc.player.getPitch();

        candidates.sort((a, b) -> {
            Block blockA = mc.world.getBlockState(a).getBlock();
            Block blockB = mc.world.getBlockState(b).getBlock();

            if (priorTitanium) {
                boolean aIsTitanium = isTitaniumBlock(blockA);
                boolean bIsTitanium = isTitaniumBlock(blockB);
                if (aIsTitanium && !bIsTitanium) return -1;
                if (!aIsTitanium && bIsTitanium) return 1;
            }

            if (hasMithril && sort != MithrilSort.NONE) {
                int indexA = getMithrilIndex(blockA);
                int indexB = getMithrilIndex(blockB);
                if (indexA != -1 && indexB != -1) {
                    return sort == MithrilSort.HARDNESS_ASC
                            ? Integer.compare(indexA, indexB)
                            : Integer.compare(indexB, indexA);
                }
                if (indexA != -1) return -1;
                if (indexB != -1) return 1;
            }

            float diffA = getRotationDiffToBlock(a, currentYaw, currentPitch);
            float diffB = getRotationDiffToBlock(b, currentYaw, currentPitch);
            return Float.compare(diffA, diffB);
        });
    }

    private float getRotationDiffToBlock(BlockPos pos, float currentYaw, float currentPitch) {
        Vec3d eyes = mc.player.getEyePos();
        BlockState state = mc.world.getBlockState(pos);
        Vec3d aimPoint = getClosestPointToShape(pos, state, eyes);
        if (aimPoint == null) aimPoint = pos.toCenterPos();

        Vec3d diff = aimPoint.subtract(eyes);
        float targetYaw = (float) Math.toDegrees(Math.atan2(-diff.x, diff.z));
        double hDist = Math.sqrt(diff.x * diff.x + diff.z * diff.z);
        float targetPitch = (float) Math.toDegrees(Math.atan2(-diff.y, hDist));

        float yawDiff = Math.abs(MathHelper.wrapDegrees(targetYaw - currentYaw));
        float pitchDiff = Math.abs(MathHelper.wrapDegrees(targetPitch - currentPitch));
        return yawDiff + pitchDiff;
    }

    private boolean isTargetBlock(Block block) {
        if (mithril.getValue()) {
            for (Block m : MITHRIL_BLOCKS) {
                if (block == m) return true;
            }
        }
        if (titanium.getValue()) {
            for (Block t : TITANIUM_BLOCKS) {
                if (block == t) return true;
            }
        }
        if (diamondBlock.getValue() && block == Blocks.DIAMOND_BLOCK) return true;
        if (coalBlock.getValue() && block == Blocks.COAL_BLOCK) return true;
        if (emeraldBlock.getValue() && block == Blocks.EMERALD_BLOCK) return true;
        if (ironBlock.getValue() && block == Blocks.IRON_BLOCK) return true;
        if (goldBlock.getValue() && block == Blocks.GOLD_BLOCK) return true;
        if (redstoneBlock.getValue() && block == Blocks.REDSTONE_BLOCK) return true;
        if (lapisBlock.getValue() && block == Blocks.LAPIS_BLOCK) return true;
        if (quartzBlock.getValue() && block == Blocks.QUARTZ_BLOCK) return true;
        return false;
    }

    private boolean isTitaniumBlock(Block block) {
        for (Block t : TITANIUM_BLOCKS) {
            if (block == t) return true;
        }
        return false;
    }

    private int getMithrilIndex(Block block) {
        for (int i = 0; i < MITHRIL_BLOCKS.length; i++) {
            if (MITHRIL_BLOCKS[i] == block) return i;
        }
        return -1;
    }

    private Vec3d getClosestPointToShape(BlockPos pos, BlockState state, Vec3d eyes) {
        VoxelShape shape = state.getOutlineShape(mc.world, pos);
        if (shape.isEmpty()) return null;

        final Vec3d[] closest = {null};
        final double[] minDistSq = {Double.MAX_VALUE};

        double inset = boxInset.getValue();
        shape.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> {
            double iMinX = minX + inset, iMaxX = maxX - inset;
            double iMinY = minY + inset, iMaxY = maxY - inset;
            double iMinZ = minZ + inset, iMaxZ = maxZ - inset;
            if (iMinX >= iMaxX || iMinY >= iMaxY || iMinZ >= iMaxZ) return;

            double cx = MathHelper.clamp(eyes.x, pos.getX() + iMinX, pos.getX() + iMaxX);
            double cy = MathHelper.clamp(eyes.y, pos.getY() + iMinY, pos.getY() + iMaxY);
            double cz = MathHelper.clamp(eyes.z, pos.getZ() + iMinZ, pos.getZ() + iMaxZ);
            double distSq = eyes.squaredDistanceTo(cx, cy, cz);
            if (distSq < minDistSq[0]) {
                minDistSq[0] = distSq;
                closest[0] = new Vec3d(cx, cy, cz);
            }
        });

        return closest[0];
    }

    private boolean canBeMined(Vec3d eyes, BlockPos pos, BlockState state) {
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.offset(dir);
            BlockState neighborState = mc.world.getBlockState(neighbor);
            if (neighborState.isOpaque()) continue;

            double[] offsets = {0.2, 0.5, 0.8};
            for (double u : offsets) {
                for (double v : offsets) {
                    Vec3d target = getPointOnFace(pos, dir, u, v, boxInset.getValue());
                    if (eyes.distanceTo(target) > getReachDistance()) continue;
                    if (isVisiblePoint(eyes, target, pos)) return true;
                }
            }
        }
        return false;
    }

    private Vec3d getPointOnFace(BlockPos pos, Direction dir, double u, double v, double inset) {
        double x = pos.getX(), y = pos.getY(), z = pos.getZ();
        double mu = inset + u * (1.0 - 2.0 * inset);
        double mv = inset + v * (1.0 - 2.0 * inset);
        return switch (dir) {
            case UP -> new Vec3d(x + mu, y + 1.0, z + mv);
            case DOWN -> new Vec3d(x + mu, y, z + mv);
            case NORTH -> new Vec3d(x + mu, y + mv, z);
            case SOUTH -> new Vec3d(x + mu, y + mv, z + 1.0);
            case WEST -> new Vec3d(x, y + mu, z + mv);
            case EAST -> new Vec3d(x + 1, y + mu, z + mv);
        };
    }

    private boolean isVisiblePoint(Vec3d eyes, Vec3d target, BlockPos expectedBlock) {
        BlockHitResult hitResult = mc.world.raycast(
                new RaycastContext(eyes, target, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mc.player)
        );
        if (hitResult.getType() == HitResult.Type.MISS) return true;
        return hitResult.getBlockPos().equals(expectedBlock);
    }

    private Vec3d findBestAimPoint(Vec3d eyes, BlockPos pos, BlockState state) {
        VoxelShape shape = state.getOutlineShape(mc.world, pos);
        if (shape.isEmpty()) return null;

        double reach = getReachDistance();
        float currentYaw = mc.player.getYaw();
        float currentPitch = mc.player.getPitch();

        Vec3d bestVisible = null;
        float bestVisibleDiff = Float.MAX_VALUE;
        Vec3d bestInvisible = null;
        float bestInvisibleDiff = Float.MAX_VALUE;

        final List<Box> boxes = new ArrayList<>();
        shape.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> {
            Box box = new Box(
                    pos.getX() + minX, pos.getY() + minY, pos.getZ() + minZ,
                    pos.getX() + maxX, pos.getY() + maxY, pos.getZ() + maxZ
            );
            boxes.add(box.contract(boxInset.getValue()));
        });

        for (Box box : boxes) {
            // Fast path: if current crosshair already hits this box, use it
            Vec3d lookDir = Vec3d.fromPolar(currentPitch, currentYaw);
            Vec3d lookEnd = eyes.add(lookDir.multiply(reach * 2));
            Optional<Vec3d> crosshairHit = box.raycast(eyes, lookEnd);
            if (crosshairHit.isPresent()) {
                Vec3d hitPoint = crosshairHit.get();
                if (isVisiblePoint(eyes, hitPoint, pos) && eyes.distanceTo(hitPoint) <= reach) {
                    return hitPoint;
                }
            }

            // Collect candidate directions to raycast through
            List<Vec3d> candidates = new ArrayList<>();

            // Nearest point on box to eyes (always a good candidate)
            candidates.add(new Vec3d(
                    MathHelper.clamp(eyes.x, box.minX, box.maxX),
                    MathHelper.clamp(eyes.y, box.minY, box.maxY),
                    MathHelper.clamp(eyes.z, box.minZ, box.maxZ)
            ));

            // Box center
            candidates.add(box.getCenter());

            // Sample exposed faces
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = pos.offset(dir);
                BlockState neighborState = mc.world.getBlockState(neighbor);
                if (neighborState.isOpaque()) continue;

                double[] offsets = {0.15, 0.35, 0.5, 0.65, 0.85};
                for (double u : offsets) {
                    for (double v : offsets) {
                        candidates.add(getPointOnFace(pos, dir, u, v, boxInset.getValue()));
                    }
                }
            }

            // Evaluate each candidate: raycast through it to find exact box intersection
            for (Vec3d candidate : candidates) {
                Vec3d toCandidate = candidate.subtract(eyes);
                Vec3d rayEnd = eyes.add(toCandidate.multiply(2.0));
                Optional<Vec3d> exactHit = box.raycast(eyes, rayEnd);
                if (exactHit.isEmpty()) continue;

                Vec3d hitPoint = exactHit.get();
                double dist = eyes.distanceTo(hitPoint);
                if (dist > reach) continue;

                boolean visible = isVisiblePoint(eyes, hitPoint, pos);

                float rotDiff = getRotationDifference(
                        hitPoint.subtract(eyes), currentYaw, currentPitch);

                if (visible) {
                    if (rotDiff < bestVisibleDiff) {
                        bestVisibleDiff = rotDiff;
                        bestVisible = hitPoint;
                    }
                } else {
                    if (rotDiff < bestInvisibleDiff) {
                        bestInvisibleDiff = rotDiff;
                        bestInvisible = hitPoint;
                    }
                }
            }
        }

        return bestVisible != null ? bestVisible : bestInvisible;
    }

    private float getRotationDifference(Vec3d diff, float currentYaw, float currentPitch) {
        float targetYaw = (float) Math.toDegrees(Math.atan2(-diff.x, diff.z));
        double hDist = Math.sqrt(diff.x * diff.x + diff.z * diff.z);
        float targetPitch = (float) Math.toDegrees(Math.atan2(-diff.y, hDist));

        float yawDiff = Math.abs(MathHelper.wrapDegrees(targetYaw - currentYaw));
        float pitchDiff = Math.abs(MathHelper.wrapDegrees(targetPitch - currentPitch));
        return yawDiff + pitchDiff;
    }

    private void rotateToward(BlockPos pos, float delta) {
        Vec3d eyes = mc.player.getEyePos();
        BlockState state = mc.world.getBlockState(pos);

        Vec3d aimTarget = findBestAimPoint(eyes, pos, state);
        if (aimTarget == null) {
            aimTarget = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        }
        aimPoint = aimTarget;

        Vec3d diff = aimTarget.subtract(eyes);

        float targetYaw = (float) Math.toDegrees(Math.atan2(-diff.x, diff.z));
        double hDist = Math.sqrt(diff.x * diff.x + diff.z * diff.z);
        float targetPitch = (float) Math.toDegrees(Math.atan2(-diff.y, hDist));

        float currentYaw = mc.player.getYaw();
        float currentPitch = mc.player.getPitch();

        float hSpeed = (float) horizontalSpeed.getValue();
        float vSpeed = (float) verticalSpeed.getValue();
        float newYaw = RotationUtil.towardsLinear(currentYaw, targetYaw, hSpeed, delta);
        float newPitch = RotationUtil.towardsLinear(currentPitch, targetPitch, vSpeed, delta);

        newYaw = RotationUtil.normalizeRotation(currentYaw, newYaw);
        newPitch = RotationUtil.normalizeRotation(currentPitch, newPitch);

        mc.player.setYaw(newYaw);
        mc.player.setPitch(MathHelper.clamp(newPitch, -90f, 90f));
    }
}
