package owo.pigeon.modules.impl.Combat;

import net.engio.mbassy.listener.Handler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import owo.pigeon.event.events.TickEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.IntSetting;
import owo.pigeon.utils.Chat.ChatUtil;
import owo.pigeon.utils.KeybindUtil;
import owo.pigeon.utils.WorldUtil;

import static owo.pigeon.Pigeon.mc;

public class AutoBow extends Module {
    public AutoBow() {
        super("AutoBow", Category.COMBAT);
    }

    public IntSetting power = setting("power", 20, 3, 20, "tick", v -> true);

    private boolean hasRelease;

    @Handler
    public void onTick(TickEvent.ClientTickEvent event) {
        if (WorldUtil.nullCheck()) return;

        if (event instanceof TickEvent.ClientTickEvent.Pre) {
            ItemStack itemStack = mc.player.getMainHandStack();
            if (itemStack.isEmpty()) return;
            if (itemStack.getItem() != Items.BOW) return;

            if (mc.player.isUsingItem()) {
                ChatUtil.sendDebugMessage(this.name, "itemUseTime: " + mc.player.getItemUseTime() + ", power: " + power.getValue());

                if (mc.player.getItemUseTime() >= power.getValue()) {
                    ChatUtil.sendDebugMessage(this.name, "Releasing bow at power " + mc.player.getItemUseTime());
                    KeybindUtil.setPressed(mc.options.useKey, false);
                    hasRelease = true;
                }
            }
        }

        if (event instanceof TickEvent.ClientTickEvent.Post) {
            if (hasRelease) {
                ChatUtil.sendDebugMessage(this.name, "Resetting use key, hasRelease: " + hasRelease);
                KeybindUtil.resetPressed(mc.options.useKey);
                hasRelease = false;
            }
        }
    }
}
