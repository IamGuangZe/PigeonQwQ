package owo.pigeon.modules.impl.skyblock.dungeon;

import net.engio.mbassy.listener.Handler;
import owo.pigeon.event.events.MessageEvent;
import owo.pigeon.event.events.TickEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.ExpandSetting;
import owo.pigeon.settings.IntSetting;
import owo.pigeon.utils.ColorUtil;
import owo.pigeon.utils.WorldUtil;
import owo.pigeon.utils.chat.ChatUtil;
import owo.pigeon.utils.hypixel.skyblock.DungeonUtil;
import owo.pigeon.utils.hypixel.skyblock.SkyblockUtil;

import static owo.pigeon.Pigeon.mc;

public class AutoGFS extends Module {
    public AutoGFS() {
        super("AutoGFS", Category.DUNGEON);
    }

    public EnableSetting inDungeon = setting("in-dungeon", true, v -> true);
    public EnableSetting onDungeonStart = setting("on-dungeon-start", true, v -> true);
    public EnableSetting refillOnTimer = setting("refill-on-timer", true, v -> true);
    public IntSetting timerInterval = setting("timer-interval", 5, 1, 60, "s", v -> refillOnTimer.getValue());
    public ExpandSetting refillItems = setting("refill-items", v -> true);
    public EnableSetting refillEnderPearl = setting("refill-ender-pearl", true, v -> refillItems.getValue());
    public EnableSetting refillSpiritLeap = setting("refill-spirit-leap", true, v -> refillItems.getValue());
    public EnableSetting refillSuperboomTNT = setting("refill-superboom-tnt", true, v -> refillItems.getValue());

    private int tickCounter = 0;

    // ENDER_PEARL
    // SPIRIT_LEAP
    // SUPERBOOM_TNT

    @Override
    public void onEnable() {
        tickCounter = 0;
    }

    @Handler
    public void onTickPre(TickEvent.ClientTickEvent.Pre event) {
        if (WorldUtil.nullCheck()) return;
        if (!refillOnTimer.getValue()) return;

        tickCounter++;
        if (tickCounter >= timerInterval.getValue() * 20) {
            refill();
            tickCounter = 0;
        }
    }

    @Handler
    public void onReceiveMessage(MessageEvent.ReceiveMessageEvent event) {
        String message = ColorUtil.removeColor(event.getMessage().getString());

        if (event.isOverlay()) return;

        if (onDungeonStart.getValue()) {
            if (message.contains("[NPC] Mort: Here, I found this map when I first entered the dungeon.") ||
                    message.contains("[NPC] Mort: Right-click the Orb for spells, and Left-click (or Drop) to use your Ultimate!")) {
                refill();
            }
        }
    }

    private void refill() {
        if (WorldUtil.nullCheck() || DungeonUtil.isGhost() || mc.currentScreen != null) return;
        if (inDungeon.getValue() && !DungeonUtil.isInDungeon()) return;

        if (refillEnderPearl.getValue() && SkyblockUtil.getTotalItemCount("ENDER_PEARL") != 0) {
            if (SkyblockUtil.fillItemFromSack(16, "ENDER_PEARL")) return;
        }

        if (refillSpiritLeap.getValue() && SkyblockUtil.getTotalItemCount("SPIRIT_LEAP") != 0) {
            if (SkyblockUtil.fillItemFromSack(16, "SPIRIT_LEAP")) return;
        }

        if (refillSuperboomTNT.getValue() && SkyblockUtil.getTotalItemCount("SUPERBOOM_TNT") != 0) {
            if (SkyblockUtil.fillItemFromSack(64, "SUPERBOOM_TNT")) return;
        }

        ChatUtil.sendDebugMessage(this.name, "refill");
    }
}
