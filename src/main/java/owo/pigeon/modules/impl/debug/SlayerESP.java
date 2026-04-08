package owo.pigeon.modules.impl.debug;

import net.engio.mbassy.listener.Handler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import owo.pigeon.event.events.RenderEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.utils.hypixel.SkyblockUtil;
import owo.pigeon.utils.player.PlayerUtil;
import owo.pigeon.utils.render.RenderUtil;

import java.awt.*;

import static owo.pigeon.Pigeon.mc;

public class SlayerESP extends Module {
    public SlayerESP() {
        super("SlayerESP",Category.DEBUG);
    }

    @Handler
    public void onRender3D(RenderEvent.Render3DEvent event) {

        RenderUtil.drawESP(event.getMatrix(), SkyblockUtil.getSlayer(),Color.PINK, RenderUtil.ESPMode.BOTH,false);

        /*
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof ArmorStandEntity stand) {
                String name = stand.getName().getString();
                if (name.startsWith("Spawned by: " + mc.player.getName().getString())) {
                    Color color = Color.PINK;

                    Entity slayer = getMobEntity(stand);
                    RenderUtil.drawESP(event.getMatrix(),slayer,color, RenderUtil.ESPMode.BOTH);
                    RenderUtil.drawESP(event.getMatrix(),stand.getBoundingBox().offset(0.0,-1.0,0.0).expand(0.2),Color.GREEN, RenderUtil.ESPMode.BOTH);
                }
            }
        }

        */
    }

    private Entity getMobEntity(ArmorStandEntity stand) {
        Box box = stand.getBoundingBox().offset(0.0, -1.0, 0.0).expand(0.2);

        Entity closest = null;
        double closestDistance = Double.MAX_VALUE;

        for (Entity entity : mc.world.getOtherEntities(stand, box)) {
            if (entity instanceof ArmorStandEntity || entity == mc.player) continue;
            if (entity instanceof WitherEntity && entity.isInvisible()) continue;
            if (entity instanceof PlayerEntity player && PlayerUtil.hasUUID(player)) continue;

            double dist = stand.distanceTo(entity);
            if (dist < closestDistance) {
                closestDistance = dist;
                closest = entity;
            }
        }

        return closest;
    }
}
