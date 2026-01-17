package owo.pigeon.commands.impl;

import owo.pigeon.commands.Command;
import owo.pigeon.modules.impl.Client.PigeonQwQ;
import owo.pigeon.utils.Chat.ChatUtil;
import owo.pigeon.utils.CommandUtil;
import owo.pigeon.utils.ModuleUtil;

public class PrefixCommand extends Command {
    public PrefixCommand() {
        super("prefix");
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

        char newCommandPrefix = args[0].charAt(0);
        ModuleUtil.getModule(PigeonQwQ.class).commandPrefix.setValue(newCommandPrefix);

        ChatUtil.sendMessage("&aThe prefix has been set to '" + CommandUtil.getCommandPrefix() + "' !");
    }

    @Override
    public String getUsage() {
        return CommandUtil.getCommandPrefix() + "prefix <char>";
    }
}
