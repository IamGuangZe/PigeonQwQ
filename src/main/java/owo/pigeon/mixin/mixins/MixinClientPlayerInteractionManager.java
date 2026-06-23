package owo.pigeon.mixin.mixins;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import owo.pigeon.Pigeon;
import owo.pigeon.event.events.ClickSlotEvent;
import owo.pigeon.modules.impl.player.NoBreakDelay;
import owo.pigeon.utils.ModuleUtil;
import owo.pigeon.utils.chat.ChatUtil;

@Mixin(MultiPlayerGameMode.class)
public class MixinClientPlayerInteractionManager {
    @Shadow
    private int destroyDelay;

    @Inject(method = "handleInventoryMouseClick", at = @At("HEAD"))
    private void onClickSlotPre(int syncId, int slotId, int button, ClickType actionType, Player player, CallbackInfo ci) {
        Pigeon.EVENT_BUS.post(new ClickSlotEvent()).now();
        ChatUtil.sendDebugMessage("MixinClientPlayerInteractionManager", "syanId: " + syncId + ", slotId: " + slotId);
    }

    @Inject(method = "continueDestroyBlock", at = @At("HEAD"))
    private void onUpdateBlockBreakingProgress(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (ModuleUtil.isEnable(NoBreakDelay.class)) destroyDelay = 0;
    }
}
