package owo.pigeon.modules.impl.Movement;

import net.engio.mbassy.listener.Handler;
import owo.pigeon.event.events.TickEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.utils.KeybindUtil;
import owo.pigeon.utils.WorldUtil;

import static owo.pigeon.Pigeon.mc;

public class Sprint extends Module {
    public Sprint() {
        super("Sprint", Category.MOVEMENT);
    }

    @Handler
    public void onTickPost(TickEvent.ClientTickEvent.Post event) {
        if (WorldUtil.nullCheck()) return;
        KeybindUtil.setPressed(mc.options.sprintKey,true);
    }

    @Override
    public void onDisable() {
        KeybindUtil.resetPressed(mc.options.sprintKey);
    }
}
