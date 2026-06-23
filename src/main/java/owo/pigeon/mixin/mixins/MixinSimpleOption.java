package owo.pigeon.mixin.mixins;

import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionInstance.class)
public class MixinSimpleOption<T> {
    @Shadow
    @Final
    private Component caption;

    @Shadow
    private T value;

    @Inject(method = "set", at = @At("HEAD"), cancellable = true)
    private void onSetValue(T value, CallbackInfo ci) {
        // ChatUtil.sendDebugMessage("MixinSimpleOption",text.getString() + " / " + text.getContent().toString() + " / " +this.text.getString());

        if (caption.getContents().toString().contains("options.gamma")) {
            this.value = value;
            ci.cancel();
        }
    }
}
