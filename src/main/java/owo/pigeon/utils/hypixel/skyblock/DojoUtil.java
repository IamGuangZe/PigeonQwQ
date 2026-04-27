package owo.pigeon.utils.hypixel.skyblock;

import owo.pigeon.Pigeon;
import owo.pigeon.utils.ColorUtil;
import owo.pigeon.utils.RegexUtil;
import owo.pigeon.utils.ScoreBoardUtil;

import static owo.pigeon.Pigeon.mc;

public class DojoUtil {
    public enum Dojo {
        Force,
        Stamina,
        Mastery,
        Discipline,
        Swiftness,
        Control,
        Tenacity
    }

    public static boolean isInDojoChallenge() {
        if (Pigeon.isDebug() && mc.isInSingleplayer()) return true;
        if (!SkyblockUtil.isInIsland(SkyblockUtil.Island.CrimsonIsle)) return false;

        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();

        return x < -189 && x > -225 &&
                y < 106 && y > 96 &&
                z < -580 && z > -616;
    }

    public static Dojo getDojoChallenge() {
        for (String line : ScoreBoardUtil.getSidebarLines()) {
            String clean = ColorUtil.removeColor(line);
            String dojoName = RegexUtil.regexGetPart("Challenge: (.*)", clean, 1);
            if (dojoName != null) {
                try {
                    return Dojo.valueOf(dojoName);
                } catch (IllegalArgumentException ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    public static boolean isDojoChallenge(Dojo dojo) {
        if (Pigeon.isDebug() && mc.isInSingleplayer()) return true;
        return getDojoChallenge() == dojo;
    }
}
