package owo.pigeon.event.events;

import owo.pigeon.event.CancellableEvent;

public class SendMessageEvent extends CancellableEvent {

    private String message;
    private boolean messageModified;

    public SendMessageEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
        messageModified = true;
    }

    public boolean isMessageModified() {
        return messageModified;
    }
}
