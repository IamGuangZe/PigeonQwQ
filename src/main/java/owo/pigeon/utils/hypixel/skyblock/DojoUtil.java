package owo.pigeon.utils.hypixel.skyblock;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import owo.pigeon.Pigeon;
import owo.pigeon.utils.hypixel.HypixelStateCache;

import static owo.pigeon.Pigeon.mc;

public class DojoUtil {
    public enum Dojo {
        UNKNOWN,
        Force,
        Stamina,
        Mastery,
        Discipline,
        Swiftness,
        Control,
        Tenacity
    }

    public static final BoundingBox DOJO_BB = new BoundingBox(-229, 95, -620, -185, 121, -576);

    public static boolean isInDojoChallenge() {
        if (Pigeon.isDebug() && mc.isLocalServer()) return true;
        return HypixelStateCache.currentDojo != Dojo.UNKNOWN;
    }

    public static boolean isInDojoChallenge(Entity entity) {
        if (Pigeon.isDebug() && mc.isLocalServer()) return true;
        return DOJO_BB.isInside(entity.blockPosition());
    }

    public static String getDojoBoundsDiagnostic(Entity entity) {
        return String.format("pos=(%d, %d, %d) inside=%b",
                entity.blockPosition().getX(), entity.blockPosition().getY(), entity.blockPosition().getZ(),
                DOJO_BB.isInside(entity.blockPosition()));
    }

    public static Dojo getDojoChallenge() {
        return HypixelStateCache.currentDojo;
    }

    public static boolean isDojoChallenge(Dojo dojo) {
        if (Pigeon.isDebug() && mc.isLocalServer()) return true;
        return HypixelStateCache.currentDojo == dojo;
    }
}
