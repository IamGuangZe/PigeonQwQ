package owo.pigeon.modules.impl.Skyblock.Rift;

import net.engio.mbassy.listener.Handler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import owo.pigeon.event.events.DoAttackEvent;
import owo.pigeon.event.events.DoItemUseEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.utils.Chat.ChatUtil;
import owo.pigeon.utils.ColorUtil;
import owo.pigeon.utils.ItemUtil;
import owo.pigeon.utils.PlayerUtil;

import static owo.pigeon.Pigeonqwq.mc;

public class SnakingHelper extends Module {
    public SnakingHelper() {
        super("SnakingHelper", Category.SKYBLOCK);
    }

    private static final String PICKAXE = " Pickaxe";
    private static final String PUNGI = "Frozen Water Pungi";

    @Handler
    public void onDoAttackPre(DoAttackEvent.Pre event) {
        switchTo(PICKAXE);
    }

    @Handler
    public void onDoItemUse(DoItemUseEvent.Pre event) {
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

        int slot = ItemUtil.getSlotFromItemName(itemName);
        ChatUtil.sendDebugMessage(this.name, "search item: " + itemName + ", slot=" + slot);

        if (slot != -1) {
            PlayerUtil.switchItemSlot(slot);
            ChatUtil.sendDebugMessage(this.name, "switched to slot " + slot);
        }
    }

    private boolean isAimSnake() {
        if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.BLOCK) return false;

        Block block = mc.world
                .getBlockState(((BlockHitResult) mc.crosshairTarget).getBlockPos())
                .getBlock();

        ChatUtil.sendDebugMessage(this.name, "aim block: " + block);

        return block == Blocks.LAPIS_BLOCK || block == Blocks.LIGHT_BLUE_STAINED_GLASS || block == Blocks.BLUE_STAINED_GLASS;
    }

    private boolean isHoldingTargetItem() {
        ItemStack stack = mc.player.getInventory().getSelectedStack();
        if (stack.isEmpty()) return false;

        String name = ColorUtil.removeColor(stack.getName().getString());

        ChatUtil.sendDebugMessage(this.name, "holding: " + name);

        return name.contains(PUNGI) || name.contains(PICKAXE);
    }
}
