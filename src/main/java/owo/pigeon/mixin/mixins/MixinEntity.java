package owo.pigeon.mixin.mixins;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import owo.pigeon.modules.impl.Combat.HitBox;
import owo.pigeon.utils.ModuleUtil;

@Mixin(Entity.class)
public class MixinEntity {
    @Inject(method = "getTargetingMargin", at = @At("RETURN"), cancellable = true)
    private void onGetTargetingMargin(CallbackInfoReturnable<Float> cir) {
        if (ModuleUtil.isEnable(HitBox.class))
            cir.setReturnValue(ModuleUtil.getModule(HitBox.class).expand.getValue());
    }
}
