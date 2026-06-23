package owo.pigeon.modules.impl.combat;

import net.engio.mbassy.listener.Handler;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import owo.pigeon.event.events.RenderEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.IntSetting;
import owo.pigeon.utils.ItemUtil;
import owo.pigeon.utils.KeybindUtil;
import owo.pigeon.utils.RandomUtil;
import owo.pigeon.utils.chat.ChatUtil;
import owo.pigeon.utils.player.PlayerUtil;

import static owo.pigeon.Pigeon.mc;

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
    private long nextLeftClickTime = 0;
    private long nextRightClickTime = 0;

    @Handler
    public void onRender3D(RenderEvent.Render3DEvent event) {
        long currentTime = System.currentTimeMillis();

        if (KeybindUtil.isPressed(mc.options.keyAttack) && leftClick.getValue()) {
            if (breakBlocksCheck()) {
                if (canClick() && onlySwordCheck()) {
                    if (firstLeftClick) {
                        firstLeftClick = false;
                        nextLeftClickTime = currentTime + (1000 / RandomUtil.intRandom(minCPS.getValue(), maxCPS.getValue()));
                    } else if (currentTime >= nextLeftClickTime) {
                        KeybindUtil.setPressed(mc.options.keyAttack, false);
                        PlayerUtil.leftClick(PlayerUtil.LeftClickMode.MOUSE);
                        int randomCPS = RandomUtil.intRandom(minCPS.getValue(), maxCPS.getValue());
                        nextLeftClickTime = currentTime + (1000 / randomCPS);
                        ChatUtil.sendDebugMessage(this.name, "Left Click Random CPS: " + randomCPS);
                    }
                }
            } else {
                KeybindUtil.setPressed(mc.options.keyAttack, true);
            }
        } else {
            firstLeftClick = true;
        }

        if (KeybindUtil.isPressed(mc.options.keyUse) && rightClick.getValue()) {
            if (canClick() && onlyBlocksCheck()) {
                if (firstRightClick) {
                    firstRightClick = false;
                    nextRightClickTime = currentTime + (1000 / RandomUtil.intRandom(minCPS.getValue(), maxCPS.getValue()));
                } else if (currentTime >= nextRightClickTime) {
                    PlayerUtil.rightClick(PlayerUtil.RightClickMode.MOUSE);
                    int randomCPS = RandomUtil.intRandom(minCPS.getValue(), maxCPS.getValue());
                    nextRightClickTime = currentTime + (1000 / randomCPS);
                    ChatUtil.sendDebugMessage(this.name, "Right Click Random CPS: " + randomCPS);
                }
            }
        } else {
            firstRightClick = true;
        }
    }

    private boolean canClick() {
        return mc.screen == null && !mc.player.isBlocking() && !mc.player.isUsingItem();
    }

    private boolean breakBlocksCheck() {
        // ChatUtil.sendDebugMessage(this.name,"isBreakingBlock: " + mc.gameMode.isBreakingBlock());
        return !(breakBlocks.getValue() && PlayerUtil.isBreakingBlock());
    }

    private boolean onlySwordCheck() {
        if (!onlySword.getValue()) return true;
        ItemStack itemStack = mc.player.getMainHandItem();
        return itemStack != null && ItemUtil.isSword(itemStack);
    }

    private boolean onlyBlocksCheck() {
        if (!onlyBlocks.getValue()) return true;
        ItemStack itemStack = mc.player.getMainHandItem();
        return itemStack != null && itemStack.getItem() instanceof BlockItem;
    }

    @Override
    public String getSuffix() {
        return minCPS.getValue() + "-" + maxCPS.getValue();
    }
}
