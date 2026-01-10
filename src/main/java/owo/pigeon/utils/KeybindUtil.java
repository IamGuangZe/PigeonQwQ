package owo.pigeon.utils;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import owo.pigeon.mixin.accessors.IAccessorKeyBinding;

import static owo.pigeon.Pigeonqwq.mc;

public class KeybindUtil {
    public static void setPressed(KeyBinding key, boolean pressed) {
        if (key.isUnbound()) return;
        key.setPressed(pressed);
    }

    public static boolean isPressed(KeyBinding key) {
        if (mc.getWindow() == null || key.isUnbound()) return false;
        return InputUtil.isKeyPressed(mc.getWindow(),((IAccessorKeyBinding) key).getBoundKey().getCode());
    }
}
