package owo.pigeon.event.events;

import owo.pigeon.event.CancellableEvent;

public class MessageEvent extends CancellableEvent {
    public enum Type {
        SEND, RECEIVE
    }

    private String message;
    private final Type type;
    private boolean messageModified;

    public MessageEvent(String message, Type type) {
        this.message = message;
        this.type = type;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
        messageModified = true;
    }

    public Type getType() {
        return type;
    }

    public boolean isMessageModified() {
        return messageModified;
    }

    public static class SendMessageEvent extends MessageEvent {
        public SendMessageEvent(String message) {
            super(message, Type.SEND);
        }
    }

    public static class ReceiveMessageEvent extends MessageEvent {
        public ReceiveMessageEvent(String message) {
            super(message, Type.RECEIVE);
        }
    }
}
