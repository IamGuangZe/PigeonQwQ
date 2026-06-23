package owo.pigeon.mixin.mixins;

import net.minecraft.client.renderer.entity.BatRenderer;
import net.minecraft.client.renderer.entity.state.BatRenderState;
import net.minecraft.world.entity.ambient.Bat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import owo.pigeon.modules.impl.skyblock.dungeon.GiantBat;
import owo.pigeon.utils.ModuleUtil;
import owo.pigeon.utils.hypixel.skyblock.DungeonUtil;

@Mixin(BatRenderer.class)
public class MixinBatRenderer {

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/ambient/Bat;Lnet/minecraft/client/renderer/entity/state/BatRenderState;F)V", at = @At("RETURN"))
    private void onUpdateRenderStateTail(Bat batEntity, BatRenderState batEntityRenderState, float f, CallbackInfo ci) {
        if (!ModuleUtil.isEnable(GiantBat.class)) return;
        if (!DungeonUtil.isInDungeon()) return;

        float maxHealth = batEntity.getMaxHealth();
        if (maxHealth != 100f && maxHealth != 200f) return;

        float scale = ModuleUtil.getModule(GiantBat.class).scale.getValue();
        batEntityRenderState.scale *= scale;
    }
}
