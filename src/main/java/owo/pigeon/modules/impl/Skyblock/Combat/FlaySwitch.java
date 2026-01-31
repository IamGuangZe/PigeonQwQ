package owo.pigeon.modules.impl.Skyblock.Combat;

import net.engio.mbassy.listener.Handler;
import net.minecraft.item.ItemStack;
import owo.pigeon.event.events.DoItemUseEvent;
import owo.pigeon.event.events.TickEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.IntSetting;
import owo.pigeon.settings.StringSetting;
import owo.pigeon.utils.Chat.ChatUtil;
import owo.pigeon.utils.ColorUtil;
import owo.pigeon.utils.ItemUtil;
import owo.pigeon.utils.PlayerUtil;
import owo.pigeon.utils.WorldUtil;

import static owo.pigeon.Pigeonqwq.mc;

public class FlaySwitch extends Module {
    public FlaySwitch() {
        super("FlaySwitch", Category.SKYBLOCK);
    }

    public StringSetting weaponName = setting("weapon-name","Figstone Splitter",v -> true);
    public EnableSetting switchBack = setting("switch-back",false,v->true);
    public IntSetting switchBackDelay = setting("switch-back-delay",15,1,20,v-> switchBack.getValue());

    private int rawSlot = 0;
    private int delay = 21;

    @Handler
    public void onTickPre(TickEvent.ClientTickEvent.Pre event) {
        if (WorldUtil.nullCheck()) return;

        if (delay <= switchBackDelay.getValue()) delay ++;
        if (delay == switchBackDelay.getValue()) PlayerUtil.switchItemSlot(rawSlot);
    }

    @Handler
    public void onDoItemUsePost(DoItemUseEvent.Post event) {
        ItemStack stack = mc.player.getInventory().getSelectedStack();
        if (stack.isEmpty()) return;

        String name = ColorUtil.removeColor(stack.getName().getString());
        ChatUtil.sendDebugMessage(this.name, "holding: " + name);
        if (!name.contains("Soul Whip") && !name.contains("Flaming Flay")) return;

        rawSlot = mc.player.getInventory().getSelectedSlot();
        ChatUtil.sendDebugMessage(this.name, String.valueOf(ItemUtil.getSlotFromItemName(weaponName.getName())));
        PlayerUtil.switchItemSlot(ItemUtil.getSlotFromItemName(weaponName.getValue()));

        if (switchBack.getValue()) delay = 0;
    }

    @Override
    public void onEnable() {
        delay = switchBackDelay.getValue() + 1;
    }
}
