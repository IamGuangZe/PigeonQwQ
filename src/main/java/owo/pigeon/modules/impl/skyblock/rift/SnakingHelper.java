package owo.pigeon.modules.impl.skyblock.rift;

import net.engio.mbassy.listener.Handler;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import owo.pigeon.event.events.StartAttackEvent;
import owo.pigeon.event.events.StartUseItemEvent;
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
    public void onStartAttackEventPre(StartAttackEvent.Pre event) {
        if (!SkyblockUtil.isInIsland(SkyblockUtil.Island.THE_RIFT)) return;
        switchTo(PICKAXE);
    }

    @Handler
    public void onStartUseItem(StartUseItemEvent.Pre event) {
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
        if (mc.hitResult == null || mc.hitResult.getType() != HitResult.Type.BLOCK) return false;

        BlockState state = mc.level.getBlockState(((BlockHitResult) mc.hitResult).getBlockPos());

        ChatUtil.sendDebugMessage(this.name, "aim block: " + state);

        return state.is(Blocks.LAPIS_BLOCK)
                || state.is(Blocks.STAINED_GLASS.lightBlue())
                || state.is(Blocks.STAINED_GLASS.blue());
    }

    private boolean isHoldingTargetItem() {
        ItemStack stack = mc.player.getMainHandItem();
        if (stack.isEmpty()) return false;

        String name = ColorUtil.removeColor(stack.getHoverName().getString());

        ChatUtil.sendDebugMessage(this.name, "holding: " + name);

        return name.contains(PUNGI) || name.contains(PICKAXE);
    }
}
