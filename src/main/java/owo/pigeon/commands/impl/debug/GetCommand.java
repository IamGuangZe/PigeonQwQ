package owo.pigeon.commands.impl.debug;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import owo.pigeon.commands.Command;
import owo.pigeon.utils.ItemUtil;
import owo.pigeon.utils.ScoreBoardUtil;
import owo.pigeon.utils.chat.ChatUtil;
import owo.pigeon.utils.hypixel.HypixelUtil;
import owo.pigeon.utils.hypixel.skyblock.DungeonUtil;
import owo.pigeon.utils.hypixel.skyblock.SkyblockUtil;

import java.util.List;
import java.util.Set;

import static owo.pigeon.Pigeon.mc;

public class GetCommand extends Command {
    public GetCommand() {
        super("get");
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) return;

        switch (args[0].toLowerCase()) {
            case "item", "hand", "i" -> {
                int slot;

                if (args.length == 1) slot = mc.player.getInventory().getSelectedSlot();
                else slot = Integer.parseInt(args[1]);

                if (slot < 0 || slot > 8) {
                    ChatUtil.sendDebugMessage("Item", "Slot must between 0 and 8!");
                    return;
                }

                ItemStack stack = ItemUtil.getItemStackfromSlot(slot);

                if (stack.isEmpty()) {
                    ChatUtil.sendDebugMessage("Item", "There is no item in this slot!");
                    return;
                }

                ChatUtil.sendDebugMessage("Item", "Name: " + stack.getHoverName().getString());
                ChatUtil.sendDebugMessage("Item", "Identifier: " + stack.getItem());
                ChatUtil.sendDebugMessage("Item", "Count: " + stack.getCount());
                ChatUtil.sendDebugMessage("Item", "Customdata: " + ItemUtil.getItemCustomData(stack));

                if (stack.is(Items.PLAYER_HEAD)) {
                    String texture = ItemUtil.getSkullTexture(stack);
                    if (texture != null) {
                        ChatUtil.sendDebugMessage("Item", "Skull Texture: " + texture);
                    }
                }

                ItemEnchantments enchants = stack.getEnchantments();
                if (!enchants.isEmpty()) {
                    ChatUtil.sendDebugMessage("Item", "");
                    ChatUtil.sendDebugMessage("Item", "Enchantments:");
                    enchants.keySet().forEach(enchantment -> {
                        String name = enchantment.value().description().getString();
                        int level = enchants.getLevel(enchantment);
                        ChatUtil.sendDebugMessage("Item", " - " + name + " " + level);
                    });
                }

                List<Component> loreLines = ItemUtil.getItemLore(stack);
                if (!loreLines.isEmpty()) {
                    ChatUtil.sendDebugMessage("Item", "");
                    ChatUtil.sendDebugMessage("Item", "Lore:");
                    for (Component line : loreLines) {
                        ChatUtil.sendDebugMessage("Item", " - " + line.getString());
                    }
                }

                Set<String> abilities = SkyblockUtil.getItemAbilityNames(stack);
                if (!abilities.isEmpty()) {
                    ChatUtil.sendDebugMessage("Item", "");
                    ChatUtil.sendDebugMessage("Item", "Ability:");

                    abilities.forEach((value) ->
                            ChatUtil.sendDebugMessage("Item", " - " + value)
                    );
                }
            }

            case "armorstand", "a" -> {
                for (Entity entity : mc.level.entitiesForRendering()) {
                    if (entity instanceof ArmorStand stand) {
                        ChatUtil.sendDebugMessage("ArmorStand", "name: " + stand.getName().getString());
                    }
                }
            }

            case "sidebar", "s" -> {
                String title = ScoreBoardUtil.getSidebarTitle();
                List<String> lines = ScoreBoardUtil.getSidebarLines();
                ChatUtil.sendDebugMessage("Sidebar", title);
                for (String line : lines)
                    ChatUtil.sendDebugMessage("Sidebar", line);
            }

            case "tab", "t" -> {
                List<String> lines = ScoreBoardUtil.getTabLines();
                for (String line : lines)
                    ChatUtil.sendDebugMessage("Tab", line);
            }

            case "hypixel", "skyblock", "hyp", "skb" -> {
                ChatUtil.sendDebugMessage("Hypixel", "isInHypixel: " + HypixelUtil.isInHypixel());
                ChatUtil.sendDebugMessage("Skyblock", "isInSkyblock: " + SkyblockUtil.isInSkyblock());

                if (SkyblockUtil.isInSkyblock()) {
                    ChatUtil.sendDebugMessage("Skyblock", "island: " + SkyblockUtil.getIsland().getDisplayName());
                    if (DungeonUtil.isInDungeon()) {
                        ChatUtil.sendDebugMessage("Skyblock", "Floor: " + DungeonUtil.getFloor());
                        ChatUtil.sendDebugMessage("Skyblock", "isInBoss: " + DungeonUtil.isInBoss());
                        if (DungeonUtil.isInFloor(7) && DungeonUtil.isInBoss(7)) {
                            ChatUtil.sendDebugMessage("Skyblock", "Floor 7 Stage: " + DungeonUtil.getFloor7Stage());
                        }
                    }
                }
            }
        }
    }
}
