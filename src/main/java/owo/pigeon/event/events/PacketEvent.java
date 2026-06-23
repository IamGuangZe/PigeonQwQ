package owo.pigeon.event.events;

import net.minecraft.network.protocol.Packet;
import owo.pigeon.event.CancellableEvent;

public class PacketEvent extends CancellableEvent {
    public enum Type {
        SEND, RECEIVE
    }

    private final Packet<?> packet;
    private final Type type;

    public PacketEvent(Packet<?> packet, Type type) {
        this.packet = packet;
        this.type = type;
    }

    public Packet<?> getPacket() {
        return packet;
    }

    public Type getType() {
        return type;
    }

    public static class SendPacketEvent extends PacketEvent {
        public SendPacketEvent(Packet<?> packet) {
            super(packet, Type.SEND);
        }
    }

    public static class ReceivePacketEvent extends PacketEvent {
        public ReceivePacketEvent(Packet<?> packet) {
            super(packet, Type.RECEIVE);
        }

        public static class Pre extends ReceivePacketEvent {
            public Pre(Packet<?> packet) {
                super(packet);
            }
        }

        public static class Post extends ReceivePacketEvent {
            public Post(Packet<?> packet) {
                super(packet);
            }
        }
    }
}
