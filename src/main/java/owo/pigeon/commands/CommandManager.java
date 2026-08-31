package owo.pigeon.commands;

import net.engio.mbassy.listener.Handler;
import owo.pigeon.Pigeon;
import owo.pigeon.commands.impl.*;
import owo.pigeon.commands.impl.debug.GetCommand;
import owo.pigeon.event.events.MessageEvent;
import owo.pigeon.utils.CommandUtil;
import owo.pigeon.utils.chat.ChatUtil;

import java.util.ArrayList;
import java.util.Arrays;

import static owo.pigeon.Pigeon.LOGGER;
import static owo.pigeon.Pigeon.mc;

public class CommandManager {

    public static boolean isSay = false;
    public static final ArrayList<Command> commands = new ArrayList<>();

    public void init() {
        Pigeon.EVENT_BUS.subscribe(this);

        commands.add(new AutoCombineCommand());
        commands.add(new AutoSellCommand());
        commands.add(new BannedStatsCommand());
        commands.add(new BindCommand());
        commands.add(new CalcCommand());
        commands.add(new ConfigCommand());
        commands.add(new CopyCommand());
        commands.add(new DailyRewardCommand());
        commands.add(new ExportCommand());
        commands.add(new FillCommand());
        commands.add(new GamemodeCommand());
        commands.add(new HelpCommand());
        commands.add(new PrefixCommand());
        commands.add(new SayCommand());
        commands.add(new SetBlockCommand());
        commands.add(new SettingCommand());
        commands.add(new ToggleCommand());
        commands.add(new PingCommand());
        commands.add(new TpsCommand());
        commands.add(new HideCommand());
        commands.add(new ShowCommand());

        commands.add(new GetCommand());

        LOGGER.info("Pigeon CommandManager loaded");
    }

    @Handler
    public void onSendChat(MessageEvent.SendChatEvent event) {
        ChatUtil.sendDebugMessage("CommandManager", "handle SendChatEvent");

        String input = CommandUtil.normalize(event.getMessage().getString());
        if (!input.startsWith(String.valueOf(CommandUtil.getCommandPrefix()))) return;
        event.setCancelled(true);

        ChatUtil.sendDebugMessage("CommandManager", "client command: " + input);

        mc.gui.getChat().addRecentChat(input);
        String inputCommand = input.substring(1);

        if (inputCommand.isEmpty()) {
            ChatUtil.sendDebugMessage("CommandManager", "empty command");
            CommandUtil.sendCommandError(CommandUtil.ErrorReason.UnknownOrIncompleteCommand, "", "");
            return;
        }

        String[] parts = inputCommand.split(" ");
        String commandName = parts[0];
        boolean executed = false;

        for (Command command : commands) {
            if (command.getCommand().equalsIgnoreCase(commandName)) {
                command.execute(Arrays.copyOfRange(parts, 1, parts.length));
                executed = true;
            }
        }

        if (!executed) {
            CommandUtil.sendCommandError(CommandUtil.ErrorReason.UnknownOrIncompleteCommand, "", parts[0]);
        }
    }
}
