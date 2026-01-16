package owo.pigeon.gui.ClickGui.panels;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.KeyInput;
import owo.pigeon.gui.ClickGui.AbstractDisplableItem;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.utils.ModuleUtil;

import java.awt.*;
import java.util.ArrayList;

import static owo.pigeon.utils.TextRendererUtil.textRenderer;

public class CategoryPanel extends AbstractDisplableItem {
    private final Category category;

    private boolean movepanel;
    private boolean hovered;
    private boolean displaymodule;
    public ArrayList<ModulePanel> modulePanels = new ArrayList<>();

    private int mx;
    private int my;

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
    public void drawScreen(DrawContext context, int mouseX, int mouseY, float delta) {
        hovered = isHovered(mouseX, mouseY, x, y, width, height);

        context.fill(x, y, x + width, y + height, Color.BLACK.getRGB());

        switch (clickGui.style.getValue()) {
            case OLD:
                context.drawTextWithShadow(textRenderer,
                        category.name(),
                        (int) ((x + ((float) width / 2)) - ((float) textRenderer.getWidth(category.name()) / 2)),
                        y + height / 2 - textRenderer.fontHeight / 2,
                        Color.WHITE.getRGB());
                break;

            case NEW:
            default:
                context.drawTextWithShadow(textRenderer,
                        category.name().substring(0, 1).toUpperCase() + category.name().substring(1).toLowerCase(),
                        x + 5,
                        y + height / 2 - textRenderer.fontHeight / 2 + 1,
                        Color.WHITE.getRGB());

                String symbol = displaymodule ? "-" : "+";
                int color = displaymodule ? Color.RED.getRGB() : Color.GREEN.getRGB();
                context.drawTextWithShadow(textRenderer,
                        symbol,
                        x + width - textRenderer.getWidth(symbol) - 4,
                        y + height / 2 - textRenderer.fontHeight / 2 + 1,
                        color
                );

        }

        if (displaymodule) {
            int startY = y + height;
            for (ModulePanel panel : modulePanels) {
                panel.x = this.x;
                panel.y = startY;
                panel.width = this.width;
                panel.height = this.height;
                panel.drawScreen(context, mouseX, mouseY, delta);
                startY += this.height + panel.getSettingHeight();
            }
        }
    }

    public boolean mouseClicked(Click click, boolean doubled) {
        boolean handled = false;

        if (hovered) {
            handled = true;
            if (click.button() == 0) {
                movepanel = true;
                mx = (int) (x - click.x());
                my = (int) (y - click.y());
            } else if (click.button() == 1) {
                displaymodule = !displaymodule;
            }
        }

        for (int i = modulePanels.size() - 1; i >= 0; i--) {
            ModulePanel panel = modulePanels.get(i);
            if (panel.mouseClicked(click,doubled)) {
                handled = true;
                break;
            }
        }

        return handled;
    }

    public void mouseReleased(Click click) {
        if (click.button() == 0) {
            movepanel = false;
        }

        for (ModulePanel panel : modulePanels) {
            panel.mouseReleased(click);
        }
    }

    public void mouseDragged(Click click, double offsetX, double offsetY) {
        if (movepanel) {
            x = (int) (mx + click.x());
            y = (int) (my + click.y());
        }

        for (ModulePanel panel : modulePanels) {
            panel.mouseDragged(click, offsetX, offsetY);
        }
    }

    public void keyPressed(KeyInput input) {
        for (ModulePanel panel : modulePanels) {
            panel.keyPressed(input);
        }
    }

    public Category getCategory() {
        return category;
    }

    public boolean getDisplayModule() {
        return displaymodule;
    }

    public void setDisplayModule(boolean value) {
        displaymodule = value;
    }
}
