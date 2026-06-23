package owo.pigeon.event.events;

import owo.pigeon.event.CancellableEvent;

public class StartUseItemEvent extends CancellableEvent {
    public StartUseItemEvent() {

    }

    public static class Pre extends StartUseItemEvent {
        public Pre() {
        }
    }

    public static class Post extends StartUseItemEvent {
        public Post() {
        }
    }
}
