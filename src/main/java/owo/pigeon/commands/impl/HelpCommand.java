package owo.pigeon.commands.impl;

import owo.pigeon.commands.Command;
import owo.pigeon.commands.CommandManager;
import owo.pigeon.utils.Chat.ChatUtil;
import owo.pigeon.utils.CommandUtil;

public class HelpCommand extends Command {
    public HelpCommand() {
        super("help");
    }

    @Override
    public void execute(String[] args) {
        if (args.length > 0) {
            String input = args[0];

            boolean found = false;
            for (Command command : CommandManager.commands) {
                if (command.getCommand().equalsIgnoreCase(input)) {
                    command.sendUsage();
                    found = true;
                }
            }

            if (found) return;
        }

        int maxpage = (int) Math.ceil((double) CommandManager.commands.size() / 7);
        int page = 1;
        try {
            if (args.length > 0) {
                int inputpage = Integer.parseInt(args[0]);
                if (inputpage > 0 && inputpage <= maxpage) {
                    page = inputpage;
                }
            }
        } catch (NumberFormatException ignored) {

        }

        int start = (page - 1) * 7;
        int end = Math.min(start + 7, CommandManager.commands.size());

        ChatUtil.sendMessage("&8Command List (Page " + page + "/" + maxpage + ")");

        for (int i = start; i < end; i++) {
            if (i > CommandManager.commands.size() - 1) break;
            ChatUtil.sendMessage("&7" + CommandUtil.getCommandPrefix() + " " + CommandManager.commands.get(i).getCommand().toLowerCase());
        }

        ChatUtil.sendMessage("&8Use \"" + CommandUtil.getCommandPrefix() + " help <page>\" to view other commands.");
    }

    @Override
    public String getUsage() {
        return CommandUtil.getCommandPrefix() + "help [<page|command>]";
    }
}
