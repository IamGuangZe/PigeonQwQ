package owo.pigeon.modules.impl.Client.Debug;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.utils.Chat.ChatUtil;
import owo.pigeon.utils.TextRendererUtil;

import java.util.ArrayList;
import java.util.List;

public class TextRenderTest extends Module {
    public TextRenderTest() {
        super("TextRenderTest", Category.CLIENT, InputUtil.GLFW_KEY_Y);
    }

    @Override
    public void onRender2D(DrawContext context) {
        TextRendererUtil.drawString(context, "line 1: with color input &a[&&a]", 100, 100, 0xFF000000);
        TextRendererUtil.drawString(context, "line 2: without color input &a[&&a]", 100, 100 + TextRendererUtil.getLineHeight());

        List<String> mutiText = new ArrayList<>();
        mutiText.add("line 3: input &a[&&a]");
        mutiText.add("line 4: muti lines &a[&&a]");

        TextRendererUtil.drawStringList(context, mutiText, 100, 100 + TextRendererUtil.getLineHeight() * 2);

        ChatUtil.sendDebugMessage(this.name,"drawString");
    }
}
