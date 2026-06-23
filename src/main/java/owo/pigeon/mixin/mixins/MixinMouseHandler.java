package owo.pigeon.mixin.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import owo.pigeon.modules.impl.render.FreeLook;
import owo.pigeon.modules.impl.skyblock.farming.RotationLock;
import owo.pigeon.utils.ModuleUtil;

@Mixin(MouseHandler.class)
public class MixinMouseHandler {
    @ModifyExpressionValue(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;", ordinal = 0))
    private Object onUpdateMouse(Object original) {
        if (ModuleUtil.isEnable(RotationLock.class) && ModuleUtil.getModule(RotationLock.class).shouldLock() && !ModuleUtil.getModule(FreeLook.class).freelooking) {
            return -1 / 3d;
        } else {
            return original;
        }
    }
}
