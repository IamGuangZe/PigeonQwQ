package owo.pigeon.modules.impl.debug;

import net.engio.mbassy.listener.Handler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import owo.pigeon.event.events.RenderEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.utils.hypixel.skyblock.SkyblockUtil;
import owo.pigeon.utils.player.PlayerUtil;
import owo.pigeon.utils.render.RenderUtil;

import java.awt.*;

import static owo.pigeon.Pigeon.mc;

public class SlayerESP extends Module {
    public SlayerESP() {
        super("SlayerESP", Category.DEBUG);
    }

    @Handler
    public void onRender3D(RenderEvent.Render3DEvent event) {

        RenderUtil.drawESP(event.getMatrix(), SkyblockUtil.getSlayer(), Color.PINK, RenderUtil.ESPMode.BOTH, false);

        /*
        for (Entity entity : mc.level.getEntities()) {
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

    private Entity getMobEntity(ArmorStand stand) {
        AABB box = stand.getBoundingBox().move(0.0, -1.0, 0.0).inflate(0.2);

        Entity closest = null;
        double closestDistance = Double.MAX_VALUE;

        for (Entity entity : mc.level.getEntities(stand, box)) {
            if (entity instanceof ArmorStand || entity == mc.player) continue;
            if (entity instanceof WitherBoss && entity.isInvisible()) continue;
            if (entity instanceof Player player && PlayerUtil.hasUUID(player)) continue;

            double dist = stand.distanceTo(entity);
            if (dist < closestDistance) {
                closestDistance = dist;
                closest = entity;
            }
        }

        return closest;
    }
}
