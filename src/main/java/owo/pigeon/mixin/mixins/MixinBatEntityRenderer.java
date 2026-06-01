package owo.pigeon.mixin.mixins;

import net.minecraft.client.render.entity.BatEntityRenderer;
import net.minecraft.client.render.entity.state.BatEntityRenderState;
import net.minecraft.entity.passive.BatEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import owo.pigeon.modules.impl.skyblock.dungeon.GiantBat;
import owo.pigeon.utils.ModuleUtil;
import owo.pigeon.utils.hypixel.skyblock.DungeonUtil;

@Mixin(BatEntityRenderer.class)
public class MixinBatEntityRenderer {

    @Inject(method = "updateRenderState(Lnet/minecraft/entity/passive/BatEntity;Lnet/minecraft/client/render/entity/state/BatEntityRenderState;F)V", at = @At("RETURN"))
    private void onUpdateRenderStateTail(BatEntity batEntity, BatEntityRenderState batEntityRenderState, float f, CallbackInfo ci) {
        if (!ModuleUtil.isEnable(GiantBat.class)) return;
        if (!DungeonUtil.isInDungeon()) return;

        float maxHealth = batEntity.getMaxHealth();
        if (maxHealth != 100f && maxHealth != 200f) return;

        float scale = ModuleUtil.getModule(GiantBat.class).scale.getValue();
        batEntityRenderState.baseScale *= scale;
    }
}
