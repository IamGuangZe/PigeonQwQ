package owo.pigeon.commands;

import owo.pigeon.utils.Chat.ChatUtil;
import owo.pigeon.utils.CommandUtil;

public abstract class Command {
    private final String command;

    public Command(String command) {
        this.command = command;
    }

    abstract public void execute(String[] args);

    public String getCommand() {
        return command;
    }

    public String getUsage() {
        return CommandUtil.getCommandPrefix() + command;
    }

    public void sendCommandError(String message) {
        ChatUtil.sendMessage("&c" + message);
    }

    public void sendUsage() {
        ChatUtil.sendMultiLineMessage(getUsage());
    }
}