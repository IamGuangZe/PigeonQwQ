package owo.pigeon.mixin.skyblocker;

import de.hysky.skyblocker.skyblock.garden.LowerSensitivity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import owo.pigeon.modules.impl.skyblock.farming.RotationLock;
import owo.pigeon.utils.ModuleUtil;

@Mixin(LowerSensitivity.class)
public class MixinLowerSensitivity {

    @Unique
    private static boolean pigeon$isRotationLockActive() {
        RotationLock rotationLock = ModuleUtil.getModule(RotationLock.class);
        return rotationLock != null && rotationLock.isEnable() && rotationLock.shouldLock();
    }

    @Inject(method = "isSensitivityLowered", at = @At("RETURN"), cancellable = true, remap = false)
    private static void pigeon$overrideFromRotationLock(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && pigeon$isRotationLockActive()) {
            cir.setReturnValue(true);
        }
    }
}
