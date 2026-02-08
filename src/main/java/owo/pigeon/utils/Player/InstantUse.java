package owo.pigeon.utils.Player;

import net.engio.mbassy.listener.Handler;
import owo.pigeon.event.events.TickEvent;
import owo.pigeon.utils.WorldUtil;

import static owo.pigeon.Pigeon.mc;

public class InstantUse {
    private static Integer inputSlot;
    private static Integer rawSlot;
    private static PlayerUtil.RightClickMode clickMode;

    public static void instantUseItem(int slot, PlayerUtil.RightClickMode mode) {
        if (slot < 0 || slot > 8) return;
        if (WorldUtil.nullCheck()) return;
        inputSlot = slot;
        clickMode = mode;
    }

    @Handler
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event instanceof TickEvent.ClientTickEvent.Pre && inputSlot != null && clickMode != null) {
            rawSlot = mc.player.getInventory().getSelectedSlot();
            mc.player.getInventory().setSelectedSlot(inputSlot);

            PlayerUtil.RightClick(clickMode);

            inputSlot = null;
            clickMode = null;
        }

        if (event instanceof TickEvent.ClientTickEvent.Post && rawSlot != null) {
            mc.player.getInventory().setSelectedSlot(rawSlot);
            rawSlot = null;
        }
    }
}
