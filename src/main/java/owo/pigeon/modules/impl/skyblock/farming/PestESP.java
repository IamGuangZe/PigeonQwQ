package owo.pigeon.modules.impl.skyblock.farming;

import net.engio.mbassy.listener.Handler;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
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
    public EnableSetting onlyHoldVacuum = setting("only-hold-vacuum", true, v -> true);
    public ModeSetting<RenderUtil.ESPMode> mode = setting("mode", RenderUtil.ESPMode.BOTH, v -> true);
    public EnableSetting tracer = setting("tracer", false, v -> true);
    public ColorSetting color = setting("color", new Color(0xAAFF4400, true), v -> true);

    @Handler
    public void onRender3D(RenderEvent.Render3DEvent event) {
        boolean inGarden = SkyblockUtil.isInIsland(SkyblockUtil.Island.GARDEN);
        boolean inHub = SkyblockUtil.isInIsland(SkyblockUtil.Island.HUB);

        if (!inGarden && !(hubRat.getValue() && inHub)) return;

        if (onlyHoldVacuum.getValue()) {
            ItemStack heldItem = mc.player.getMainHandItem();
            if (heldItem.isEmpty()) return;

            String name = ColorUtil.removeColor(heldItem.getHoverName().getString());
            if (!name.contains("Vacuum")) return;
        }

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (inHub) {
                if (!(entity instanceof Display.ItemDisplay itemDisplay)) continue;
                if (!SkyblockUtil.RAT.equals(getSkullTexture(itemDisplay))) continue;

            } else if (inGarden) {
                if (!(entity instanceof ArmorStand stand)) continue;

                String texture = getSkullTexture(stand);
                if (texture == null || !SkyblockUtil.PESTS.contains(texture)) continue;

                // middle earthworm
                if (SkyblockUtil.EARTHWORM_TAIL.equals(texture)) continue;
                if (SkyblockUtil.EARTHWORM.equals(texture)) {
                    Entity middlePart = findClosestTail(stand);
                    if (middlePart != null) entity = middlePart;
                }
            }

            AABB box = entity.getBoundingBox();
            if (inHub) box = box.inflate(0.4).move(0.0, 0.275, 0.0);
            else if (inGarden) box = getGardenPestBox(box);

            RenderUtil.drawESP(event.getMatrix(), entity, box, color.getValue(), mode.getValue(), tracer.getValue());
        }
    }

    private AABB getGardenPestBox(AABB box) {
        double size = 0.8d;
        double x = (box.minX + box.maxX) / 2.0;
        double y = box.maxY - size / 2;
        double z = (box.minZ + box.maxZ) / 2.0;

        return new AABB(x, y, z, x, y, z).inflate(size / 2).move(0.0, 0.175, 0.0);
    }

    private String getSkullTexture(Entity entity) {
        ItemStack stack;
        if (entity instanceof Display.ItemDisplay itemDisplay) {
            stack = itemDisplay.getItemStack();
        } else if (entity instanceof LivingEntity livingEntity) {
            stack = livingEntity.getItemBySlot(EquipmentSlot.HEAD);
        } else {
            return null;
        }

        if (stack.isEmpty() || !stack.is(Items.PLAYER_HEAD)) return null;
        return ItemUtil.getSkullTexture(stack);
    }

    private Entity findClosestTail(Entity entity) {
        Entity closest = null;
        double closestDistance = Double.MAX_VALUE;
        AABB box = entity.getBoundingBox().inflate(0.5);

        for (Entity nearbyEntity : mc.level.getEntities(entity, box)) {
            if (!(nearbyEntity instanceof ArmorStand)) continue;
            if (!SkyblockUtil.EARTHWORM_TAIL.equals(getSkullTexture(nearbyEntity))) continue;

            double dist = entity.distanceTo(nearbyEntity);
            if (dist < closestDistance) {
                closestDistance = dist;
                closest = nearbyEntity;
            }
        }

        return closest;
    }
}
