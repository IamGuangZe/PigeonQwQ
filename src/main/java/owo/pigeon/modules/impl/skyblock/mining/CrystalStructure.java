package owo.pigeon.modules.impl.skyblock.mining;

import com.mojang.blaze3d.vertex.PoseStack;
import net.engio.mbassy.listener.Handler;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import owo.pigeon.Pigeon;
import owo.pigeon.event.events.ClientTickEvent;
import owo.pigeon.event.events.RenderEvent;
import owo.pigeon.event.events.WorldChangeEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.utils.WorldUtil;
import owo.pigeon.utils.chat.ChatUtil;
import owo.pigeon.utils.hypixel.skyblock.SkyblockUtil;
import owo.pigeon.utils.render.RenderUtil;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static owo.pigeon.Pigeon.mc;

// Reference: https://github.com/SuperShadiao/skydiao/blob/26.1.2/src/client/java/pers/XiaoShadiao/skydiao/eventbuslistener/CrystalHollowHelperListener.java

public class CrystalStructure extends Module {
    public CrystalStructure() {
        super("CrystalStructure", Category.MINING);
    }

    private enum StructureType {
        LOST_PRECURSOR_CITY("Lost Precursor City", 64, 189, SkyblockUtil.PRECURSOR_REMNANTS_BB),
        JUNGLE_TEMPLE("Jungle Temple", 64, 189, SkyblockUtil.JUNGLE_BB),
        KHAZAD_DUM("Khazad-dûm", 0, 63, SkyblockUtil.MAGMA_FIELDS_BB),
        MINES_OF_DIVAN("Mines of Divan", 64, 189, SkyblockUtil.MITHRIL_DEPOSITS_BB),
        GOBLIN_QUEEN("Goblin Queen's Den", 64, 189, SkyblockUtil.GOBLIN_HOLDOUT_BB),
        GOBLIN_KING("King Yolkar", 0, 189, SkyblockUtil.GOBLIN_HOLDOUT_BB),
        DRAGON_LAIR("Dragon's Lair", 0, 189, SkyblockUtil.MITHRIL_DEPOSITS_BB),
        CORLEONE("Corleone", 0, 189, SkyblockUtil.MITHRIL_DEPOSITS_BB),
        FAIRY_GROTTO("Fairy Grotto", 0, 189, null),
        BEAR("Bear", 0, 189, SkyblockUtil.GOBLIN_HOLDOUT_BB);

        final String internalName;
        final int minY;
        final int maxY;

        final BoundingBox zone;

        StructureType(String internalName, int minY, int maxY, BoundingBox zone) {
            this.internalName = internalName;
            this.minY = minY;
            this.maxY = maxY;
            this.zone = zone;
        }

        boolean isInRange(int y) {
            return y >= minY && y <= maxY;
        }

    }

    public EnableSetting skyblockerWaypointMsg = setting("skyblocker-waypoint-msg", false, v -> true);
    public EnableSetting skyblockerAutoWaypoint = setting("skyblocker-auto-waypoint", false, v -> true);

    private static final List<StructureType> CRYSTAL_TYPES = List.of(StructureType.LOST_PRECURSOR_CITY, StructureType.JUNGLE_TEMPLE, StructureType.KHAZAD_DUM, StructureType.GOBLIN_QUEEN, StructureType.MINES_OF_DIVAN);
    private static final List<BoundingBox> FAIRY_ZONES = List.of(SkyblockUtil.MITHRIL_DEPOSITS_BB, SkyblockUtil.JUNGLE_BB, SkyblockUtil.PRECURSOR_REMNANTS_BB, SkyblockUtil.GOBLIN_HOLDOUT_BB);
    private static final Set<Block> FEATURE_BLOCKS = Set.of(Blocks.BARRIER, Blocks.STONE_BRICKS, Blocks.WOOL.red(), Blocks.SMOOTH_SANDSTONE, Blocks.DYED_TERRACOTTA.red(), Blocks.DYED_TERRACOTTA.cyan(), Blocks.STAINED_GLASS_PANE.magenta(), Blocks.STAINED_GLASS.magenta(), Blocks.OAK_PLANKS, Blocks.FIRE, Blocks.COBBLESTONE_WALL);
    private final Map<BlockPos, BlockState> featureBlockCache = new ConcurrentHashMap<>();
    private final Map<StructureType, Set<BlockPos>> pendingVerifications = new ConcurrentHashMap<>();
    private final Set<StructureType> reportedStructures = ConcurrentHashMap.newKeySet();
    private volatile boolean inCrystalHollows = false;
    private int tickCounter = 0;
    private final Map<BlockPos, StructureType> positionTypes = new ConcurrentHashMap<>();
    private final Set<BlockPos> matchedPositions = ConcurrentHashMap.newKeySet();

    @Override
    public void onEnable() {
        resetCache();
        if (WorldUtil.nullCheck()) return;
        if (SkyblockUtil.isInIsland(SkyblockUtil.Island.CRYSTAL_HOLLOWS)) {
            inCrystalHollows = true;
            mc.levelExtractor.allChanged();
        }
    }

    @Handler
    public void onRenderBlock(RenderEvent.RenderBlockEvent event) {
        if (WorldUtil.nullCheck()) return;
        if (!inCrystalHollows) return;

        BlockPos pos = event.getPos();
        BlockState state = event.getState();
        if (pos == null || state == null) return;

        Block block = state.getBlock();
        if (!FEATURE_BLOCKS.contains(block)) return;

        BlockPos immutable = pos.immutable();

        if (block == Blocks.BARRIER) {
            if (!SkyblockUtil.NUCLEUS_BB.isInside(immutable)) {
                featureBlockCache.put(immutable, state);
                checkCrystal(immutable);
            }
            return;
        }

        featureBlockCache.put(immutable, state);

        StructureType type = crystalTypeForBlock(block);
        if (type != null) {
            positionTypes.put(immutable, type);
            if (!reportedStructures.contains(type)) {
                pendingVerifications.computeIfAbsent(type, k -> ConcurrentHashMap.newKeySet()).add(immutable);
            }
        }
    }

    @Handler
    public void onRender3D(RenderEvent.Render3DEvent event) {
        if (!Pigeon.isDebug()) return;
        if (WorldUtil.nullCheck()) return;
        if (!inCrystalHollows) return;

        PoseStack stack = event.getMatrix();
        for (BlockPos pos : featureBlockCache.keySet()) {
            if (matchedPositions.contains(pos)) {
                RenderUtil.drawBox(stack, pos, Color.GREEN, 1.0);
            } else {
                StructureType type = positionTypes.get(pos);
                if (type == null || !reportedStructures.contains(type)) {
                    RenderUtil.drawBox(stack, pos, Color.YELLOW, 1.0);
                }
            }
        }
    }

    @Handler
    public void onTickPost(ClientTickEvent.Post event) {
        if (WorldUtil.nullCheck()) return;

        boolean currentlyInCH = SkyblockUtil.isInIsland(SkyblockUtil.Island.CRYSTAL_HOLLOWS);
        if (currentlyInCH != inCrystalHollows) {
            inCrystalHollows = currentlyInCH;
            if (!inCrystalHollows) {
                resetCache();
            }
        }

        if (!inCrystalHollows) return;

        tickCounter++;
        if (tickCounter % 20 != 0) return;

        verifyPendingCandidates();
    }

    @Handler
    public void onWorldChange(WorldChangeEvent event) {
        resetCache();
    }

    private void checkCrystal(BlockPos pos) {
        if (SkyblockUtil.NUCLEUS_BB.isInside(pos)) return;
        if (!SkyblockUtil.HOLLOWS_BB.isInside(pos)) return;

        for (StructureType type : CRYSTAL_TYPES) {
            if (reportedStructures.contains(type)) continue;
            if (type.zone != null && !type.zone.isInside(pos)) continue;
            if (!type.isInRange(pos.getY())) continue;

            reportStructure(type, pos);
            return;
        }
    }

    private static StructureType crystalTypeForBlock(Block block) {
        if (block == Blocks.STONE_BRICKS || block == Blocks.WOOL.red()) return StructureType.GOBLIN_KING;
        if (block == Blocks.SMOOTH_SANDSTONE || block == Blocks.DYED_TERRACOTTA.red()) return StructureType.DRAGON_LAIR;
        if (block == Blocks.DYED_TERRACOTTA.cyan()) return StructureType.CORLEONE;
        if (block == Blocks.STAINED_GLASS_PANE.magenta() || block == Blocks.STAINED_GLASS.magenta())
            return StructureType.FAIRY_GROTTO;
        if (block == Blocks.OAK_PLANKS || block == Blocks.FIRE || block == Blocks.COBBLESTONE_WALL)
            return StructureType.BEAR;
        return null;
    }

    private void verifyPendingCandidates() {
        Iterator<Map.Entry<StructureType, Set<BlockPos>>> entryIterator = pendingVerifications.entrySet().iterator();
        while (entryIterator.hasNext()) {
            Map.Entry<StructureType, Set<BlockPos>> entry = entryIterator.next();
            StructureType type = entry.getKey();
            Set<BlockPos> candidates = entry.getValue();

            if (reportedStructures.contains(type)) {
                for (BlockPos p : candidates) {
                    featureBlockCache.remove(p);
                    positionTypes.remove(p);
                }
                entryIterator.remove();
                continue;
            }

            Iterator<BlockPos> posIterator = candidates.iterator();
            while (posIterator.hasNext()) {
                BlockPos pos = posIterator.next();

                Optional<Boolean> result = switch (type) {
                    case GOBLIN_KING -> verifyGoblinKing(pos);
                    case DRAGON_LAIR -> verifyDragonLair(pos);
                    case CORLEONE -> verifyCorleone(pos);
                    case FAIRY_GROTTO -> verifyFairyGrotto(pos);
                    case BEAR -> verifyBear(pos);
                    default -> Optional.of(false);
                };

                if (result.isEmpty()) {
                    continue;
                } else if (result.get()) {
                    reportStructure(type, pos);
                    entryIterator.remove();
                    break;
                } else {
                    posIterator.remove();
                    featureBlockCache.remove(pos);
                    positionTypes.remove(pos);
                }
            }

            if (candidates.isEmpty()) {
                entryIterator.remove();
            }
        }
    }

    private Optional<Boolean> verifyGoblinKing(BlockPos pos) {
        BlockPos origin = adjustOrigin(pos, Blocks.WOOL.red());
        if (!isBlockAt(origin, Blocks.STONE_BRICKS)) return Optional.of(false);
        if (!isValidForStructure(origin, StructureType.GOBLIN_KING)) return Optional.of(false);

        Optional<Integer> base = countBlocks(origin, 2, 0, Blocks.STONE_BRICKS);
        if (base.isEmpty()) return Optional.empty();
        if (base.get() != 25) return Optional.of(false);

        Optional<Integer> wool = countBlocks(origin, 2, 1, Blocks.WOOL.red());
        if (wool.isEmpty()) return Optional.empty();
        return Optional.of(wool.get() == 6);
    }

    private Optional<Boolean> verifyDragonLair(BlockPos pos) {
        BlockPos origin = adjustOrigin(pos, Blocks.DYED_TERRACOTTA.red());
        if (!isBlockAt(origin, Blocks.SMOOTH_SANDSTONE)) return Optional.of(false);
        if (!isValidForStructure(origin, StructureType.DRAGON_LAIR)) return Optional.of(false);

        Optional<Integer> sandstone = countBlocks(origin, 2, 0, Blocks.SMOOTH_SANDSTONE);
        if (sandstone.isEmpty()) return Optional.empty();
        if (sandstone.get() < 8) return Optional.of(false);

        Optional<Integer> clay = countBlocks(origin, 2, 1, Blocks.DYED_TERRACOTTA.red());
        if (clay.isEmpty()) return Optional.empty();
        return Optional.of(clay.get() >= 8);
    }

    private Optional<Boolean> verifyCorleone(BlockPos pos) {
        if (!isBlockAt(pos, Blocks.DYED_TERRACOTTA.cyan())) return Optional.of(false);
        if (!isValidForStructure(pos, StructureType.CORLEONE)) return Optional.of(false);

        Optional<Integer> cyan = countBlocks(pos, 2, 0, Blocks.DYED_TERRACOTTA.cyan());
        if (cyan.isEmpty()) return Optional.empty();
        if (cyan.get() < 20) return Optional.of(false);

        Optional<Boolean> posZ = hasBlockInDirection(pos, 0, 1, 4, Blocks.STONE_BRICKS);
        Optional<Boolean> negZ = hasBlockInDirection(pos, 0, -1, 4, Blocks.STONE_BRICKS);
        Optional<Boolean> posX = hasBlockInDirection(pos, 1, 0, 4, Blocks.STONE_BRICKS);
        Optional<Boolean> negX = hasBlockInDirection(pos, -1, 0, 4, Blocks.STONE_BRICKS);

        if (posZ.isEmpty() || negZ.isEmpty() || posX.isEmpty() || negX.isEmpty()) return Optional.empty();
        return Optional.of((posZ.get() && negZ.get()) || (posX.get() && negX.get()));
    }

    private Optional<Boolean> verifyFairyGrotto(BlockPos pos) {
        if (!isBlockAt(pos, Blocks.STAINED_GLASS_PANE.magenta()) && !isBlockAt(pos, Blocks.STAINED_GLASS.magenta())) {
            return Optional.of(false);
        }

        boolean inAnyZone = false;
        for (BoundingBox zone : FAIRY_ZONES) {
            if (zone.isInside(pos)) {
                inAnyZone = true;
                break;
            }
        }
        if (!inAnyZone) return Optional.of(false);
        if (!isValidForStructure(pos, StructureType.FAIRY_GROTTO)) return Optional.of(false);

        return Optional.of(true);
    }

    private Optional<Boolean> verifyBear(BlockPos pos) {
        BlockPos origin = adjustOrigin(pos, Blocks.FIRE, Blocks.COBBLESTONE_WALL);

        if (!isValidForStructure(origin, StructureType.BEAR)) return Optional.of(false);

        Optional<Integer> planks = countBlocks(origin, 2, 0, Blocks.OAK_PLANKS);
        if (planks.isEmpty()) return Optional.empty();
        if (planks.get() < 15) return Optional.of(false);

        Optional<Integer> fire = countBlocks(origin, 3, 1, Blocks.FIRE);
        if (fire.isEmpty()) return Optional.empty();
        Optional<Integer> wall = countBlocks(origin, 3, 1, Blocks.COBBLESTONE_WALL);
        if (wall.isEmpty()) return Optional.empty();

        return Optional.of(fire.get() >= 2 && wall.get() >= 2);
    }

    private BlockState getBlock(BlockPos pos) {
        BlockState cached = featureBlockCache.get(pos);
        if (cached != null) return cached;
        if (!WorldUtil.nullCheck() && mc.level.isLoaded(pos)) {
            return mc.level.getBlockState(pos);
        }
        return null;
    }

    private boolean isBlockAt(BlockPos pos, Block block) {
        BlockState state = getBlock(pos);
        return state != null && state.is(block);
    }

    private Optional<Boolean> checkBlock(BlockPos pos, Block target) {
        BlockState cached = featureBlockCache.get(pos);
        if (cached != null) return Optional.of(cached.is(target));
        if (WorldUtil.nullCheck()) return Optional.empty();
        if (!mc.level.isLoaded(pos)) return Optional.empty();
        return Optional.of(mc.level.getBlockState(pos).is(target));
    }

    private Optional<Integer> countBlocks(BlockPos origin, int radius, int yOffset, Block target) {
        int count = 0;
        BlockPos.MutableBlockPos temp = new BlockPos.MutableBlockPos();
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                temp.set(origin).move(x, yOffset, z);
                Optional<Boolean> result = checkBlock(temp, target);
                if (result.isEmpty()) return Optional.empty();
                if (result.get()) count++;
            }
        }
        return Optional.of(count);
    }

    private Optional<Boolean> hasBlockInDirection(BlockPos origin, int dx, int dz, int range, Block target) {
        BlockPos.MutableBlockPos temp = new BlockPos.MutableBlockPos();
        for (int i = 1; i <= range; i++) {
            temp.set(origin).move(dx * i, 0, dz * i);
            Optional<Boolean> result = checkBlock(temp, target);
            if (result.isEmpty()) return Optional.empty();
            if (result.get()) return Optional.of(true);
        }
        return Optional.of(false);
    }

    private boolean isValidForStructure(BlockPos pos, StructureType type) {
        if (type.zone != null && !type.zone.isInside(pos)) return false;
        if (!type.isInRange(pos.getY())) return false;
        return !SkyblockUtil.NUCLEUS_BB.isInside(pos) && SkyblockUtil.HOLLOWS_BB.isInside(pos);
    }

    private BlockPos adjustOrigin(BlockPos pos, Block... triggerBlocks) {
        for (Block block : triggerBlocks) {
            if (isBlockAt(pos, block)) return pos.below();
        }
        return pos;
    }

    private void reportStructure(StructureType type, BlockPos pos) {
        reportedStructures.add(type);
        matchedPositions.add(pos);
        mc.execute(() -> {
            String cmd = "/skyblocker crystalWaypoints add " + pos.getX() + " " + pos.getY() + " " + pos.getZ() + " " + type.internalName;

            MutableComponent msg = Component.literal("Found " + type.internalName + " at (" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")");

            boolean skyblockerLoaded = FabricLoader.getInstance().isModLoaded("skyblocker");

            if (skyblockerLoaded && skyblockerWaypointMsg.getValue()) {
                MutableComponent waypoint = Component.literal(" [Add Skyblocker waypoint]")
                        .withStyle(style -> style.withColor(ChatFormatting.GREEN)
                                .withClickEvent(new ClickEvent.RunCommand(cmd))
                                .withHoverEvent(new HoverEvent.ShowText(
                                        Component.literal(type.internalName + ": (" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")\n" + cmd))));
                msg.append(waypoint);
            }

            ChatUtil.sendMessage(msg);

            if (skyblockerLoaded && skyblockerAutoWaypoint.getValue()) {
                mc.player.connection.sendChat(cmd);
            }
        });
    }

    private void resetCache() {
        inCrystalHollows = false;
        featureBlockCache.clear();
        pendingVerifications.clear();
        reportedStructures.clear();
        positionTypes.clear();
        matchedPositions.clear();
    }
}
