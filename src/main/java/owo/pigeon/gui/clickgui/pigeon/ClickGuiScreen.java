package owo.pigeon.gui.clickgui.pigeon;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import owo.pigeon.Pigeon;
import owo.pigeon.gui.clickgui.pigeon.panels.CategoryPanel;
import owo.pigeon.gui.clickgui.pigeon.panels.ModulePanel;
import owo.pigeon.gui.clickgui.pigeon.panels.SettingPanel;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.impl.client.ClickGui;
import owo.pigeon.utils.ModuleUtil;
import owo.pigeon.utils.render.TextRendererUtil;

import java.awt.*;
import java.util.ArrayList;

import static owo.pigeon.Pigeon.mc;

public class ClickGuiScreen extends Screen {

    public final ArrayList<CategoryPanel> categoryPanels = new ArrayList<>();
    private Screen parentScreen = null;

    public ClickGuiScreen() {
        super(Text.literal("ClickGui"));

        int x = 5;
        int y = 5;
        int width = 100;
        int height = 16;

        for (Category category : Category.values()) {
            categoryPanels.add(new CategoryPanel(category, x, y, width, height));
            // x += width + 2;
            y += height + 2;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        for (CategoryPanel panel : categoryPanels) {
            panel.drawScreen(context, mouseX, mouseY, delta);
        }

        context.drawTextWithShadow(TextRendererUtil.textRenderer,
                Pigeon.WATERMARK,
                this.width - TextRendererUtil.textRenderer.getWidth(Pigeon.WATERMARK) - 2,
                this.height - TextRendererUtil.textRenderer.fontHeight - 2,
                Color.WHITE.getRGB());
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        ClickGui clickGui = ModuleUtil.getModule(ClickGui.class);

        switch (clickGui.background.getValue()) {
            case INGAME -> this.renderInGameBackground(context);
            case PANORAMA -> this.renderPanoramaBackground(context, deltaTicks);
            case BLUR -> this.applyBlur(context);
            case DARKENING -> this.renderDarkening(context);
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        for (int i = categoryPanels.size() - 1; i >= 0; i--) {
            CategoryPanel panel = categoryPanels.get(i);
            if (panel.mouseClicked(click, doubled)) {
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
            panel.mouseDragged(click, offsetX, offsetY);
        }

        return super.mouseDragged(click, offsetX, offsetY);
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
                categoryPanel.y += moveAmount;
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
                        break;
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

    @Override
    public void close() {
        mc.setScreen(this.parentScreen);
    }

    public void setParentScreen(Screen parent) {
        this.parentScreen = parent;
    }
}
