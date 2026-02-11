package owo.pigeon.modules.impl.Skyblock.Rift;

import net.engio.mbassy.listener.Handler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import owo.pigeon.event.events.DoAttackEvent;
import owo.pigeon.event.events.DoItemUseEvent;
import owo.pigeon.event.events.TickEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.ModeSetting;
import owo.pigeon.utils.Chat.ChatUtil;
import owo.pigeon.utils.ColorUtil;
import owo.pigeon.utils.Hypixel.SkyblockUtil;
import owo.pigeon.utils.ItemUtil;
import owo.pigeon.utils.KeybindUtil;
import owo.pigeon.utils.Player.PlayerUtil;
import owo.pigeon.utils.WorldUtil;

import static owo.pigeon.Pigeon.mc;

public class TimiteMiner extends Module {
    public TimiteMiner() {
        super("TimiteMiner", Category.SKYBLOCK);
    }

    public enum TimiteStage {
        YOUNGITE(0), TIMITE(1), OBSOLITE(2);

        final int level;

        TimiteStage(int level) {
            this.level = level;
        }
    }

    public EnableSetting autoMine = setting("auto-mine", false, v -> true);
    public EnableSetting highliteMode = setting("highlite-mode", false, v -> autoMine.getValue());
    public ModeSetting<TimiteStage> timiteStage = setting("timite-stage", TimiteStage.TIMITE, v -> autoMine.getValue() && !highliteMode.getValue());

    private static final String PICKAXE = " Pickaxe";
    private static final String GUN = "Time Gun";

    @Handler
    public void onTickPre(TickEvent.ClientTickEvent.Pre event) {
        if (WorldUtil.nullCheck()) return;
        if (!SkyblockUtil.isInIsland(SkyblockUtil.Island.Rift)) return;
        if (!autoMine.getValue()) return;

        if (!isHoldingTimiteTools()) {
            KeybindUtil.resetPressed(mc.options.useKey);
            KeybindUtil.resetPressed(mc.options.attackKey);
            return;
        }

        TimiteStage currentBlockStage = getTargetBlockStage();

        if (currentBlockStage == null) {
            KeybindUtil.resetPressed(mc.options.useKey);
            KeybindUtil.resetPressed(mc.options.attackKey);
            return;
        }

        int targetLevel;
        int currentLevel = currentBlockStage.level;

        if (highliteMode.getValue()) targetLevel = getTargetStageForHighliteMode().level;
        else targetLevel = timiteStage.getValue().level;

        if (currentLevel < targetLevel) {
            // 阶段低于目标
            switchTo(GUN);
            KeybindUtil.resetPressed(mc.options.attackKey);
            KeybindUtil.setPressed(mc.options.useKey, true);
        } else if (currentLevel == targetLevel) {
            // 阶段等于目标
            switchTo(PICKAXE);
            KeybindUtil.resetPressed(mc.options.useKey);
            KeybindUtil.setPressed(mc.options.attackKey, true);
        } else {
            // 阶段高于目标
            KeybindUtil.resetPressed(mc.options.useKey);
            KeybindUtil.resetPressed(mc.options.attackKey);
        }
    }

    @Handler
    public void onDoAttackPre(DoAttackEvent.Pre event) {
        if (!SkyblockUtil.isInIsland(SkyblockUtil.Island.Rift)) return;
        switchTo(PICKAXE);
    }

    @Handler
    public void onDoItemUse(DoItemUseEvent.Pre event) {
        if (!SkyblockUtil.isInIsland(SkyblockUtil.Island.Rift)) return;
        switchTo(GUN);
    }

    @Override
    public void onDisable() {
        if (mc.options != null) {
            KeybindUtil.resetPressed(mc.options.useKey);
            KeybindUtil.resetPressed(mc.options.attackKey);
        }
    }

    private void switchTo(String itemName) {
        if (!isHoldingTimiteTools()) return;
        if (getTargetBlockStage() == null) return;

        int slot = ItemUtil.getSlotFromItemName(itemName);

        if (slot != -1) {
            PlayerUtil.switchItemSlot(slot);
            ChatUtil.sendDebugMessage(this.name, "Switched to " + itemName);
        }
    }

    private TimiteStage getTargetBlockStage() {
        if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.BLOCK) return null;

        Block block = mc.world
                .getBlockState(((BlockHitResult) mc.crosshairTarget).getBlockPos())
                .getBlock();

        if (block == Blocks.LIGHT_BLUE_STAINED_GLASS_PANE) return TimiteStage.YOUNGITE;
        if (block == Blocks.BLUE_STAINED_GLASS_PANE) return TimiteStage.TIMITE;
        if (block == Blocks.PURPLE_STAINED_GLASS_PANE) return TimiteStage.OBSOLITE;

        return null;
    }

    private boolean isHoldingTimiteTools() {
        ItemStack stack = mc.player.getInventory().getSelectedStack();
        if (stack.isEmpty()) return false;

        String name = ColorUtil.removeColor(stack.getName().getString());
        return name.contains(PICKAXE) || name.contains(GUN);
    }

    private TimiteStage getTargetStageForHighliteMode() {
        int youngiteCount = ItemUtil.getTotalItemCount("Youngite");
        int timiteCount = ItemUtil.getTotalItemCount("Timite");
        int obsoliteCount = ItemUtil.getTotalItemCount("Obsolite");

        // 计算当前能合成多少个Highlite
        int completedHighlites = Math.min(
            youngiteCount / 32,
            Math.min(timiteCount / 32, obsoliteCount / 16)
        );

        // 计算下一个Highlite需要的数量
        int youngiteNeeded = (completedHighlites + 1) * 32;
        int timiteNeeded = (completedHighlites + 1) * 32;
        int obsoliteNeeded = (completedHighlites + 1) * 16;

        // 判断应该挖哪个 (优先级：Youngite -> Timite -> Obsolite)
        if (youngiteCount < youngiteNeeded) {
            return TimiteStage.YOUNGITE;
        } else if (timiteCount < timiteNeeded) {
            return TimiteStage.TIMITE;
        } else {
            return TimiteStage.OBSOLITE;
        }
    }
}