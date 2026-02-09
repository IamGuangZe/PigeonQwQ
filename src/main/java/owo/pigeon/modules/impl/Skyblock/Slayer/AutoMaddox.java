package owo.pigeon.modules.impl.Skyblock.Slayer;

import net.engio.mbassy.listener.Handler;
import owo.pigeon.event.events.MessageEvent;
import owo.pigeon.event.events.TickEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.IntSetting;
import owo.pigeon.settings.ModeSetting;
import owo.pigeon.utils.ItemUtil;
import owo.pigeon.utils.Player.PlayerUtil;
import owo.pigeon.utils.WorldUtil;

import static owo.pigeon.Pigeon.mc;

public class AutoMaddox extends Module {
    public AutoMaddox() {
        super("AutoMaddox", Category.SKYBLOCK);
    }

    public enum CallMode {
        CALL, BATPHONE
    }

    public enum SwitchMode {
        NORMAL, INSTANT
    }

    public ModeSetting<CallMode> callMode = setting("call-mode", CallMode.BATPHONE, v -> true);
    public ModeSetting<PlayerUtil.RightClickMode> clickMode = setting("click-mode", PlayerUtil.RightClickMode.MOUSE, v -> callMode.getValue() == CallMode.BATPHONE);
    public ModeSetting<SwitchMode> switchMode = setting("switch-mode", SwitchMode.NORMAL, v -> callMode.getValue() == CallMode.BATPHONE);
    public IntSetting switchTick = setting("switch-tick", 2, 1, 20, "tick", v -> callMode.getValue() == CallMode.BATPHONE && switchMode.getValue() == SwitchMode.NORMAL);

    private int s_tick;
    private int rawSlot = -1;

    @Handler
    public void onTick(TickEvent.ClientTickEvent.Post event) {
        if (WorldUtil.nullCheck()) return;

        if (rawSlot != -1) {
            if (s_tick <= 0) {
                PlayerUtil.switchItemSlot(rawSlot);
                rawSlot = -1;
            } else {
                s_tick--;
            }
        }
    }

    @Handler
    public void onReceiveMessage(MessageEvent.ReceiveMessageEvent event) {
        //   » Talk to Maddox to claim your Zombie Slayer XP!
        if (event.getMessage().matches(" {3}» Talk to Maddox to claim your (.*) Slayer XP!")) {
            switch (callMode.getValue()) {
                case CALL -> {
                    mc.player.networkHandler.sendChatMessage("/call maddox");
                }

                case BATPHONE -> {
                    int slot = ItemUtil.getSlotFromItemName("Maddox ");
                    if (slot == -1) return;

                    switch (switchMode.getValue()) {
                        case NORMAL -> {
                            s_tick = switchTick.getValue();
                            rawSlot = mc.player.getInventory().getSelectedSlot();
                            PlayerUtil.switchUseItem(slot, clickMode.getValue());
                        }

                        case INSTANT -> {
                            PlayerUtil.InstantUseItem(slot, clickMode.getValue());
                        }
                    }

                }
            }
        }
    }
}
