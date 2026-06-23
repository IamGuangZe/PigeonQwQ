package owo.pigeon.modules.impl.skyblock.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import net.engio.mbassy.listener.Handler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.dolphin.Dolphin;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.frog.FrogVariants;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.animal.squid.GlowSquid;
import net.minecraft.world.entity.animal.turtle.Turtle;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import owo.pigeon.event.events.RenderEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.ExpandSetting;
import owo.pigeon.settings.IntSetting;
import owo.pigeon.utils.hypixel.skyblock.SkyblockUtil;
import owo.pigeon.utils.render.RenderUtil;

import java.awt.*;
import java.util.*;
import java.util.List;
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
        if (mc.levelRenderer == null) return;
        mc.levelRenderer.allChanged();
    }

    @Handler
    public void onRender3D(RenderEvent.Render3DEvent event) {
        PoseStack stack = event.getMatrix();

        if (SkyblockUtil.isInIsland(SkyblockUtil.Island.CRIMSON_ISLE)) {
            for (Entity entity : mc.level.entitiesForRendering()) {
                if (entity instanceof Bat && !entity.isInvisible() && cinderbatEsp.getValue()) {
                    RenderUtil.drawESP(stack, entity, Color.GREEN, RenderUtil.ESPMode.BOTH, false);
                }
            }
        } else if (SkyblockUtil.isInIsland(SkyblockUtil.Island.CRYSTAL_HOLLOWS)) {
            for (Entity entity : mc.level.entitiesForRendering()) {
                if (entity instanceof Zombie zombie && zombie.getMainHandItem().is(Items.BONE) && keyGuardianEsp.getValue()) {
                    RenderUtil.drawESP(stack, entity, Color.YELLOW, RenderUtil.ESPMode.BOTH, false);
                } else if (entity instanceof IronGolem && automatonEsp.getValue()) {
                    RenderUtil.drawESP(stack, entity, Color.WHITE, RenderUtil.ESPMode.BOTH, false);
                } else if (entity instanceof Slime && !(entity instanceof MagmaCube) && sludgeEsp.getValue()) {
                    RenderUtil.drawESP(stack, entity, Color.GREEN, RenderUtil.ESPMode.BOTH, false);
                } else if (entity instanceof MagmaCube && yogEsp.getValue()) {
                    RenderUtil.drawESP(stack, entity, Color.ORANGE, RenderUtil.ESPMode.BOTH, false);
                }
            }

            if (wormLavaEsp.getValue()) {
                wormLavas.removeIf(pos -> !mc.level.getBlockState(pos).is(Blocks.LAVA));
                renderBlocks(stack, wormLavas, Color.ORANGE, wormLavaEspLimit.getValue());
            }

            if (corleoneBatcaveEsp.getValue()) {
                batcaveBlocks.removeIf(pos -> !mc.level.getBlockState(pos).is(Blocks.BLUE_STAINED_GLASS));
                for (BlockPos pos : batcaveBlocks) {
                    RenderUtil.drawESP(event.getMatrix(), pos, Color.CYAN, RenderUtil.ESPMode.BOTH, false);
                }
            }

            if (jadeEsp.getValue()) {
                jades.removeIf(pos -> !mc.level.getBlockState(pos).is(Blocks.LIME_STAINED_GLASS) && !mc.level.getBlockState(pos).is(Blocks.LIME_STAINED_GLASS_PANE));
                renderBlocks(stack, jades, Color.GREEN, gemstoneEspLimit.getValue());
            }
            if (amberEsp.getValue()) {
                ambers.removeIf(pos -> !mc.level.getBlockState(pos).is(Blocks.ORANGE_STAINED_GLASS) && !mc.level.getBlockState(pos).is(Blocks.ORANGE_STAINED_GLASS_PANE));
                renderBlocks(stack, ambers, Color.ORANGE, gemstoneEspLimit.getValue());
            }
            if (sapphireEsp.getValue()) {
                sapphires.removeIf(pos -> !mc.level.getBlockState(pos).is(Blocks.LIGHT_BLUE_STAINED_GLASS) && !mc.level.getBlockState(pos).is(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE));
                renderBlocks(stack, sapphires, new Color(0xADD8FF, true), gemstoneEspLimit.getValue());
            }
            if (amethystEsp.getValue()) {
                amethysts.removeIf(pos -> !mc.level.getBlockState(pos).is(Blocks.PURPLE_STAINED_GLASS) && !mc.level.getBlockState(pos).is(Blocks.PURPLE_STAINED_GLASS_PANE));
                renderBlocks(stack, amethysts, new Color(0x9932CC, true), gemstoneEspLimit.getValue());
            }
            if (rubyEsp.getValue()) {
                rubys.removeIf(pos -> !mc.level.getBlockState(pos).is(Blocks.RED_STAINED_GLASS) && !mc.level.getBlockState(pos).is(Blocks.RED_STAINED_GLASS_PANE));
                renderBlocks(stack, rubys, Color.RED, gemstoneEspLimit.getValue());
            }
            if (topazEsp.getValue()) {
                topazs.removeIf(pos -> !mc.level.getBlockState(pos).is(Blocks.YELLOW_STAINED_GLASS) && !mc.level.getBlockState(pos).is(Blocks.YELLOW_STAINED_GLASS_PANE));
                renderBlocks(stack, topazs, Color.YELLOW, gemstoneEspLimit.getValue());
            }
            if (jasperEsp.getValue()) {
                jaspers.removeIf(pos -> !mc.level.getBlockState(pos).is(Blocks.MAGENTA_STAINED_GLASS) && !mc.level.getBlockState(pos).is(Blocks.MAGENTA_STAINED_GLASS_PANE));
                renderBlocks(stack, jaspers, Color.MAGENTA, gemstoneEspLimit.getValue());
            }
        } else if (SkyblockUtil.isInIsland(SkyblockUtil.Island.DWARVEN_MINES)) {
            if (titaniumEsp.getValue()) {
                titaniums.removeIf(pos -> !mc.level.getBlockState(pos).is(Blocks.POLISHED_DIORITE));
                for (BlockPos pos : titaniums) {
                    RenderUtil.drawESP(event.getMatrix(), pos, Color.WHITE, RenderUtil.ESPMode.BOTH, false);
                }
            }
            if (dragonEggEsp.getValue()) {
                dragonEggs.removeIf(pos -> !mc.level.getBlockState(pos).is(Blocks.DRAGON_EGG));
                for (BlockPos pos : dragonEggs) {
                    RenderUtil.drawESP(event.getMatrix(), pos, Color.DARK_GRAY, RenderUtil.ESPMode.BOTH, false);
                }
            }
        } else if (SkyblockUtil.isInIsland(SkyblockUtil.Island.GALATEA)) {
            Set<BlockPos> renderedMudworms = new HashSet<>();

            for (Entity entity : mc.level.entitiesForRendering()) {
                if (entity instanceof Shulker && hideonleafEsp.getValue()) {
                    RenderUtil.drawESP(stack, entity, Color.GREEN, RenderUtil.ESPMode.BOTH, false);
                } else if (entity instanceof Phantom phantom) {
                    float scale = phantom.getScale();
                    if (scale == 0.4f && phanpyreEsp.getValue()) {
                        RenderUtil.drawESP(stack, entity, Color.WHITE, RenderUtil.ESPMode.BOTH, false);
                    } else if (scale == 1.0f && phanflareEsp.getValue()) {
                        RenderUtil.drawESP(stack, entity, Color.WHITE, RenderUtil.ESPMode.BOTH, false);
                    } else if (scale == 3.0f && dreadwingEsp.getValue()) {
                        RenderUtil.drawESP(stack, entity, Color.WHITE, RenderUtil.ESPMode.BOTH, false);
                    }
                } else if (entity instanceof Display.ItemDisplay itemDisplay && itemDisplay.getItemStack().is(Items.STRING) && mudwormEsp.getValue()) {
                    BlockPos blockPos = entity.blockPosition();
                    if (!renderedMudworms.contains(blockPos)) {
                        renderedMudworms.add(blockPos);
                        RenderUtil.drawESP(stack, blockPos, Color.YELLOW, RenderUtil.ESPMode.BOTH, false);
                    }
                }
            }
        } else if (SkyblockUtil.isInIsland(SkyblockUtil.Island.LOTUS_ATOLL)) {
            for (Entity entity : mc.level.entitiesForRendering()) {
                if (entity instanceof Frog frog && frog.getVariant().is(FrogVariants.TEMPERATE)) {
                    float scale = frog.getScale();
                    if (scale == 1.0f && lotumEsp.getValue()) {
                        RenderUtil.drawESP(stack, entity, new Color(0xFFCD853F, true), RenderUtil.ESPMode.BOTH, false);
                    } else if (scale == 4.0f && puddleJumperEsp.getValue()) {
                        RenderUtil.drawESP(stack, entity, Color.WHITE, RenderUtil.ESPMode.BOTH, false);
                    }
                } else if (entity instanceof Turtle && tewtilEsp.getValue()) {
                    RenderUtil.drawESP(stack, entity, Color.GREEN, RenderUtil.ESPMode.BOTH, false);
                } else if (entity instanceof Dolphin && flipflopperEsp.getValue()) {
                    RenderUtil.drawESP(stack, entity, Color.CYAN, RenderUtil.ESPMode.BOTH, false);
                } else if (entity instanceof GlowSquid && seashineEsp.getValue()) {
                    RenderUtil.drawESP(stack, entity, Color.BLUE, RenderUtil.ESPMode.BOTH, false);
                }
            }
        } else if (SkyblockUtil.isInIsland(SkyblockUtil.Island.THE_END)) {
            if (enderNodeEsp.getValue()) {
                enderNodes.removeIf(pos -> !mc.level.getBlockState(pos).is(Blocks.PURPLE_TERRACOTTA));
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

        if (state.is(Blocks.LAVA) && pos.getX() > 513 && pos.getZ() > 513 && (pos.getX() > 559 || pos.getZ() > 559) && pos.getY() > 64) {
            wormLavas.add(pos.immutable());
        } else if (state.is(Blocks.BLUE_STAINED_GLASS)) {
            batcaveBlocks.add(pos.immutable());
        } else if (state.is(Blocks.LIME_STAINED_GLASS) || state.is(Blocks.LIME_STAINED_GLASS_PANE)) {
            jades.add(pos.immutable());
        } else if (state.is(Blocks.ORANGE_STAINED_GLASS) || state.is(Blocks.ORANGE_STAINED_GLASS_PANE)) {
            ambers.add(pos.immutable());
        } else if (state.is(Blocks.LIGHT_BLUE_STAINED_GLASS) || state.is(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE)) {
            sapphires.add(pos.immutable());
        } else if (state.is(Blocks.PURPLE_STAINED_GLASS) || state.is(Blocks.PURPLE_STAINED_GLASS_PANE)) {
            amethysts.add(pos.immutable());
        } else if (state.is(Blocks.RED_STAINED_GLASS) || state.is(Blocks.RED_STAINED_GLASS_PANE)) {
            rubys.add(pos.immutable());
        } else if (state.is(Blocks.MAGENTA_STAINED_GLASS) || state.is(Blocks.MAGENTA_STAINED_GLASS_PANE)) {
            jaspers.add(pos.immutable());
        } else if (state.is(Blocks.YELLOW_STAINED_GLASS) || state.is(Blocks.YELLOW_STAINED_GLASS_PANE)) {
            topazs.add(pos.immutable());
        } else if (state.is(Blocks.POLISHED_DIORITE)) {
            titaniums.add(pos.immutable());
        } else if (state.is(Blocks.DRAGON_EGG)) {
            dragonEggs.add(pos.immutable());
        } else if (state.is(Blocks.PURPLE_TERRACOTTA) && pos.getX() > -597) {
            enderNodes.add(pos.immutable());
        }
    }

    private void renderBlocks(PoseStack stack, Set<BlockPos> positions, Color color, int limit) {
        if (limit == -1) {
            for (BlockPos pos : positions) {
                RenderUtil.drawESP(stack, pos, color, RenderUtil.ESPMode.BOTH, false);
            }
        } else {
            List<BlockPos> sorted = new ArrayList<>(positions);
            sorted.sort(Comparator.comparingDouble(pos -> pos.distToLowCornerSqr(mc.player.getX(), mc.player.getY(), mc.player.getZ())));
            int count = Math.min(limit, sorted.size());
            for (int i = 0; i < count; i++) {
                RenderUtil.drawESP(stack, sorted.get(i), color, RenderUtil.ESPMode.BOTH, false);
            }
        }
    }
}
