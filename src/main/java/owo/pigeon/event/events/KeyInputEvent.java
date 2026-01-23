package owo.pigeon.event.events;

import net.minecraft.client.input.KeyInput;
import org.lwjgl.glfw.GLFW;
import owo.pigeon.event.Event;

public class KeyInputEvent extends Event {

    private final int action;
    private final KeyInput keyInput;

    public KeyInputEvent(int action, KeyInput keyInput) {
        this.action = action;
        this.keyInput = keyInput;
    }

    public int getKeyCode () {
        return keyInput.key();
    }

    public boolean isPressed() {
        return action == GLFW.GLFW_PRESS;
    }
}
