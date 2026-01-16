package owo.pigeon.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import owo.pigeon.utils.Chat.ChatUtil;
import owo.pigeon.utils.CommandUtil;

public abstract class Command {
    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

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