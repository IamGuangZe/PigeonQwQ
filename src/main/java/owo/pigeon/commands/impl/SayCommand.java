package owo.pigeon.commands.impl;

import owo.pigeon.commands.Command;
import owo.pigeon.commands.CommandManager;
import owo.pigeon.utils.CommandUtil;

import static owo.pigeon.Pigeon.mc;

public class SayCommand extends Command {
    public SayCommand() {
        super("say");
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            CommandUtil.sendCommandError(CommandUtil.ErrorReason.UnknownOrIncompleteCommand,
                    this.getCommand(),
                    args,
                    args.length
            );
            return;
        }
        String message = String.join(" ", args);
        CommandManager.isSay = true;
        mc.player.networkHandler.sendChatMessage(message);
    }

    @Override
    public String getUsage() {
        return CommandUtil.getCommandPrefix() + "say <message>";
    }
}
