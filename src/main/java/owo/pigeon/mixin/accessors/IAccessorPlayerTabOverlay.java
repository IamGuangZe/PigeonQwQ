package owo.pigeon.mixin.accessors;

import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerTabOverlay.class)
public interface IAccessorPlayerTabOverlay {
    @Accessor("header")
    Component pigeon$getHeader();

    @Accessor("footer")
    Component pigeon$getFooter();
}
