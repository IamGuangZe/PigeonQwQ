package owo.pigeon.modules.impl.skyblock.misc;

import net.engio.mbassy.listener.Handler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.*;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import owo.pigeon.event.events.RenderEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.ExpandSetting;
import owo.pigeon.settings.IntSetting;
import owo.pigeon.utils.hypixel.skyblock.SkyblockUtil;
import owo.pigeon.utils.render.RenderUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static owo.pigeon.Pigeon.mc;

public class SkyblockESP extends Module {
    public SkyblockESP() {
        super("SkyblockESP", Category.MISC);
    }

    // 套公式... - 2026/4/13

    public ExpandSetting crimsonIsle = setting("crimson-isle", v -> true);
    public EnableSetting cinderbatEsp = setting("cinderbat-esp", false, v -> crimsonIsle.getValue());

    public ExpandSetting crystalHollows = setting("crystal-hollows", v -> true);
    public EnableSetting keyGuardianEsp = setting("key-guardian-esp", false, v -> crystalHollows.getValue());
    public EnableSetting automatonEsp = setting("automaton-esp", false, v -> crystalHollows.getValue());
    public EnableSetting sludgeEsp = setting("sludge-esp", false, v -> crystalHollows.getValue());
    public EnableSetting yogEsp = setting("yog-esp", false, v -> crystalHollows.getValue());
    public EnableSetting corleoneBatcaveEsp = setting("corleone-batcave-esp", false, v -> crystalHollows.getValue());
    public EnableSetting wormLavaEsp = setting("worm-lava-esp", false, v -> crystalHollows.getValue());
    public IntSetting wormLavaEspLimit = setting("worm-lava-esp-limit", 9, -1, 100, v -> wormLavaEsp.isVisible() && wormLavaEsp.getValue());
    public ExpandSetting gemstoneEsp = setting("gemstone-esp", v -> crystalHollows.getValue());
    public EnableSetting jadeEsp = setting("jade-esp", false, v -> gemstoneEsp.isVisible() && gemstoneEsp.getValue());
    public EnableSetting amberEsp = setting("amber-esp", false, v -> gemstoneEsp.isVisible() && gemstoneEsp.getValue());
    public EnableSetting sapphireEsp = setting("sapphire-esp", false, v -> gemstoneEsp.isVisible() && gemstoneEsp.getValue());
    public EnableSetting amethystEsp = setting("amethyst-esp", false, v -> gemstoneEsp.isVisible() && gemstoneEsp.getValue());
    public EnableSetting rubyEsp = setting("ruby-esp", false, v -> gemstoneEsp.isVisible() && gemstoneEsp.getValue());
    public EnableSetting topazEsp = setting("topaz-esp", false, v -> gemstoneEsp.isVisible() && gemstoneEsp.getValue());
    public EnableSetting jasperEsp = setting("jasper-esp", false, v -> gemstoneEsp.isVisible() && gemstoneEsp.getValue());
    public IntSetting gemstoneEspLimit = setting("gemstone-esp-limit", 30, -1, 100, v -> gemstoneEsp.isVisible() && gemstoneEsp.getValue());

    public ExpandSetting dwarvenMines = setting("dwarven-mines", v -> true);
    public EnableSetting titaniumEsp = setting("titanium-esp", false, v -> dwarvenMines.getValue());
    public EnableSetting dragonEggEsp = setting("dragon-egg-esp", false, v -> dwarvenMines.getValue());

    public ExpandSetting galatea = setting("galatea", v -> true);
    public EnableSetting hideonleafEsp = setting("hideonleaf-esp", false, v -> galatea.getValue());
    public EnableSetting phanpyreEsp = setting("phanpyre-esp", false, v -> galatea.getValue());
    public EnableSetting phanflareEsp = setting("phanflare-esp", false, v -> galatea.getValue());
    public EnableSetting dreadwingEsp = setting("dreadwing-esp", false, v -> galatea.getValue());
    public EnableSetting mudwormEsp = setting("mudworm-esp", false, v -> galatea.getValue());

    public ExpandSetting lotusAtoll = setting("lotus-atoll", v -> true);
    public EnableSetting lotumEsp = setting("lotum-esp", false, v -> lotusAtoll.getValue());
    public EnableSetting puddleJumperEsp = setting("puddle-jumper-esp", false, v -> lotusAtoll.getValue());
    public EnableSetting tewtilEsp = setting("tewtil-esp", false, v -> lotusAtoll.getValue());
    public EnableSetting flipflopperEsp = setting("flipflopper-esp", false, v -> lotusAtoll.getValue());
    public EnableSetting seashineEsp = setting("seashine-esp", false, v -> lotusAtoll.getValue());

    public ExpandSetting theEnd = setting("the-end", v -> true);
    public EnableSetting enderNodeEsp = setting("ender-node-esp", false, v -> theEnd.getValue());

    private final Set<BlockPos> batcaveBlocks = ConcurrentHashMap.newKeySet();
    private final Set<BlockPos> wormLavas = ConcurrentHashMap.newKeySet();
    private final Set<BlockPos> jades = ConcurrentHashMap.newKeySet();
    private final Set<BlockPos> ambers = ConcurrentHashMap.newKeySet();
    private final Set<BlockPos> sapphires = ConcurrentHashMap.newKeySet();
    private final Set<BlockPos> amethysts = ConcurrentHashMap.newKeySet();
    private final Set<BlockPos> rubys = ConcurrentHashMap.newKeySet();
    private final Set<BlockPos> jaspers = ConcurrentHashMap.newKeySet();
    private final Set<BlockPos> topazs = ConcurrentHashMap.newKeySet();
    private final Set<BlockPos> titaniums = ConcurrentHashMap.newKeySet();
    private final Set<BlockPos> dragonEggs = ConcurrentHashMap.newKeySet();
    private final Set<BlockPos> enderNodes = ConcurrentHashMap.newKeySet();

    @Override
    public void onEnable() {
        if (mc.worldRenderer == null) return;
        mc.worldRenderer.reload();
    }

    @Handler
    public void onRender3D(RenderEvent.Render3DEvent event) {
        MatrixStack stack = event.getMatrix();

        if (SkyblockUtil.isInIsland(SkyblockUtil.Island.CRIMSON_ISLE)) {
            for (Entity entity : mc.world.getEntities()) {
                if (entity instanceof BatEntity && !entity.isInvisible() && cinderbatEsp.getValue()) {
                    RenderUtil.drawESP(stack, entity, Color.GREEN, RenderUtil.ESPMode.BOTH, false);
                }
            }
        } else if (SkyblockUtil.isInIsland(SkyblockUtil.Island.CRYSTAL_HOLLOWS)) {
            for (Entity entity : mc.world.getEntities()) {
                if (entity instanceof ZombieEntity zombie && zombie.getMainHandStack().isOf(Items.BONE) && keyGuardianEsp.getValue()) {
                    RenderUtil.drawESP(stack, entity, Color.YELLOW, RenderUtil.ESPMode.BOTH, false);
                } else if (entity instanceof IronGolemEntity && automatonEsp.getValue()) {
                    RenderUtil.drawESP(stack, entity, Color.WHITE, RenderUtil.ESPMode.BOTH, false);
                } else if (entity instanceof SlimeEntity && !(entity instanceof MagmaCubeEntity) && sludgeEsp.getValue()) {
                    RenderUtil.drawESP(stack, entity, Color.GREEN, RenderUtil.ESPMode.BOTH, false);
                } else if (entity instanceof MagmaCubeEntity && yogEsp.getValue()) {
                    RenderUtil.drawESP(stack, entity, Color.ORANGE, RenderUtil.ESPMode.BOTH, false);
                }
            }

            if (wormLavaEsp.getValue()) {
                wormLavas.removeIf(pos -> !mc.world.getBlockState(pos).isOf(Blocks.LAVA));
                renderBlocks(stack, wormLavas, Color.ORANGE, wormLavaEspLimit.getValue());
            }

            if (corleoneBatcaveEsp.getValue()) {
                batcaveBlocks.removeIf(pos -> !mc.world.getBlockState(pos).isOf(Blocks.BLUE_STAINED_GLASS));
                for (BlockPos pos : batcaveBlocks) {
                    RenderUtil.drawESP(event.getMatrix(), pos, Color.CYAN, RenderUtil.ESPMode.BOTH, false);
                }
            }

            if (jadeEsp.getValue()) {
                jades.removeIf(pos -> !mc.world.getBlockState(pos).isOf(Blocks.LIME_STAINED_GLASS) && !mc.world.getBlockState(pos).isOf(Blocks.LIME_STAINED_GLASS_PANE));
                renderBlocks(stack, jades, Color.GREEN, gemstoneEspLimit.getValue());
            }
            if (amberEsp.getValue()) {
                ambers.removeIf(pos -> !mc.world.getBlockState(pos).isOf(Blocks.ORANGE_STAINED_GLASS) && !mc.world.getBlockState(pos).isOf(Blocks.ORANGE_STAINED_GLASS_PANE));
                renderBlocks(stack, ambers, Color.ORANGE, gemstoneEspLimit.getValue());
            }
            if (sapphireEsp.getValue()) {
                sapphires.removeIf(pos -> !mc.world.getBlockState(pos).isOf(Blocks.LIGHT_BLUE_STAINED_GLASS) && !mc.world.getBlockState(pos).isOf(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE));
                renderBlocks(stack, sapphires, new Color(0xADD8FF, true), gemstoneEspLimit.getValue());
            }
            if (amethystEsp.getValue()) {
                amethysts.removeIf(pos -> !mc.world.getBlockState(pos).isOf(Blocks.PURPLE_STAINED_GLASS) && !mc.world.getBlockState(pos).isOf(Blocks.PURPLE_STAINED_GLASS_PANE));
                renderBlocks(stack, amethysts, new Color(0x9932CC, true), gemstoneEspLimit.getValue());
            }
            if (rubyEsp.getValue()) {
                rubys.removeIf(pos -> !mc.world.getBlockState(pos).isOf(Blocks.RED_STAINED_GLASS) && !mc.world.getBlockState(pos).isOf(Blocks.RED_STAINED_GLASS_PANE));
                renderBlocks(stack, rubys, Color.RED, gemstoneEspLimit.getValue());
            }
            if (topazEsp.getValue()) {
                topazs.removeIf(pos -> !mc.world.getBlockState(pos).isOf(Blocks.YELLOW_STAINED_GLASS) && !mc.world.getBlockState(pos).isOf(Blocks.YELLOW_STAINED_GLASS_PANE));
                renderBlocks(stack, topazs, Color.YELLOW, gemstoneEspLimit.getValue());
            }
            if (jasperEsp.getValue()) {
                jaspers.removeIf(pos -> !mc.world.getBlockState(pos).isOf(Blocks.MAGENTA_STAINED_GLASS) && !mc.world.getBlockState(pos).isOf(Blocks.MAGENTA_STAINED_GLASS_PANE));
                renderBlocks(stack, jaspers, Color.MAGENTA, gemstoneEspLimit.getValue());
            }
        } else if (SkyblockUtil.isInIsland(SkyblockUtil.Island.DWARVEN_MINES)) {
            if (titaniumEsp.getValue()) {
                titaniums.removeIf(pos -> !mc.world.getBlockState(pos).isOf(Blocks.POLISHED_DIORITE));
                for (BlockPos pos : titaniums) {
                    RenderUtil.drawESP(event.getMatrix(), pos, Color.WHITE, RenderUtil.ESPMode.BOTH, false);
                }
            }
            if (dragonEggEsp.getValue()) {
                dragonEggs.removeIf(pos -> !mc.world.getBlockState(pos).isOf(Blocks.DRAGON_EGG));
                for (BlockPos pos : dragonEggs) {
                    RenderUtil.drawESP(event.getMatrix(), pos, Color.DARK_GRAY, RenderUtil.ESPMode.BOTH, false);
                }
            }
        } else if (SkyblockUtil.isInIsland(SkyblockUtil.Island.GALATEA)) {
            for (Entity entity : mc.world.getEntities()) {
                if (entity instanceof ShulkerEntity && hideonleafEsp.getValue()) {
                    RenderUtil.drawESP(stack, entity, Color.GREEN, RenderUtil.ESPMode.BOTH, false);
                } else if (entity instanceof PhantomEntity phantom) {
                    float scale = phantom.getScale();
                    if (scale == 0.4f && phanpyreEsp.getValue()) {
                        RenderUtil.drawESP(stack, entity, Color.WHITE, RenderUtil.ESPMode.BOTH, false);
                    } else if (scale == 1.0f && phanflareEsp.getValue()) {
                        RenderUtil.drawESP(stack, entity, Color.WHITE, RenderUtil.ESPMode.BOTH, false);
                    } else if (scale == 3.0f && dreadwingEsp.getValue()) {
                        RenderUtil.drawESP(stack, entity, Color.WHITE, RenderUtil.ESPMode.BOTH, false);
                    }
                } else if (entity instanceof DisplayEntity.ItemDisplayEntity itemDisplay && itemDisplay.getItemStack().isOf(Items.STRING) && mudwormEsp.getValue()) {
                    RenderUtil.drawESP(stack, entity.getBlockPos(), Color.YELLOW, RenderUtil.ESPMode.BOTH, false);
                }
            }
        } else if (SkyblockUtil.isInIsland(SkyblockUtil.Island.LOTUS_ATOLL)) {
            for (Entity entity : mc.world.getEntities()) {
                if (entity instanceof FrogEntity frog && frog.getVariant().matchesKey(FrogVariants.TEMPERATE)) {
                    float scale = frog.getScale();
                    if (scale == 1.0f && lotumEsp.getValue()) {
                        RenderUtil.drawESP(stack, entity, new Color(0xFFCD853F, true), RenderUtil.ESPMode.BOTH, false);
                    } else if (scale == 4.0f && puddleJumperEsp.getValue()) {
                        RenderUtil.drawESP(stack, entity, Color.WHITE, RenderUtil.ESPMode.BOTH, false);
                    }
                } else if (entity instanceof TurtleEntity && tewtilEsp.getValue()) {
                    RenderUtil.drawESP(stack, entity, Color.GREEN, RenderUtil.ESPMode.BOTH, false);
                } else if (entity instanceof DolphinEntity && flipflopperEsp.getValue()) {
                    RenderUtil.drawESP(stack, entity, Color.CYAN, RenderUtil.ESPMode.BOTH, false);
                } else if (entity instanceof GlowSquidEntity && seashineEsp.getValue()) {
                    RenderUtil.drawESP(stack, entity, Color.BLUE, RenderUtil.ESPMode.BOTH, false);
                }
            }
        } else if (SkyblockUtil.isInIsland(SkyblockUtil.Island.THE_END)) {
            if (enderNodeEsp.getValue()) {
                enderNodes.removeIf(pos -> !mc.world.getBlockState(pos).isOf(Blocks.PURPLE_TERRACOTTA));
                for (BlockPos pos : enderNodes) {
                    RenderUtil.drawESP(event.getMatrix(), pos, Color.WHITE, RenderUtil.ESPMode.BOTH, false);
                }
            }
        }
    }

    @Handler
    public void onRenderBlock(RenderEvent.RenderBlockEvent event) {
        BlockState state = event.getState();
        BlockPos pos = event.getPos();

        if (state.isOf(Blocks.LAVA) && pos.getX() > 513 && pos.getZ() > 513 && (pos.getX() > 559 || pos.getZ() > 559) && pos.getY() > 64) {
            wormLavas.add(pos.toImmutable());
        } else if (state.isOf(Blocks.BLUE_STAINED_GLASS)) {
            batcaveBlocks.add(pos.toImmutable());
        } else if (state.isOf(Blocks.LIME_STAINED_GLASS) || state.isOf(Blocks.LIME_STAINED_GLASS_PANE)) {
            jades.add(pos.toImmutable());
        } else if (state.isOf(Blocks.ORANGE_STAINED_GLASS) || state.isOf(Blocks.ORANGE_STAINED_GLASS_PANE)) {
            ambers.add(pos.toImmutable());
        } else if (state.isOf(Blocks.LIGHT_BLUE_STAINED_GLASS) || state.isOf(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE)) {
            sapphires.add(pos.toImmutable());
        } else if (state.isOf(Blocks.PURPLE_STAINED_GLASS) || state.isOf(Blocks.PURPLE_STAINED_GLASS_PANE)) {
            amethysts.add(pos.toImmutable());
        } else if (state.isOf(Blocks.RED_STAINED_GLASS) || state.isOf(Blocks.RED_STAINED_GLASS_PANE)) {
            rubys.add(pos.toImmutable());
        } else if (state.isOf(Blocks.MAGENTA_STAINED_GLASS) || state.isOf(Blocks.MAGENTA_STAINED_GLASS_PANE)) {
            jaspers.add(pos.toImmutable());
        } else if (state.isOf(Blocks.YELLOW_STAINED_GLASS) || state.isOf(Blocks.YELLOW_STAINED_GLASS_PANE)) {
            topazs.add(pos.toImmutable());
        } else if (state.isOf(Blocks.POLISHED_DIORITE)) {
            titaniums.add(pos.toImmutable());
        } else if (state.isOf(Blocks.DRAGON_EGG)) {
            dragonEggs.add(pos.toImmutable());
        } else if (state.isOf(Blocks.PURPLE_TERRACOTTA) && pos.getX() > -597) {
            enderNodes.add(pos.toImmutable());
        }
    }

    private void renderBlocks(MatrixStack stack, Set<BlockPos> positions, Color color, int limit) {
        if (limit == -1) {
            for (BlockPos pos : positions) {
                RenderUtil.drawESP(stack, pos, color, RenderUtil.ESPMode.BOTH, false);
            }
        } else {
            List<BlockPos> sorted = new ArrayList<>(positions);
            sorted.sort(Comparator.comparingDouble(pos -> pos.getSquaredDistance(mc.player.getX(), mc.player.getY(), mc.player.getZ())));
            int count = Math.min(limit, sorted.size());
            for (int i = 0; i < count; i++) {
                RenderUtil.drawESP(stack, sorted.get(i), color, RenderUtil.ESPMode.BOTH, false);
            }
        }
    }
}
