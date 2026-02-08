package owo.pigeon.utils;

import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.scoreboard.*;
import net.minecraft.text.Text;
import owo.pigeon.mixin.accessors.IAccessorPlayerListHud;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static owo.pigeon.Pigeonqwq.mc;

public class ScoreBoardUtil {
    public static String getSidebarTitle() {
        if (mc.world == null) return null;

        Scoreboard scoreboard = mc.world.getScoreboard();
        ScoreboardObjective sidebarObjective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR); // 1 = SIDEBAR
        if (sidebarObjective != null) return sidebarObjective.getDisplayName().getString();

        return null;
    }

    public static List<String> getSidebarLines() {
        if (mc.world == null) return List.of();

        Scoreboard scoreboard = mc.world.getScoreboard();
        ScoreboardObjective sidebarObjective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (sidebarObjective == null) return List.of();

        Collection<ScoreboardEntry> entries = scoreboard.getScoreboardEntries(sidebarObjective);
        return entries.stream()
                .filter(entry -> !entry.hidden())
                .sorted(Comparator.comparingInt(ScoreboardEntry::value).reversed())
                .map(entry -> {
                    String owner = entry.owner();
                    Text baseName = entry.name();
                    Team team = scoreboard.getScoreHolderTeam(owner);
                    Text fullText = (team!= null)? team.decorateName(baseName) : baseName;
                    return fullText.getString();
                })
                .toList();
    }

    public static String getSidebarLineTopDown(int line) {
        List<String> lines = getSidebarLines();
        int index = line - 1; // 转换为 0 索引
        return (index >= 0 && index < lines.size()) ? lines.get(index) : null;
    }

    public static String getSidebarLineBottomUp(int line) {
        List<String> lines = getSidebarLines();
        int index = lines.size() - line; // 从末尾倒数
        return (index >= 0 && index < lines.size()) ? lines.get(index) : null;
    }

    public static String getTabHeader() {
        if (mc.inGameHud == null || mc.inGameHud.getPlayerListHud() == null) return null;
        Text header = ((IAccessorPlayerListHud) mc.inGameHud.getPlayerListHud()).getHeader();
        return header == null ? null : header.getString();
    }

    public static String getTabFooter() {
        if (mc.inGameHud == null || mc.inGameHud.getPlayerListHud() == null) return null;
        Text footer = ((IAccessorPlayerListHud) mc.inGameHud.getPlayerListHud()).getFooter();
        return footer == null ? null : footer.getString();
    }

    public static List<String> getTabLines() {
        if (mc.getNetworkHandler() == null || mc.inGameHud == null || mc.inGameHud.getPlayerListHud() == null) return List.of();

        PlayerListHud playerListHud = mc.inGameHud.getPlayerListHud();

        Collection<PlayerListEntry> entries = mc.getNetworkHandler().getPlayerList();
        return entries.stream()
                .map(entry -> playerListHud.getPlayerName(entry).getString())
                .toList();
    }
}
