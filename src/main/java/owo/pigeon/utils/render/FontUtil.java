package owo.pigeon.utils.render;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import owo.pigeon.utils.ColorUtil;

import java.util.List;

import static owo.pigeon.Pigeon.mc;

public class FontUtil {
    public static final Font font = mc.font;

    public static int getFontHeight() {
        return font.lineHeight;
    }

    public static int getLineHeight() {
        return font.lineHeight + 1;
    }

    public static int getStringWidth(String text) {
        return font.width(ColorUtil.removeColor(text));
    }

    public static int getTextWidth(Component text) {
        return font.width(text);
    }

    public static void drawText(GuiGraphicsExtractor context, Component text, int x, int y, int color) {
        context.text(font, text, x, y, color, true);
    }

    public static void drawText(GuiGraphicsExtractor context, Component text, int x, int y) {
        context.text(font, text, x, y, 0xFFFFFFFF, true);
    }

    public static void drawString(GuiGraphicsExtractor context, String text, int x, int y, int color) {
        context.text(font, Component.literal(text), x, y, color, true);
    }

    public static void drawString(GuiGraphicsExtractor context, String text, int x, int y) {
        context.text(font, Component.literal(ColorUtil.parseColor(text)), x, y, 0xFFFFFFFF, true);
    }

    public static void drawTextList(GuiGraphicsExtractor context, List<Component> lines, int x, int y) {
        int offsetY = y;
        for (Component line : lines) {
            drawText(context, line, x, offsetY);
            offsetY += getLineHeight();
        }
    }

    public static void drawStringList(GuiGraphicsExtractor context, List<String> lines, int x, int y) {
        int offsetY = y;
        for (String line : lines) {
            drawString(context, line, x, offsetY);
            offsetY += getLineHeight();
        }
    }
}
