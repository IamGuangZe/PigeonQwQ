package owo.pigeon.mixin.accessors;

import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Gui.class)
public interface IAccessorInGameHud {
    @Accessor("title")
    Component pigeon$getTitle();

    @Accessor("subtitle")
    Component pigeon$getSubtitle();
}
