package owo.pigeon.modules.impl.skyblock.farming;

import net.engio.mbassy.listener.Handler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Box;
import owo.pigeon.event.events.RenderEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.ColorSetting;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.ModeSetting;
import owo.pigeon.utils.ColorUtil;
import owo.pigeon.utils.ItemUtil;
import owo.pigeon.utils.hypixel.skyblock.SkyblockUtil;
import owo.pigeon.utils.render.RenderUtil;

import java.awt.*;

import static owo.pigeon.Pigeon.mc;

public class PestESP extends Module {
    public PestESP() {
        super("PestESP", Category.FARMING);
    }

    public EnableSetting hubRat = setting("hub-rat", true, v -> true);
    public EnableSetting onlyVacuum = setting("only-vacuum", true, v -> true);
    public ModeSetting<RenderUtil.ESPMode> mode = setting("mode", RenderUtil.ESPMode.BOTH, v -> true);
    public EnableSetting tracer = setting("tracer", false, v -> true);
    public ColorSetting color = setting("color", new Color(0xAAFF4400, true), v -> true);

    @Handler
    public void onRender3D(RenderEvent.Render3DEvent event) {
        boolean inGarden = SkyblockUtil.isInIsland(SkyblockUtil.Island.GARDEN);
        boolean inHub = SkyblockUtil.isInIsland(SkyblockUtil.Island.HUB);

        if (!inGarden && !(hubRat.getValue() && inHub)) return;

        if (onlyVacuum.getValue()) {
            ItemStack heldItem = mc.player.getMainHandStack();
            if (heldItem.isEmpty()) return;

            String name = ColorUtil.removeColor(heldItem.getName().getString());
            if (!name.contains("Vacuum")) return;
        }

        for (Entity entity : mc.world.getEntities()) {
            boolean shouldDraw = false;
            String currentTexture = null;

            if (inHub) {
                if (!(entity instanceof DisplayEntity.ItemDisplayEntity itemDisplay)) continue;
                ItemStack stack = itemDisplay.getItemStack();
                if (stack.isEmpty() || !stack.isOf(Items.PLAYER_HEAD)) continue;

                currentTexture = ItemUtil.getSkullTexture(stack);
                if (!currentTexture.equals(SkyblockUtil.RAT)) continue;

                shouldDraw = true;
            } else if (inGarden) {
                if (!(entity instanceof ArmorStandEntity stand)) continue;

                ItemStack helmet = stand.getEquippedStack(EquipmentSlot.HEAD);
                if (helmet.isEmpty() || !helmet.isOf(Items.PLAYER_HEAD)) continue;

                currentTexture = ItemUtil.getSkullTexture(helmet);
                if (currentTexture == null || !SkyblockUtil.PESTS.contains(currentTexture)) continue;

                shouldDraw = true;
            }

            if (shouldDraw) {
                boolean shouldTracer = tracer.getValue() && !SkyblockUtil.EARTHWORM_TAIL.equals(currentTexture);
                if (inHub) {
                    Box box = entity.getBoundingBox().expand(0.4).offset(0.0, 0.275, 0.0);
                    RenderUtil.drawESP(event.getMatrix(), entity, box, color.getValue(), mode.getValue(), shouldTracer);
                } else if (inGarden) {
                    RenderUtil.drawESP(event.getMatrix(), entity, color.getValue(), mode.getValue(), shouldTracer);
                }
            }
        }
    }
}
