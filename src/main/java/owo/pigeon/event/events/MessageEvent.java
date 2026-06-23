package owo.pigeon.event.events;

import net.minecraft.network.chat.Component;
import owo.pigeon.event.CancellableEvent;

public class MessageEvent extends CancellableEvent {
    public enum Type {
        SEND, RECEIVE
    }

    private Component message;
    private final Type type;
    private boolean messageModified;

    public MessageEvent(Component message, Type type) {
        this.message = message;
        this.type = type;
    }

    public Component getMessage() {
        return this.message;
    }

    public void setMessage(Component message) {
        this.message = message;
        messageModified = true;
    }

    public Type getType() {
        return type;
    }

    public boolean isMessageModified() {
        return messageModified;
    }

    public static class SendChatEvent extends MessageEvent {
        public SendChatEvent(Component message) {
            super(message, Type.SEND);
        }
    }

    public static class ReceiveMessageEvent extends MessageEvent {
        private final boolean overlay;

        public ReceiveMessageEvent(Component message, boolean overlay) {
            super(message, Type.RECEIVE);
            this.overlay = overlay;
        }

        public boolean isOverlay() {
            return overlay;
        }
    }
}
