package owo.pigeon.utils;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import owo.pigeon.mixin.accessors.IAccessorKeyBinding;

import static owo.pigeon.Pigeon.mc;

public class KeybindUtil {
    public static void onPressed(KeyBinding key) {
        if (mc.currentScreen != null) return;
        KeyBinding.onKeyPressed(((IAccessorKeyBinding) key).pigeon$getBoundKey());
    }

    public static void setPressed(KeyBinding key, boolean pressed) {
        if (mc.currentScreen != null) return;
        if (key.isUnbound()) return;
        key.setPressed(pressed);
    }

    public static boolean isPressed(KeyBinding key) {
        if (mc.getWindow() == null || key.isUnbound()) return false;
        return isPressed(((IAccessorKeyBinding) key).pigeon$getBoundKey());
    }

    public static boolean isPressed(int keyCode) {
        if (mc.getWindow() == null || keyCode == InputUtil.UNKNOWN_KEY.getCode()) return false;
        return InputUtil.isKeyPressed(mc.getWindow(), keyCode);
    }

    public static boolean isPressed(InputUtil.Key boundKey) {
        if (mc.getWindow() == null || boundKey == InputUtil.UNKNOWN_KEY) return false;

        if (boundKey.getCategory() == InputUtil.Type.MOUSE) {
            return GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), boundKey.getCode()) == GLFW.GLFW_PRESS;
        } else {
            return InputUtil.isKeyPressed(mc.getWindow(), boundKey.getCode());
        }
    }

    public static void resetPressed(KeyBinding key) {
        if (mc.currentScreen != null) return;
        setPressed(key, isPressed(key));
    }

    public static String getKeyDisplayName(int keyCode) {
        if (keyCode == -1) return "NONE";
        return InputUtil.Type.KEYSYM
                .createFromCode(keyCode)
                .getTranslationKey()
                .replace("key.keyboard.", "")
                .toUpperCase();
    }
}
