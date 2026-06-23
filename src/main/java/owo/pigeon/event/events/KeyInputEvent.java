package owo.pigeon.event.events;

import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;
import owo.pigeon.event.Event;

public class KeyInputEvent extends Event {

    private final int action;
    private final KeyEvent keyInput;

    public KeyInputEvent(int action, KeyEvent keyInput) {
        this.action = action;
        this.keyInput = keyInput;
    }

    public int getKeyCode() {
        return keyInput.key();
    }

    public boolean isPressed() {
        return action == GLFW.GLFW_PRESS;
    }
}
