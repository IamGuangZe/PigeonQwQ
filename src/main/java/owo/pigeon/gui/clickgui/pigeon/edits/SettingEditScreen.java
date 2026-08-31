package owo.pigeon.gui.clickgui.pigeon.edits;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import owo.pigeon.Pigeon;
import owo.pigeon.modules.impl.client.ClickGui;
import owo.pigeon.settings.AbstractSetting;
import owo.pigeon.settings.BlockSetting;
import owo.pigeon.settings.CharSetting;
import owo.pigeon.settings.StringSetting;
import owo.pigeon.utils.ModuleUtil;
import owo.pigeon.utils.render.FontUtil;

import java.awt.*;

public class SettingEditScreen extends Screen {
    protected final AbstractSetting<?> setting;

    private EditBox textField;
    private String currentText = null;

    public SettingEditScreen(AbstractSetting<?> setting) {
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

        this.textField = new EditBox(FontUtil.font, x, y, fieldWidth, 20, Component.nullToEmpty("Setting Input"));

        if (currentText != null) {
            textField.setValue(currentText);
        } else if (setting instanceof BlockSetting blockSetting) {
            textField.setValue(BuiltInRegistries.BLOCK.getKey(blockSetting.getValue()).toString());
        } else if (setting instanceof StringSetting stringSetting) {
            textField.setValue(stringSetting.getValue());
        } else if (setting instanceof CharSetting charSetting) {
            textField.setValue(String.valueOf(charSetting.getValue()));
            textField.setMaxLength(1);
        }

        this.textField.setResponder(text -> this.currentText = text);

        this.addRenderableWidget(this.textField);
        this.setInitialFocus(this.textField);

        int buttonWidth = (fieldWidth - 10) / 2;
        int buttonY = y + 25;

        this.addRenderableWidget(Button.builder(Component.nullToEmpty("Confirm"), (button) -> saveAndClose()).bounds(x, buttonY, buttonWidth, 20).build());

        this.addRenderableWidget(Button.builder(Component.nullToEmpty("Cancel"), (button) -> this.onClose()).bounds(x + buttonWidth + 10, buttonY, buttonWidth, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
        String text = "You are editing: " + setting.getName();
        int textWidth = FontUtil.font.width(text);
        int textX = (this.width - textWidth) / 2;
        int textY = this.textField.getY() - FontUtil.font.lineHeight - 2;
        context.text(FontUtil.font, text, textX, textY, Color.WHITE.getRGB());

        if (setting instanceof BlockSetting blockSetting) {
            Identifier id = Identifier.tryParse(textField.getValue().toLowerCase().trim());

            if (id != null && BuiltInRegistries.BLOCK.containsKey(id)) {
                textField.setTextColor(0xFFFFFFFF);

                int previewX = textField.getX() + textField.getWidth() + 4;
                int previewY = textField.getY() + (textField.getHeight() - 16) / 2;

                context.item(BuiltInRegistries.BLOCK.getValue(id).asItem().getDefaultInstance(), previewX, previewY);
            } else {
                textField.setTextColor(0xFFFF5555);
            }

        }

        super.extractRenderState(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
        ClickGui clickGui = ModuleUtil.getModule(ClickGui.class);

        // if (clickGui.background.getValue()) super.extractBackground(context, mouseX, mouseY, deltaTicks);
        switch (clickGui.background.getValue()) {
            case INGAME -> this.extractTransparentBackground(context);
            case PANORAMA -> this.extractPanorama(context, deltaTicks);
            case BLUR -> this.extractBlurredBackground(context);
            case DARKENING -> this.extractMenuBackground(context);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (input.isConfirmation()) {
            saveAndClose();
            return true;
        }

        return super.keyPressed(input);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(Pigeon.clickGuiScreen);
    }

    private void saveAndClose() {
        if (textField.getValue().isEmpty()) {
            this.onClose();
            return;
        }

        if (setting instanceof BlockSetting blockSetting) {
            Identifier id = Identifier.tryParse(textField.getValue().toLowerCase());
            if (id != null && BuiltInRegistries.BLOCK.containsKey(id)) {
                blockSetting.setValue(BuiltInRegistries.BLOCK.getValue(id));
            } else {
                this.onClose();
                return;
            }
        } else if (setting instanceof CharSetting charSetting) {
            charSetting.setValue(textField.getValue().charAt(0));
        } else if (setting instanceof StringSetting stringSetting) {
            stringSetting.setValue(textField.getValue());
        }

        this.onClose();
    }
}
