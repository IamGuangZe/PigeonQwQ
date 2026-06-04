package owo.pigeon.commands.impl;

import net.minecraft.item.ItemStack;
import owo.pigeon.commands.Command;
import owo.pigeon.modules.impl.skyblock.misc.AutoCombine;
import owo.pigeon.settings.ListSetting;
import owo.pigeon.utils.CommandUtil;
import owo.pigeon.utils.ModuleUtil;
import owo.pigeon.utils.chat.ChatUtil;

import java.util.Arrays;
import java.util.Set;

import static owo.pigeon.Pigeon.mc;

public class AutoCombineCommand extends Command {
    public AutoCombineCommand() {
        super("autocombine");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            CommandUtil.sendCommandError(CommandUtil.ErrorReason.UnknownOrIncompleteCommand,
                    this.getCommand(), args, args.length);
            return;
        }

        String type = args[0].toLowerCase();
        if (!type.equals("enchant") && !type.equals("rune")) {
            CommandUtil.sendCommandError(CommandUtil.ErrorReason.IncorrectArgument,
                    this.getCommand(), args, 0);
            return;
        }

        AutoCombine autoCombine = ModuleUtil.getModule(AutoCombine.class);
        boolean isEnchant = type.equals("enchant");
        ListSetting list = isEnchant ? autoCombine.anvilCombineList : autoCombine.runeCombineList;

        String action = args[1].toLowerCase();
        switch (action) {
            case "add" -> {
                String value;
                if (args.length >= 3) {
                    value = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                } else {
                    value = getHeldKey(autoCombine, isEnchant);
                    if (value == null) {
                        ChatUtil.sendMessage("AutoCombine", "&cNo valid item held or unable to get info.");
                        return;
                    }
                }

                if (list.contains(value)) {
                    ChatUtil.sendMessage("AutoCombine", "&7" + type + " &7&l" + value + " &r&7already exists in list.");
                    return;
                }
                list.add(value);
                ChatUtil.sendMessage("AutoCombine", "&aAdded " + type + " &7&l" + value + " &r&ato combine list.");
            }
            case "remove" -> {
                String value;
                if (args.length >= 3) {
                    value = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                } else {
                    value = getHeldKey(autoCombine, isEnchant);
                    if (value == null) {
                        ChatUtil.sendMessage("AutoCombine", "&cNo valid item held or unable to get info.");
                        return;
                    }
                }

                if (!list.contains(value)) {
                    CommandUtil.sendCommandError(CommandUtil.ErrorReason.ListItemNotFound,
                            this.getCommand(), args, args.length >= 3 ? 2 : args.length);
                    return;
                }
                list.remove(value);
                ChatUtil.sendMessage("AutoCombine", "&aRemoved " + type + " &7&l" + value + " &r&afrom combine list.");
            }
            case "list" -> {
                if (list.size() == 0) {
                    ChatUtil.sendMessage("AutoCombine", "&7" + type + " combine list is empty.");
                } else {
                    ChatUtil.sendMessage("AutoCombine", "&7" + type + " combine list (" + list.size() + " items):");
                    for (int i = 0; i < list.size(); i++) {
                        ChatUtil.sendMessage("AutoCombine", "&7  " + (i + 1) + ". &f" + list.get(i));
                    }
                }
            }
            default -> {
                CommandUtil.sendCommandError(CommandUtil.ErrorReason.UnknownListAction,
                        this.getCommand(), args, 1);
            }
        }
    }

    private String getHeldKey(AutoCombine autoCombine, boolean isEnchant) {
        if (mc.player == null) return null;
        ItemStack heldStack = mc.player.getMainHandStack();
        if (heldStack.isEmpty()) return null;

        if (isEnchant) {
            if (!autoCombine.isValidBook(heldStack)) return null;
            Set<String> keys = autoCombine.getEnchantKeys(heldStack);
            return pickKey(keys);
        } else {
            if (!autoCombine.isValidRune(heldStack)) return null;
            Set<String> keys = autoCombine.getRuneKeys(heldStack);
            return pickKey(keys);
        }
    }

    private String pickKey(Set<String> keys) {
        if (keys.isEmpty()) return null;
        if (keys.size() == 1) return keys.iterator().next();
        for (String key : keys) {
            ChatUtil.sendMessage("AutoCombine", "&7  - &f" + key);
        }
        ChatUtil.sendMessage("AutoCombine", "&cMultiple keys found. Please specify one.");
        return null;
    }

    @Override
    public String getUsage() {
        return CommandUtil.getCommandPrefix() + "autocombine (enchant|rune) (add|remove|list) [<name:level>]";
    }
}
