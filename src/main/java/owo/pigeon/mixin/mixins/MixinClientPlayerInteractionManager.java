package owo.pigeon.mixin.mixins;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import owo.pigeon.Pigeonqwq;
import owo.pigeon.event.events.ClickSlotEvent;
import owo.pigeon.modules.impl.Player.NoBreakDelay;
import owo.pigeon.utils.ModuleUtil;

@Mixin(ClientPlayerInteractionManager.class)
public class MixinClientPlayerInteractionManager {
    @Shadow
    private int blockBreakingCooldown;

    @Inject(method = "clickSlot",at = @At("HEAD"))
    public void onClickSlotPre(int syncId, int slotId, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        Pigeonqwq.EVENT_BUS.post(new ClickSlotEvent()).now();
    }

    @Inject(method = "updateBlockBreakingProgress", at = @At("HEAD"))
    public void onUpdateBlockBreakingProgress(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (ModuleUtil.isEnable(NoBreakDelay.class)) blockBreakingCooldown = 0;
    }
}
