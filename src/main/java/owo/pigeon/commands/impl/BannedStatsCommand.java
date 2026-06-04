package owo.pigeon.commands.impl;

import owo.pigeon.Pigeon;
import owo.pigeon.commands.Command;
import owo.pigeon.utils.CommandUtil;
import owo.pigeon.utils.chat.ChatUtil;

public class BannedStatsCommand extends Command {
    public BannedStatsCommand() {
        super("bannedstats");
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            CommandUtil.sendCommandError(CommandUtil.ErrorReason.UnknownOrIncompleteCommand,
                    this.getCommand(),
                    args,
                    args.length
            );
            return;
        }

        try {
            int requestedMinutes = Integer.parseInt(args[0]);

            if (requestedMinutes <= 0) {
                ChatUtil.sendMessage("BannedStats", "Please enter a positive integer.");
                return;
            }

            long banCount = Pigeon.banTracker.getBansInLast(requestedMinutes);
            int trackedMinutes = Pigeon.banTracker.getTrackedMinutes();

            int actualMinutes = Math.min(requestedMinutes, trackedMinutes);

            if (actualMinutes == 0) {
                ChatUtil.sendMessage("BannedStats", "No stats recorded yet. Please wait at least a minute.");
            } else {
                String personText = banCount == 1 ? "person" : "people";
                String minuteText = actualMinutes == 1 ? "minute" : "minutes";
                ChatUtil.sendMessage("BannedStats", "Staff has banned " + banCount + " " + personText + " in last " + actualMinutes + " " + minuteText);

                if (requestedMinutes > trackedMinutes) {
                    ChatUtil.sendMessage("BannedStats", "(Only " + trackedMinutes + " min of data available since startup)");
                }
            }

        } catch (NumberFormatException e) {
            CommandUtil.sendCommandError(CommandUtil.ErrorReason.ExpectedInteger,
                    this.getCommand(),
                    args,
                    0
            );
        }

    }

    @Override
    public String getUsage() {
        return CommandUtil.getCommandPrefix() + "bannedstats <minute>";
    }
}
