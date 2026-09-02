package owo.pigeon.utils;

import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.*;
import owo.pigeon.mixin.accessors.IAccessorPlayerTabOverlay;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static owo.pigeon.Pigeon.mc;

public class ScoreBoardUtil {
    public static String getSidebarTitle() {
        if (mc.level == null) return null;

        Scoreboard scoreboard = mc.level.getScoreboard();
        Objective sidebarObjective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR); // 1 = SIDEBAR
        if (sidebarObjective != null) return sidebarObjective.getDisplayName().getString();

        return null;
    }

    public static List<String> getSidebarLines() {
        if (mc.level == null) return List.of();

        Scoreboard scoreboard = mc.level.getScoreboard();
        Objective sidebarObjective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
        if (sidebarObjective == null) return List.of();

        Collection<PlayerScoreEntry> entries = scoreboard.listPlayerScores(sidebarObjective);
        return entries.stream()
                .filter(entry -> !entry.isHidden())
                .sorted(Comparator.comparingInt(PlayerScoreEntry::value).reversed())
                .map(entry -> {
                    String owner = entry.owner();
                    Component baseName = entry.ownerName();
                    PlayerTeam team = scoreboard.getPlayersTeam(owner);
                    Component fullText = (team != null) ? team.getFormattedName(baseName) : baseName;
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
        if (mc.gui == null || mc.gui.hud.getTabList() == null) return null;
        Component header = ((IAccessorPlayerTabOverlay) mc.gui.hud.getTabList()).pigeon$getHeader();
        return header == null ? null : header.getString();
    }

    public static String getTabFooter() {
        if (mc.gui == null || mc.gui.hud.getTabList() == null) return null;
        Component footer = ((IAccessorPlayerTabOverlay) mc.gui.hud.getTabList()).pigeon$getFooter();
        return footer == null ? null : footer.getString();
    }

    public static List<String> getTabLines() {
        if (mc.getConnection() == null || mc.gui == null || mc.gui.hud.getTabList() == null)
            return List.of();

        PlayerTabOverlay playerTabOverlay = mc.gui.hud.getTabList();

        Collection<PlayerInfo> entries = mc.getConnection().getOnlinePlayers();
        return entries.stream()
                .map(entry -> playerTabOverlay.getNameForDisplay(entry).getString())
                .toList();
    }

    public static boolean isInTabList(Player player) {
        if (mc.getConnection() == null) return false;
        return mc.getConnection().getListedOnlinePlayers().stream()
                .anyMatch(info -> info.getProfile().id().equals(player.getUUID()));
    }
}
