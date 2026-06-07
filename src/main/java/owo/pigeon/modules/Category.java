package owo.pigeon.modules;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import owo.pigeon.utils.ItemUtil;
import owo.pigeon.utils.hypixel.skyblock.SkyblockUtil;

public enum Category {
    CLIENT(Items.GRASS_BLOCK),
    COMBAT(Items.DIAMOND_SWORD),
    DUNGEON(ItemUtil.getSkullFromTexture(SkyblockUtil.DUNGEONEERING)),
    EVENT(Items.CLOCK),
    FARMING(Items.DIAMOND_HOE),
    HUNTING(Items.LEAD),
    HYPIXEL(Items.GOLD_INGOT),
    MINING(Items.DIAMOND_PICKAXE),
    MISC(Items.SLIME_BALL),
    MOVEMENT(Items.DIAMOND_BOOTS),
    NETHER(Items.NETHERRACK),
    PLAYER(Items.PLAYER_HEAD),
    RENDER(Items.ENDER_EYE),
    RIFT(Items.MYCELIUM),
    SLAYER(Items.ROTTEN_FLESH),
    WORLD(Items.FILLED_MAP),
    DEBUG(Items.TEST_INSTANCE_BLOCK);

    static {
        RIFT.icon.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        ItemUtil.setCustomDataValue(SLAYER.icon, "id", (nbt, k) -> nbt.putString(k, "REVENANT_FLESH"));
    }

    private ItemStack icon;

    Category(Item item) {
        this.icon = item.getDefaultStack();
    }

    Category(ItemStack itemStack) {
        this.icon = itemStack;
    }

    public void setIcon(ItemStack icon) {
        this.icon = icon;
    }

    public ItemStack getIcon() {
        return icon;
    }
}
