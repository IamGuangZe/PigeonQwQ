package owo.pigeon.modules.impl.skyblock.dungeon;

import net.engio.mbassy.listener.Handler;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
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
        super("StarMobESP", Category.SKYBLOCK);
    }

    public enum Target {
        STAND, MOB
    }

    public ModeSetting<Target> target = setting("target", Target.MOB, v -> true);
    public ModeSetting<RenderUtil.ESPMode> espMode = setting("esp-mode", RenderUtil.ESPMode.OUTLINE, v -> true);
    public ColorSetting color = setting("color", new Color(0xFFFF6666, true), v -> true);

    @Handler
    public void onRender3D(RenderEvent.Render3DEvent event) {
        MatrixStack stack = event.getMatrix();
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof ArmorStandEntity stand) {
                String name = stand.getName().getString();

                if (name.contains("✯") && name.contains("❤")) {
                    if (Pigeon.isDebug())
                        RenderUtil.drawESP(stack,
                                stand,
                                stand.getBoundingBox().offset(0.0, -1.0, 0.0).expand(0.2),
                                Color.RED,
                                RenderUtil.ESPMode.BOTH,
                                false
                        );


                    switch (target.getValue()) {
                        case STAND -> {
                            Box customBox = new Box(
                                    entity.getX() - 0.5, entity.getY() - 2.0, entity.getZ() - 0.5,
                                    entity.getX() + 0.5, entity.getY(), entity.getZ() + 0.5
                            );

                            RenderUtil.drawESP(stack, customBox, color.getValue(), espMode.getValue(),false);
                        }

                        case MOB -> {
                            Entity starMob = getMobEntity(stand);
                            if (starMob != null)
                                RenderUtil.drawESP(stack, starMob, color.getValue(), espMode.getValue(),false);
                        }
                    }
                }
            }
        }
    }

    private Entity getMobEntity(ArmorStandEntity stand) {
        Box box = stand.getBoundingBox().offset(0.0, -1.0, 0.0);

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
