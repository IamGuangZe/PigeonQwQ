package owo.pigeon.mixin.accessors;

import net.minecraft.client.input.Input;
import net.minecraft.util.math.Vec2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Input.class)
public interface IAccessorInput {

    @Accessor("movementVector")
    Vec2f pigeon$getMovementVector();

    @Accessor("movementVector")
    void pigeon$setMovementVector(Vec2f movementVector);
}
