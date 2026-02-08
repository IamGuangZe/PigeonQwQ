package owo.pigeon.mixin.mixins;

import net.minecraft.client.Keyboard;
import net.minecraft.client.input.KeyInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import owo.pigeon.Pigeon;
import owo.pigeon.event.events.KeyInputEvent;

@Mixin(Keyboard.class)
public class MixinKeyboard {
    @Inject(method = "onKey", at = @At("HEAD"))
    public void onKeyInputPre(long window, int action, KeyInput input, CallbackInfo ci) {
        if (input.key() != -1)
            Pigeon.EVENT_BUS.post(new KeyInputEvent(action, input)).now();
    }
}
