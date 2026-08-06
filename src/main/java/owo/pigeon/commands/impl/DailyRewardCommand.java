package owo.pigeon.commands.impl;

import owo.pigeon.commands.Command;
import owo.pigeon.utils.CommandUtil;
import owo.pigeon.utils.chat.ChatUtil;
import owo.pigeon.utils.hypixel.dailyreward.DailyRewardClaimer;

public class DailyRewardCommand extends Command {
    public DailyRewardCommand() {
        super("dailyreward");
    }

    // Reference: https://5ixsd.top/skydiao (HypixelRewardClaimer)

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

        DailyRewardClaimer claimer = DailyRewardClaimer.getCurrent();
        if (claimer == null || !claimer.hasData) {
            ChatUtil.sendMessage("&cNo daily reward data available. Open a reward link first.");
            return;
        }
        if (claimer.claimed) {
            ChatUtil.sendMessage("&cDaily reward has already been claimed.");
            return;
        }

        switch (args[0].toLowerCase()) {
            case "claim" -> {
                if (args.length < 2) {
                    CommandUtil.sendCommandError(CommandUtil.ErrorReason.UnknownOrIncompleteCommand,
                            this.getCommand(),
                            args,
                            args.length
                    );
                    return;
                }
                int index;
                try {
                    index = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    CommandUtil.sendCommandError(CommandUtil.ErrorReason.ExpectedInteger,
                            this.getCommand(),
                            args,
                            1
                    );
                    return;
                }
                if (index < 1 || index > 3) {
                    CommandUtil.sendCommandError(CommandUtil.ErrorReason.IncorrectArgument,
                            this.getCommand(),
                            args,
                            1
                    );
                    return;
                }
                claimer.setTargetReward(index);
                claimer.doClaim();
            }
            case "best" -> {
                claimer.setHighestTargetReward();
                claimer.doClaim();
            }
            case "list" -> claimer.displayRewards();
            default -> CommandUtil.sendCommandError(CommandUtil.ErrorReason.IncorrectArgument,
                    this.getCommand(),
                    args,
                    0
            );
        }
    }

    @Override
    public String getUsage() {
        return CommandUtil.getCommandPrefix() + "dailyreward claim <index>\n" +
                CommandUtil.getCommandPrefix() + "dailyreward (best|list)";
    }
}
