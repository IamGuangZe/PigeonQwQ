package owo.pigeon.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.TagKey;

import java.util.Collection;

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

    public static int getTotalItemCount(Item item) {
        int totalCount = 0;
        for (int i = 0; i < mc.player.getInventory().getMainStacks().size(); i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.isOf(item)) {
                totalCount += stack.getCount();
            }
        }
        return totalCount;
    }

    public static int getTotalItemCount(TagKey<Item> tag) {
        int totalCount = 0;
        for (int i = 0; i < mc.player.getInventory().getMainStacks().size(); i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.isIn(tag)) {
                totalCount += stack.getCount();
            }
        }
        return totalCount;
    }

    public static int getTotalItemCount(String name) {
        int totalCount = 0;
        for (int i = 0; i < mc.player.getInventory().getMainStacks().size(); i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getName().getString().contains(name)) {
                totalCount += stack.getCount();
            }
        }
        return totalCount;
    }

    public static String getSkullTexture(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.isOf(Items.PLAYER_HEAD)) return null;

        ProfileComponent profileComponent = stack.get(DataComponentTypes.PROFILE);
        if (profileComponent == null) return null;

        GameProfile profile = profileComponent.getGameProfile();
        if (profile == null) return null;

        Collection<Property> textures = profile.properties().get("textures");
        if (textures.isEmpty()) return null;

        return textures.iterator().next().value();
    }
}
