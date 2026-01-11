package owo.pigeon.commands;

import owo.pigeon.modules.impl.Client.PigeonQwQ;
import owo.pigeon.utils.Chat.ChatUtil;
import owo.pigeon.utils.ModuleUtil;

public abstract class Command {
    private final String command;

    abstract public void execute(String[] args);

    public Command(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public String getUsage() {
        return ((PigeonQwQ) ModuleUtil.getModule(PigeonQwQ.class)).commandPrefix + command;
    }

    public void sendCommandError(String message) {
        ChatUtil.sendMessage("&c" + message);
    }

    public void sendUsage() {
        ChatUtil.sendMultiLineMessage(getUsage());
    }
}