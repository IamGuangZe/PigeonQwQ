package owo.pigeon.utils;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import owo.pigeon.modules.impl.client.PigeonQwQ;

import java.awt.*;

public class ColorUtil {

    public enum Theme {
        NORMAL, SUNSET, AMETHYST, AQUA, AURORA;

        public int[] getGradient() {
            return switch (this) {
                case NORMAL -> null;
                case SUNSET -> GRADIENT_SUNSET;
                case AMETHYST -> GRADIENT_AMETHYST;
                case AQUA -> GRADIENT_AQUA;
                case AURORA -> GRADIENT_AURORA;
            };
        }

        public boolean isGradient() {
            return this != NORMAL;
        }

        public int getMidColor() {
            int[] g = getGradient();
            return g != null ? interpolateGradient(g, 0.5f) : Color.WHITE.getRGB();
        }
    }

    // #fa61da → #ff8891 → #ffcd5a
    public static final int[] GRADIENT_SUNSET = {
            0xFA61DA, 0xFF8891, 0xFFCD5A
    };

    // #d9b3e2 → #522ca4 (13 stops)
    public static final int[] GRADIENT_AMETHYST = {
            0xD9B3E2, 0xD2A4E3, 0xCA94E2, 0xC183E1, 0xB673DE,
            0xAA63DA, 0x9D54D5, 0x9047CE, 0x833CC7, 0x7533BF,
            0x692EB7, 0x5D2BAE, 0x522CA4
    };

    // #07aeea → #2bf598
    public static final int[] GRADIENT_AQUA = {
            0x07AEEA, 0x2BF598
    };

    // #7b84ff → #aeff6f (7 stops)
    public static final int[] GRADIENT_AURORA = {
            0x7B84FF, 0x68A5FF, 0x5FC6FF, 0x63E5FC,
            0x73FFCD, 0x8DFF9D, 0xAEFF6F
    };

    public static Theme getTheme() {
        return ModuleUtil.getModule(PigeonQwQ.class).theme.getValue();
    }

    public static MutableComponent gradientText(String text, int[] colors) {
        MutableComponent result = Component.empty();
        int len = text.length();
        if (len == 0) return result;

        for (int i = 0; i < len; i++) {
            float ratio = len > 1 ? (float) i / (len - 1) : 0f;
            int color = interpolateGradient(colors, ratio);

            result.append(Component.literal(String.valueOf(text.charAt(i)))
                    .withStyle(style -> style.withColor(color)));
        }
        return result;
    }

    public static int interpolateGradient(int[] colors, float ratio) {
        if (colors.length == 1) return colors[0];

        float scaled = ratio * (colors.length - 1);
        int index = Math.min((int) scaled, colors.length - 2);
        float localRatio = scaled - index;

        int c1 = colors[index];
        int c2 = colors[index + 1];

        int r = (int) (((c1 >> 16) & 0xFF) + (((c2 >> 16) & 0xFF) - ((c1 >> 16) & 0xFF)) * localRatio);
        int g = (int) (((c1 >> 8) & 0xFF) + (((c2 >> 8) & 0xFF) - ((c1 >> 8) & 0xFF)) * localRatio);
        int b = (int) ((c1 & 0xFF) + ((c2 & 0xFF) - (c1 & 0xFF)) * localRatio);

        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    public static String parseColor(String msg) {
        if (msg == null) return null;
        return msg.replaceAll("&", "§").replaceAll("§§", "&");
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

    // Supports #RRGGBB, 0xRRGGBB, RRGGBB, with optional AA alpha
    public static Color parseHexColor(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Color string cannot be null or empty");
        }

        String hex;

        if (value.startsWith("#")) {
            hex = value.substring(1);
        } else if (value.startsWith("0x")) {
            hex = value.substring(2);
        } else {
            hex = value;
        }

        if (hex.length() != 6 && hex.length() != 8) {
            throw new IllegalArgumentException("Invalid HEX color length: " + value + ". Expected 6 or 8 characters.");
        }

        if (!hex.matches("(?i)[0-9A-F]{" + hex.length() + "}")) {
            throw new IllegalArgumentException("Invalid HEX color format: " + value);
        }

        if (hex.length() == 8) {
            int a = Integer.parseInt(hex.substring(6, 8), 16);
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            return new Color(r, g, b, a);
        } else if (hex.length() == 6) {
            return Color.decode("#" + hex);
        } else {
            throw new IllegalArgumentException("Invalid HEX color length: " + value);
        }
    }

    public static Color parseDecimalColor(long value, Integer alpha) {
        if (alpha != null) {
            int rgb = (int) (value & 0x00FFFFFF);
            int clampedAlpha = colorClamp(alpha);
            return new Color(rgb | (clampedAlpha << 24), true);
        } else if (value > 0xFFFFFF) {
            int r = (int) ((value >> 24) & 0xFF);
            int g = (int) ((value >> 16) & 0xFF);
            int b = (int) ((value >> 8) & 0xFF);
            int a = (int) (value & 0xFF);
            return new Color(r, g, b, a);
        } else {
            int rgb = (int) (value & 0x00FFFFFF);
            return new Color(rgb);
        }
    }

    public static Color parseDecimalColor(long value) {
        return parseDecimalColor(value, null);
    }
}
