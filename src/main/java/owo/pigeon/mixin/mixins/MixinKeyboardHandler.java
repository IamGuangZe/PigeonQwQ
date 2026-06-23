package owo.pigeon.mixin.mixins;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import owo.pigeon.Pigeon;
import owo.pigeon.event.events.KeyInputEvent;

@Mixin(KeyboardHandler.class)
public class MixinKeyboardHandler {
    @Inject(method = "keyPress", at = @At("HEAD"))
    private void onKeyInputPre(long window, int action, KeyEvent input, CallbackInfo ci) {
        if (input.key() != -1)
            Pigeon.EVENT_BUS.post(new KeyInputEvent(action, input)).now();
    }
}
