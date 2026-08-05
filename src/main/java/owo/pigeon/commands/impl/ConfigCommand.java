package owo.pigeon.commands.impl;

import owo.pigeon.commands.Command;
import owo.pigeon.config.configs.SettingConfig;
import owo.pigeon.utils.CommandUtil;
import owo.pigeon.utils.chat.ChatUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigCommand extends Command {
    public ConfigCommand() {
        super("config");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            CommandUtil.sendCommandError(CommandUtil.ErrorReason.UnknownOrIncompleteCommand,
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
                    CommandUtil.sendCommandError(CommandUtil.ErrorReason.UnknownOrIncompleteCommand,
                            getCommand(), args, args.length);
                    return;
                }
                if (isInvalidConfigName(args[1])) {
                    ChatUtil.sendMessage("&cInvalid config name! Cannot contain path separators.");
                    return;
                }
                new SettingConfig(args[1]).save();
                break;
            }

            case "load": {
                if (args.length < 2) {
                    CommandUtil.sendCommandError(CommandUtil.ErrorReason.UnknownOrIncompleteCommand,
                            getCommand(), args, args.length);
                    return;
                }
                if (isInvalidConfigName(args[1])) {
                    ChatUtil.sendMessage("&cInvalid config name! Cannot contain path separators.");
                    return;
                }
                new SettingConfig(args[1]).load();
                break;
            }

            case "rename": {
                if (args.length < 3) {
                    CommandUtil.sendCommandError(CommandUtil.ErrorReason.UnknownOrIncompleteCommand,
                            getCommand(), args, args.length);
                    return;
                }
                if (isInvalidConfigName(args[1]) || isInvalidConfigName(args[2])) {
                    ChatUtil.sendMessage("&cInvalid config name! Cannot contain path separators.");
                    return;
                }

                File dir = new SettingConfig().getBaseDir();
                File oldFile = new File(dir, args[1] + ".json");
                File newFile = new File(dir, args[2] + ".json");

                try {
                    Path oldPath = oldFile.toPath().normalize();
                    Path newPath = newFile.toPath().normalize();
                    Path dirPath = dir.toPath().normalize();

                    if (!oldPath.startsWith(dirPath) || !newPath.startsWith(dirPath)) {
                        ChatUtil.sendMessage("&cAccess denied: Cannot operate outside config directory!");
                        return;
                    }

                    if (!Files.exists(oldPath)) {
                        ChatUtil.sendMessage("&cUnknown config &o" + args[1]);
                        return;
                    }
                    if (Files.exists(newPath)) {
                        ChatUtil.sendMessage("&cConfig &o" + args[2] + "&r&c already exists!");
                        return;
                    }

                    Files.move(oldPath, newPath);
                    ChatUtil.sendMessage("&aConfig &o" + args[1] + ".json &ahas been renamed to &o" + args[2] + ".json");

                } catch (IOException e) {
                    ChatUtil.sendMessage("&cFailed to rename config: " + e.getMessage());
                }
                break;
            }

            case "delete": {
                if (args.length < 2) {
                    CommandUtil.sendCommandError(CommandUtil.ErrorReason.UnknownOrIncompleteCommand,
                            getCommand(), args, args.length);
                    return;
                }
                if (isInvalidConfigName(args[1])) {
                    ChatUtil.sendMessage("&cInvalid config name! Cannot contain path separators.");
                    return;
                }

                File file = new File(new SettingConfig().getBaseDir(), args[1] + ".json");

                try {
                    Path filePath = file.toPath().normalize();
                    Path dirPath = new SettingConfig().getBaseDir().toPath().normalize();

                    if (!filePath.startsWith(dirPath)) {
                        ChatUtil.sendMessage("&cAccess denied: Cannot operate outside config directory!");
                        return;
                    }

                    if (!Files.exists(filePath)) {
                        ChatUtil.sendMessage("&cUnknown config &o" + args[1]);
                        return;
                    }

                    Files.delete(filePath);
                    ChatUtil.sendMessage("&aConfig &o" + args[1] + ".json &r&ahas been deleted.");

                } catch (IOException e) {
                    ChatUtil.sendMessage("&cFailed to delete config: " + e.getMessage());
                }
                break;
            }

            case "list": {
                File dir = new SettingConfig().getBaseDir();
                if (!dir.exists() || !dir.isDirectory()) {
                    ChatUtil.sendMessage("&cNo configs found!");
                    return;
                }

                String[] files = dir.list((d, name) -> name.endsWith(".json"));
                if (files == null || files.length == 0) {
                    ChatUtil.sendMessage("&cNo configs found!");
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
                    String os = System.getProperty("os.name").toLowerCase(java.util.Locale.ROOT);
                    ChatUtil.sendDebugMessage("Config", "Operating system: " + os);
                    ProcessBuilder processBuilder;

                    if (os.contains("win")) {
                        // Windows: 使用 explorer 命令
                        processBuilder = new ProcessBuilder("explorer", dir.getAbsolutePath());
                    } else if (os.contains("mac")) {
                        // macOS: 使用 open 命令
                        processBuilder = new ProcessBuilder("open", dir.getAbsolutePath());
                    } else {
                        // Linux/Unix: 使用 xdg-open 命令
                        processBuilder = new ProcessBuilder("xdg-open", dir.getAbsolutePath());
                    }

                    processBuilder.start();
                    ChatUtil.sendMessage("&aOpened config folder: &7" + dir.getAbsolutePath());

                } catch (IOException e) {
                    ChatUtil.sendMessage("&cFailed to open config folder: " + e.getMessage());
                }
                break;
            }
            default: {
                CommandUtil.sendCommandError(CommandUtil.ErrorReason.IncorrectArgument,
                        getCommand(),
                        args,
                        0
                );
                break;
            }
        }
    }

    private boolean isInvalidConfigName(String name) {
        if (name == null || name.isEmpty()) return true;
        return name.contains("/") || name.contains("\\") || name.contains("..") || name.contains(File.separator);
    }

    @Override
    public String getUsage() {
        return CommandUtil.getCommandPrefix() + "config (save|load|delete) <name>\n" +
                CommandUtil.getCommandPrefix() + "config rename <oldname> <newname>\n" +
                CommandUtil.getCommandPrefix() + "config list [<page>]\n" +
                CommandUtil.getCommandPrefix() + "config dir";
    }
}
