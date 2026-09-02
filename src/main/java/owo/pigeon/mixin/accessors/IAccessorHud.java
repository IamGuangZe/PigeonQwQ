package owo.pigeon.mixin.accessors;

import net.minecraft.client.gui.Hud;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Hud.class)
public interface IAccessorHud {
    @Accessor("title")
    Component pigeon$getTitle();

    @Accessor("subtitle")
    Component pigeon$getSubtitle();
}
