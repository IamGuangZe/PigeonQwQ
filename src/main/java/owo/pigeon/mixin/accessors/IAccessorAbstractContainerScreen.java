package owo.pigeon.mixin.accessors;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface IAccessorAbstractContainerScreen {
    @Accessor("leftPos")
    int pigeon$getLeftPos();

    @Accessor("topPos")
    int pigeon$getTopPos();

    @Accessor("imageWidth")
    int pigeon$getImageWidth();

    @Accessor("imageHeight")
    int pigeon$getImageHeight();
}
