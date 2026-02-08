package owo.pigeon.mixin.mixins;

import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import owo.pigeon.modules.impl.Render.ModifyCamera;
import owo.pigeon.utils.ModuleUtil;

@Mixin(Camera.class)
public class MixinCamera {
    @ModifyVariable(method = "clipToSpace", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float onClipToSpace(float value) {
        return ModuleUtil.isEnable(ModifyCamera.class) ? ModuleUtil.getModule(ModifyCamera.class).distance.getValue() : 4.0f;
    }

    @Inject(method = "clipToSpace", at = @At("HEAD"), cancellable = true)
    private void onClipToSpace(float f, CallbackInfoReturnable<Float> cir) {
        if (ModuleUtil.isEnable(ModifyCamera.class) && ModuleUtil.getModule(ModifyCamera.class).camNoClip.getValue())
            cir.setReturnValue(f);
    }
}
