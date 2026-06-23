package owo.pigeon.gui.clickgui.pigeon.edits;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import owo.pigeon.Pigeon;
import owo.pigeon.modules.impl.client.ClickGui;
import owo.pigeon.settings.ListSetting;
import owo.pigeon.utils.ModuleUtil;
import owo.pigeon.utils.render.FontUtil;

import java.awt.*;

public class ListSettingEditScreen extends Screen {
    private final ListSetting setting;

    private EditBox textField;
    private String currentText = null;
    private int scrollOffset = 0;

    public ListSettingEditScreen(ListSetting setting) {
        super(Component.nullToEmpty("Editing " + setting.getName()));
        this.setting = setting;
    }

    @Override
    protected void init() {
        if (this.textField != null) {
            this.currentText = this.textField.getValue();
        }

        int fieldWidth = (int) (this.width * 0.618f);
        int x = (this.width - fieldWidth) / 2;
        int y = this.height / 2 - 10;

        this.textField = new EditBox(FontUtil.font, x, y, fieldWidth - 60, 20, Component.nullToEmpty("Add Item"));

        if (currentText != null) {
            textField.setValue(currentText);
        }

        this.textField.setResponder(text -> this.currentText = text);

        this.addRenderableWidget(this.textField);
        this.setInitialFocus(this.textField);

        this.addRenderableWidget(Button.builder(Component.nullToEmpty("Add"), (button) -> {
            if (!textField.getValue().isEmpty()) {
                setting.add(textField.getValue());
                textField.setValue("");
            }
        }).bounds(x + fieldWidth - 55, y, 50, 20).build());

        int buttonY = y + 25;
        int buttonWidth = (fieldWidth - 10) / 2;
        this.addRenderableWidget(Button.builder(Component.nullToEmpty("Clear All"), (button) -> {
            setting.clear();
            scrollOffset = 0;
        }).bounds(x, buttonY, buttonWidth, 20).build());

        this.addRenderableWidget(Button.builder(Component.nullToEmpty("Back"), (button) -> this.onClose()).bounds(x + buttonWidth + 10, buttonY, buttonWidth, 20).build());
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
        String title = "Editing list: " + setting.getName() + " (" + setting.size() + " items)";
        int titleWidth = FontUtil.font.width(title);
        context.drawString(FontUtil.font, title, (this.width - titleWidth) / 2, this.textField.getY() - FontUtil.font.lineHeight - 6, Color.WHITE.getRGB());

        int listY = this.textField.getY() + 50;
        int listHeight = this.height - listY - 10;
        int itemHeight = FontUtil.font.lineHeight + 4;
        int maxVisible = listHeight / itemHeight;

        int start = scrollOffset;
        int end = Math.min(start + maxVisible, setting.size());

        int listLeft = this.textField.getX();
        int listRight = listLeft + this.textField.getWidth() + 60;

        context.fill(listLeft, listY - 2, listRight, listY + listHeight, new Color(0, 0, 0, 120).getRGB());

        for (int i = start; i < end; i++) {
            String item = setting.get(i);
            int itemY = listY + (i - start) * itemHeight;

            boolean hovered = mouseX >= listLeft && mouseX <= listRight && mouseY >= itemY && mouseY <= itemY + itemHeight;
            if (hovered) {
                context.fill(listLeft, itemY, listRight, itemY + itemHeight, new Color(255, 255, 255, 30).getRGB());
            }

            context.drawString(FontUtil.font, item, listLeft + 4, itemY + 2, Color.LIGHT_GRAY.getRGB());

            String removeLabel = "x";
            int removeX = listRight - 15;
            int removeColor = Color.RED.getRGB();

            if (hovered && mouseX >= removeX) {
                removeColor = Color.WHITE.getRGB();
            }
            context.drawString(FontUtil.font, removeLabel, removeX, itemY + 2, removeColor);
        }

        super.render(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (click.button() == 0) {
            int listY = this.textField.getY() + 50;
            int itemHeight = FontUtil.font.lineHeight + 4;
            int listLeft = this.textField.getX();
            int listRight = listLeft + this.textField.getWidth() + 60;
            int removeX = listRight - 15;

            for (int i = scrollOffset; i < setting.size(); i++) {
                int itemY = listY + (i - scrollOffset) * itemHeight;
                if (click.x() >= removeX && click.x() <= listRight
                        && click.y() >= itemY && click.y() <= itemY + itemHeight) {
                    setting.remove(setting.get(i));
                    return true;
                }
            }
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int maxScroll = Math.max(0, setting.size() - 5);
        if (verticalAmount > 0) {
            scrollOffset = Math.max(0, scrollOffset - 1);
        } else if (verticalAmount < 0) {
            scrollOffset = Math.min(maxScroll, scrollOffset + 1);
        }
        return true;
    }

    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
        ClickGui clickGui = ModuleUtil.getModule(ClickGui.class);
        switch (clickGui.background.getValue()) {
            case INGAME -> this.renderTransparentBackground(context);
            case PANORAMA -> this.renderPanorama(context, deltaTicks);
            case BLUR -> this.renderBlurredBackground(context);
            case DARKENING -> this.renderMenuBackground(context);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (input.isConfirmation()) {
            if (!textField.getValue().isEmpty()) {
                setting.add(textField.getValue());
                textField.setValue("");
            }
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(Pigeon.clickGuiScreen);
    }
}
