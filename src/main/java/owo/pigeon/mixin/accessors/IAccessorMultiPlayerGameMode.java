package owo.pigeon.mixin.accessors;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MultiPlayerGameMode.class)
public interface IAccessorMultiPlayerGameMode {
    @Accessor("isDestroying")
    boolean pigeon$isDestroying();
}
