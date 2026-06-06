package owo.pigeon.modules;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public enum Category {
    CLIENT(Items.GRASS_BLOCK),
    COMBAT(Items.DIAMOND_SWORD),
    DUNGEON(Items.MOSSY_STONE_BRICKS),
    EVENT(Items.CLOCK),
    FARMING(Items.DIAMOND_HOE),
    HUNTING(Items.COBWEB),
    HYPIXEL(Items.GOLD_INGOT),
    MINING(Items.DIAMOND_PICKAXE),
    MISC(Items.SLIME_BALL),
    MOVEMENT(Items.DIAMOND_BOOTS),
    NETHER(Items.NETHERRACK),
    PLAYER(Items.PLAYER_HEAD),
    RENDER(Items.ENDER_EYE),
    RIFT(Items.END_PORTAL_FRAME),
    SLAYER(Items.ROTTEN_FLESH),
    WORLD(Items.FILLED_MAP),
    DEBUG(Items.TEST_INSTANCE_BLOCK);

    private final ItemStack icon;

    Category(Item item) {
        this.icon = item.getDefaultStack();
    }

    public ItemStack getIcon() {
        return icon;
    }
}
