package owo.pigeon.commands.impl;

import owo.pigeon.commands.Command;
import owo.pigeon.utils.Chat.ChatUtil;
import owo.pigeon.utils.CommandUtil;

import static owo.pigeon.Pigeonqwq.mc;

public class CopyCommand extends Command {
    public CopyCommand() {
        super("copy");
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            CommandUtil.sendCommandError(CommandUtil.errorReason.UnknownOrIncompleteCommand,
                    this.getCommand(),
                    args,
                    args.length
            );
            return;
        }
        String message = String.join(" ", args);
        mc.keyboard.setClipboard(message);
        ChatUtil.sendMessage("You copied the text : " + message);
    }

    @Override
    public String getUsage() {
        return CommandUtil.getCommandPrefix() + "copy <message>";
    }
}
