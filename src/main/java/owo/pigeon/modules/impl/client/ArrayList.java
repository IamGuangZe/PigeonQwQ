package owo.pigeon.modules.impl.client;

import net.engio.mbassy.listener.Handler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import owo.pigeon.event.events.RenderEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.modules.ModuleManager;
import owo.pigeon.settings.ColorSetting;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.FloatSetting;
import owo.pigeon.settings.ModeSetting;
import owo.pigeon.utils.ColorUtil;
import owo.pigeon.utils.render.TextRendererUtil;

import java.awt.*;
import java.util.Comparator;
import java.util.List;

import static owo.pigeon.Pigeon.mc;

public class ArrayList extends Module {
    public ArrayList() {
        super("ArrayList", Category.CLIENT);
    }

    public enum ColorMode {
        NONE, ROW_GRADIENT, CHAR_GRADIENT
    }

    public enum SuffixStyle {
        NONE, BRACKET, PARENTHESIS, ANGLE
    }

    public ModeSetting<ColorMode> colorMode = setting("color-mode", ColorMode.ROW_GRADIENT, v -> true);
    public FloatSetting colorSpeed = setting("color-speed", 0.25f, 0.1f, 1.0f, v -> colorMode.getValue() != ColorMode.NONE);
    public ModeSetting<SuffixStyle> suffixStyle = setting("suffix-style", SuffixStyle.BRACKET, v -> true);
    public EnableSetting background = setting("background", true, v -> true);
    public ColorSetting backgroundColor = setting("background-color", new Color(0x50000000, true), v -> background.getValue());
    public EnableSetting bar = setting("bar", true, v -> true);

    private static final int SUFFIX_COLOR = new Color(0xFFA5A5A5, true).getRGB();
    private static final int PADDING = 2;
    private static final int LINE_SPACING = 1;
    private static final float OFFSET_MAX = 100.0f;

    private float offsetTick = 0.0f;

    @Handler
    public void onRender2D(RenderEvent.Render2DEvent event) {
        if (mc.options.hideGui) return;

        ColorMode mode = colorMode.getValue();
        offsetTick += colorSpeed.getValue();
        if (offsetTick > OFFSET_MAX) offsetTick -= OFFSET_MAX;

        GuiGraphics context = event.getContext();
        List<Module> sorted = ModuleManager.modules.stream()
                .filter(m -> m.isEnable() && !m.isHide() && m != this)
                .sorted(Comparator.comparingInt(this::getDisplayWidth).reversed())
                .toList();

        if (sorted.isEmpty()) return;

        ColorUtil.Theme theme = ColorUtil.getTheme();
        int[] gradient = theme.getGradient();
        boolean hasGradient = mode != ColorMode.NONE && theme.isGradient();
        float globalRatio = offsetTick / OFFSET_MAX;
        int staticColor = theme.isGradient() ? theme.getMidColor() : Color.WHITE.getRGB();

        int scaledWidth = mc.getWindow().getGuiScaledWidth();
        int fontHeight = TextRendererUtil.getFontHeight();
        int lineHeight = fontHeight + LINE_SPACING;
        int y = PADDING;
        int barWidth = bar.getValue() ? 2 : 0;

        for (int i = 0; i < sorted.size(); i++) {
            Module module = sorted.get(i);
            String suffix = formatSuffix(module.getSuffix());
            String displayText = suffix.isEmpty() ? module.name : module.name + " " + suffix;
            int textWidth = TextRendererUtil.getStringWidth(displayText);
            int x = scaledWidth - textWidth - PADDING - barWidth;

            int rowColor = getRowColor(hasGradient, gradient, mode, globalRatio, i, sorted.size(), staticColor);

            if (background.getValue()) {
                context.fill(x - 2, y - 1, scaledWidth - PADDING + 1, y + lineHeight - 1, backgroundColor.getValue().getRGB());
            }

            int nameWidth = TextRendererUtil.getStringWidth(module.name);

            if (hasGradient && mode == ColorMode.CHAR_GRADIENT) {
                float spread = Math.max(0.05f, 1.0f / Math.max(1, sorted.size()));
                float rowOffset = (float) i * spread;
                int totalChars = module.name.length();
                for (int ci = 0; ci < totalChars; ci++) {
                    float charRatio = totalChars > 1 ? ((float) ci / (totalChars - 1)) * spread : 0f;
                    float raw = (globalRatio + rowOffset + charRatio) % 1.0f;
                    int color = ColorUtil.interpolateGradient(gradient, pingPong(raw));
                    int charWidth = TextRendererUtil.getStringWidth(String.valueOf(module.name.charAt(ci)));
                    TextRendererUtil.drawText(context, Component.literal(String.valueOf(module.name.charAt(ci)))
                            .withStyle(s -> s.withColor(color)), x, y, color);
                    x += charWidth;
                }
            } else if (hasGradient) {
                TextRendererUtil.drawText(context, Component.literal(module.name).withStyle(s -> s.withColor(rowColor)), x, y, rowColor);
            } else {
                TextRendererUtil.drawText(context, Component.literal(module.name), x, y, staticColor);
            }

            if (!suffix.isEmpty()) {
                TextRendererUtil.drawText(context, Component.literal(" " + suffix), scaledWidth - textWidth - PADDING - barWidth + nameWidth, y, SUFFIX_COLOR);
            }

            if (bar.getValue()) {
                context.fill(scaledWidth - barWidth, y - 1, scaledWidth, y + lineHeight - 1, rowColor);
            }

            y += lineHeight;
        }
    }

    private float pingPong(float t) {
        return (float) (1.0 - Math.cos(t * 2.0 * Math.PI)) * 0.5f;
    }

    private int getRowColor(boolean hasGradient, int[] gradient, ColorMode mode, float globalRatio, int index, int total, int staticColor) {
        if (!hasGradient) return staticColor;
        if (mode == ColorMode.CHAR_GRADIENT) {
            return ColorUtil.interpolateGradient(gradient, pingPong(globalRatio));
        }
        float rowRatio = total > 1 ? (float) index / (total - 1) : 0f;
        float raw = (globalRatio + rowRatio * 0.3f) % 1.0f;
        return ColorUtil.interpolateGradient(gradient, pingPong(raw));
    }

    private String formatSuffix(String suffix) {
        if (suffix.isEmpty()) return "";
        return switch (suffixStyle.getValue()) {
            case BRACKET -> "[" + suffix + "]";
            case PARENTHESIS -> "(" + suffix + ")";
            case ANGLE -> "<" + suffix + ">";
            default -> suffix;
        };
    }

    private int getDisplayWidth(Module module) {
        String suffix = formatSuffix(module.getSuffix());
        String displayText = suffix.isEmpty() ? module.name : module.name + " " + suffix;
        return TextRendererUtil.getStringWidth(displayText);
    }
}
