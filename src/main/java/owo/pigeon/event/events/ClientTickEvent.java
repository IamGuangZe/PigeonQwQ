package owo.pigeon.event.events;

import owo.pigeon.event.Event;

public class ClientTickEvent extends Event {
    public ClientTickEvent() {

    }

    public static class Pre extends ClientTickEvent {
        public Pre() {}
    }

    public static class Post extends ClientTickEvent {
        public Post() {}
    }
}
