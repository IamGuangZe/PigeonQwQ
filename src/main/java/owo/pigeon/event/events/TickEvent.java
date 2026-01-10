package owo.pigeon.event.events;

import owo.pigeon.event.Event;

public class TickEvent extends Event {
    public enum Type {
        CLIENT, SERVER
    }

    public enum Phase {
        PRE, POST;
    }

    public final Type type;
    public final Phase phase;

    public TickEvent(Type type, Phase phase) {
        this.type = type;
        this.phase = phase;
    }

    public static class ClientTickEvent extends TickEvent {
        public ClientTickEvent(Phase phase) {
            super(Type.CLIENT, phase);
        }
    }

    public static class ServerTickEvent extends TickEvent {
        public ServerTickEvent(Phase phase) {
            super(Type.SERVER, phase);
        }
    }
}
