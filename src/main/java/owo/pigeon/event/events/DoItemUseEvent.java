package owo.pigeon.event.events;

import owo.pigeon.event.CancellableEvent;

public class DoItemUseEvent extends CancellableEvent {
    public DoItemUseEvent() {

    }

    public static class Pre extends DoItemUseEvent {
        public Pre() {}
    }

    public static class Post extends DoItemUseEvent {
        public Post() {}
    }
}
