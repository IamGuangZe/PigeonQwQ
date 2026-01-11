package owo.pigeon.commands.impl;

import owo.pigeon.commands.Command;
import owo.pigeon.utils.Chat.ChatUtil;

public class HelpCommand extends Command {
    public HelpCommand() {
        super("help");
    }

    @Override
    public void execute(String[] args) {
        ChatUtil.sendMessage("Help message.");
    }
}
