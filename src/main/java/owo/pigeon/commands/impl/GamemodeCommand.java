package owo.pigeon.commands.impl;

import net.minecraft.world.level.GameType;
import owo.pigeon.commands.Command;
import owo.pigeon.utils.CommandUtil;
import owo.pigeon.utils.chat.ChatUtil;

import static owo.pigeon.Pigeon.mc;

public class GamemodeCommand extends Command {
    public GamemodeCommand() {
        super("gamemode");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            CommandUtil.sendCommandError(CommandUtil.ErrorReason.UnknownOrIncompleteCommand,
                    this.getCommand(),
                    args,
                    args.length
            );
            return;
        }

        GameType gamemode = GameType.byName(args[0], null);

        if (gamemode == null) {
            CommandUtil.sendCommandError(CommandUtil.ErrorReason.UnknownGamemode,
                    this.getCommand(),
                    args,
                    0
            );
            return;
        }

        mc.gameMode.setLocalMode(gamemode);
        ChatUtil.sendMessage("Set own game mode to " + gamemode.name().toLowerCase() + " mode");
    }

    @Override
    public String getUsage() {
        return CommandUtil.getCommandPrefix() + "gamemode <gamemode>";
    }
}
