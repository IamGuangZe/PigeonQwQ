package owo.pigeon.modules.impl.misc;

import net.engio.mbassy.listener.Handler;
import owo.pigeon.event.events.TickEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.IntSetting;
import owo.pigeon.settings.StringSetting;
import owo.pigeon.utils.WorldUtil;

import static owo.pigeon.Pigeon.mc;

public class Spammer extends Module {
    public Spammer() {
        super("Spammer", Category.MISC);
    }

    public IntSetting delay = setting("delay", 5, 1, 120, "s", v -> true);
    public StringSetting message = setting("message", "/pc Ciallo～ (∠・ω< )⌒★", v -> true);

    private int tick;
    private int previousDelay;

    @Override
    public void onEnable() {
        tick = 0;
        previousDelay = delay.getValue();
    }

    @Handler
    public void onTickPost(TickEvent.ClientTickEvent.Post event) {
        if (WorldUtil.nullCheck()) return;

        if (delay.getValue() != previousDelay) {
            tick = 0;
            previousDelay = delay.getValue();
        }

        tick++;

        if (tick >= delay.getValue() * 20) {
            mc.player.networkHandler.sendChatMessage(message.getValue());
            tick = 0;
        }
    }
}
