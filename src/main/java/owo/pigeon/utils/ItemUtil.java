package owo.pigeon.utils;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import static owo.pigeon.Pigeon.mc;

public class ItemUtil {
    public static boolean isSword(Item item) {
        return item == Items.WOODEN_SWORD
                || item == Items.STONE_SWORD
                || item == Items.IRON_SWORD
                || item == Items.GOLDEN_SWORD
                || item == Items.DIAMOND_SWORD
                || item == Items.NETHERITE_SWORD;
    }

    public static int getSlotFromItemName(String itemName) {
        if (mc.player == null) return -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);

            if (stack.isEmpty()) continue;

            String name = ColorUtil.removeColor(stack.getName().getString());

            if (name.toLowerCase().contains(itemName.toLowerCase())) return i;
        }

        return -1;
    }

    public static ItemStack getItemStackfromSlot(int slot) {
        return mc.player.getInventory().getStack(slot);
    }
}
