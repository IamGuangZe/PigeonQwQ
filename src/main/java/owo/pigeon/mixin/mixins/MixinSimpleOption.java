package owo.pigeon.mixin.mixins;

import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import owo.pigeon.utils.Chat.ChatUtil;

@Mixin(SimpleOption.class)
public class MixinSimpleOption<T> {
    @Shadow
    @Final
    private Text text;

    @Shadow
    private T value;

    @Inject(method = "setValue", at = @At("HEAD"), cancellable = true)
    public void onSetValue(T value, CallbackInfo ci) {
        ChatUtil.sendDebugMessage("MixinSimpleOption",text.getString() + " / " + text.getContent().toString() + " / " +this.text.getString());

        if (text.getContent().toString().contains("options.gamma")) {
            this.value = value;
            ci.cancel();
        }
    }
}
