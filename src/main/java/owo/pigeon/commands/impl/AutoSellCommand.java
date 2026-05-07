package owo.pigeon.commands.impl;

import net.minecraft.item.ItemStack;
import owo.pigeon.commands.Command;
import owo.pigeon.modules.impl.skyblock.misc.AutoSell;
import owo.pigeon.utils.ColorUtil;
import owo.pigeon.utils.CommandUtil;
import owo.pigeon.utils.ModuleUtil;
import owo.pigeon.utils.chat.ChatUtil;
import owo.pigeon.utils.hypixel.skyblock.SkyblockUtil;

import java.util.Arrays;

import static owo.pigeon.Pigeon.mc;

public class AutoSellCommand extends Command {
    public AutoSellCommand() {
        super("autosell");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            CommandUtil.sendCommandError(CommandUtil.errorReason.UnknownOrIncompleteCommand,
                    this.getCommand(), args, args.length);
            return;
        }

        String type = args[0].toLowerCase();
        if (!type.equals("id") && !type.equals("name")) {
            CommandUtil.sendCommandError(CommandUtil.errorReason.IncorrectArgument,
                    this.getCommand(), args, 0);
            return;
        }

        AutoSell autoSell = ModuleUtil.getModule(AutoSell.class);
        boolean isId = type.equals("id");

        String action = args[1].toLowerCase();
        switch (action) {
            case "add" -> {
                String value;
                if (args.length >= 3) {
                    value = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                } else {
                    // Fallback to held item info
                    value = getHeldItemInfo(isId);
                    if (value == null) {
                        ChatUtil.sendMessage("AutoSell", "&cNo item held or unable to get item info.");
                        return;
                    }
                }

                if (isId) {
                    if (autoSell.itemId.contains(value)) {
                        ChatUtil.sendMessage("AutoSell", "&7Item ID &7&l" + value + " &r&7already exists in list.");
                        return;
                    }
                    autoSell.itemId.add(value);
                    ChatUtil.sendMessage("AutoSell", "&aAdded ID &7&l" + value + " &r&ato auto-sell list.");
                } else {
                    if (autoSell.itemName.contains(value)) {
                        ChatUtil.sendMessage("AutoSell", "&7Item name &7&l" + value + " &r&7already exists in list.");
                        return;
                    }
                    autoSell.itemName.add(value);
                    ChatUtil.sendMessage("AutoSell", "&aAdded name &7&l" + value + " &r&ato auto-sell list.");
                }
            }
            case "remove" -> {
                String value;
                if (args.length >= 3) {
                    value = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                } else {
                    value = getHeldItemInfo(isId);
                    if (value == null) {
                        ChatUtil.sendMessage("AutoSell", "&cNo item held or unable to get item info.");
                        return;
                    }
                }

                if (isId) {
                    if (!autoSell.itemId.contains(value)) {
                        CommandUtil.sendCommandError(CommandUtil.errorReason.ListItemNotFound,
                                this.getCommand(), args, args.length >= 3 ? 2 : args.length);
                        return;
                    }
                    autoSell.itemId.remove(value);
                    ChatUtil.sendMessage("AutoSell", "&aRemoved ID &7&l" + value + " &r&afrom auto-sell list.");
                } else {
                    if (!autoSell.itemName.contains(value)) {
                        CommandUtil.sendCommandError(CommandUtil.errorReason.ListItemNotFound,
                                this.getCommand(), args, args.length >= 3 ? 2 : args.length);
                        return;
                    }
                    autoSell.itemName.remove(value);
                    ChatUtil.sendMessage("AutoSell", "&aRemoved name &7&l" + value + " &r&afrom auto-sell list.");
                }
            }
            case "list" -> {
                if (isId) {
                    if (autoSell.itemId.size() == 0) {
                        ChatUtil.sendMessage("AutoSell", "&7Auto-sell ID list is empty.");
                    } else {
                        ChatUtil.sendMessage("AutoSell", "&7Auto-sell ID list (" + autoSell.itemId.size() + " items):");
                        for (int i = 0; i < autoSell.itemId.size(); i++) {
                            ChatUtil.sendMessage("AutoSell", "&7  " + (i + 1) + ". &f" + autoSell.itemId.get(i));
                        }
                    }
                } else {
                    if (autoSell.itemName.size() == 0) {
                        ChatUtil.sendMessage("AutoSell", "&7Auto-sell name list is empty.");
                    } else {
                        ChatUtil.sendMessage("AutoSell", "&7Auto-sell name list (" + autoSell.itemName.size() + " items):");
                        for (int i = 0; i < autoSell.itemName.size(); i++) {
                            ChatUtil.sendMessage("AutoSell", "&7  " + (i + 1) + ". &f" + autoSell.itemName.get(i));
                        }
                    }
                }
            }
            default -> {
                CommandUtil.sendCommandError(CommandUtil.errorReason.UnknownListAction,
                        this.getCommand(), args, 1);
            }
        }
    }

    private String getHeldItemInfo(boolean isId) {
        ItemStack heldStack = mc.player.getMainHandStack();
        if (heldStack.isEmpty()) return null;

        if (isId) {
            return SkyblockUtil.getItemCustomData(heldStack, "id");
        } else {
            return ColorUtil.removeColor(heldStack.getName().getString());
        }
    }

    @Override
    public String getUsage() {
        return CommandUtil.getCommandPrefix() + "autosell (id|name) (add|remove|list) [<value>]";
    }
}
