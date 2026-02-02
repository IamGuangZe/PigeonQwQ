package owo.pigeon.commands.impl.Debug;

import owo.pigeon.commands.Command;
import owo.pigeon.utils.Chat.ChatUtil;
import owo.pigeon.utils.Hypixel.HypixelUtil;
import owo.pigeon.utils.Hypixel.SkyblockUtil;
import owo.pigeon.utils.ScoreBoardUtil;

import java.util.List;

public class GetCommand extends Command {
    public GetCommand() {
        super("get");
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) return;

        switch (args[0].toLowerCase()) {
            case "sidebar","s" -> {
                String title = ScoreBoardUtil.getSidebarTitle();
                List<String> lines = ScoreBoardUtil.getSidebarLines();
                ChatUtil.sendDebugMessage("Sidebar", title);
                for (String line : lines)
                    ChatUtil.sendDebugMessage("Sidebar", line);
            }

            case "tab","t" -> {
                List<String> lines = ScoreBoardUtil.getTabLines();
                for (String line : lines)
                    ChatUtil.sendDebugMessage("Tab", line);
            }

            case "hypixel","skyblock","hyp","skb" -> {
                ChatUtil.sendDebugMessage("Hypixel", "isInHypixel: " + HypixelUtil.isInHypixel());
                ChatUtil.sendDebugMessage("Skyblock", "isInSkyblock: " + SkyblockUtil.isInSkyblock());

                if (SkyblockUtil.isInSkyblock()) {
                    ChatUtil.sendDebugMessage("Skyblock", "island: " + SkyblockUtil.getIsland().getDisplayName());
                    if (SkyblockUtil.isInIsland(SkyblockUtil.Island.Dungeon)) {
                        ChatUtil.sendDebugMessage("Skyblock","Floor: " + SkyblockUtil.getFloor());
                        ChatUtil.sendDebugMessage("Skyblock","isInBoss: " + SkyblockUtil.isInBoss());
                        if (SkyblockUtil.isInFloor(7) && SkyblockUtil.isInBoss(7)) {
                            ChatUtil.sendDebugMessage("Skyblock","Floor 7 Stage: " + SkyblockUtil.getFloor7Stage());
                        }
                    }
                }
            }
        }
    }
}
