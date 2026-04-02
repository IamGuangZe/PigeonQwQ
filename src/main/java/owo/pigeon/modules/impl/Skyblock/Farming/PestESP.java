package owo.pigeon.modules.impl.skyblock.farming;

import net.engio.mbassy.listener.Handler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import owo.pigeon.event.events.RenderEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.ColorSetting;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.ModeSetting;
import owo.pigeon.utils.ItemUtil;
import owo.pigeon.utils.hypixel.SkyblockUtil;
import owo.pigeon.utils.render.RenderUtil;

import java.awt.*;

import static owo.pigeon.Pigeon.mc;

public class PestESP extends Module {
    public PestESP() {
        super("PestESP", Category.SKYBLOCK);
    }

    public EnableSetting hubRat = setting("hub-rat", true, v -> true);
    public ModeSetting<RenderUtil.ESPMode> mode = setting("mode", RenderUtil.ESPMode.BOTH, v -> true);
    public EnableSetting tracer = setting("tracer", false, v -> true);
    public ColorSetting color = setting("color", new Color(0xAAFF4400, true), v -> true);

    @Handler
    public void onRender3D(RenderEvent.Render3DEvent event) {
        boolean inGarden = SkyblockUtil.isInIsland(SkyblockUtil.Island.Garden);
        boolean inHub = SkyblockUtil.isInIsland(SkyblockUtil.Island.Hub);

        if (!inGarden && !(hubRat.getValue() && inHub)) return;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof ArmorStandEntity stand)) continue;

            ItemStack helmet = stand.getEquippedStack(EquipmentSlot.HEAD);
            if (helmet.isEmpty() || !helmet.isOf(Items.PLAYER_HEAD)) continue;

            String texture = ItemUtil.getSkullTexture(helmet);
            if (texture == null || !SkyblockUtil.PESTS.contains(texture)) continue;
            if (!texture.equals(SkyblockUtil.RAT) && inHub) continue;

            boolean shouldTracer = tracer.getValue() && !SkyblockUtil.EARTHWORM_TAIL.equals(texture);

            RenderUtil.drawESP(event.getMatrix(), entity, color.getValue(), mode.getValue(), shouldTracer);
        }
    }
}
