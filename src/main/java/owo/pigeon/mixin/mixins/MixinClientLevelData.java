package owo.pigeon.mixin.mixins;

import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import owo.pigeon.modules.impl.world.Environment;
import owo.pigeon.utils.ModuleUtil;

@Mixin(ClientLevel.ClientLevelData.class)
public class MixinClientLevelData {
    @Inject(method = "getDayTime", at = @At("HEAD"), cancellable = true)
    private void onGetTimeOfDay(CallbackInfoReturnable<Long> cir) {
        if (ModuleUtil.isEnable(Environment.class) && ModuleUtil.getModule(Environment.class).shouldModifyTime()) {
            long customTime = ModuleUtil.getModule(Environment.class).getTimeOfDay();
            if (customTime != -1) cir.setReturnValue(customTime);
        }
    }
}
