package owo.pigeon.utils;

import com.google.common.collect.LinkedListMultimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.ResolvableProfile;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static owo.pigeon.Pigeon.mc;

public class ItemUtil {
    public static final BiFunction<CompoundTag, String, String> STRING_EXTRACTOR = (nbt, k) -> nbt.getString(k).orElse(null);
    public static final BiFunction<CompoundTag, String, CompoundTag> COMPOUND_EXTRACTOR = (nbt, k) -> nbt.getCompound(k).orElse(null);
    public static final BiFunction<CompoundTag, String, Integer> INT_EXTRACTOR = (nbt, k) -> nbt.getInt(k).orElse(null);

    public static boolean isSword(ItemStack stack) {
        return stack.is(Items.WOODEN_SWORD)
                || stack.is(Items.STONE_SWORD)
                || stack.is(Items.IRON_SWORD)
                || stack.is(Items.GOLDEN_SWORD)
                || stack.is(Items.DIAMOND_SWORD)
                || stack.is(Items.NETHERITE_SWORD);
    }


    public static int getSlotFromItemName(String itemName, boolean onlyHotbar) {
        if (mc.player == null) return -1;

        int limit = onlyHotbar ? 9 : 36;

        for (int i = 0; i < limit; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            String name = ColorUtil.removeColor(stack.getHoverName().getString());
            if (name.toLowerCase().contains(itemName.toLowerCase())) return i;
        }

        return -1;
    }

    public static ItemStack getItemStackfromSlot(int slot) {
        return mc.player.getInventory().getItem(slot);
    }

    public static int getTotalItemCount(Item item) {
        int totalCount = 0;
        for (int i = 0; i < mc.player.getInventory().getNonEquipmentItems().size(); i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.is(item)) {
                totalCount += stack.getCount();
            }
        }
        return totalCount;
    }

    public static int getTotalItemCount(TagKey<Item> tag) {
        int totalCount = 0;
        for (int i = 0; i < mc.player.getInventory().getNonEquipmentItems().size(); i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.is(tag)) {
                totalCount += stack.getCount();
            }
        }
        return totalCount;
    }

    public static int getTotalItemCount(String name) {
        int totalCount = 0;
        for (int i = 0; i < mc.player.getInventory().getNonEquipmentItems().size(); i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getHoverName().getString().contains(name)) {
                totalCount += stack.getCount();
            }
        }
        return totalCount;
    }

    public static List<Component> getItemLore(ItemStack stack) {
        if (stack.isEmpty()) return Collections.emptyList();

        ItemLore loreComponent = stack.get(DataComponents.LORE);

        if (loreComponent == null) return Collections.emptyList();
        return loreComponent.lines();
    }

    public static CompoundTag getItemCustomData(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;

        CustomData component = stack.get(DataComponents.CUSTOM_DATA);

        return (component != null) ? component.copyTag() : null;
    }

    public static String getSkullTexture(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.is(Items.PLAYER_HEAD)) return null;

        ResolvableProfile profileComponent = stack.get(DataComponents.PROFILE);
        if (profileComponent == null) return null;

        GameProfile profile = profileComponent.partialProfile();
        if (profile == null) return null;

        Collection<Property> textures = profile.properties().get("textures");
        if (textures.isEmpty()) return null;

        return textures.iterator().next().value();
    }

    public static ItemStack getSkullFromPlayer(Player player) {
        if (player == null) return new ItemStack(Items.PLAYER_HEAD);

        ItemStack headStack = new ItemStack(Items.PLAYER_HEAD);
        headStack.set(DataComponents.PROFILE, ResolvableProfile.createResolved(player.getGameProfile()));
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

        headStack.set(DataComponents.PROFILE, ResolvableProfile.createResolved(profile));
        return headStack;
    }

    public static <T> T getCustomDataValue(ItemStack stack, String key, BiFunction<CompoundTag, String, T> extractor) {
        CompoundTag nbt = getItemCustomData(stack);
        if (nbt != null && nbt.contains(key)) {
            try {
                return extractor.apply(nbt, key);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public static ItemStack setCustomDataValue(ItemStack stack, String key, BiConsumer<CompoundTag, String> inserter) {
        if (stack == null || stack.isEmpty()) return stack;
        CustomData.update(DataComponents.CUSTOM_DATA, stack, nbt -> {
            inserter.accept(nbt, key);
        });
        return stack;
    }
}
