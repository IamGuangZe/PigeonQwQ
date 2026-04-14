package owo.pigeon.modules.impl.skyblock.misc;

import net.engio.mbassy.listener.Handler;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MagmaCubeEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import owo.pigeon.event.events.RenderEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.ExpandSetting;
import owo.pigeon.settings.IntSetting;
import owo.pigeon.utils.hypixel.SkyblockUtil;
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

    public static Set<BlockPos> batcaveBlocks = ConcurrentHashMap.newKeySet();
    public static Set<BlockPos> wormLavas = ConcurrentHashMap.newKeySet();
    public static Set<BlockPos> jades = ConcurrentHashMap.newKeySet();
    public static Set<BlockPos> ambers = ConcurrentHashMap.newKeySet();
    public static Set<BlockPos> sapphires = ConcurrentHashMap.newKeySet();
    public static Set<BlockPos> amethysts = ConcurrentHashMap.newKeySet();
    public static Set<BlockPos> rubys = ConcurrentHashMap.newKeySet();
    public static Set<BlockPos> jaspers = ConcurrentHashMap.newKeySet();
    public static Set<BlockPos> topazs = ConcurrentHashMap.newKeySet();
    public static Set<BlockPos> titaniums = ConcurrentHashMap.newKeySet();

    @Override
    public void onEnable() {
        if (mc.worldRenderer == null) return;
        mc.worldRenderer.reload();
    }

    @Handler
    public void onRender3D(RenderEvent.Render3DEvent event) {
        MatrixStack stack = event.getMatrix();

        if (SkyblockUtil.isInIsland(SkyblockUtil.Island.CrystalHollows)) {
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
                renderBlocks(stack, sapphires, new Color(173, 216, 255), gemstoneEspLimit.getValue());
            }
            if (amethystEsp.getValue()) {
                amethysts.removeIf(pos -> !mc.world.getBlockState(pos).isOf(Blocks.PURPLE_STAINED_GLASS) && !mc.world.getBlockState(pos).isOf(Blocks.PURPLE_STAINED_GLASS_PANE));
                renderBlocks(stack, amethysts, new Color(153, 50, 204), gemstoneEspLimit.getValue());
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
        }

        if (SkyblockUtil.isInIsland(SkyblockUtil.Island.DwarvenMines)) {
            if (titaniumEsp.getValue()) {
                titaniums.removeIf(pos -> !mc.world.getBlockState(pos).isOf(Blocks.POLISHED_DIORITE));
                for (BlockPos pos : titaniums) {
                    RenderUtil.drawESP(event.getMatrix(), pos, Color.WHITE, RenderUtil.ESPMode.BOTH, false);
                }
            }
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
