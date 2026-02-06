package owo.pigeon.mixin.mixins;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import owo.pigeon.modules.impl.Movement.NoJumpDelay;
import owo.pigeon.utils.ModuleUtil;

@Mixin(LivingEntity.class)
public class MixinLivingEntity {
    @Shadow
    private int jumpingCooldown;

    @Inject(method = "tickMovement", at = @At("HEAD"))
    public void onTickMovement(CallbackInfo ci) {
        if (ModuleUtil.isEnable(NoJumpDelay.class)) jumpingCooldown = 0;
    }
}
