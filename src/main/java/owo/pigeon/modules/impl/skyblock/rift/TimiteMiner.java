package owo.pigeon.modules.impl.skyblock.rift;

import net.engio.mbassy.listener.Handler;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import owo.pigeon.event.events.ClientTickEvent;
import owo.pigeon.event.events.DoAttackEvent;
import owo.pigeon.event.events.DoItemUseEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.ModeSetting;
import owo.pigeon.utils.ColorUtil;
import owo.pigeon.utils.ItemUtil;
import owo.pigeon.utils.KeybindUtil;
import owo.pigeon.utils.WorldUtil;
import owo.pigeon.utils.chat.ChatUtil;
import owo.pigeon.utils.hypixel.skyblock.SkyblockUtil;
import owo.pigeon.utils.player.PlayerUtil;

import static owo.pigeon.Pigeon.mc;

public class TimiteMiner extends Module {
    public TimiteMiner() {
        super("TimiteMiner", Category.RIFT);
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
    public void onTickPre(ClientTickEvent.Pre event) {
        if (WorldUtil.nullCheck()) return;
        if (!SkyblockUtil.isInIsland(SkyblockUtil.Island.THE_RIFT)) return;
        if (!autoMine.getValue()) return;

        if (!isHoldingTimiteTools()) {
            KeybindUtil.resetPressed(mc.options.keyUse);
            KeybindUtil.resetPressed(mc.options.keyAttack);
            return;
        }

        TimiteStage currentBlockStage = getTargetBlockStage();

        if (currentBlockStage == null) {
            KeybindUtil.resetPressed(mc.options.keyUse);
            KeybindUtil.resetPressed(mc.options.keyAttack);
            return;
        }

        int targetLevel;
        int currentLevel = currentBlockStage.level;

        if (highliteMode.getValue()) targetLevel = getTargetStageForHighliteMode().level;
        else targetLevel = timiteStage.getValue().level;

        if (currentLevel < targetLevel) {
            // 阶段低于目标
            switchTo(GUN);
            KeybindUtil.resetPressed(mc.options.keyAttack);
            KeybindUtil.setPressed(mc.options.keyUse, true);
        } else if (currentLevel == targetLevel) {
            // 阶段等于目标
            switchTo(PICKAXE);
            KeybindUtil.resetPressed(mc.options.keyUse);
            KeybindUtil.setPressed(mc.options.keyAttack, true);
        } else {
            // 阶段高于目标
            KeybindUtil.resetPressed(mc.options.keyUse);
            KeybindUtil.resetPressed(mc.options.keyAttack);
        }
    }

    @Handler
    public void onDoAttackPre(DoAttackEvent.Pre event) {
        if (!SkyblockUtil.isInIsland(SkyblockUtil.Island.THE_RIFT)) return;
        switchTo(PICKAXE);
    }

    @Handler
    public void onDoItemUse(DoItemUseEvent.Pre event) {
        if (!SkyblockUtil.isInIsland(SkyblockUtil.Island.THE_RIFT)) return;
        switchTo(GUN);
    }

    @Override
    public void onDisable() {
        if (mc.options != null) {
            KeybindUtil.resetPressed(mc.options.keyUse);
            KeybindUtil.resetPressed(mc.options.keyAttack);
        }
    }

    private void switchTo(String itemName) {
        if (!isHoldingTimiteTools()) return;
        if (getTargetBlockStage() == null) return;

        int slot = ItemUtil.getSlotFromItemName(itemName, true);

        if (slot != -1) {
            PlayerUtil.switchItemSlot(slot);
            ChatUtil.sendDebugMessage(this.name, "Switched to " + itemName);
        }
    }

    private TimiteStage getTargetBlockStage() {
        if (mc.hitResult == null || mc.hitResult.getType() != HitResult.Type.BLOCK) return null;

        BlockState state = mc.level.getBlockState(((BlockHitResult) mc.hitResult).getBlockPos());

        if (state.is(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE)) return TimiteStage.YOUNGITE;
        if (state.is(Blocks.BLUE_STAINED_GLASS_PANE)) return TimiteStage.TIMITE;
        if (state.is(Blocks.PURPLE_STAINED_GLASS_PANE)) return TimiteStage.OBSOLITE;

        return null;
    }

    private boolean isHoldingTimiteTools() {
        ItemStack stack = mc.player.getMainHandItem();
        if (stack.isEmpty()) return false;

        String name = ColorUtil.removeColor(stack.getHoverName().getString());
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

    @Override
    public String getSuffix() {
        return String.valueOf(timiteStage.getValue());
    }
}