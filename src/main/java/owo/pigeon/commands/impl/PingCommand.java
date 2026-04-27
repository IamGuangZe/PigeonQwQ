package owo.pigeon.commands.impl;

import owo.pigeon.commands.Command;
import owo.pigeon.utils.chat.ChatUtil;
import owo.pigeon.utils.world.ServerUtil;

public class PingCommand extends Command {
    public PingCommand() {
        super("ping");
    }

    @Override
    public void execute(String[] args) {
        int ping = ServerUtil.getAveragePing();

        String color;
        if (ping <= 50) {
            color = "&a";
        } else if (ping <= 100) {
            color = "&e";
        } else if (ping <= 200) {
            color = "&6";
        } else {
            color = "&c";
        }

        ChatUtil.sendMessage("Ping: " + color + ping + "ms");
    }

    @Override
    public String getUsage() {
        return super.getUsage();
    }
}
