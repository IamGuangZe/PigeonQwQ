package owo.pigeon.mixin.mixins;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import owo.pigeon.modules.impl.client.PigeonQwQ;
import owo.pigeon.utils.ColorUtil;
import owo.pigeon.utils.CommandUtil;
import owo.pigeon.utils.ModuleUtil;
import owo.pigeon.utils.render.RenderUtil;

import java.awt.*;

@Mixin(ChatScreen.class)
public class MixinChatScreen {

    @Shadow
    protected EditBox input;

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!ModuleUtil.getModule(PigeonQwQ.class).commandBorder.getValue()) return;

        String text = input.getValue();
        if (text.isBlank()) return;
        if (!text.stripLeading().startsWith(String.valueOf(CommandUtil.getCommandPrefix()))) return;

        int x = input.getX();
        int y = input.getY();
        int w = input.getWidth();
        int h = input.getHeight();

        ColorUtil.Theme theme = ColorUtil.getTheme();
        int[] gradient = theme.getGradient();
        if (gradient != null) {
            RenderUtil.drawGradientBorder(context, x - 3, y - 3, w + 2, h + 2, gradient);
        } else {
            RenderUtil.drawBorder(context, x - 3, y - 3, w + 2, h + 2, Color.WHITE.getRGB());
        }
    }
}
