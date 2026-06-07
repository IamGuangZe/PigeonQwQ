package owo.pigeon.utils;

import com.google.common.collect.LinkedListMultimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static owo.pigeon.Pigeon.mc;

public class ItemUtil {
    public static final BiFunction<NbtCompound, String, String> STRING_EXTRACTOR = (nbt, k) -> nbt.getString(k).orElse(null);
    public static final BiFunction<NbtCompound, String, NbtCompound> COMPOUND_EXTRACTOR = (nbt, k) -> nbt.getCompound(k).orElse(null);
    public static final BiFunction<NbtCompound, String, Integer> INT_EXTRACTOR = (nbt, k) -> nbt.getInt(k).orElse(null);

    public static boolean isSword(ItemStack stack) {
        return stack.isOf(Items.WOODEN_SWORD)
                || stack.isOf(Items.STONE_SWORD)
                || stack.isOf(Items.IRON_SWORD)
                || stack.isOf(Items.GOLDEN_SWORD)
                || stack.isOf(Items.DIAMOND_SWORD)
                || stack.isOf(Items.NETHERITE_SWORD);
    }


    public static int getSlotFromItemName(String itemName, boolean onlyHotbar) {
        if (mc.player == null) return -1;

        int limit = onlyHotbar ? 9 : 36;

        for (int i = 0; i < limit; i++) {
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

    public static List<Text> getItemLore(ItemStack stack) {
        if (stack.isEmpty()) return Collections.emptyList();

        LoreComponent loreComponent = stack.get(DataComponentTypes.LORE);

        if (loreComponent == null) return Collections.emptyList();
        return loreComponent.lines();
    }

    public static NbtCompound getItemCustomData(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;

        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);

        return (component != null) ? component.copyNbt() : null;
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

    public static ItemStack getSkullFromPlayer(PlayerEntity player) {
        if (player == null) return new ItemStack(Items.PLAYER_HEAD);

        ItemStack headStack = new ItemStack(Items.PLAYER_HEAD);
        headStack.set(DataComponentTypes.PROFILE, ProfileComponent.ofStatic(player.getGameProfile()));
        return headStack;
    }

    public static ItemStack getSkullFromTexture(String textureValue) {
        return getSkullFromTexture(textureValue, null);
    }

    public static ItemStack getSkullFromTexture(String textureValue, String textureSignature) {
        if (textureValue == null || textureValue.isEmpty()) return new ItemStack(Items.PLAYER_HEAD);

        ItemStack headStack = new ItemStack(Items.PLAYER_HEAD);

        // Build a GameProfile with the texture property
        // Use a random UUID and empty name since we only need the texture data
        UUID uuid = UUID.randomUUID();
        LinkedListMultimap<String, Property> multimap = LinkedListMultimap.create();
        if (textureSignature != null && !textureSignature.isEmpty()) {
            multimap.put("textures", new Property("textures", textureValue, textureSignature));
        } else {
            multimap.put("textures", new Property("textures", textureValue));
        }
        PropertyMap properties = new PropertyMap(multimap);
        GameProfile profile = new GameProfile(uuid, "", properties);

        headStack.set(DataComponentTypes.PROFILE, ProfileComponent.ofStatic(profile));
        return headStack;
    }

    public static <T> T getCustomDataValue(ItemStack stack, String key, BiFunction<NbtCompound, String, T> extractor) {
        NbtCompound nbt = getItemCustomData(stack);
        if (nbt != null && nbt.contains(key)) {
            try {
                return extractor.apply(nbt, key);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public static ItemStack setCustomDataValue(ItemStack stack, String key, BiConsumer<NbtCompound, String> inserter) {
        if (stack == null || stack.isEmpty()) return stack;
        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, nbt -> {
            inserter.accept(nbt, key);
        });
        return stack;
    }
}
