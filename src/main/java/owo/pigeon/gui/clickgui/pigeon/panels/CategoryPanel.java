package owo.pigeon.gui.clickgui.pigeon.panels;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.item.ItemStack;
import owo.pigeon.gui.clickgui.pigeon.AbstractDisplableItem;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.modules.impl.client.PigeonQwQ;
import owo.pigeon.utils.ColorUtil;
import owo.pigeon.utils.ItemUtil;
import owo.pigeon.utils.ModuleUtil;
import owo.pigeon.utils.animation.AnimationValue;
import owo.pigeon.utils.render.RenderUtil;

import java.awt.*;
import java.util.ArrayList;

import static owo.pigeon.Pigeon.mc;
import static owo.pigeon.utils.render.TextRendererUtil.textRenderer;

public class CategoryPanel extends AbstractDisplableItem {
    private final Category category;

    private boolean movepanel;
    private boolean hovered;
    private final AnimationValue expandProgress = new AnimationValue(1.0f, 0.3f);
    public ArrayList<ModulePanel> modulePanels = new ArrayList<>();

    private int mx;
    private int my;
    private int clipBottom;

    public CategoryPanel(Category category, int x, int y, int width, int height) {
        this.category = category;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        for (Module module : ModuleUtil.getAllModule(category)) {
            modulePanels.add(new ModulePanel(module, x, 0, width, height));
        }
    }

    @Override
    public void drawScreen(GuiGraphics context, int mouseX, int mouseY, float delta) {
        expandProgress.setDuration(clickGui.animationSpeed.getValue());
        expandProgress.update(delta);

        hovered = isHovered(mouseX, mouseY, x, y, width, height);

        context.fill(x, y, x + width, y + height, Color.BLACK.getRGB());
        if (owo.pigeon.Pigeon.isDebug())
            RenderUtil.drawBorder(context, x, y, width, height, hovered ? Color.YELLOW.getRGB() : Color.RED.getRGB());

        // Draw category item icon
        if (category.equals(Category.PLAYER)) {
            ItemStack iconSkull = category.getIcon();
            ItemStack playerSkull = ItemUtil.getSkullFromPlayer(mc.player);

            String iconSkullTexture = ItemUtil.getSkullTexture(iconSkull);
            String playerSkullTexture = ItemUtil.getSkullTexture(playerSkull);
            if (playerSkullTexture != null && !playerSkullTexture.equals(iconSkullTexture)) {
                category.setIcon(playerSkull);
            }
        }


        float scale = 0.75f;
        int iconSize = (int) (scale * 16);
        int iconX = x + 2;
        int iconY = y + height / 2 - iconSize / 2;

        context.pose().pushMatrix();
        context.pose().translate(iconX, iconY);
        context.pose().scale(scale, scale);
        context.renderItem(category.getIcon(), 0, 0);
        context.pose().popMatrix();

        int textOffset = iconSize + 4; // icon width + gap
        context.drawString(textRenderer,
                category.name().substring(0, 1).toUpperCase() + category.name().substring(1).toLowerCase(),
                x + textOffset,
                y + height / 2 - textRenderer.lineHeight / 2,
                Color.WHITE.getRGB());

        boolean expanded = expandProgress.isExpanded();
        String symbol = expanded ? "-" : "+";
        String color = expanded ? "&c" : "&a";
        context.drawString(textRenderer,
                ColorUtil.parseColor(color + symbol),
                x + width - textRenderer.width(symbol) - 4,
                y + height / 2 - textRenderer.lineHeight / 2,
                Color.WHITE.getRGB()
        );

        float progress = expandProgress.getValue();

        int totalExpandedHeight = 0;
        for (ModulePanel panel : modulePanels) {
            totalExpandedHeight += this.height + panel.getSettingHeight();
        }

        int animatedHeight = (int) (totalExpandedHeight * progress);
        clipBottom = y + height + animatedHeight;

        if (animatedHeight > 0) {
            context.enableScissor(x, y + height, x + width, y + height + animatedHeight);

            int startY = y + height;
            for (ModulePanel panel : modulePanels) {
                panel.x = this.x;
                panel.y = startY;
                panel.width = this.width;
                panel.height = this.height;
                panel.drawScreen(context, mouseX, mouseY, delta);
                startY += this.height + panel.getSettingHeight();
            }

            context.disableScissor();
        }

        if (!owo.pigeon.Pigeon.isDebug()) {
            PigeonQwQ pigeonQwQ = ModuleUtil.getModule(PigeonQwQ.class);
            int[] gradient = pigeonQwQ != null ? pigeonQwQ.theme.getValue().getGradient() : null;
            RenderUtil.drawGradientBorder(context, x - 1, y - 1, width + 1, height + animatedHeight + 1, gradient);
        }
    }

    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (hovered) {
            if (click.button() == 0) {
                movepanel = true;
                mx = (int) (x - click.x());
                my = (int) (y - click.y());
            } else if (click.button() == 1) {
                expandProgress.setTarget(expandProgress.isExpanded() ? 0.0f : 1.0f);
            }
            return true;
        }

        if (!expandProgress.isCollapsed()) {
            for (int i = modulePanels.size() - 1; i >= 0; i--) {
                ModulePanel panel = modulePanels.get(i);
                if (panel.y < clipBottom && panel.mouseClicked(click, doubled)) {
                    return true;
                }
            }
        }

        return false;
    }

    public void mouseReleased(MouseButtonEvent click) {
        if (click.button() == 0) {
            movepanel = false;
        }

        if (!expandProgress.isCollapsed()) {
            for (ModulePanel panel : modulePanels) {
                if (panel.y < clipBottom) panel.mouseReleased(click);
            }
        }
    }

    public void mouseDragged(MouseButtonEvent click, double offsetX, double offsetY) {
        if (movepanel) {
            x = (int) (mx + click.x());
            y = (int) (my + click.y());
        }

        if (!expandProgress.isCollapsed()) {
            for (ModulePanel panel : modulePanels) {
                if (panel.y < clipBottom) panel.mouseDragged(click, offsetX, offsetY);
            }
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!expandProgress.isCollapsed()) {
            for (ModulePanel panel : modulePanels) {
                if (panel.y < clipBottom && panel.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
                    return true;
                }
            }
        }
        return hovered;
    }

    public void keyPressed(KeyEvent input) {
        if (!expandProgress.isCollapsed()) {
            for (ModulePanel panel : modulePanels) {
                if (panel.y < clipBottom) panel.keyPressed(input);
            }
        }
    }

    public Category getCategory() {
        return category;
    }

    public boolean getDisplayModule() {
        return expandProgress.isExpanded();
    }

    public void setDisplayModule(boolean value) {
        expandProgress.force(value ? 1.0f : 0.0f);
    }
}
