package owo.pigeon.modules.impl.debug;

import net.engio.mbassy.listener.Handler;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import owo.pigeon.event.events.ClickSlotEvent;
import owo.pigeon.event.events.ClientTickEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.IntSetting;
import owo.pigeon.utils.RandomUtil;
import owo.pigeon.utils.WorldUtil;
import owo.pigeon.utils.chat.ChatUtil;
import owo.pigeon.utils.player.PlayerUtil;

import static owo.pigeon.Pigeon.mc;

public class ClickSlotTest extends Module {
    public ClickSlotTest() {
        super("ClickSlotTest", Category.DEBUG);
    }

    public IntSetting minDelay = setting("min-delay", 2, 0, 20, v -> true);
    public IntSetting maxDelay = setting("max-delay", 3, 0, 20, v -> true);
    public IntSetting startDelay = setting("start-delay", 1, 0, 20, v -> true);

    private int s_delay = 0;
    private int p_delay = 0;

    @Handler
    public void onTickPost(ClientTickEvent.Post event) {
        if (WorldUtil.nullCheck()) return;
        if (mc.player.containerMenu instanceof ChestMenu containerScreen) {

            if (s_delay > 0) {
                s_delay--;
                p_delay = 0;
            }
            if (p_delay > 0) {
                p_delay--;
            }

            int size = containerScreen.getContainer().getContainerSize();

            if (p_delay <= 0) {
                for (int i = 0; i < size; i++) {
                    if (containerScreen.getSlot(i).getItem().isEmpty()) continue;

                    PlayerUtil.clickSlot(containerScreen.containerId, i, 0, ContainerInput.QUICK_MOVE);

                    if (p_delay != 0) break;
                }
            }
        } else {
            s_delay = startDelay.getValue();
            p_delay = 0;
        }
    }

    @Handler
    public void onClickSlot(ClickSlotEvent event) {
        ChatUtil.sendDebugMessage(this.name, "ClickSlotEvent");
        p_delay = RandomUtil.intRandom(minDelay.getValue(), maxDelay.getValue());
    }
}
