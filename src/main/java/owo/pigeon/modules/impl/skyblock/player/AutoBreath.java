package owo.pigeon.modules.impl.skyblock.player;

import net.engio.mbassy.listener.Handler;
import net.minecraft.client.player.ClientInput;
import net.minecraft.world.entity.player.Input;
import owo.pigeon.event.events.MoveInputEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.FloatSetting;
import owo.pigeon.utils.hypixel.skyblock.SkyblockUtil;

import static owo.pigeon.Pigeon.mc;

public class AutoBreath extends Module {
    public AutoBreath() {
        super("AutoBreath", Category.PLAYER);
    }

    public FloatSetting air = setting("air", 4.0f, 0.0f, 20.0f, v -> true);
    public EnableSetting reDive = setting("re-dive", true, v -> true);
    public EnableSetting onlyInGalatea = setting("only-in-galatea", true, v -> true);

    @Handler
    public void onMoveInput(MoveInputEvent event) {
        if (mc.gui.screen() != null) return;
        if (onlyInGalatea.getValue() && !SkyblockUtil.isInIsland(SkyblockUtil.Island.GALATEA))
            return;

        ClientInput input = event.getInput();
        Input playerInput = input.keyPresses;

        boolean shouldJump = mc.player.getAirSupply() / 15.0f <= air.getValue() || playerInput.jump();
        boolean shouldSneak = reDive.getValue()
                && mc.player.getAirSupply() / 15.0f > air.getValue()
                && mc.player.isInLiquid()
                && !mc.player.onGround()
                && !shouldJump
                || playerInput.shift();

        input.keyPresses = new Input(
                playerInput.forward(), playerInput.backward(),
                playerInput.left(), playerInput.right(),
                shouldJump, shouldSneak, playerInput.sprint()
        );
    }
}
