package owo.pigeon.gui.clickgui.panels;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import owo.pigeon.settings.AbstractSetting;
import owo.pigeon.settings.ColorSetting;
import owo.pigeon.utils.ColorUtil;
import owo.pigeon.utils.render.RenderUtil;

import java.awt.*;

import static owo.pigeon.utils.render.TextRendererUtil.textRenderer;

public class SettingColorPanel extends SettingPanel {
    public ColorSetting colorSetting;

    private boolean hovered;
    private boolean[] sliderHovered = new boolean[4];
    private boolean[] dragging = new boolean[4];

    public SettingColorPanel(AbstractSetting<?> setting, int x, int y, int width, int height) {
        super(setting, x, y, width, height);
        colorSetting = (ColorSetting) setting;
    }

    @Override
    public void drawScreen(DrawContext context, int mouseX, int mouseY, float delta) {
        hovered = isHovered(mouseX, mouseY, x, y, width, height);

        String displayName = colorSetting.getName().replaceAll("-and-", "-&&-").replaceAll("-", " ");

        // 总滑条区域
        int totalSliderAreaX = x;
        int totalSliderAreaY = y + height - 20;
        int totalSliderAreaWidth = width;
        int totalSliderAreaHeight = 20;
        int rawHeight = height - totalSliderAreaHeight;

        double[] percents = new double[4];
        percents[0] = colorSetting.getRed() / 255.0;
        percents[1] = colorSetting.getGreen() / 255.0;
        percents[2] = colorSetting.getBlue() / 255.0;
        percents[3] = colorSetting.getAlpha() / 255.0;

        int[] sliderAreaY = new int[4];

        // 滑条
        int barX = totalSliderAreaX + 4;
        int[] barY = new int[4];
        int barWidth = totalSliderAreaWidth - 4 * 2;
        int barHeight = 2;

        // 滑块
        int knobWidth = 2;
        int knobHeight = 4;
        int[] knobX = new int[4];
        int[] knobY = new int[4];

        // 每个滑条高度 5
        int sliderHeight = 5;

        for (int i = 0; i < 4; i++) {
            sliderAreaY[i] = totalSliderAreaY + i * sliderHeight;

            barY[i] = sliderAreaY[i] + (sliderHeight - 2) / 2 - 1;

            knobX[i] = (int) (barX + percents[i] * barWidth - knobWidth / 2D);
            knobY[i] = barY[i] + barHeight / 2 - knobHeight / 2;

            sliderHovered[i] = isHovered(mouseX, mouseY, barX, knobY[i], barWidth, knobHeight);
        }

        switch (clickGui.style.getValue()) {
            case OLD:
                context.fill(x, y, x + width, y + height, color_old);
                break;
            case NEW:
            default:
                context.fill(x, y, x + width, y + height, new Color(0, 0, 0, 100).getRGB());
        }

        for (int i = 0; i < 4; i++) {
            // 滑条
            context.fill(barX, barY[i], barX + barWidth, barY[i] + barHeight, Color.GRAY.getRGB());

            // 滑块
            context.fill(knobX[i], knobY[i], knobX[i] + knobWidth, knobY[i] + knobHeight, getKnobColor(i).getRGB());
        }

        float scale = 0.5f;
        context.getMatrices().pushMatrix();
        context.getMatrices().scale(scale, scale);

        String rHex = String.format("%02X", colorSetting.getRed());
        String gHex = String.format("%02X", colorSetting.getGreen());
        String bHex = String.format("%02X", colorSetting.getBlue());
        String aHex = String.format("%02X", colorSetting.getAlpha());

        String displayValue = "&c" + rHex + "&a" + gHex + "&9" + bHex + "&f" + aHex;
        context.drawTextWithShadow(textRenderer,
                ColorUtil.parseColor(displayName + " : " + displayValue),
                (int) ((x + 4) / scale),
                (int) ((y + (float) rawHeight / 2 - (float) textRenderer.fontHeight * scale / 2) / scale),
                Color.LIGHT_GRAY.getRGB());

        context.getMatrices().popMatrix();

        // 颜色预览
        int colorCubeSize = (int)(textRenderer.fontHeight * scale);
        int colorCubeX = x + width - 4 - colorCubeSize;
        int colorCubeY = y;

        context.fill(colorCubeX, colorCubeY, colorCubeX + colorCubeSize, colorCubeY + colorCubeSize, colorSetting.getRGB());
        if (owo.pigeon.Pigeon.isDebug()) RenderUtil.drawBorder(context, x, y, width, height, hovered ? Color.YELLOW.getRGB() : Color.GREEN.getRGB());
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (!hovered) return false;

        for (int i = 0; i < 4; i++) {
            if (sliderHovered[i] && click.button() == 0) {
                dragging[i] = true;
                updateSliderValue(click.x(), i);
            }
        }

        return true;
    }

    @Override
    public void mouseReleased(Click click) {
        for (int i = 0; i < 4; i++) {
            dragging[i] = false;
        }
    }

    @Override
    public void mouseDragged(Click click, double offsetX, double offsetY) {
        for (int i = 0; i < 4; i++) {
            if (sliderHovered[i] && dragging[i] && click.button() == 0) {
                updateSliderValue(click.x(), i);
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount == 0) return false;

        for (int i = 0; i < 4; i++) {
            if (sliderHovered[i]) {
                int scrollAmount = verticalAmount > 0 ? 1 : -1;

                int currentValue = getColorValue(i);
                int newValue = currentValue + scrollAmount;
                newValue = Math.max(0, Math.min(255, newValue));

                setColor(newValue, i);
                return true;
            }
        }

        return hovered;
    }

    private void updateSliderValue(double mouseX, int index) {
        int barX = x + 4;
        int barWidth = width - 8;

        double percent = (mouseX - barX) / (double) barWidth;
        percent = Math.max(0.0, Math.min(1.0, percent)); // clamp 到 [0,1]

        int value = (int) Math.round(percent * 255.0);

        setColor(value, index);
    }

    private int getColorValue(int index) {
        return switch (index) {
            case 0 -> colorSetting.getRed();
            case 1 -> colorSetting.getGreen();
            case 2 -> colorSetting.getBlue();
            case 3 -> colorSetting.getAlpha();
            default -> 0;
        };
    }

    private void setColor(int value, int index) {
        switch (index) {
            case 0:
                colorSetting.setRed(value);
                break;
            case 1:
                colorSetting.setGreen(value);
                break;
            case 2:
                colorSetting.setBlue(value);
                break;
            case 3:
                colorSetting.setAlpha(value);
                break;
        }
    }

    private Color getKnobColor(int index) {
        return switch (index) {
            case 0 -> Color.RED;
            case 1 -> Color.GREEN;
            case 2 -> Color.BLUE;
            case 3 -> Color.WHITE;
            default -> Color.GRAY;
        };
    }
}
