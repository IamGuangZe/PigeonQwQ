package owo.pigeon.modules.impl.hypixel;

import net.engio.mbassy.listener.Handler;
import net.minecraft.block.Block;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import owo.pigeon.event.events.RenderEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.ColorSetting;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.ModeSetting;
import owo.pigeon.utils.ItemUtil;
import owo.pigeon.utils.hypixel.HypixelUtil;
import owo.pigeon.utils.render.RenderUtil;

import java.awt.*;

import static owo.pigeon.Pigeon.mc;

public class PixelHelper extends Module {
    public PixelHelper() {
        super("PixelHelper", Category.HYPIXEL);
    }

    public ModeSetting<RenderUtil.ESPMode> mode = setting("mode", RenderUtil.ESPMode.BOTH, v -> true);
    public ColorSetting color = setting("color", new Color(0xFFFFFF), v -> true);
    public EnableSetting nearestTracer = setting("nearest-tracer", true, v -> true);
    public EnableSetting anvilWarning = setting("anvil-warning", true, v -> true);


    @Handler
    public void onRender3D(RenderEvent.Render3DEvent event) {
        if (!HypixelUtil.isInGame(HypixelUtil.Game.PIXELPARTY)) return;

        MatrixStack matrixStack = event.getMatrix();

        if (anvilWarning.getValue()) {
            for (Entity entity : mc.world.getEntities()) {
                if (entity instanceof FallingBlockEntity fallingBlockEntity) {
                    Box box = new Box(fallingBlockEntity.getX() - 0.5, 1, fallingBlockEntity.getZ() - 0.5,
                            fallingBlockEntity.getX() + 0.5, 3, fallingBlockEntity.getZ() + 0.5);
                    RenderUtil.drawESP(matrixStack, box, new Color(0xBBFF0000, true), RenderUtil.ESPMode.BOTH, false);
                }
            }
        }

        ItemStack itemStack = ItemUtil.getItemStackfromSlot(8);
        if (itemStack.isEmpty() || !(itemStack.getItem() instanceof BlockItem blockItem)) {
            return;
        }

        Block block = blockItem.getBlock();

        BlockPos closestPos = null;
        double minDistanceSq = Double.MAX_VALUE;
        Vec3d playerPos = mc.player.getEntityPos();

        for (int x = -32; x <= 32; x++) {
            for (int z = -32; z <= 32; z++) {
                BlockPos pos = new BlockPos(x, 0, z);
                if (mc.world.getBlockState(pos).getBlock() == block) {
                    RenderUtil.drawESP(event.getMatrix(), pos, color.getValue(), mode.getValue(), false);

                    double distSq = pos.getSquaredDistance(playerPos.x, playerPos.y, playerPos.z);
                    if (distSq < minDistanceSq) {
                        minDistanceSq = distSq;
                        closestPos = pos;
                    }
                }
            }
        }

        if (nearestTracer.getValue() && closestPos != null) {
            RenderUtil.drawTracer(event.getMatrix(), closestPos, color.getValue(), 1.5);
        }
    }
}
