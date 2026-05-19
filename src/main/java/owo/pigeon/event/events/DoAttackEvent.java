package owo.pigeon.event.events;

import owo.pigeon.event.CancellableEvent;

public class DoAttackEvent extends CancellableEvent {
    public DoAttackEvent() {

    }

    public static class Pre extends DoAttackEvent {
        public Pre() {
        }
    }

    public static class Post extends DoAttackEvent {
        public Post() {
        }
    }
}
