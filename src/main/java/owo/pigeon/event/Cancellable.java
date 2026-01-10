package owo.pigeon.event;

public interface Cancellable {

    boolean isCancelled();

    void setCancelled(boolean cancelled);
}
