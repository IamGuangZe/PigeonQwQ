package owo.pigeon.modules.impl.Player;

import net.minecraft.block.Blocks;
import net.minecraft.item.*;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.IntSetting;
import owo.pigeon.utils.WorldUtil;

import static owo.pigeon.Pigeonqwq.mc;

public class FastPlace extends Module {
    public FastPlace() {
        super("FastPlace", Category.PLAYER);
    }

    public IntSetting delay = setting("delay", 1, 0, 3, v -> true); // 0 代表完全无间隔
    public EnableSetting anyItem = setting("any-item", false, v -> true);

    public EnableSetting blocks = setting("blocks", true, v -> !anyItem.getValue());
    public EnableSetting endstone = setting("endstone", false, v -> !anyItem.getValue() && blocks.getValue());
    public EnableSetting obsidian = setting("obsidian", false, v -> !anyItem.getValue() && blocks.getValue());

    public EnableSetting xpBottle = setting("xp-bottle", true, v -> !anyItem.getValue());
    public EnableSetting snowAndEgg = setting("snow-and-egg", true, v -> !anyItem.getValue());
    public EnableSetting enderPearl = setting("ender-pearl", false, v -> !anyItem.getValue());
    public EnableSetting fishingRod = setting("fishing-rod", false, v -> !anyItem.getValue());
    public EnableSetting nothing = setting("nothing", true, v -> !anyItem.getValue());
    public EnableSetting others = setting("others", false, v -> !anyItem.getValue());

    public boolean canFastPlace() {
        if (WorldUtil.nullCheck()) return false;
        if (anyItem.getValue()) return true;

        ItemStack itemStack = mc.player.getMainHandStack();
        if (itemStack.isEmpty()) return nothing.getValue();

        Item item = itemStack.getItem();

        if (item instanceof BlockItem blockItem) {
            if (!blocks.getValue()) return false;

            var block = blockItem.getBlock();
            if (block == Blocks.END_STONE) return endstone.getValue();
            if (block == Blocks.OBSIDIAN) return obsidian.getValue();

            return true;
        }

        if (item instanceof ExperienceBottleItem) return xpBottle.getValue();
        if (item instanceof SnowballItem || item instanceof EggItem) return snowAndEgg.getValue();
        if (item instanceof EnderPearlItem) return enderPearl.getValue();
        if (item instanceof FishingRodItem) return fishingRod.getValue();

        return others.getValue();
    }
}
