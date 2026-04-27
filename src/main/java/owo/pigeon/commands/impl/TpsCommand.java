package owo.pigeon.commands.impl;

import owo.pigeon.commands.Command;
import owo.pigeon.utils.chat.ChatUtil;
import owo.pigeon.utils.world.ServerUtil;

public class TpsCommand extends Command {
    public TpsCommand() {
        super("tps");
    }

    @Override
    public void execute(String[] args) {
        float tps = ServerUtil.getTps();

        String color;
        if (tps >= 19.5f) {
            color = "&a";
        } else if (tps >= 18.0f) {
            color = "&e";
        } else if (tps >= 15.0f) {
            color = "&6";
        } else {
            color = "&c";
        }

        ChatUtil.sendMessage("TPS: " + color + String.format("%.1f", tps));
    }

    @Override
    public String getUsage() {
        return super.getUsage();
    }
}
