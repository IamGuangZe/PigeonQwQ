package owo.pigeon.commands.impl;

import net.minecraft.client.util.InputUtil;
import owo.pigeon.commands.Command;
import owo.pigeon.modules.Module;
import owo.pigeon.modules.ModuleManager;
import owo.pigeon.utils.CommandUtil;
import owo.pigeon.utils.ModuleUtil;
import owo.pigeon.utils.chat.ChatUtil;

import java.util.Map;
import java.util.TreeMap;

public class BindCommand extends Command {
    public BindCommand() {
        super("bind");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            CommandUtil.sendCommandError(CommandUtil.errorReason.UnknownOrIncompleteCommand,
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
                        CommandUtil.errorReason.UnknownOrIncompleteCommand,
                        this.getCommand(),
                        args,
                        args.length
                );
                return;
            }

            int keyCode = -1;
            String keyName = args[1].toLowerCase();

            try {
                keyCode = InputUtil
                        .fromTranslationKey("key.keyboard." + keyName)
                        .getCode();
            } catch (Exception ignored) {

            }

            module.setKey(keyCode);

            if (keyCode == -1) {
                ChatUtil.sendMessage("&a" + module.name + " has been unbound!");
            } else {
                String displayName = InputUtil.Type.KEYSYM
                        .createFromCode(keyCode)
                        .getTranslationKey()
                        .replace("key.keyboard.", "")
                        .toUpperCase();

                ChatUtil.sendMessage("&a" + module.name +
                        " has been bound to " + displayName +
                        " (keycode : " + keyCode + ") !");
            }

            return;
        }

        if (input.equalsIgnoreCase("list")) {

            Map<Integer, String> bindList = new TreeMap<>();

            for (Module module : ModuleManager.modules) {
                if (module.getKey() != -1) {
                    bindList.put(module.getKey(), module.name);
                }
            }

            ChatUtil.sendMessage("&8Key Bindings List:");
            for (Map.Entry<Integer, String> entry : bindList.entrySet()) {
                String keyName = InputUtil.Type.KEYSYM
                        .createFromCode(entry.getKey())
                        .getTranslationKey()
                        .replace("key.keyboard.", "")
                        .toUpperCase();

                ChatUtil.sendMessage("&7[" + keyName + "] " + entry.getValue());
            }
            return;
        }

        CommandUtil.sendCommandError(
                CommandUtil.errorReason.IncorrectArgument,
                this.getCommand(),
                args,
                0
        );
    }

    @Override
    public String getUsage() {
        return CommandUtil.getCommandPrefix() + "bind <module> <key>\n" +
                CommandUtil.getCommandPrefix() + "bind list [<page>]";
    }
}
