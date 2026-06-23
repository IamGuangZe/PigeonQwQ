package owo.pigeon.mixin.accessors;

import net.minecraft.client.player.ClientInput;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientInput.class)
public interface IAccessorClientInput {

    @Accessor("moveVector")
    Vec2 pigeon$getMoveVector();

    @Accessor("moveVector")
    void pigeon$setMoveVector(Vec2 movementVector);
}
