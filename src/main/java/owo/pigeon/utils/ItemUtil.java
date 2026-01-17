package owo.pigeon.utils;

import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class ItemUtil {
    public static boolean isSword(Item item) {
        return item == Items.WOODEN_SWORD
                || item == Items.STONE_SWORD
                || item == Items.IRON_SWORD
                || item == Items.GOLDEN_SWORD
                || item == Items.DIAMOND_SWORD
                || item == Items.NETHERITE_SWORD;
    }
}
