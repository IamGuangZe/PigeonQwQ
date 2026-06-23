package owo.pigeon.event.events;

import owo.pigeon.event.CancellableEvent;

public class StartAttackEvent extends CancellableEvent {
    public StartAttackEvent() {

    }

    public static class Pre extends StartAttackEvent {
        public Pre() {
        }
    }

    public static class Post extends StartAttackEvent {
        public Post() {
        }
    }
}
