package owo.pigeon.mixin.mixins;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PingDebugMonitor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.ping.ClientboundPongResponsePacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import owo.pigeon.Pigeon;
import owo.pigeon.commands.CommandManager;
import owo.pigeon.event.events.MessageEvent;
import owo.pigeon.event.events.WorldChangeEvent;
import owo.pigeon.utils.chat.ChatUtil;
import owo.pigeon.utils.world.ServerUtil;

@Mixin(ClientPacketListener.class)
public abstract class MixinClientPacketListener {

    @Shadow
    public abstract void sendChat(String content);

    @Shadow
    private ClientLevel level;

    @Final
    @Shadow
    private PingDebugMonitor pingDebugMonitor;

    @Inject(method = "sendChat", at = @At("HEAD"), cancellable = true)
    private void onSendChatPre(String content, CallbackInfo ci) {
        ChatUtil.sendDebugMessage("MixinClientPacketListener", "Message: " + content);

        if (CommandManager.isSay) {
            ChatUtil.sendDebugMessage("MixinClientPacketListener", "return because say command");
            CommandManager.isSay = false;
            return;
        }

        MessageEvent.SendChatEvent event = new MessageEvent.SendChatEvent(Component.nullToEmpty(content));
        Pigeon.EVENT_BUS.post(event).now();

        if (event.isCancelled()) {

            ChatUtil.sendDebugMessage("MixinClientPacketListener", "sendMessage cancel");

            ci.cancel();
            return;
        }

        if (event.isMessageModified()) {
            Component modifiedMessage = event.getMessage();
            if (modifiedMessage != null && !modifiedMessage.getString().isEmpty()) {
                ci.cancel();
                this.sendChat(modifiedMessage.getString());
            }
        }
    }

    @Inject(method = "handleLogin", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setLevel(Lnet/minecraft/client/multiplayer/ClientLevel;)V", shift = At.Shift.AFTER))
    private void onHandleLogin(ClientboundLoginPacket packet, CallbackInfo ci) {
        if (level == null) return;
        Pigeon.EVENT_BUS.post(new WorldChangeEvent()).now();
    }

    @Inject(method = "handleSetTime", at = @At("RETURN"))
    private void onHandleSetTime(ClientboundSetTimePacket packet, CallbackInfo ci) {
        ServerUtil.onTimeUpdate();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTickTail(CallbackInfo ci) {
        this.pingDebugMonitor.tick();
    }

    @Inject(method = "handlePongResponse", at = @At("TAIL"))
    private void onHandlePongResponse(ClientboundPongResponsePacket packet, CallbackInfo ci) {
        ServerUtil.onPongResponse(packet.time());
    }
}
