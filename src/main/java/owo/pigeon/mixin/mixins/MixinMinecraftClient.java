package owo.pigeon.mixin.mixins;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import owo.pigeon.Pigeon;
import owo.pigeon.event.events.DoAttackEvent;
import owo.pigeon.event.events.DoItemUseEvent;
import owo.pigeon.event.events.TickEvent;
import owo.pigeon.modules.impl.Combat.AutoClicker;
import owo.pigeon.modules.impl.Combat.NoHitDelay;
import owo.pigeon.modules.impl.Player.FastPlace;
import owo.pigeon.modules.impl.Render.FreeLook;
import owo.pigeon.utils.ModuleUtil;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {

    @Shadow
    public int attackCooldown;

    @Shadow
    private int itemUseCooldown;

    @Unique
    private ClientWorld lastProcessedWorld;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onClientTickPre(CallbackInfo ci) {
        Pigeon.EVENT_BUS.post(new TickEvent.ClientTickEvent.Pre()).now();
    }

    @Inject(method = "tick",at = @At("RETURN"))
    private void onClientTickPost(CallbackInfo ci) {
        Pigeon.EVENT_BUS.post(new TickEvent.ClientTickEvent.Post()).now();
    }

    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void onDoAttackPre(CallbackInfoReturnable<Boolean> cir) {
        DoAttackEvent.Pre event = new DoAttackEvent.Pre();
        Pigeon.EVENT_BUS.post(event).now();
        if (event.isCancelled()) {
            cir.cancel();
        }

        boolean removeDelay = false;
        if (ModuleUtil.isEnable(NoHitDelay.class)) removeDelay = true;
        if (ModuleUtil.isEnable(AutoClicker.class) && ModuleUtil.getModule(AutoClicker.class).leftClick.getValue()) removeDelay = true;
        if (removeDelay) this.attackCooldown = 0;
    }

    @Inject(method = "doItemUse", at = @At("HEAD"), cancellable = true)
    private void onDoItemUsePre(CallbackInfo ci) {
        DoItemUseEvent.Pre event = new DoItemUseEvent.Pre();
        Pigeon.EVENT_BUS.post(event).now();
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "doItemUse", at = @At("RETURN"))
    private void onDoItemUsePost(CallbackInfo ci) {
        Pigeon.EVENT_BUS.post(new DoItemUseEvent.Post()).now();

        if (ModuleUtil.isEnable(FastPlace.class) && ModuleUtil.getModule(FastPlace.class).canFastPlace()) {
            itemUseCooldown = ModuleUtil.getModule(FastPlace.class).delay.getValue();
        }
    }

    @Redirect(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/Perspective;next()Lnet/minecraft/client/option/Perspective;"))
    private Perspective onCyclePerspective(Perspective instance) {
        Perspective next = instance.next();

        if (ModuleUtil.getModule(FreeLook.class).freelooking && next == Perspective.FIRST_PERSON) {
            return next.next();
        }

        return next;
    }
}
