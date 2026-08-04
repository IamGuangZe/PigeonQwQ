package owo.pigeon.modules.impl.skyblock.dungeon;

import com.mojang.blaze3d.vertex.PoseStack;
import net.engio.mbassy.listener.Handler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import owo.pigeon.Pigeon;
import owo.pigeon.event.events.RenderEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.ColorSetting;
import owo.pigeon.settings.ModeSetting;
import owo.pigeon.utils.player.PlayerUtil;
import owo.pigeon.utils.render.RenderUtil;

import java.awt.*;

import static owo.pigeon.Pigeon.mc;

public class StarMobESP extends Module {
    public StarMobESP() {
        super("StarMobESP", Category.DUNGEON);
    }

    public enum Target {
        STAND, MOB
    }

    public ModeSetting<Target> target = setting("target", Target.MOB, v -> true);
    public ModeSetting<RenderUtil.ESPMode> espMode = setting("esp-mode", RenderUtil.ESPMode.OUTLINE, v -> true);
    public ColorSetting color = setting("color", new Color(0xFFFF6666, true), v -> true);

    @Handler
    public void onRender3D(RenderEvent.Render3DEvent event) {
        PoseStack stack = event.getMatrix();
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof ArmorStand stand) {
                String name = stand.getName().getString();

                if (name.contains("✯") && name.contains("❤")) {
                    if (Pigeon.isDebug())
                        RenderUtil.drawESP(stack,
                                stand,
                                stand.getBoundingBox().move(0.0, -1.0, 0.0).inflate(0.2),
                                Color.RED,
                                RenderUtil.ESPMode.BOTH,
                                false
                        );


                    switch (target.getValue()) {
                        case STAND -> {
                            AABB customBox = new AABB(
                                    entity.getX() - 0.5, entity.getY() - 2.0, entity.getZ() - 0.5,
                                    entity.getX() + 0.5, entity.getY(), entity.getZ() + 0.5
                            );

                            RenderUtil.drawESP(stack, customBox, color.getValue(), espMode.getValue(), false);
                        }

                        case MOB -> {
                            Entity starMob = getMobEntity(stand);
                            if (starMob != null)
                                RenderUtil.drawESP(stack, starMob, color.getValue(), espMode.getValue(), false);
                        }
                    }
                }
            }
        }
    }

    private Entity getMobEntity(ArmorStand stand) {
        AABB box = stand.getBoundingBox().move(0.0, -1.0, 0.0);

        Entity closest = null;
        double closestDistance = Double.MAX_VALUE;

        for (Entity entity : mc.level.getEntities(stand, box)) {
            if (entity instanceof ArmorStand || entity == mc.player) continue;
            if (entity instanceof WitherBoss && entity.isInvisible()) continue;
            if (entity instanceof Player player && PlayerUtil.hasPremiumUuid(player)) continue;

            double dist = stand.distanceTo(entity);
            if (dist < closestDistance) {
                closestDistance = dist;
                closest = entity;
            }
        }

        return closest;
    }
}
