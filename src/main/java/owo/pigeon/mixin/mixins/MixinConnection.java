package owo.pigeon.mixin.mixins;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import owo.pigeon.Pigeon;
import owo.pigeon.event.events.PacketEvent;

@Mixin(Connection.class)
public class MixinConnection {
    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void onChannelRead0Pre(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
        PacketEvent.ReceivePacketEvent.Pre receivePacketEvent = new PacketEvent.ReceivePacketEvent.Pre(packet);
        Pigeon.EVENT_BUS.post(receivePacketEvent).now();
        if (receivePacketEvent.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V", at = @At("RETURN"))
    private void onChannelRead0Post(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
        Pigeon.EVENT_BUS.post(new PacketEvent.ReceivePacketEvent.Post(packet)).now();
    }

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;Z)V", at = @At("HEAD"), cancellable = true)
    private void onSendPre(Packet<?> packet, @Nullable ChannelFutureListener channelFutureListener, boolean flush, CallbackInfo ci) {
        PacketEvent.SendPacketEvent sendPacketEvent = new PacketEvent.SendPacketEvent(packet);
        Pigeon.EVENT_BUS.post(sendPacketEvent).now();
        if (sendPacketEvent.isCancelled()) {
            ci.cancel();
        }
    }
}
