package owo.pigeon.commands.impl;

import owo.pigeon.commands.Command;
import owo.pigeon.utils.CommandUtil;
import owo.pigeon.utils.chat.ChatUtil;
import owo.pigeon.utils.export.ExportManager;
import owo.pigeon.utils.hypixel.skyblock.SkyblockUtil;

public class ExportCommand extends Command {
    public ExportCommand() {
        super("export");
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            CommandUtil.sendCommandError(CommandUtil.errorReason.ExpectedInteger,
                    this.getCommand(),
                    args,
                    args.length
            );
            return;
        }

        switch (args[0].toLowerCase()) {
            case "hb","hunting_box" -> {
                if (!SkyblockUtil.isInSkyblock()) {
                    ChatUtil.sendMessage("Export","Can only be used in Hypixel Skyblock!");
                    return;
                }
                ExportManager.startExport(ExportManager.ExportTask.HUNTING_BOX);
            }

            default -> {
                CommandUtil.sendCommandError(CommandUtil.errorReason.IncorrectArgument,
                        this.getCommand(),
                        args,
                        0
                );
                return;
            }
        }
    }

    @Override
    public String getUsage() {
        return CommandUtil.getCommandPrefix() + "export (hb|hunting_box)";
    }
}
