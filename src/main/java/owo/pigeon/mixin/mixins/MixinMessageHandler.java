package owo.pigeon.mixin.mixins;

import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import owo.pigeon.Pigeon;
import owo.pigeon.event.events.MessageEvent;

@Mixin(ChatListener.class)
public class MixinMessageHandler {
    @Inject(method = "handleSystemMessage", at = @At("HEAD"), cancellable = true)
    private void onGameMessage(Component message, boolean overlay, CallbackInfo ci) {
        // ChatUtil.sendDebugMessage("MixinMessageHandler","Game Message: " + message.getString() + ", Overlay: " + overlay);

        MessageEvent.ReceiveMessageEvent event = new MessageEvent.ReceiveMessageEvent(message, overlay);
        Pigeon.EVENT_BUS.post(event).now();

        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
