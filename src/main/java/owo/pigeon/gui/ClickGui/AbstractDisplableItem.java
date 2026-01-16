package owo.pigeon.gui.ClickGui;

import net.minecraft.client.gui.DrawContext;
import owo.pigeon.modules.impl.Client.ClickGui;
import owo.pigeon.utils.ModuleUtil;

public abstract class AbstractDisplableItem {
    public static ClickGui clickGui = ModuleUtil.getModule(ClickGui.class);

    public int x, y, width, height = 0;

    public abstract void drawScreen(DrawContext context, int mouseX, int mouseY, float delta);

    public boolean isHovered(int mouseX, int mouseY) {
        return (mouseX > x && mouseX < x + width) && (mouseY > y && mouseY < y + height);
    }

    protected static boolean isHovered(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX - width <= x && mouseY >= y && mouseY - height <= y;//获取鼠标位置是否在指定位置
    }
}
