package owo.pigeon.commands.impl;

import net.minecraft.item.ItemStack;
import owo.pigeon.commands.Command;
import owo.pigeon.modules.impl.skyblock.misc.AutoCombine;
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
        if (args.length < 1) {
            CommandUtil.sendCommandError(CommandUtil.errorReason.UnknownOrIncompleteCommand,
                    this.getCommand(), args, args.length);
            return;
        }

        AutoCombine autoCombine = ModuleUtil.getModule(AutoCombine.class);

        String action = args[0].toLowerCase();
        switch (action) {
            case "add" -> {
                String value;
                if (args.length >= 2) {
                    value = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                } else {
                    value = getHeldEnchantKey(autoCombine);
                    if (value == null) {
                        ChatUtil.sendMessage("AutoCombine", "&cNo enchanted book held or unable to get enchant info.");
                        return;
                    }
                }

                if (autoCombine.anvilCombineList.contains(value)) {
                    ChatUtil.sendMessage("AutoCombine", "&7Enchant &7&l" + value + " &r&7already exists in list.");
                    return;
                }
                autoCombine.anvilCombineList.add(value);
                ChatUtil.sendMessage("AutoCombine", "&aAdded enchant &7&l" + value + " &r&ato combine list.");
            }
            case "remove" -> {
                String value;
                if (args.length >= 2) {
                    value = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                } else {
                    value = getHeldEnchantKey(autoCombine);
                    if (value == null) {
                        ChatUtil.sendMessage("AutoCombine", "&cNo enchanted book held or unable to get enchant info.");
                        return;
                    }
                }

                if (!autoCombine.anvilCombineList.contains(value)) {
                    CommandUtil.sendCommandError(CommandUtil.errorReason.ListItemNotFound,
                            this.getCommand(), args, args.length >= 2 ? 1 : args.length);
                    return;
                }
                autoCombine.anvilCombineList.remove(value);
                ChatUtil.sendMessage("AutoCombine", "&aRemoved enchant &7&l" + value + " &r&afrom combine list.");
            }
            case "list" -> {
                if (autoCombine.anvilCombineList.size() == 0) {
                    ChatUtil.sendMessage("AutoCombine", "&7Combine list is empty.");
                } else {
                    ChatUtil.sendMessage("AutoCombine", "&7Combine list (" + autoCombine.anvilCombineList.size() + " enchants):");
                    for (int i = 0; i < autoCombine.anvilCombineList.size(); i++) {
                        ChatUtil.sendMessage("AutoCombine", "&7  " + (i + 1) + ". &f" + autoCombine.anvilCombineList.get(i));
                    }
                }
            }
            default -> {
                CommandUtil.sendCommandError(CommandUtil.errorReason.UnknownListAction,
                        this.getCommand(), args, 0);
            }
        }
    }

    private String getHeldEnchantKey(AutoCombine autoCombine) {
        if (mc.player == null) return null;
        ItemStack heldStack = mc.player.getMainHandStack();
        if (heldStack.isEmpty()) return null;
        if (!autoCombine.isValidBook(heldStack)) return null;

        Set<String> keys = autoCombine.getKeys(heldStack);
        if (keys.isEmpty()) return null;

        if (keys.size() == 1) return keys.iterator().next();

        for (String key : keys) {
            ChatUtil.sendMessage("AutoCombine", "&7  - &f" + key);
        }
        ChatUtil.sendMessage("AutoCombine", "&cMultiple enchants found. Please specify one.");
        return null;
    }

    @Override
    public String getUsage() {
        return CommandUtil.getCommandPrefix() + "autocombine (add|remove|list) [<enchant:level>]";
    }
}
