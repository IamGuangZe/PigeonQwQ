package owo.pigeon.utils.Render;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import owo.pigeon.utils.ColorUtil;

import java.util.List;

import static owo.pigeon.Pigeon.mc;

public class TextRendererUtil {
    public static final TextRenderer textRenderer = mc.textRenderer;

    public static int getStringWidth(String text) {
        return textRenderer.getWidth(ColorUtil.removeColor(text));
    }

    public static int getFontHeight() {
        return textRenderer.fontHeight;
    }

    public static int getLineHeight() {
        return textRenderer.fontHeight + 1;
    }

    public static void drawString(DrawContext context, String text, int x, int y, int color) {
        context.drawText(textRenderer, Text.literal(text), x, y, color, true);
    }

    public static void drawString(DrawContext context, String text, int x, int y) {
        context.drawText(textRenderer, Text.literal(ColorUtil.parseColor(text)), x, y, 0xFFFFFFFF, true);
    }

    public static void drawStringList(DrawContext context, List<String> lines, int x, int y) {
        int offsetY = y;
        for (String line : lines) {
            drawString(context, line, x, offsetY);
            offsetY += getLineHeight();
        }
    }
}
