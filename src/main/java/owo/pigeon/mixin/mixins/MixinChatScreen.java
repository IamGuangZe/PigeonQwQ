package owo.pigeon.mixin.mixins;

import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import owo.pigeon.Pigeonqwq;
import owo.pigeon.commands.CommandManager;
import owo.pigeon.event.events.SendMessageEvent;
import owo.pigeon.utils.Chat.ChatUtil;

@Mixin(ChatScreen.class)
public abstract class MixinChatScreen {

    @Shadow
    public abstract void sendMessage(String chatText, boolean addToHistory);

    @Inject(method = "sendMessage", at = @At("HEAD"), cancellable = true)
    public void onSendMessagePre(String chatText, boolean addToHistory, CallbackInfo ci) {
        if (CommandManager.isSay) {
            ChatUtil.sendDebugMessage("MixinChatScreen", "return because say command");
            CommandManager.isSay = false;
            return;
        }

        SendMessageEvent event = new SendMessageEvent(chatText);
        Pigeonqwq.EVENT_BUS.post(event).now();
        ChatUtil.sendDebugMessage("MixinChatScreen", "EVENT_BUS post SendMessageEvent");

        if (event.isCancelled()) {

            ChatUtil.sendDebugMessage("MixinChatScreen", "sendMessage cancel");

            ci.cancel();
            return;
        }

        if (event.isMessageModified()) {
            String modifiedMessage = event.getMessage();
            if (modifiedMessage != null && !modifiedMessage.isEmpty()) {
                ci.cancel();
                this.sendMessage(modifiedMessage, addToHistory);
            }
        }
    }
}
