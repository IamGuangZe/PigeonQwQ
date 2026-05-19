package owo.pigeon.commands.impl;

import owo.pigeon.commands.Command;
import owo.pigeon.config.configs.SettingConfig;
import owo.pigeon.utils.CommandUtil;
import owo.pigeon.utils.chat.ChatUtil;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class ConfigCommand extends Command {
    public ConfigCommand() {
        super("config");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            CommandUtil.sendCommandError(CommandUtil.errorReason.UnknownOrIncompleteCommand,
                    getCommand(),
                    args,
                    args.length
            );
            return;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "save": {
                if (args.length < 2) {
                    CommandUtil.sendCommandError(CommandUtil.errorReason.UnknownOrIncompleteCommand,
                            getCommand(), args, args.length);
                    return;
                }
                new SettingConfig(args[1]).save();
                break;
            }

            case "load": {
                if (args.length < 2) {
                    CommandUtil.sendCommandError(CommandUtil.errorReason.UnknownOrIncompleteCommand,
                            getCommand(), args, args.length);
                    return;
                }
                new SettingConfig(args[1]).load();
                break;
            }

            case "rename": {
                if (args.length < 3) {
                    CommandUtil.sendCommandError(CommandUtil.errorReason.UnknownOrIncompleteCommand,
                            getCommand(), args, args.length);
                    return;
                }
                File dir = new SettingConfig().getBaseDir();
                File oldFile = new File(dir, args[1] + ".json");
                File newFile = new File(dir, args[2] + ".json");

                if (!oldFile.exists()) {
                    sendCommandError("Unknown config &o" + args[1]);
                    return;
                }
                if (newFile.exists()) {
                    sendCommandError("Config &o" + args[2] + " already exists!");
                    return;
                }
                if (oldFile.renameTo(newFile)) {
                    ChatUtil.sendMessage("&aConfig &o" + args[1] + ".json &ahas been renamed to &o" + args[2] + ".json");
                } else {
                    sendCommandError("Failed to rename config!");
                }
                break;
            }

            case "delete": {
                if (args.length < 2) {
                    CommandUtil.sendCommandError(CommandUtil.errorReason.UnknownOrIncompleteCommand,
                            getCommand(), args, args.length);
                    return;
                }
                File file = new File(new SettingConfig().getBaseDir(), args[1] + ".json");
                if (!file.exists()) {
                    sendCommandError("Unknown config &o" + args[1]);
                    return;
                }
                if (file.delete()) {
                    ChatUtil.sendMessage("&aConfig &o" + args[1] + ".json &r&ahas been deleted.");
                } else {
                    sendCommandError("Failed to delete config!");
                }
                break;
            }

            case "list": {
                File dir = new SettingConfig().getBaseDir();
                if (!dir.exists() || !dir.isDirectory()) {
                    sendCommandError("No configs found!");
                    return;
                }

                String[] files = dir.list((d, name) -> name.endsWith(".json"));
                if (files == null || files.length == 0) {
                    sendCommandError("No configs found!");
                    return;
                }

                int page = 1;
                int perPage = 7;
                try {
                    if (args.length > 1) {
                        int input = Integer.parseInt(args[1]);
                        if (input > 0) page = input;
                    }
                } catch (NumberFormatException ignored) {
                }

                int maxPage = (int) Math.ceil((double) files.length / perPage);
                if (page > maxPage) page = maxPage;

                int start = (page - 1) * perPage;
                int end = Math.min(start + perPage, files.length);

                ChatUtil.sendMessage("&8Config List (Page " + page + "/" + maxPage + "):");
                for (int i = start; i < end; i++) {
                    ChatUtil.sendMessage("&7- " + files[i].replace(".json", ""));
                }
                ChatUtil.sendMessage("&8Use \"" + CommandUtil.getCommandPrefix() + " config list <page>\" to view other pages.");
                break;
            }

            case "dir": {
                File dir = new SettingConfig().getBaseDir();
                if (!dir.exists()) dir.mkdirs();
                try {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(dir);
                        ChatUtil.sendMessage("&aOpened config folder: &7" + dir.getAbsolutePath());
                    } else {
                        sendCommandError("Desktop is not supported on this system!");
                    }
                } catch (IOException e) {
                    sendCommandError("Failed to open config folder: " + e.getMessage());
                }
                break;
            }

            default: {
                CommandUtil.sendCommandError(CommandUtil.errorReason.IncorrectArgument,
                        getCommand(),
                        args,
                        0
                );
                break;
            }
        }
    }

    @Override
    public String getUsage() {
        return CommandUtil.getCommandPrefix() + "config (save|load|delete) <name>\n" +
                CommandUtil.getCommandPrefix() + "config rename <oldname> <newname>\n" +
                CommandUtil.getCommandPrefix() + "config list [<page>]\n" +
                CommandUtil.getCommandPrefix() + "config dir";
    }
}
