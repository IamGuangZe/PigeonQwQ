package owo.pigeon.mixin.mixins;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import owo.pigeon.Pigeonqwq;
import owo.pigeon.event.events.TickEvent;
import owo.pigeon.modules.impl.Combat.AutoClicker;
import owo.pigeon.modules.impl.Combat.NoHitDelay;
import owo.pigeon.utils.ModuleUtil;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {

    @Shadow
    public int attackCooldown;

    @Inject(method = "tick", at = @At("HEAD"))
    public void onClientTickPre(CallbackInfo ci) {
        Pigeonqwq.EVENT_BUS.post(new TickEvent.ClientTickEvent(TickEvent.Phase.PRE)).now();
    }

    @Inject(method = "tick",at = @At("RETURN"))
    public void onClientTickPost(CallbackInfo ci) {
        Pigeonqwq.EVENT_BUS.post(new TickEvent.ClientTickEvent(TickEvent.Phase.POST)).now();
    }

    @Inject(method = "doAttack", at = @At("HEAD"))
    public void onDoAttackPre(CallbackInfoReturnable<Boolean> cir) {
        boolean removeDelay = false;
        if (ModuleUtil.isEnable(NoHitDelay.class)) removeDelay = true;
        if (ModuleUtil.isEnable(AutoClicker.class) && ModuleUtil.getModule(AutoClicker.class).leftClick.getValue()) removeDelay = true;
        if (removeDelay) this.attackCooldown = 0;


    }
}
