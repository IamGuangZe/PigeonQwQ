package owo.pigeon.utils;

public class ColorUtil {
    public static String parseColor(String msg) {
        return msg.replaceAll("&", "§")  //& -> §
                .replaceAll("§§", "&");  //&& -> §§ -> &
    }

    public static String removeColor(String msg) {
        return msg.replaceAll("§.", "");
    }

    public static String removeColorA(String msg) {
        return msg.replaceAll("§", "&");
    }

    public static String removeEmoji(String s) {
        return s.replaceAll("[🎂🎉🎁👹🏀⚽🍭🌠👾🐍🔮👽💣🍫🔫]", "");
    }
}
