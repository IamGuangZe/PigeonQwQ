package owo.pigeon.modules.impl.Movement;

import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.utils.KeybindUtil;
import owo.pigeon.utils.WorldUtil;

import static owo.pigeon.Pigeonqwq.mc;

public class Sprint extends Module {
    public Sprint() {
        super("Sprint", Category.MOVEMENT);
    }

    @Override
    public void onTickPost() {
        if (WorldUtil.nullCheck()) return;
        KeybindUtil.setPressed(mc.options.sprintKey,true);
    }

    @Override
    public void onDisable() {
        if (!KeybindUtil.isPressed(mc.options.sprintKey)) {
            KeybindUtil.setPressed(mc.options.sprintKey,false);
        }
    }
}
