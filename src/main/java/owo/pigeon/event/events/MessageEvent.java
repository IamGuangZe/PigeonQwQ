package owo.pigeon.event.events;

import net.minecraft.text.Text;
import owo.pigeon.event.CancellableEvent;

public class MessageEvent extends CancellableEvent {
    public enum Type {
        SEND, RECEIVE
    }

    private Text message;
    private final Type type;
    private boolean messageModified;

    public MessageEvent(Text message, Type type) {
        this.message = message;
        this.type = type;
    }

    public Text getMessage() {
        return this.message;
    }

    public void setMessage(Text message) {
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
        public SendMessageEvent(Text message) {
            super(message, Type.SEND);
        }
    }

    public static class ReceiveMessageEvent extends MessageEvent {
        public ReceiveMessageEvent(Text message) {
            super(message, Type.RECEIVE);
        }
    }
}
