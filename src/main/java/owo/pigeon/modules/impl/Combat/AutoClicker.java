package owo.pigeon.modules.impl.Combat;

import net.engio.mbassy.listener.Handler;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import owo.pigeon.event.events.TickEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.IntSetting;
import owo.pigeon.utils.ItemUtil;
import owo.pigeon.utils.KeybindUtil;
import owo.pigeon.utils.Player.PlayerUtil;
import owo.pigeon.utils.RandomUtil;
import owo.pigeon.utils.WorldUtil;

import static owo.pigeon.Pigeonqwq.mc;

public class AutoClicker extends Module {
    public AutoClicker() {
        super("AutoClicker", Category.COMBAT);
    }

    public IntSetting minCPS = setting("min-cps", 12, 1, 20, v -> true);
    public IntSetting maxCPS = setting("max-cps", 18, 1, 20, v -> true);
    public EnableSetting leftClick = setting("left-click", true, v -> true);
    public EnableSetting onlySword = setting("only-sword", false, v -> leftClick.getValue());
    public EnableSetting breakBlocks = setting("break-blocks", true, v -> leftClick.getValue());
    public EnableSetting rightClick = setting("right-click", true, v -> true);
    public EnableSetting onlyBlocks = setting("only-blocks", false, v -> rightClick.getValue());

    private boolean firstLeftClick = true;
    private boolean firstRightClick = true;
    private long lastLeftClickTime = 0;
    private long lastRightClickTime = 0;

    @Handler
    public void onTickPost(TickEvent.ClientTickEvent.Post event) {
        if (WorldUtil.nullCheck()) return;

        long clickInterval = 1000 / RandomUtil.intRandom(minCPS.getValue(), maxCPS.getValue());
        long currentTime = System.currentTimeMillis();

        if (KeybindUtil.isPressed(mc.options.attackKey) && leftClick.getValue()) {
            if (breakBlocksCheck()) {
                if (canClick() && onlySwordCheck() && currentTime - lastLeftClickTime >= clickInterval) {
                    if (firstLeftClick) {
                        firstLeftClick = false;
                    } else {
                        KeybindUtil.setPressed(mc.options.attackKey,false);
                        PlayerUtil.LeftClick(PlayerUtil.LeftClickMode.MOUSE);
                    }
                    lastLeftClickTime = currentTime;
                }
            } else {
                KeybindUtil.setPressed(mc.options.attackKey, true);
            }
        } else {
            firstLeftClick = true;
        }

        if (KeybindUtil.isPressed(mc.options.useKey) && rightClick.getValue()) {
            if (canClick() && onlyBlocksCheck() && currentTime - lastRightClickTime >= clickInterval) {
                if (firstRightClick) {
                    firstRightClick = false;
                } else {
                    PlayerUtil.RightClick(PlayerUtil.RightClickMode.MOUSE);
                }
                lastRightClickTime = currentTime;
            }
        } else {
            firstRightClick = true;
        }
    }

    private boolean canClick() {
        return mc.currentScreen == null && !mc.player.isBlocking() && !mc.player.isUsingItem();
    }

    private boolean breakBlocksCheck() {
        return !(breakBlocks.getValue() && PlayerUtil.isBreakingBlock());
    }

    private boolean onlySwordCheck() {
        if (!onlySword.getValue()) return true;
        ItemStack itemStack = mc.player.getMainHandStack();
        return itemStack != null && ItemUtil.isSword(itemStack.getItem());
    }

    private boolean onlyBlocksCheck() {
        if (!onlyBlocks.getValue()) return true;
        ItemStack itemStack = mc.player.getMainHandStack();
        return itemStack != null && itemStack.getItem() instanceof BlockItem;
    }
}
