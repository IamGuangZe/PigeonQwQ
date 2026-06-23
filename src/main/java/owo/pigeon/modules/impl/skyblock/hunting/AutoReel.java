package owo.pigeon.modules.impl.skyblock.hunting;

import com.mojang.blaze3d.vertex.PoseStack;
import net.engio.mbassy.listener.Handler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.decoration.ArmorStand;
import owo.pigeon.Pigeon;
import owo.pigeon.event.events.ClientTickEvent;
import owo.pigeon.event.events.RenderEvent;
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
        super("AutoReel", Category.HUNTING);
    }

    public ModeSetting<PlayerUtil.RightClickMode> clickMode = setting("click-mode", PlayerUtil.RightClickMode.MOUSE, v -> true);

    private boolean isReeled;
    private Bat lassoEntity;
    private ArmorStand reelEntity;

    @Override
    public void onEnable() {
        isReeled = false;
        reelEntity = null;
    }

    @Handler
    public void onTickPost(ClientTickEvent.Post event) {
        if (WorldUtil.nullCheck()) return;
        if (!mc.player.getMainHandItem().getHoverName().getString().contains("Lasso")) return;

        lassoEntity = getLassoEntity();

        if (lassoEntity == null) {
            isReeled = false;
            reelEntity = null;
            return;
        }

        if (reelEntity == null || reelEntity.isRemoved())
            reelEntity = mc.level.getEntitiesOfClass(
                    ArmorStand.class,
                    lassoEntity.getBoundingBox().inflate(0.5, 2, 0.5).move(0.0, 2.0, 0.0),
                    stand -> stand.getName().getString().contains("REEL")
            ).stream().findFirst().orElse(null);


        if (reelEntity != null && !isReeled) {
            PlayerUtil.rightClick(clickMode.getValue());
        }

        isReeled = reelEntity != null;
    }

    @Handler
    public void onRender3D(RenderEvent.Render3DEvent event) {
        PoseStack stack = event.getMatrix();
        if (!Pigeon.isDebug()) return;
        RenderUtil.drawESP(stack, lassoEntity, lassoEntity.getBoundingBox().inflate(0.5, 2, 0.5).move(0.0, 2.0, 0.0), Color.BLUE, RenderUtil.ESPMode.BOTH, false);
        RenderUtil.drawESP(stack, reelEntity, reelEntity.getBoundingBox().inflate(0.1), Color.YELLOW, RenderUtil.ESPMode.BOTH, false);
    }

    private Bat getLassoEntity() {
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof Bat bat && bat.isLeashed() && bat.getLeashHolder() == mc.player) {
                return bat;
            }
        }
        return null;
    }
}
