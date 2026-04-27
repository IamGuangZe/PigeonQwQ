package owo.pigeon.utils.world;

import net.minecraft.util.Util;
import owo.pigeon.utils.chat.ChatUtil;

import static owo.pigeon.Pigeon.mc;

public class ServerUtil {

    private static long prevTime = 0L;
    private static float tps = 20.0f;

    private static int currentPing = 0;
    private static int averagePing = 0;

    public static float getTps() {
        return tps;
    }

    public static int getCurrentPing() {
        return currentPing;
    }

    public static int getAveragePing() {
        return averagePing;
    }

    public static void onTimeUpdate() {
        long now = System.currentTimeMillis();
        if (prevTime != 0L) {
            tps = (20000f / (now - prevTime + 1));
            if (tps < 0f) tps = 0f;
            if (tps > 20f) tps = 20f;
        }
        prevTime = now;
    }

    public static void onPongResponse(long startTime) {
        currentPing = (int) (Util.getMeasuringTimeMs() - startTime);
        if (currentPing < 0) currentPing = 0;

        ChatUtil.sendDebugMessage("ServerUtil", "Ping RTT: " + currentPing + "ms (startTime=" + startTime + ")");

        var pingLog = mc.getDebugHud().getPingLog();
        int sampleSize = Math.min(pingLog.getLength(), 20);
        if (sampleSize == 0) {
            averagePing = currentPing;
            return;
        }
        long total = 0L;
        for (int i = 0; i < sampleSize; i++) {
            total += pingLog.get(i);
        }
        averagePing = (int) (total / sampleSize);
    }
}
