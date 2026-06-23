package owo.pigeon.event.events;

import net.minecraft.client.player.ClientInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import owo.pigeon.event.Event;
import owo.pigeon.mixin.accessors.IAccessorClientInput;

public class MoveInputEvent extends Event {

    private final ClientInput input;

    public MoveInputEvent(ClientInput input) {
        this.input = input;
    }

    public ClientInput getInput() {
        return input;
    }

    public Input getPlayerInput() {
        return input.keyPresses;
    }

    public void setPlayerInput(Input playerInput) {
        input.keyPresses = playerInput;
    }

    public Vec2 getMovementVector() {
        return ((IAccessorClientInput) input).pigeon$getMoveVector();
    }

    public void setMovementVector(Vec2 movementVector) {
        ((IAccessorClientInput) input).pigeon$setMoveVector(movementVector);
    }

    public boolean isSneaking() {
        return input.keyPresses.shift();
    }

    public boolean isJumping() {
        return input.keyPresses.jump();
    }

    public void setSneaking(boolean sneak) {
        Input old = input.keyPresses;
        if (old.shift() == sneak) return;

        input.keyPresses = new Input(
                old.forward(), old.backward(),
                old.left(), old.right(),
                old.jump(), sneak, old.sprint()
        );

        Vec2 mv = getMovementVector();
        if (sneak) {
            setMovementVector(new Vec2(mv.x * 0.3f, mv.y * 0.3f));
        } else {
            setMovementVector(new Vec2(mv.x / 0.3f, mv.y / 0.3f));
        }
    }
}
