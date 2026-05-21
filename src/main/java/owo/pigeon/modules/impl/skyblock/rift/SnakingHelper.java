package owo.pigeon.modules.impl.skyblock.rift;

import net.engio.mbassy.listener.Handler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import owo.pigeon.event.events.DoAttackEvent;
import owo.pigeon.event.events.DoItemUseEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.utils.ColorUtil;
import owo.pigeon.utils.ItemUtil;
import owo.pigeon.utils.chat.ChatUtil;
import owo.pigeon.utils.hypixel.skyblock.SkyblockUtil;
import owo.pigeon.utils.player.PlayerUtil;

import static owo.pigeon.Pigeon.mc;

public class SnakingHelper extends Module {
    public SnakingHelper() {
        super("SnakingHelper", Category.RIFT);
    }

    private static final String PICKAXE = " Pickaxe";
    private static final String PUNGI = "Frozen Water Pungi";

    @Handler
    public void onDoAttackPre(DoAttackEvent.Pre event) {
        if (!SkyblockUtil.isInIsland(SkyblockUtil.Island.THE_RIFT)) return;
        switchTo(PICKAXE);
    }

    @Handler
    public void onDoItemUse(DoItemUseEvent.Pre event) {
        if (!SkyblockUtil.isInIsland(SkyblockUtil.Island.THE_RIFT)) return;
        switchTo(PUNGI);
    }

    private void switchTo(String itemName) {
        if (!isHoldingTargetItem()) {
            ChatUtil.sendDebugMessage(this.name, "not holding target item");
            return;
        }
        if (!isAimSnake()) {
            ChatUtil.sendDebugMessage(this.name, "not aiming snake block");
            return;
        }

        int slot = ItemUtil.getSlotFromItemName(itemName, true);
        ChatUtil.sendDebugMessage(this.name, "search item: " + itemName + ", slot=" + slot);

        if (slot != -1) {
            PlayerUtil.switchItemSlot(slot);
            ChatUtil.sendDebugMessage(this.name, "switched to slot " + slot);
        }
    }

    private boolean isAimSnake() {
        if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.BLOCK) return false;

        BlockState state = mc.world.getBlockState(((BlockHitResult) mc.crosshairTarget).getBlockPos());

        ChatUtil.sendDebugMessage(this.name, "aim block: " + state);

        return state.isOf(Blocks.LAPIS_BLOCK)
                || state.isOf(Blocks.LIGHT_BLUE_STAINED_GLASS)
                || state.isOf(Blocks.BLUE_STAINED_GLASS);
    }

    private boolean isHoldingTargetItem() {
        ItemStack stack = mc.player.getMainHandStack();
        if (stack.isEmpty()) return false;

        String name = ColorUtil.removeColor(stack.getName().getString());

        ChatUtil.sendDebugMessage(this.name, "holding: " + name);

        return name.contains(PUNGI) || name.contains(PICKAXE);
    }
}
