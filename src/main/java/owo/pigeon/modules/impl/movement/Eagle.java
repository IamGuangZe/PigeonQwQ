package owo.pigeon.modules.impl.movement;

import net.engio.mbassy.listener.Handler;
import net.minecraft.item.BlockItem;
import net.minecraft.util.math.BlockPos;
import owo.pigeon.event.events.ClientTickEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.IntSetting;
import owo.pigeon.utils.KeybindUtil;
import owo.pigeon.utils.RandomUtil;
import owo.pigeon.utils.WorldUtil;

import static owo.pigeon.Pigeon.mc;

public class Eagle extends Module {

    public Eagle() {
        super("Eagle", Category.MOVEMENT);
    }

    public final IntSetting minDelay = setting("min-delay", 2, 0, 10, v -> true);
    public final IntSetting maxDelay = setting("max-delay", 3, 0, 10, v -> true);
    public final EnableSetting directionCheck = setting("direction-check", true, v -> true);
    public final EnableSetting jumpCheck = setting("jump-check", true, v -> true);
    public final EnableSetting pitchCheck = setting("pitch-check", true, v -> true);
    public final EnableSetting blocksOnly = setting("blocks-only", true, v -> true);

    private int sneakDelay = 0;

    @Handler
    public void onTickPre(ClientTickEvent.Pre event) {
        if (WorldUtil.nullCheck()) return;

        if (sneakDelay > 0) sneakDelay--;
        if (sneakDelay == 0 && isAtEdge()) sneakDelay = RandomUtil.intRandom(minDelay.getValue(), maxDelay.getValue());

        if (KeybindUtil.isPressed(mc.options.sneakKey)) return;

        if (shouldSneak() && (sneakDelay > 0 || isAtEdge())) {
            KeybindUtil.setPressed(mc.options.sneakKey, true);
        } else {
            KeybindUtil.resetPressed(mc.options.sneakKey);
        }
    }

    @Override
    public void onDisable() {
        KeybindUtil.resetPressed(mc.options.sneakKey);
    }

    private boolean shouldSneak() {
        if (directionCheck.getValue() && !KeybindUtil.isPressed(mc.options.backKey)) {
            return false;
        } else if (jumpCheck.getValue() && KeybindUtil.isPressed(mc.options.jumpKey)) {
            return false;
        } else if (pitchCheck.getValue() && mc.player.getPitch() < 67.0F) {
            return false;
        } else {
            return !blocksOnly.getValue() || mc.player.getMainHandStack().getItem() instanceof BlockItem;
        }
    }

    private boolean isAtEdge() {
        BlockPos pos = BlockPos.ofFloored(mc.player.getX(), mc.player.getY() - 1, mc.player.getZ());
        return mc.player.isOnGround() && mc.world.getBlockState(pos).isAir();
    }

    @Override
    public String getSuffix() {
        return minDelay.getValue() + "-" + maxDelay.getValue();
    }
}