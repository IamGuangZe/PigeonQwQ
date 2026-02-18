package owo.pigeon.utils;

import java.awt.*;

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

    /**
     * 解析HEX颜色字符串，支持多种格式
     * 支持的格式：
     * - #RRGGBB 或 #RRGGBBAA (例如: #39C5BB 或 #39C5BBFF)
     * - 0xRRGGBB 或 0xRRGGBBAA (例如: 0x39C5BB 或 0x39C5BBFF)
     * - RRGGBB 或 RRGGBBAA (例如: 39C5BB 或 39C5BBFF)
     */
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

        // 检查是否为有效的HEX格式，只接受6位或8位
        if (hex.length() != 6 && hex.length() != 8) {
            throw new IllegalArgumentException("Invalid HEX color length: " + value + ". Expected 6 or 8 characters.");
        }

        if (!hex.matches("(?i)[0-9A-F]{" + hex.length() + "}")) {
            throw new IllegalArgumentException("Invalid HEX color format: " + value);
        }

        if (hex.length() == 8) {
            // ARGB格式 (RRGGBBAA)
            int a = Integer.parseInt(hex.substring(6, 8), 16);
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            return new Color(r, g, b, a);
        } else if (hex.length() == 6) {
            // RGB格式 (RRGGBB)
            return Color.decode("#" + hex);
        } else {
            throw new IllegalArgumentException("Invalid HEX color length: " + value);
        }
    }

    /**
     * 解析十进制RGBA格式的颜色值
     * 支持的格式：
     * - RGB格式 (例如: 3786171，alpha默认为255)
     * - RGBA格式 (例如: 3786171255，其中R=57, G=197, B=187, A=255)
     */
    public static Color parseDecimalColor(long value, Integer alpha) {
        if (alpha != null) {
            // 使用提供的alpha值
            int rgb = (int) (value & 0x00FFFFFF);
            int clampedAlpha = colorClamp(alpha);
            return new Color(rgb | (clampedAlpha << 24), true);
        } else {
            // 检查是否包含alpha值
            if (value > 0xFFFFFF) {
                // RGBA格式: extract R, G, B, A
                int r = (int) ((value >> 24) & 0xFF);
                int g = (int) ((value >> 16) & 0xFF);
                int b = (int) ((value >> 8) & 0xFF);
                int a = (int) (value & 0xFF);
                return new Color(r, g, b, a);
            } else {
                // RGB格式，alpha默认为255
                int rgb = (int) (value & 0x00FFFFFF);
                return new Color(rgb);
            }
        }
    }

    /**
     * 解析十进制RGBA格式的颜色值（不提供alpha）
     */
    public static Color parseDecimalColor(long value) {
        return parseDecimalColor(value, null);
    }
}
