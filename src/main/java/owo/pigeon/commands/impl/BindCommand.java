package owo.pigeon.commands.impl;

import com.mojang.blaze3d.platform.InputConstants;
import owo.pigeon.commands.Command;
import owo.pigeon.modules.Module;
import owo.pigeon.modules.ModuleManager;
import owo.pigeon.utils.CommandUtil;
import owo.pigeon.utils.KeybindUtil;
import owo.pigeon.utils.ModuleUtil;
import owo.pigeon.utils.chat.ChatUtil;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class BindCommand extends Command {
    public BindCommand() {
        super("bind");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            CommandUtil.sendCommandError(CommandUtil.ErrorReason.UnknownOrIncompleteCommand,
                    this.getCommand(),
                    args,
                    args.length
            );
            return;
        }

        String input = args[0];
        if (ModuleUtil.isModuleExist(input)) {
            Module module = ModuleUtil.getModule(input);

            if (args.length < 2) {
                CommandUtil.sendCommandError(
                        CommandUtil.ErrorReason.UnknownOrIncompleteCommand,
                        this.getCommand(),
                        args,
                        args.length
                );
                return;
            }

            int keyCode = -1;
            String keyName = args[1].toLowerCase();

            try {
                keyCode = InputConstants
                        .getKey("key.keyboard." + keyName)
                        .getValue();
            } catch (Exception ignored) {
            }

            module.setKey(keyCode);

            if (keyCode == -1) {
                ChatUtil.sendMessage("&a" + module.name + " has been unbound!");
            } else {
                String displayName = KeybindUtil.getKeyDisplayName(keyCode);
                ChatUtil.sendMessage("&a" + module.name + " has been bound to " + displayName + " (keycode : " + keyCode + ") !");
            }

            return;
        }

        if (input.equalsIgnoreCase("list")) {
            Map<Integer, List<String>> bindList = ModuleManager.modules.stream()
                    .filter(module -> module.getKey() != -1)
                    .collect(Collectors.groupingBy(
                            Module::getKey,
                            TreeMap::new,
                            Collectors.mapping(
                                    module -> module.name,
                                    Collectors.toList()
                            )
                    ));

            ChatUtil.sendMessage("&8Key Bindings List:");
            for (Map.Entry<Integer, List<String>> entry : bindList.entrySet()) {
                String keyName = KeybindUtil.getKeyDisplayName(entry.getKey());
                String modules = String.join(", ", entry.getValue());
                ChatUtil.sendMessage("&7[" + keyName + "] " + modules);
            }
            return;
        }

        CommandUtil.sendCommandError(
                CommandUtil.ErrorReason.IncorrectArgument,
                this.getCommand(),
                args,
                0
        );
    }

    @Override
    public String getUsage() {
        return CommandUtil.getCommandPrefix() + "bind <module> <key>\n" +
                CommandUtil.getCommandPrefix() + "bind list";
    }
}