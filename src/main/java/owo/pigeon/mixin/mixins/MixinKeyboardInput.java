package owo.pigeon.mixin.mixins;

import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import owo.pigeon.Pigeon;
import owo.pigeon.event.events.MoveInputEvent;

@Mixin(KeyboardInput.class)
public class MixinKeyboardInput {

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTickTail(CallbackInfo ci) {
        ClientInput input = (ClientInput) (Object) this;
        Pigeon.EVENT_BUS.post(new MoveInputEvent(input)).now();
    }
}
