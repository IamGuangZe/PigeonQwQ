package owo.pigeon.mixin.accessors;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HandledScreen.class)
public interface IAccessorHandledScreen {
    @Accessor("x")
    int pigeon$getX();

    @Accessor("y")
    int pigeon$getY();

    @Accessor("backgroundWidth")
    int pigeon$getBackgroundWidth();

    @Accessor("backgroundHeight")
    int pigeon$getBackgroundHeight();
}
