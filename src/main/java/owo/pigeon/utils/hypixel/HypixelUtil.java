package owo.pigeon.utils.hypixel;

import com.google.common.collect.Sets;
import owo.pigeon.Pigeon;

import java.util.Set;

import static owo.pigeon.Pigeon.mc;

public class HypixelUtil {
    public enum Game {
        MURDERMYSTERY(Sets.newHashSet("MURDER MYSTERY", "密室杀手")),
        PIXELPARTY(Sets.newHashSet("PIXEL PARTY", "像素派对", "跳色舞會")),
        REPLAY(Sets.newHashSet("REPLAY", "回放系统")),
        SKYBLOCK(Sets.newHashSet("SKYBLOCK", "空岛生存", "空島生存", "SKIBLOCK")),
        UNKNOWN(Sets.newHashSet());

        private final Set<String> displayNames;

        Game(Set<String> displayNames) {
            this.displayNames = displayNames;
        }

        public Set<String> getDisplayNames() {
            return displayNames;
        }
    }

    public static boolean isInHypixel() {
        if (Pigeon.isDebug() && mc.isLocalServer()) return true;
        return HypixelStateCache.isOnHypixel;
    }

    public static boolean isInGame(Game game) {
        if (Pigeon.isDebug() && mc.isLocalServer()) return true;
        return HypixelStateCache.currentGame == game;
    }
}
