package owo.pigeon.event.events;

import owo.pigeon.event.Event;

public class TickEvent extends Event {
    public enum Type {
        CLIENT, SERVER
    }

    public enum Phase {
        PRE, POST
    }

    private final Type type;
    private final Phase phase;

    public TickEvent(Type type, Phase phase) {
        this.type = type;
        this.phase = phase;
    }

    public Type getType() {
        return type;
    }

    public Phase getPhase() {
        return phase;
    }

    public static class ClientTickEvent extends TickEvent {
        public ClientTickEvent(Phase phase) {
            super(Type.CLIENT, phase);
        }

        public static class Pre extends ClientTickEvent {
            public Pre() { super(Phase.PRE); }
        }

        public static class Post extends ClientTickEvent {
            public Post() { super(Phase.POST); }
        }
    }

    public static class ServerTickEvent extends TickEvent {
        public ServerTickEvent(Phase phase) {
            super(Type.SERVER, phase);
        }

        public static class Pre extends ServerTickEvent {
            public Pre() { super(Phase.PRE); }
        }

        public static class Post extends ServerTickEvent {
            public Post() { super(Phase.POST); }
        }
    }
}
