package owo.pigeon.utils.hypixel;

import net.engio.mbassy.listener.Handler;
import net.minecraft.client.multiplayer.ServerData;
import owo.pigeon.event.events.ClientTickEvent;
import owo.pigeon.utils.ColorUtil;
import owo.pigeon.utils.RegexUtil;
import owo.pigeon.utils.ScoreBoardUtil;
import owo.pigeon.utils.WorldUtil;
import owo.pigeon.utils.hypixel.skyblock.DojoUtil;
import owo.pigeon.utils.hypixel.skyblock.DungeonUtil;
import owo.pigeon.utils.hypixel.skyblock.SkyblockUtil;

import java.util.List;
import java.util.Objects;

import static owo.pigeon.Pigeon.mc;

public class HypixelStateCache {
    public static final HypixelStateCache INSTANCE = new HypixelStateCache();

    public static volatile boolean isOnHypixel;
    public static volatile HypixelUtil.Game currentGame = HypixelUtil.Game.UNKNOWN;
    public static volatile SkyblockUtil.Island currentIsland = SkyblockUtil.Island.UNKNOWN;
    public static volatile DungeonUtil.Floor currentFloor = DungeonUtil.Floor.Unknown;
    public static volatile DojoUtil.Dojo currentDojo = DojoUtil.Dojo.UNKNOWN;

    private HypixelStateCache() {
    }

    @Handler
    public void onTickPost(ClientTickEvent.Post event) {
        if (WorldUtil.nullCheck()) {
            resetToDefaults();
            return;
        }

        isOnHypixel = checkOnHypixel();
        currentGame = isOnHypixel ? detectCurrentGame() : HypixelUtil.Game.UNKNOWN;
        currentIsland = (currentGame == HypixelUtil.Game.SKYBLOCK)
                ? parseIsland() : SkyblockUtil.Island.UNKNOWN;
        currentFloor = (currentIsland == SkyblockUtil.Island.DUNGEON)
                ? parseFloor() : DungeonUtil.Floor.Unknown;
        currentDojo = (currentGame == HypixelUtil.Game.SKYBLOCK && inDojoBounds())
                ? parseDojoChallenge() : DojoUtil.Dojo.UNKNOWN;
    }

    private static boolean checkOnHypixel() {
        ServerData serverInfo = mc.getCurrentServer();
        if (serverInfo == null) return false;

        String ip = serverInfo.ip == null ? "" : serverInfo.ip.toLowerCase();
        String name = serverInfo.name == null ? "" : serverInfo.name;
        String sidebarLine = ScoreBoardUtil.getSidebarLineBottomUp(1);
        String sidebarIp = sidebarLine == null ? "" : sidebarLine;
        String tabHeader = ScoreBoardUtil.getTabHeader() == null ? "" : ColorUtil.removeColor(ScoreBoardUtil.getTabHeader());
        return ip.contains("hypixel.net") || name.contains("hypixel") || sidebarIp.contains("hypixel") || tabHeader.contains("You are playing on MC.HYPIXEL.NET");
    }

    private static HypixelUtil.Game detectCurrentGame() {
        String sidebarTitle = ColorUtil.removeColor(ScoreBoardUtil.getSidebarTitle());
        if (sidebarTitle == null) return HypixelUtil.Game.UNKNOWN;

        for (HypixelUtil.Game game : HypixelUtil.Game.values()) {
            if (game == HypixelUtil.Game.UNKNOWN) continue;
            for (String displayName : game.getDisplayNames()) {
                if (displayName != null && sidebarTitle.contains(displayName)) {
                    return game;
                }
            }
        }
        return HypixelUtil.Game.UNKNOWN;
    }

    private static SkyblockUtil.Island parseIsland() {
        if (mc.isLocalServer()) return SkyblockUtil.Island.SINGLE_PLAYER;

        List<String> tabLines = ScoreBoardUtil.getTabLines();
        if (tabLines.isEmpty()) return SkyblockUtil.Island.UNKNOWN;

        for (String line : tabLines) {
            if (line.startsWith("Area: ") || line.startsWith("Dungeon: ")) {
                for (SkyblockUtil.Island island : SkyblockUtil.Island.values()) {
                    if (line.toLowerCase().contains(island.getDisplayName().toLowerCase())) {
                        return island;
                    }
                }
                break;
            }
        }
        return SkyblockUtil.Island.UNKNOWN;
    }

    private static DungeonUtil.Floor parseFloor() {
        List<String> sidebarLines = ScoreBoardUtil.getSidebarLines();
        if (sidebarLines.isEmpty()) return DungeonUtil.Floor.Unknown;

        return sidebarLines.stream()
                .filter(line -> line.contains("⏣"))
                .map(line -> RegexUtil.regexGetPart(" ⏣ The Catacombs \\((.*)\\)", ColorUtil.removeColor(line), 1))
                .filter(Objects::nonNull)
                .map(DungeonUtil::getFloor)
                .findFirst()
                .orElse(DungeonUtil.Floor.Unknown);
    }

    private static boolean inDojoBounds() {
        return DojoUtil.DOJO_BB.isInside(mc.player.blockPosition());
    }

    private static DojoUtil.Dojo parseDojoChallenge() {
        List<String> sidebarLines = ScoreBoardUtil.getSidebarLines();
        for (String line : sidebarLines) {
            String clean = ColorUtil.removeColor(line);
            String dojoName = RegexUtil.regexGetPart("Challenge: (.*)", clean, 1);
            if (dojoName != null) {
                try {
                    return DojoUtil.Dojo.valueOf(dojoName);
                } catch (IllegalArgumentException ignored) {
                    return DojoUtil.Dojo.UNKNOWN;
                }
            }
        }
        return DojoUtil.Dojo.UNKNOWN;
    }

    private static void resetToDefaults() {
        isOnHypixel = false;
        currentGame = HypixelUtil.Game.UNKNOWN;
        currentIsland = SkyblockUtil.Island.UNKNOWN;
        currentFloor = DungeonUtil.Floor.Unknown;
        currentDojo = DojoUtil.Dojo.UNKNOWN;
    }
}
