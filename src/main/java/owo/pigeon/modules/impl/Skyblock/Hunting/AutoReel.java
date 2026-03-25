package owo.pigeon.modules.impl.skyblock.hunting;

import net.engio.mbassy.listener.Handler;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import owo.pigeon.Pigeon;
import owo.pigeon.event.events.RenderEvent;
import owo.pigeon.event.events.TickEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.ModeSetting;
import owo.pigeon.utils.WorldUtil;
import owo.pigeon.utils.player.PlayerUtil;
import owo.pigeon.utils.render.RenderUtil;

import java.awt.*;

import static owo.pigeon.Pigeon.mc;

public class AutoReel extends Module {
    public AutoReel() {
        super("AutoReel",Category.SKYBLOCK);
    }

    public ModeSetting<PlayerUtil.RightClickMode> clickMode = setting("click-mode", PlayerUtil.RightClickMode.MOUSE, v -> true);

    private boolean isReeled;

    @Override
    public void onEnable() {
        isReeled = false;
    }

    @Handler
    public void onTickPost(TickEvent.ClientTickEvent.Post event) {
        if (WorldUtil.nullCheck()) return;
        if (!mc.player.getInventory().getSelectedStack().getName().getString().contains("Lasso")) return;

        Entity entity = getLeashedEntity(mc.player);
        if (entity == null) {
            isReeled = false;
        } else {
            boolean foundStand = false;

            for (ArmorStandEntity stand : mc.world.getEntitiesByClass(ArmorStandEntity.class, entity.getBoundingBox().expand(1.0).offset(0.0, 3.0, 0.0), stand -> true)) {
                if (stand.getName().getString().contains("REEL")) {
                    foundStand = true;
                    break;
                }
            }

            if (foundStand) {
                if (!isReeled) {
                    isReeled = true;
                    PlayerUtil.RightClick(clickMode.getValue());
                }
            } else {
                isReeled = false;
            }
        }
    }

    @Handler
    public void onRender3D(RenderEvent.Render3DEvent event) {
        MatrixStack stack = event.getMatrix();
        if (Pigeon.isDebug()) {
            Entity entity = getLeashedEntity(mc.player);
            if (entity == null) return;

            RenderUtil.drawESP(stack, entity, Color.GREEN, RenderUtil.ESPMode.BOTH,false);
            RenderUtil.drawESP(stack, entity, entity.getBoundingBox().expand(1.0).offset(0.0, 3.0, 0.0), Color.BLUE, RenderUtil.ESPMode.BOTH,false);
        }
    }

    private Entity getLeashedEntity(PlayerEntity player) {
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof MobEntity mob) {
                if (mob.isLeashed() && mob.getLeashHolder() == player) {
                    return mob;
                }
            }
        }
        return null;
    }
}
