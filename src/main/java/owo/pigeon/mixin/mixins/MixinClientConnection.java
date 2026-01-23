package owo.pigeon.mixin.mixins;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import owo.pigeon.Pigeonqwq;
import owo.pigeon.event.events.PacketEvent;

@Mixin(ClientConnection.class)
public class MixinClientConnection {

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V", at=@At("HEAD"), cancellable = true)
    public void onChannelRead0Pre (ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
        PacketEvent.ReceivePacketEvent receivePacketEvent = new PacketEvent.ReceivePacketEvent(packet);
        Pigeonqwq.EVENT_BUS.post(receivePacketEvent).now();
        if (receivePacketEvent.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;Lio/netty/channel/ChannelFutureListener;Z)V", at = @At("HEAD"), cancellable = true)
    public void onSendPre(Packet<?> packet, @Nullable ChannelFutureListener channelFutureListener, boolean flush, CallbackInfo ci) {
        PacketEvent.SendPacketEvent sendPacketEvent = new PacketEvent.SendPacketEvent(packet);
        Pigeonqwq.EVENT_BUS.post(sendPacketEvent).now();
        if (sendPacketEvent.isCancelled()) {
            ci.cancel();
        }
    }
}
