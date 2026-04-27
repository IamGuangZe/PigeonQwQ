package owo.pigeon.mixin.mixins;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PingMeasurer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.network.packet.s2c.query.PingResultS2CPacket;
import net.minecraft.text.Text;
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

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {

    @Shadow
    public abstract void sendChatMessage(String content);

    @Shadow
    private ClientWorld world;

    @Shadow
    private PingMeasurer pingMeasurer;

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

    @Inject(method = "onGameJoin", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;joinWorld(Lnet/minecraft/client/world/ClientWorld;)V", shift = At.Shift.AFTER))
    private void onJoinWorld(GameJoinS2CPacket packet, CallbackInfo ci) {
        if (world == null) return;
        Pigeon.EVENT_BUS.post(new WorldChangeEvent()).now();
    }

    @Inject(method = "onWorldTimeUpdate", at = @At("RETURN"))
    private void onWorldTimeUpdate(WorldTimeUpdateS2CPacket packet, CallbackInfo ci) {
        ServerUtil.onTimeUpdate();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void alwaysSendPing(CallbackInfo ci) {
        this.pingMeasurer.ping();
    }

    @Inject(method = "onPingResult", at = @At("TAIL"))
    private void onPingResult(PingResultS2CPacket packet, CallbackInfo ci) {
        ServerUtil.onPongResponse(packet.startTime());
    }
}
