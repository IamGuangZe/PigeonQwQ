package owo.pigeon.modules.impl.skyblock.farming;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.utils.ItemUtil;
import owo.pigeon.utils.hypixel.skyblock.SkyblockUtil;

import java.util.List;

import static owo.pigeon.Pigeon.mc;

public class RotationLock extends Module {
    public RotationLock() {
        super("RotationLock", Category.FARMING);
    }

    public EnableSetting onlyOnGround = setting("only-on-ground", true, v -> true);
    public EnableSetting onlyInGarden = setting("only-in-garden", true, v -> true);
    public EnableSetting onlyHoldFarmingTool = setting("only-hold-farming-tool", true, v -> true);

    public boolean shouldLock() {
        if (onlyOnGround.getValue() && !mc.player.isOnGround()) {
            return false;
        }

        if (onlyInGarden.getValue() && !SkyblockUtil.isInIsland(SkyblockUtil.Island.GARDEN)) {
            return false;
        }

        return !onlyHoldFarmingTool.getValue() || isFarmingTool(mc.player.getMainHandStack());
    }

    private boolean isFarmingTool(ItemStack stack) {
        if (stack.isEmpty()) return false;
        List<Text> lore = ItemUtil.getItemLore(stack);
        if (lore.isEmpty()) return false;
        return lore.getLast().getString().contains("FARMING TOOL");
    }
}
