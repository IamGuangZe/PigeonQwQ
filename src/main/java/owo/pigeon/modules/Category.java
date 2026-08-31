package owo.pigeon.modules;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import owo.pigeon.utils.ItemUtil;
import owo.pigeon.utils.hypixel.skyblock.SkyblockUtil;

import java.util.function.Supplier;

public enum Category {
    CLIENT(() -> Items.GRASS_BLOCK.getDefaultInstance()),
    COMBAT(() -> Items.DIAMOND_SWORD.getDefaultInstance()),
    DUNGEON(() -> ItemUtil.getSkullFromTexture(SkyblockUtil.DUNGEONEERING)),
    EVENT(() -> Items.CLOCK.getDefaultInstance()),
    FARMING(() -> Items.DIAMOND_HOE.getDefaultInstance()),
    HUNTING(() -> Items.LEAD.getDefaultInstance()),
    HYPIXEL(() -> Items.GOLD_INGOT.getDefaultInstance()),
    MINING(() -> Items.DIAMOND_PICKAXE.getDefaultInstance()),
    MISC(() -> Items.SLIME_BALL.getDefaultInstance()),
    MOVEMENT(() -> Items.DIAMOND_BOOTS.getDefaultInstance()),
    NETHER(() -> Items.NETHERRACK.getDefaultInstance()),
    PLAYER(() -> Items.PLAYER_HEAD.getDefaultInstance()),
    RENDER(() -> Items.ENDER_EYE.getDefaultInstance()),
    RIFT(() -> {
        ItemStack icon = Items.MYCELIUM.getDefaultInstance();
        icon.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        return icon;
    }),
    SLAYER(() -> {
        ItemStack icon = Items.ROTTEN_FLESH.getDefaultInstance();
        ItemUtil.setCustomDataValue(icon, "id", (nbt, k) -> nbt.putString(k, "REVENANT_FLESH"));
        return icon;
    }),
    WORLD(() -> Items.FILLED_MAP.getDefaultInstance()),
    DEBUG(() -> Items.TEST_INSTANCE_BLOCK.getDefaultInstance());

    private final Supplier<ItemStack> iconFactory;
    private ItemStack icon;

    Category(Supplier<ItemStack> iconFactory) {
        this.iconFactory = iconFactory;
    }

    public void setIcon(ItemStack icon) {
        this.icon = icon;
    }

    public ItemStack getIcon() {
        if (icon == null) {
            icon = iconFactory.get();
        }
        return icon;
    }
}
