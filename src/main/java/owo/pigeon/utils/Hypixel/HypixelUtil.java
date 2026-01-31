package owo.pigeon.utils.Hypixel;

import com.google.common.collect.Sets;
import net.minecraft.client.network.ServerInfo;
import owo.pigeon.Pigeonqwq;
import owo.pigeon.utils.ColorUtil;
import owo.pigeon.utils.ScoreBoardUtil;

import java.util.Set;

import static owo.pigeon.Pigeonqwq.mc;

public class HypixelUtil {
    public enum Game {
        SKYBLOCK(Sets.newHashSet("SKYBLOCK", "SKYBLOCK CO-OP", "空岛生存", "空島生存", "SKIBLOCK"));

        private final Set<String> displayNames;

        Game(Set<String> displayNames) {
            this.displayNames = displayNames;
        }

        public Set<String> getDisplayNames() {
            return displayNames;
        }
    }

    public static boolean isInHypixel() {
        if (Pigeonqwq.isDebug() && mc.isInSingleplayer()) return true;

        ServerInfo serverInfo = mc.getCurrentServerEntry();
        if (serverInfo == null) return false;

        String ip = serverInfo.address == null ? "" : serverInfo.address.toLowerCase();
        String name = serverInfo.label == null ? "" : serverInfo.label.getString().toLowerCase();
        String sidebarIp = ScoreBoardUtil.getSidebarLineBottomUp(1) == null ? "" : ScoreBoardUtil.getSidebarLineBottomUp(1);

        return ip.contains("hypixel.net") || name.contains("hypixel") || sidebarIp.contains("hypixel");
    }

    public static boolean isInGame(Game game) {
        if (Pigeonqwq.isDebug() && mc.isInSingleplayer()) return true;
        if (!isInHypixel()) return false;

        String sidebarTitle = ColorUtil.removeColor(ScoreBoardUtil.getSidebarTitle());
        return sidebarTitle != null && game.getDisplayNames().contains(sidebarTitle);
    }
}
