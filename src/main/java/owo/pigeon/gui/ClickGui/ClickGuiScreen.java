package owo.pigeon.gui.ClickGui;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import owo.pigeon.Pigeon;
import owo.pigeon.gui.ClickGui.panels.CategoryPanel;
import owo.pigeon.gui.ClickGui.panels.ModulePanel;
import owo.pigeon.gui.ClickGui.panels.SettingPanel;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.impl.Client.ClickGui;
import owo.pigeon.utils.ModuleUtil;

import java.awt.*;
import java.util.ArrayList;

import static owo.pigeon.Pigeon.mc;

public class ClickGuiScreen extends Screen {

    public final ArrayList<CategoryPanel> categoryPanels = new ArrayList<>();

    public ClickGuiScreen() {
        super(Text.literal("ClickGui"));

        int x = 5;
        int y = 5;
        int width = 90;
        int height = 17;

        for (Category category : Category.values()) {
            categoryPanels.add(new CategoryPanel(category, x, y, width, height));
            // x += width + 2;
            y += height + 2;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ClickGui clickGui = ModuleUtil.getModule(ClickGui.class);

        for (CategoryPanel panel : categoryPanels) {
            switch (clickGui.style.getValue()) {
                case OLD:
                    panel.height = 20;
                    break;
                case NEW:
                default:
                    panel.height = 17;
                    break;
            }

            panel.drawScreen(context,mouseX,mouseY,delta);
        }

        context.drawTextWithShadow(textRenderer,
                Pigeon.WATERMARK,
                this.width - textRenderer.getWidth(Pigeon.WATERMARK) - 2,
                this.height - textRenderer.fontHeight - 2,
                Color.WHITE.getRGB());
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        ClickGui clickGui = ModuleUtil.getModule(ClickGui.class);

        if (clickGui.background.getValue()) super.renderBackground(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        for (int i = categoryPanels.size() - 1; i >= 0; i--) {
            CategoryPanel panel = categoryPanels.get(i);
            if (panel.mouseClicked(click,doubled)) {
                categoryPanels.remove(panel);
                categoryPanels.add(panel);
                return true;
            }
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(Click click) {
        for (CategoryPanel panel : categoryPanels) {
            panel.mouseReleased(click);
        }

        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        for (CategoryPanel panel : categoryPanels) {
            panel.mouseDragged(click,offsetX,offsetY);
        }

        return super.mouseDragged(click,offsetX,offsetY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount == 0) return false;

        for (CategoryPanel categoryPanel : categoryPanels) {
            if (categoryPanel.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
                return true;
            }
        }

        int moveAmount = verticalAmount > 0 ? 10 : -10;

        if (mc.isShiftPressed()) {
            for (CategoryPanel categoryPanel : categoryPanels) {
                categoryPanel.x += moveAmount;
            }
        } else {
            for (CategoryPanel categoryPanel : categoryPanels) {
                categoryPanel.y -= moveAmount;
            }
        }

        return true;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        boolean waitingForKey = false;

        for (CategoryPanel panel : categoryPanels) {
            for (ModulePanel modulePanel : panel.modulePanels) {

                if (modulePanel.keybindPanel.isWaitingForKey()) {
                    waitingForKey = true;
                }

                for (SettingPanel settingPanel : modulePanel.visiblePanels) {
                    if (settingPanel.isWaitingForKey()) {
                        waitingForKey = true;
                    }
                }
            }
            panel.keyPressed(input);
        }

        if (waitingForKey && input.isEscape()) {
            return true;
        }

        return super.keyPressed(input);
    }
}
