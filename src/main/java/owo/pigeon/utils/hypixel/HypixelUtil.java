package owo.pigeon.utils.hypixel;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import net.minecraft.client.network.ServerInfo;
import owo.pigeon.Pigeon;
import owo.pigeon.utils.ColorUtil;
import owo.pigeon.utils.ScoreBoardUtil;

import java.util.Set;

import static owo.pigeon.Pigeon.mc;

public class HypixelUtil {
    public enum Game {
        MURDERMYSTERY(Sets.newHashSet("MURDER MYSTERY", "密室杀手")),
        PIXELPARTY(Sets.newHashSet("PIXEL PARTY", "像素派对", "跳色舞會")),
        REPLAY(Sets.newHashSet("REPLAY", "回放系统")),
        SKYBLOCK(Sets.newHashSet("SKYBLOCK", "空岛生存", "空島生存", "SKIBLOCK"));

        private final Set<String> displayNames;

        Game(Set<String> displayNames) {
            this.displayNames = displayNames;
        }

        public Set<String> getDisplayNames() {
            return displayNames;
        }
    }

    public static boolean isInHypixel() {
        if (Pigeon.isDebug() && mc.isInSingleplayer()) return true;

        ServerInfo serverInfo = mc.getCurrentServerEntry();
        if (serverInfo == null) return false;

        String ip = serverInfo.address == null ? "" : serverInfo.address.toLowerCase();
        String name = serverInfo.label == null ? "" : serverInfo.label.getString().toLowerCase();
        String sidebarIp = ScoreBoardUtil.getSidebarLineBottomUp(1) == null ? "" : ScoreBoardUtil.getSidebarLineBottomUp(1);
        String tabHeader = ScoreBoardUtil.getTabHeader() == null ? "" : ColorUtil.removeColor(ScoreBoardUtil.getTabHeader());
        return ip.contains("hypixel.net") || name.contains("hypixel") || sidebarIp.contains("hypixel") || tabHeader.contains("You are playing on MC.HYPIXEL.NET");
    }

    public static boolean isInGame(Game game) {
        if (Pigeon.isDebug() && mc.isInSingleplayer()) return true;
        if (!isInHypixel()) return false;

        String sidebarTitle = ColorUtil.removeColor(ScoreBoardUtil.getSidebarTitle());
        return sidebarTitle != null && Iterables.any(game.getDisplayNames(), s -> s != null && sidebarTitle.contains(s));
    }
}
