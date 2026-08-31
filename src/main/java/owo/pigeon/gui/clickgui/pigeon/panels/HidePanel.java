package owo.pigeon.gui.clickgui.pigeon.panels;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import owo.pigeon.gui.clickgui.pigeon.AbstractDisplableItem;
import owo.pigeon.modules.Module;
import owo.pigeon.utils.ColorUtil;
import owo.pigeon.utils.render.RenderUtil;

import java.awt.*;

import static owo.pigeon.utils.render.FontUtil.font;

public class HidePanel extends AbstractDisplableItem {
    private final Module module;
    private boolean hovered;

    public HidePanel(Module module, int x, int y, int width, int height) {
        this.module = module;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public void drawScreen(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        hovered = isHovered(mouseX, mouseY, x, y, width, height);

        context.fill(x, y, x + width, y + height, new Color(0, 0, 0, 100).getRGB());

        float scale = 0.5f;
        context.pose().pushMatrix();
        context.pose().scale(scale, scale);

        boolean value = module.isHide();
        String displayValue = value ? "&atrue" : "&cfalse";

        context.text(
                font,
                ColorUtil.parseColor("hide : " + displayValue),
                (int) ((x + 4) / scale),
                (int) ((y + (float) height / 2 - (float) font.lineHeight * scale / 2) / scale),
                Color.LIGHT_GRAY.getRGB());

        context.pose().popMatrix();
        if (owo.pigeon.Pigeon.isDebug())
            RenderUtil.drawBorder(context, x, y, width, height, hovered ? Color.YELLOW.getRGB() : Color.GREEN.getRGB());
    }

    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (!hovered) return false;

        if (click.button() == 0) {
            module.setHide(!module.isHide());
        }

        return true;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return hovered;
    }
}
