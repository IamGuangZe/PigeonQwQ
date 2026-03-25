package owo.pigeon.commands.impl;

import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import owo.pigeon.commands.Command;
import owo.pigeon.commands.CommandManager;
import owo.pigeon.utils.Chat.ChatUtil;
import owo.pigeon.utils.ColorUtil;
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

            String cmdName = CommandManager.commands.get(i).getCommand().toLowerCase();
            String fullCommand = CommandUtil.getCommandPrefix() + cmdName;

            MutableText commandText = Text.literal(ColorUtil.parseColor("&7" + fullCommand))
                    .styled(style -> style
                            .withClickEvent(new ClickEvent.SuggestCommand(fullCommand + " "))
                            .withHoverEvent(new HoverEvent.ShowText(Text.literal(ColorUtil.parseColor("&bClick to suggest to chat\n&7" + fullCommand))))
                    );
            ChatUtil.sendMessage(commandText);
        }

        ChatUtil.sendMessage("&8Use \"" + CommandUtil.getCommandPrefix() + " help <page>\" to view other commands.");
    }

    @Override
    public String getUsage() {
        return CommandUtil.getCommandPrefix() + "help [<page|command>]";
    }
}