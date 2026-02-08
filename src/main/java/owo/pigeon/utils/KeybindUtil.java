package owo.pigeon.utils;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import owo.pigeon.mixin.accessors.IAccessorKeyBinding;

import static owo.pigeon.Pigeon.mc;

public class KeybindUtil {
    public static void onPressed(KeyBinding key) {
        KeyBinding.onKeyPressed(((IAccessorKeyBinding) key).getBoundKey());
    }

    public static void setPressed(KeyBinding key, boolean pressed) {
        if (key.isUnbound()) return;
        key.setPressed(pressed);
    }

    public static boolean isPressed(KeyBinding key) {
        if (mc.getWindow() == null || key.isUnbound()) return false;

        InputUtil.Key boundKey = ((IAccessorKeyBinding) key).getBoundKey();
        if (boundKey.getCategory() == InputUtil.Type.MOUSE) {
            return GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), boundKey.getCode()) == GLFW.GLFW_PRESS;
        } else {
            return InputUtil.isKeyPressed(mc.getWindow(), boundKey.getCode());
        }
    }

    public static void resetPressed(KeyBinding key) {
        setPressed(key, isPressed(key));
    }
}
