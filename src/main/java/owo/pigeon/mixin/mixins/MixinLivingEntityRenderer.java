package owo.pigeon.mixin.mixins;

import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.BatEntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import owo.pigeon.modules.impl.skyblock.dungeon.GiantBat;
import owo.pigeon.utils.ModuleUtil;
import owo.pigeon.utils.hypixel.skyblock.DungeonUtil;

@Mixin(LivingEntityRenderer.class)
public class MixinLivingEntityRenderer {

    @Inject(method = "scale", at = @At("TAIL"))
    private void onScale(LivingEntityRenderState state, MatrixStack matrices, CallbackInfo ci) {
        if (!(state instanceof BatEntityRenderState)) return;
        if (!ModuleUtil.getModule(GiantBat.class).isEnable()) return;
        if (!DungeonUtil.isInDungeon()) return;

        float s = ModuleUtil.getModule(GiantBat.class).scale.getValue();
        matrices.scale(s, s, s);
    }
}
