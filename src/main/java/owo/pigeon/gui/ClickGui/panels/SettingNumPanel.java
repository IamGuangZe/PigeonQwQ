package owo.pigeon.gui.ClickGui.panels;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import owo.pigeon.Pigeon;
import owo.pigeon.settings.AbstractNumSetting;
import owo.pigeon.settings.AbstractSetting;
import owo.pigeon.settings.FloatSetting;
import owo.pigeon.settings.IntSetting;
import owo.pigeon.utils.ColorUtil;
import owo.pigeon.utils.Render.RenderUtil;

import java.awt.*;

import static owo.pigeon.Pigeon.mc;
import static owo.pigeon.utils.Render.TextRendererUtil.textRenderer;

public class SettingNumPanel extends SettingPanel {
    public AbstractNumSetting<?> numberSetting;

    private boolean hovered;
    private boolean sliderHovered;
    private boolean dragging;

    public SettingNumPanel(AbstractSetting<?> setting, int x, int y, int width, int height) {
        super(setting, x, y, width, height);
        numberSetting = (AbstractNumSetting<?>) setting;
    }

    @Override
    public void drawScreen(DrawContext context, int mouseX, int mouseY, float delta) {
        hovered = isHovered(mouseX, mouseY, x, y, width, height);

        String displayName = numberSetting.getName().replaceAll("-and-","-&&-").replaceAll("-", " ");

        int sliderAreaX = x;
        int sliderAreaY = y + height - 8;
        int sliderAreaWidth = width;
        int sliderAreaHeight = 8;
        int rawHeight = height - sliderAreaHeight;

        double min = numberSetting.getMinValue().doubleValue();
        double max = numberSetting.getMaxValue().doubleValue();
        double value = numberSetting.getValue().doubleValue();
        double percent = (value - min) / (max - min);

        // 滑条
        int barX = sliderAreaX + 4;
        int barY = sliderAreaY + (sliderAreaHeight - 2) / 2 - 1;
        int barWidth = sliderAreaWidth - 4 * 2;
        int barHeight = 2;
        int knobCenterY = barY + barHeight / 2;

        // 滑块
        int knobWidth = 2;
        int knobHeight = 4;
        int knobX = (int) (barX + percent * barWidth - knobWidth / 2D);
        int knobY = knobCenterY - knobHeight / 2;

        sliderHovered = isHovered(mouseX, mouseY, barX, knobY, barWidth, knobHeight);

        // TODO : OLD STYLE使用双色矩形不同占比来替代滑块
        switch (clickGui.style.getValue()) {
            case OLD:
                context.fill(x, y, x + width, y + height, color_old);
                break;
            case NEW:
            default:
                context.fill(x, y, x + width, y + height, new Color(0, 0, 0, 100).getRGB());
        }

        // 滑条
        context.fill(barX, barY, barX + barWidth, barY + barHeight, Color.GRAY.getRGB());

        // 滑块
        context.fill(knobX, knobY, knobX + knobWidth, knobY + knobHeight, new Color(220, 220, 220).getRGB());

        float scale = 0.5f;
        context.getMatrices().pushMatrix();
        context.getMatrices().scale(scale, scale);

        if (numberSetting instanceof FloatSetting floatSetting) {
            String displayValue = "&e" + floatSetting.getValue();
            String unit = floatSetting.getUnit();
            if (unit != null && !unit.isEmpty()) {
                displayValue += " &r" + unit;
            }

            context.drawTextWithShadow(textRenderer,
                    ColorUtil.parseColor(displayName + " : " + displayValue),
                    (int) ((x + 4) / scale),
                    (int) ((y + (float) rawHeight / 2 - (float) textRenderer.fontHeight * scale / 2) / scale),
                    Color.LIGHT_GRAY.getRGB());

        } else if (numberSetting instanceof IntSetting intSetting) {
            String displayValue = "&6" + intSetting.getValue();
            String unit = intSetting.getUnit();
            if (unit != null && !unit.isEmpty()) {
                displayValue += " &r" + unit;
            }

            context.drawTextWithShadow(textRenderer,
                    ColorUtil.parseColor(displayName + " : " + displayValue),
                    (int) ((x + 4) / scale),
                    (int) ((y + (float) rawHeight / 2 - (float) textRenderer.fontHeight * scale / 2) / scale),
                    Color.LIGHT_GRAY.getRGB());

        }

        context.getMatrices().popMatrix();
        if (owo.pigeon.Pigeon.isDebug()) RenderUtil.drawBorder(context, x, y, width, height, Color.GREEN.getRGB());
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (!hovered) return false;

        if (sliderHovered && click.button() == 0) {
            dragging = true;
            updateSliderValue(click.x());
        }

        return true;
    }

    @Override
    public void mouseReleased(Click click) {
        dragging = false;
    }

    @Override
    public void mouseDragged(Click click, double offsetX, double offsetY) {
        if (sliderHovered && dragging && click.button() == 0) {
            updateSliderValue(click.x());
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount == 0) return false;

        if (sliderHovered) {
            double min = numberSetting.getMinValue().doubleValue();
            double max = numberSetting.getMaxValue().doubleValue();
            double value = numberSetting.getValue().doubleValue();

            double scrollAmount = verticalAmount < 0 ? 1 : -1;

            if (numberSetting instanceof FloatSetting floatSetting) {
                double baseStep = 0.01;
                double multiplier = 1.0;

                if (Pigeon.mc.isCtrlPressed()) {
                    multiplier *= 2;
                }

                if (mc.isShiftPressed()) {
                    multiplier *= 5;
                }

                double newValue = value + scrollAmount * (baseStep * multiplier);
                newValue = Math.max(min, Math.min(max, newValue));
                newValue = Math.round(newValue * 100.0) / 100.0;
                floatSetting.setValue((float) newValue);
                return true;
            } else if (numberSetting instanceof IntSetting intSetting) {
                int newValue = (int) Math.round(value + scrollAmount);
                newValue = Math.max((int) min, Math.min((int) max, newValue));
                intSetting.setValue(newValue);
                return true;
            }
        }

        return hovered;
    }

    private void updateSliderValue(double mouseX) {
        double min = numberSetting.getMinValue().doubleValue();
        double max = numberSetting.getMaxValue().doubleValue();
        int barX = this.x + 4;
        int barWidth = this.width - 8;

        double percent = (mouseX - barX) / (double) barWidth;
        percent = Math.max(0, Math.min(1, percent));

        double newValue = min + percent * (max - min);

        if (numberSetting instanceof FloatSetting floatSetting) {
            newValue = Math.round(newValue * 100.0) / 100.0;
            floatSetting.setValue((float) newValue);
        } else if (numberSetting instanceof IntSetting intSetting) {
            intSetting.setValue((int) Math.round(newValue));
        }
    }
}
