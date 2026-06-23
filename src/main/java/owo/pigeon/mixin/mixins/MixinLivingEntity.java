package owo.pigeon.mixin.mixins;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import owo.pigeon.modules.impl.movement.NoJumpDelay;
import owo.pigeon.utils.ModuleUtil;

@Mixin(LivingEntity.class)
public class MixinLivingEntity {
    @Shadow
    private int noJumpDelay;

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void onAiStep(CallbackInfo ci) {
        if (ModuleUtil.isEnable(NoJumpDelay.class)) noJumpDelay = 0;
    }
}
