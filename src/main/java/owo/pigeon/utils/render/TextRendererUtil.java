package owo.pigeon.utils.render;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import owo.pigeon.utils.ColorUtil;

import java.util.List;

import static owo.pigeon.Pigeon.mc;

public class TextRendererUtil {
    public static final Font textRenderer = mc.font;

    public static int getFontHeight() {
        return textRenderer.lineHeight;
    }

    public static int getLineHeight() {
        return textRenderer.lineHeight + 1;
    }

    public static int getStringWidth(String text) {
        return textRenderer.width(ColorUtil.removeColor(text));
    }

    public static int getTextWidth(Component text) {
        return textRenderer.width(text);
    }

    public static void drawText(GuiGraphics context, Component text, int x, int y, int color) {
        context.drawString(textRenderer, text, x, y, color, true);
    }

    public static void drawText(GuiGraphics context, Component text, int x, int y) {
        context.drawString(textRenderer, text, x, y, 0xFFFFFFFF, true);
    }

    public static void drawString(GuiGraphics context, String text, int x, int y, int color) {
        context.drawString(textRenderer, Component.literal(text), x, y, color, true);
    }

    public static void drawString(GuiGraphics context, String text, int x, int y) {
        context.drawString(textRenderer, Component.literal(ColorUtil.parseColor(text)), x, y, 0xFFFFFFFF, true);
    }

    public static void drawTextList(GuiGraphics context, List<Component> lines, int x, int y) {
        int offsetY = y;
        for (Component line : lines) {
            drawText(context, line, x, offsetY);
            offsetY += getLineHeight();
        }
    }

    public static void drawStringList(GuiGraphics context, List<String> lines, int x, int y) {
        int offsetY = y;
        for (String line : lines) {
            drawString(context, line, x, offsetY);
            offsetY += getLineHeight();
        }
    }
}
