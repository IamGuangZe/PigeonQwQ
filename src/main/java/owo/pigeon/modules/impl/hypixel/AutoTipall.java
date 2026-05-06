package owo.pigeon.modules.impl.hypixel;

import net.engio.mbassy.listener.Handler;
import owo.pigeon.event.events.ClientTickEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.IntSetting;
import owo.pigeon.utils.WorldUtil;
import owo.pigeon.utils.hypixel.HypixelUtil;

import static owo.pigeon.Pigeon.mc;

public class AutoTipall extends Module {
    public AutoTipall() {
        super("AutoTipall", Category.HYPIXEL);
    }

    public IntSetting interval = setting("interval", 15, 1, 60, "min", v -> true);

    private int tick;
    private int previousIntervalValue = 15;

    @Override
    public void onEnable() {
        tick = 0;
        previousIntervalValue = interval.getValue();
    }

    @Handler
    public void onTickPost(ClientTickEvent.Post event) {
        if (WorldUtil.nullCheck() || !HypixelUtil.isInHypixel()) return;

        if (interval.getValue() != previousIntervalValue) {
            tick = 0;
            previousIntervalValue = interval.getValue();
        }

        tick++;

        if (tick >= interval.getValue() * 60 * 20) {
            mc.player.networkHandler.sendChatCommand("tipall");
            tick = 0;
        }
    }
}
