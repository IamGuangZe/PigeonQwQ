package owo.pigeon.mixin.accessors;

import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(InGameHud.class)
public interface IAccessorInGameHud {
    @Accessor("title")
    Text getTitle();

    @Accessor("subtitle")
    Text getSubtitle();
}
