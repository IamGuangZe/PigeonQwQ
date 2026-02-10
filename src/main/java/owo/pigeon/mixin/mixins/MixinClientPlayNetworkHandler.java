package owo.pigeon.mixin.mixins;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import owo.pigeon.Pigeon;
import owo.pigeon.commands.CommandManager;
import owo.pigeon.event.events.MessageEvent;
import owo.pigeon.utils.Chat.ChatUtil;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {

    @Shadow
    public abstract void sendChatMessage(String content);

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void onSendMessagePre(String content, CallbackInfo ci) {
        ChatUtil.sendDebugMessage("MixinClientPlayNetworkHandler", "Message: " + content);

        if (CommandManager.isSay) {
            ChatUtil.sendDebugMessage("MixinClientPlayNetworkHandler", "return because say command");
            CommandManager.isSay = false;
            return;
        }

        MessageEvent.SendMessageEvent event = new MessageEvent.SendMessageEvent(Text.of(content));
        Pigeon.EVENT_BUS.post(event).now();

        if (event.isCancelled()) {

            ChatUtil.sendDebugMessage("MixinClientPlayNetworkHandler", "sendMessage cancel");

            ci.cancel();
            return;
        }

        if (event.isMessageModified()) {
            Text modifiedMessage = event.getMessage();
            if (modifiedMessage != null && !modifiedMessage.getString().isEmpty()) {
                ci.cancel();
                this.sendChatMessage(modifiedMessage.getString());
            }
        }
    }
}
