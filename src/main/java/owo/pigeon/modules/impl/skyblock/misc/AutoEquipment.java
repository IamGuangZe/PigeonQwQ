package owo.pigeon.modules.impl.skyblock.misc;

import net.engio.mbassy.listener.Handler;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import owo.pigeon.event.events.ClientTickEvent;
import owo.pigeon.event.events.KeyInputEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.IntSetting;
import owo.pigeon.settings.KeySetting;
import owo.pigeon.settings.StringSetting;
import owo.pigeon.utils.ItemUtil;
import owo.pigeon.utils.WorldUtil;
import owo.pigeon.utils.chat.ChatUtil;
import owo.pigeon.utils.player.PlayerUtil;

import static owo.pigeon.Pigeon.mc;

public class AutoEquipment extends Module {
    public AutoEquipment() {
        super("AutoEquipment", Category.MISC);
    }

    public KeySetting bonzoMask = setting("bonzo-mask", -1, v -> true);
    public KeySetting spiritMask = setting("spirit-mask", -1, v -> true);
    public KeySetting davidCloak = setting("david-cloak", -1, v -> true);
    public StringSetting custom = setting("custom", "Golden Necron Head", v -> true);
    public KeySetting customKey = setting("custom-key", -1, v -> true);
    public IntSetting delay = setting("delay", 5, 0, 20, "tick", v -> true);

    private final String BONZO_MASK = "Bonzo's Mask";
    private final String SPIRIT_MASK = "Spirit Mask";
    private final String DAVID_CLOAK = "David's Cloak";
    private int ticksOpened;
    private Integer targetSlot;
    private boolean isWaitingToClose;

    @Override
    public void onEnable() {
        targetSlot = null;
        isWaitingToClose = false;
        ticksOpened = 0;
    }

    @Handler
    public void onKeyInput(KeyInputEvent event) {
        if (mc.screen != null) return;
        int keyCode = event.getKeyCode();
        if (event.isPressed()) {

            String itemName = null;
            if (keyCode == bonzoMask.getValue()) itemName = BONZO_MASK;
            else if (keyCode == spiritMask.getValue()) itemName = SPIRIT_MASK;
            else if (keyCode == davidCloak.getValue()) itemName = DAVID_CLOAK;
            else if (keyCode == customKey.getValue()) itemName = custom.getValue();

            if (itemName != null) {
                int slot = ItemUtil.getSlotFromItemName(itemName, false);
                if (slot == -1) {
                    ChatUtil.sendMessage(this.name, "Specified item not found: " + itemName);
                    return;
                }

                targetSlot = slot;
                ticksOpened = 0;
                mc.execute(() -> mc.player.connection.sendChat("/eq"));
            }
        }
    }

    @Handler
    public void onTick(ClientTickEvent event) {
        if (WorldUtil.nullCheck()) return;

        if (event instanceof ClientTickEvent.Pre) {
            if (targetSlot == null) return;
            if (mc.player.containerMenu instanceof ChestMenu containerScreen) {
                String title = mc.screen.getTitle().getString();
                if (title.equals("Your Equipment and Stats")) {

                    if (ticksOpened < delay.getValue()) {
                        ticksOpened++;
                        return;
                    }

                    int containerSize = containerScreen.slots.size() - 36;

                    int slotId = (targetSlot < 9)
                            ? containerSize + 27 + targetSlot
                            : containerSize + (targetSlot - 9);

                    PlayerUtil.clickSlot(containerScreen.containerId, slotId, 0, ClickType.QUICK_MOVE);
                    targetSlot = null;
                    isWaitingToClose = true;
                    ticksOpened = 0;
                }
            } else ticksOpened = 0;
        }

        if (event instanceof ClientTickEvent.Post) {
            if (isWaitingToClose && mc.player.containerMenu instanceof ChestMenu) {
                mc.player.closeContainer();
                isWaitingToClose = false;
            }
        }
    }
}
