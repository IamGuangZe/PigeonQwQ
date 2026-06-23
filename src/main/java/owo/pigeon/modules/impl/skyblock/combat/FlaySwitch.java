package owo.pigeon.modules.impl.skyblock.combat;

import net.engio.mbassy.listener.Handler;
import net.minecraft.world.item.ItemStack;
import owo.pigeon.event.events.ClientTickEvent;
import owo.pigeon.event.events.StartUseItemEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.IntSetting;
import owo.pigeon.settings.StringSetting;
import owo.pigeon.utils.ColorUtil;
import owo.pigeon.utils.ItemUtil;
import owo.pigeon.utils.WorldUtil;
import owo.pigeon.utils.chat.ChatUtil;
import owo.pigeon.utils.hypixel.skyblock.SkyblockUtil;
import owo.pigeon.utils.player.PlayerUtil;

import static owo.pigeon.Pigeon.mc;

public class FlaySwitch extends Module {
    public FlaySwitch() {
        super("FlaySwitch", Category.COMBAT);
    }

    public StringSetting weaponName = setting("weapon-name", "Figstone Splitter", v -> true);
    public EnableSetting switchBack = setting("switch-back", false, v -> true);
    public IntSetting switchBackDelay = setting("switch-back-delay", 15, 1, 20, "tick", v -> switchBack.getValue());
    public EnableSetting onlyInGalatea = setting("only-in-galatea", true, v -> true);

    private int rawSlot = 0;
    private int delay = 21;

    @Handler
    public void onTickPre(ClientTickEvent.Pre event) {
        if (WorldUtil.nullCheck()) return;

        if (delay <= switchBackDelay.getMaxValue()) delay++;
        if (delay == switchBackDelay.getValue()) PlayerUtil.switchItemSlot(rawSlot);
    }

    @Handler
    public void onStartUseItemPost(StartUseItemEvent.Post event) {
        if (onlyInGalatea.getValue() && !SkyblockUtil.isInIsland(SkyblockUtil.Island.GALATEA))
            return;

        ItemStack stack = mc.player.getMainHandItem();
        if (stack.isEmpty()) return;

        String name = ColorUtil.removeColor(stack.getHoverName().getString());
        ChatUtil.sendDebugMessage(this.name, "holding: " + name);
        if (!name.contains("Soul Whip") && !name.contains("Flaming Flay")) return;

        rawSlot = mc.player.getInventory().getSelectedSlot();
        int weaponSlot = ItemUtil.getSlotFromItemName(weaponName.getValue(), true);

        ChatUtil.sendDebugMessage(this.name, String.valueOf(weaponSlot));
        if (weaponSlot == -1) return;

        PlayerUtil.switchItemSlot(weaponSlot);
        if (switchBack.getValue()) delay = 0;
    }

    @Override
    public void onEnable() {
        delay = switchBackDelay.getMaxValue() + 1;
    }
}
