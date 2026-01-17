package owo.pigeon.commands;

import net.engio.mbassy.listener.Handler;
import owo.pigeon.Pigeonqwq;
import owo.pigeon.commands.impl.*;
import owo.pigeon.event.events.SendMessageEvent;
import owo.pigeon.utils.Chat.ChatUtil;
import owo.pigeon.utils.CommandUtil;

import java.util.ArrayList;
import java.util.Arrays;

import static owo.pigeon.Pigeonqwq.mc;

public class CommandManager {

    public static boolean isSay = false;
    public static final ArrayList<Command> commands = new ArrayList<>();

    public void init() {
        Pigeonqwq.EVENT_BUS.subscribe(this);

        commands.add(new BindCommand());
        commands.add(new ConfigCommand());
        commands.add(new CopyCommand());
        commands.add(new FillCommand());
        commands.add(new HelpCommand());
        commands.add(new PrefixCommand());
        commands.add(new SayCommand());
        commands.add(new SetBlockCommand());
        commands.add(new SettingCommand());
        commands.add(new ToggleCommand());
    }

    @Handler
    public void onSendMessage(SendMessageEvent event) {

        ChatUtil.sendDebugMessage("CommandManager", "handle SendMessageEvent");

        String input = CommandUtil.normalize(event.getMessage());

        if (!input.startsWith(
                String.valueOf(
                        CommandUtil.getCommandPrefix()
                )
        )) return;

        event.setCancelled(true);

        ChatUtil.sendDebugMessage("CommandManager", "client command: " + input);

        mc.inGameHud.getChatHud().addToMessageHistory(input);

        String command = input.substring(1);

        if (command.isEmpty()) {
            ChatUtil.sendDebugMessage("CommandManager", "empty command");
            CommandUtil.sendCommandError(CommandUtil.errorReason.UnknownOrIncompleteCommand, "", "");
            return;
        }

        String[] parts = command.split(" ");
        String commandName = parts[0];
        boolean executed = false;

        for (Command Command : commands) {
            if (Command.getCommand().equalsIgnoreCase(commandName)) {
                Command.execute(Arrays.copyOfRange(parts, 1, parts.length));
                executed = true;
            }
        }

        if (!executed) {
            CommandUtil.sendCommandError(CommandUtil.errorReason.UnknownOrIncompleteCommand, "", parts[0]);
        }
    }
}
