package owo.pigeon.mixin.mixins;

import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import owo.pigeon.Pigeon;
import owo.pigeon.event.events.MessageEvent;

@Mixin(MessageHandler.class)
public class MixinMessageHandler {
    @Inject(method = "onGameMessage", at = @At("HEAD"), cancellable = true)
    private void onGameMessage(Text message, boolean overlay, CallbackInfo ci) {
        // ChatUtil.sendDebugMessage("MixinMessageHandler","Game Message: " + message.getString());

        MessageEvent.ReceiveMessageEvent event = new MessageEvent.ReceiveMessageEvent(message);
        Pigeon.EVENT_BUS.post(event).now();

        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
