package owo.pigeon.utils.hypixel.skyblock;

import net.minecraft.world.entity.Entity;
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

    private static final double DOJO_MIN_X = -229;
    private static final double DOJO_MAX_X = -185;
    private static final double DOJO_MIN_Y = 95;
    private static final double DOJO_MAX_Y = 121;
    private static final double DOJO_MIN_Z = -620;
    private static final double DOJO_MAX_Z = -576;

    public static boolean isInDojoChallenge() {
        if (Pigeon.isDebug() && mc.isLocalServer()) return true;
        if (!SkyblockUtil.isInIsland(SkyblockUtil.Island.CRIMSON_ISLE)) return false;

        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();

        return x > DOJO_MIN_X && x < DOJO_MAX_X &&
                y > DOJO_MIN_Y && y < DOJO_MAX_Y &&
                z > DOJO_MIN_Z && z < DOJO_MAX_Z;
    }

    public static boolean isInDojoChallenge(Entity entity) {
        if (Pigeon.isDebug() && mc.isLocalServer()) return true;

        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();

        return x > DOJO_MIN_X && x < DOJO_MAX_X &&
                y > DOJO_MIN_Y && y < DOJO_MAX_Y &&
                z > DOJO_MIN_Z && z < DOJO_MAX_Z;
    }

    public static String getDojoBoundsDiagnostic(Entity entity) {
        double x = entity.getX(), y = entity.getY(), z = entity.getZ();
        boolean inX = x > DOJO_MIN_X && x < DOJO_MAX_X;
        boolean inY = y > DOJO_MIN_Y && y < DOJO_MAX_Y;
        boolean inZ = z > DOJO_MIN_Z && z < DOJO_MAX_Z;
        return String.format("pos=(%.1f, %.1f, %.1f) inX=%s[%.0f,%.0f] inY=%s[%.0f,%.0f] inZ=%s[%.0f,%.0f]",
                x, y, z, inX, DOJO_MIN_X, DOJO_MAX_X, inY, DOJO_MIN_Y, DOJO_MAX_Y, inZ, DOJO_MIN_Z, DOJO_MAX_Z);
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
        if (Pigeon.isDebug() && mc.isLocalServer()) return true;
        return getDojoChallenge() == dojo;
    }
}
