package owo.pigeon.utils;

public class ColorUtil {
    public static String parseColor(String msg) {
        if (msg == null) return null;
        return msg.replaceAll("&", "§")  //& -> §
                .replaceAll("§§", "&");  //&& -> §§ -> &
    }

    public static String removeColor(String msg) {
        if (msg == null) return null;
        return msg.replaceAll("§.", "");
    }

    public static String removeColorA(String msg) {
        if (msg == null) return null;
        return msg.replaceAll("§", "&");
    }

    public static int colorClamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
