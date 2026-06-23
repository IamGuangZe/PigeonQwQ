package owo.pigeon.mixin.accessors;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface IAccessorHandledScreen {
    @Accessor("leftPos")
    int pigeon$getX();

    @Accessor("topPos")
    int pigeon$getY();

    @Accessor("imageWidth")
    int pigeon$getBackgroundWidth();

    @Accessor("imageHeight")
    int pigeon$getBackgroundHeight();
}
