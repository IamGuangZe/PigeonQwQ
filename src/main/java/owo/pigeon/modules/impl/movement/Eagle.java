package owo.pigeon.modules.impl.movement;

import net.engio.mbassy.listener.Handler;
import net.minecraft.client.input.Input;
import net.minecraft.item.BlockItem;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.Vec2f;
import owo.pigeon.event.events.MoveInputEvent;
import owo.pigeon.event.events.TickEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.IntSetting;
import owo.pigeon.utils.KeybindUtil;
import owo.pigeon.utils.RandomUtil;
import owo.pigeon.utils.WorldUtil;
import owo.pigeon.utils.player.MoveUtil;
import owo.pigeon.utils.player.PlayerUtil;

import static owo.pigeon.Pigeon.mc;

public class Eagle extends Module {

    public Eagle() {
        super("Eagle", Category.MOVEMENT);
    }

    // Ported from: https://github.com/60124808866/OpenMyau/blob/main/src/main/java/myau/module/modules/Eagle.java

    public final IntSetting minDelay = setting("min-delay", 2, 0, 10, v -> true);
    public final IntSetting maxDelay = setting("max-delay", 3, 0, 10, v -> true);
    public final EnableSetting directionCheck = setting("direction-check", true, v -> true);
    public final EnableSetting jumpCheck = setting("jump-check", true, v -> true);
    public final EnableSetting pitchCheck = setting("pitch-check", true, v -> true);
    public final EnableSetting blocksOnly = setting("blocks-only", true, v -> true);

    private int sneakDelay = 0;

    private boolean canMoveSafely() {
        double[] offset = MoveUtil.predictMovement();
        return PlayerUtil.canMove(mc.player.getVelocity().x + offset[0], mc.player.getVelocity().z + offset[1]);
    }

    private boolean shouldSneak() {
        if (directionCheck.getValue() && !KeybindUtil.isPressed(mc.options.backKey)) {
            return false;
        } else if (jumpCheck.getValue() && KeybindUtil.isPressed(mc.options.jumpKey)) {
            return false;
        } else if (pitchCheck.getValue() && mc.player.getPitch() < 67.0F) {
            return false;
        } else {
            return (!blocksOnly.getValue() || mc.player.getMainHandStack().getItem() instanceof BlockItem) && mc.player.isOnGround();
        }
    }

    @Handler
    public void onTickPre(TickEvent.ClientTickEvent.Pre event) {
        if (WorldUtil.nullCheck()) return;

        if (sneakDelay > 0) {
            sneakDelay--;
        }
        if (sneakDelay == 0 && canMoveSafely()) {
            sneakDelay = RandomUtil.intRandom(minDelay.getValue(), maxDelay.getValue());
        }
    }

    @Handler
    public void onMoveInput(MoveInputEvent event) {
        if (mc.currentScreen != null) return;

        Input input = event.getInput();
        PlayerInput playerInput = input.playerInput;

        if (!playerInput.sneak()) {
            if (shouldSneak() && (sneakDelay > 0 || canMoveSafely())) {
                input.playerInput = new PlayerInput(
                        playerInput.forward(), playerInput.backward(),
                        playerInput.left(), playerInput.right(),
                        playerInput.jump(), true, playerInput.sprint()
                );
                Vec2f mv = event.getMovementVector();
                event.setMovementVector(new Vec2f(mv.x * 0.3f, mv.y * 0.3f));
            }
        }
    }

    @Override
    public void onDisable() {
        sneakDelay = 0;
    }

    @Override
    public String getSuffix() {
        return minDelay.getValue() + "-" + maxDelay.getValue();
    }
}