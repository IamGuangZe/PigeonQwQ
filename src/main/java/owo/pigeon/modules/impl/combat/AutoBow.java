package owo.pigeon.modules.impl.combat;

import net.engio.mbassy.listener.Handler;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import owo.pigeon.event.events.ClientTickEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.IntSetting;
import owo.pigeon.utils.KeybindUtil;
import owo.pigeon.utils.WorldUtil;
import owo.pigeon.utils.chat.ChatUtil;

import static owo.pigeon.Pigeon.mc;

public class AutoBow extends Module {
    public AutoBow() {
        super("AutoBow", Category.COMBAT);
    }

    public IntSetting power = setting("power", 20, 3, 20, "tick", v -> true);

    private boolean hasRelease;

    @Handler
    public void onTick(ClientTickEvent event) {
        if (WorldUtil.nullCheck()) return;

        if (event instanceof ClientTickEvent.Pre) {
            ItemStack itemStack = mc.player.getMainHandItem();
            if (itemStack.isEmpty()) return;
            if (itemStack.getItem() != Items.BOW) return;

            if (mc.player.isUsingItem()) {
                ChatUtil.sendDebugMessage(this.name, "itemUseTime: " + mc.player.getTicksUsingItem() + ", power: " + power.getValue());

                if (mc.player.getTicksUsingItem() >= power.getValue()) {
                    ChatUtil.sendDebugMessage(this.name, "Releasing bow at power " + mc.player.getTicksUsingItem());
                    KeybindUtil.setPressed(mc.options.keyUse, false);
                    hasRelease = true;
                }
            }
        }

        if (event instanceof ClientTickEvent.Post) {
            if (hasRelease) {
                ChatUtil.sendDebugMessage(this.name, "Resetting use key, hasRelease: " + hasRelease);
                KeybindUtil.resetPressed(mc.options.keyUse);
                hasRelease = false;
            }
        }
    }
}
