package owo.pigeon.utils;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;
import owo.pigeon.mixin.accessors.IAccessorKeyMapping;

import static owo.pigeon.Pigeon.mc;

public class KeybindUtil {
    public static void onPressed(KeyMapping key) {
        if (mc.screen != null) return;
        KeyMapping.click(((IAccessorKeyMapping) key).pigeon$getBoundKey());
    }

    public static void setPressed(KeyMapping key, boolean pressed) {
        if (mc.screen != null) return;
        if (key.isUnbound()) return;
        key.setDown(pressed);
    }

    public static boolean isPressed(KeyMapping key) {
        if (mc.getWindow() == null || key.isUnbound()) return false;
        return isPressed(((IAccessorKeyMapping) key).pigeon$getBoundKey());
    }

    public static boolean isPressed(int keyCode) {
        if (mc.getWindow() == null || keyCode == InputConstants.UNKNOWN.getValue()) return false;
        return InputConstants.isKeyDown(mc.getWindow(), keyCode);
    }

    public static boolean isPressed(InputConstants.Key boundKey) {
        if (mc.getWindow() == null || boundKey == InputConstants.UNKNOWN) return false;

        if (boundKey.getType() == InputConstants.Type.MOUSE) {
            return GLFW.glfwGetMouseButton(mc.getWindow().handle(), boundKey.getValue()) == GLFW.GLFW_PRESS;
        } else {
            return InputConstants.isKeyDown(mc.getWindow(), boundKey.getValue());
        }
    }

    public static void resetPressed(KeyMapping key) {
        if (mc.screen != null) return;
        setPressed(key, isPressed(key));
    }

    public static String getKeyDisplayName(int keyCode) {
        if (keyCode == -1) return "NONE";
        return InputConstants.Type.KEYSYM
                .getOrCreate(keyCode)
                .getName()
                .replace("key.keyboard.", "")
                .toUpperCase();
    }
}
