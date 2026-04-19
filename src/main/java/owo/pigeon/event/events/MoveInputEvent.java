package owo.pigeon.event.events;

import net.minecraft.client.input.Input;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.Vec2f;
import owo.pigeon.event.Event;
import owo.pigeon.mixin.accessors.IAccessorInput;

public class MoveInputEvent extends Event {

    private final Input input;

    public MoveInputEvent(Input input) {
        this.input = input;
    }

    public Input getInput() {
        return input;
    }

    public PlayerInput getPlayerInput() {
        return input.playerInput;
    }

    public void setPlayerInput(PlayerInput playerInput) {
        input.playerInput = playerInput;
    }

    public Vec2f getMovementVector() {
        return ((IAccessorInput) input).pigeon$getMovementVector();
    }

    public void setMovementVector(Vec2f movementVector) {
        ((IAccessorInput) input).pigeon$setMovementVector(movementVector);
    }

    public boolean isSneaking() {
        return input.playerInput.sneak();
    }

    public boolean isJumping() {
        return input.playerInput.jump();
    }

    public void setSneaking(boolean sneak) {
        PlayerInput old = input.playerInput;
        if (old.sneak() == sneak) return;

        input.playerInput = new PlayerInput(
                old.forward(), old.backward(),
                old.left(), old.right(),
                old.jump(), sneak, old.sprint()
        );

        Vec2f mv = getMovementVector();
        if (sneak) {
            setMovementVector(new Vec2f(mv.x * 0.3f, mv.y * 0.3f));
        } else {
            setMovementVector(new Vec2f(mv.x / 0.3f, mv.y / 0.3f));
        }
    }
}
