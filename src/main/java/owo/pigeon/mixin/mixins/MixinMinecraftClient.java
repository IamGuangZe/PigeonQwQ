package owo.pigeon.mixin.mixins;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import owo.pigeon.Pigeon;
import owo.pigeon.event.events.ClientTickEvent;
import owo.pigeon.event.events.DoAttackEvent;
import owo.pigeon.event.events.DoItemUseEvent;
import owo.pigeon.modules.impl.combat.AutoClicker;
import owo.pigeon.modules.impl.combat.NoHitDelay;
import owo.pigeon.modules.impl.player.FastPlace;
import owo.pigeon.modules.impl.render.FreeLook;
import owo.pigeon.utils.ModuleUtil;

@Mixin(Minecraft.class)
public class MixinMinecraftClient {

    @Shadow
    public int missTime;

    @Shadow
    private int rightClickDelay;

    @Unique
    private ClientLevel lastProcessedWorld;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onClientTickPre(CallbackInfo ci) {
        Pigeon.EVENT_BUS.post(new ClientTickEvent.Pre()).now();
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void onClientTickPost(CallbackInfo ci) {
        Pigeon.EVENT_BUS.post(new ClientTickEvent.Post()).now();
    }

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void onDoAttackPre(CallbackInfoReturnable<Boolean> cir) {
        DoAttackEvent.Pre event = new DoAttackEvent.Pre();
        Pigeon.EVENT_BUS.post(event).now();
        if (event.isCancelled()) {
            cir.cancel();
        }

        boolean removeDelay = ModuleUtil.isEnable(NoHitDelay.class);
        if (ModuleUtil.isEnable(AutoClicker.class) && ModuleUtil.getModule(AutoClicker.class).leftClick.getValue())
            removeDelay = true;
        if (removeDelay) this.missTime = 0;
    }

    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void onDoItemUsePre(CallbackInfo ci) {
        DoItemUseEvent.Pre event = new DoItemUseEvent.Pre();
        Pigeon.EVENT_BUS.post(event).now();
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "startUseItem", at = @At("RETURN"))
    private void onDoItemUsePost(CallbackInfo ci) {
        Pigeon.EVENT_BUS.post(new DoItemUseEvent.Post()).now();

        if (ModuleUtil.isEnable(FastPlace.class) && ModuleUtil.getModule(FastPlace.class).canFastPlace()) {
            rightClickDelay = ModuleUtil.getModule(FastPlace.class).delay.getValue();
        }
    }

    @Redirect(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/CameraType;cycle()Lnet/minecraft/client/CameraType;"))
    private CameraType onCyclePerspective(CameraType instance) {
        CameraType next = instance.cycle();

        if (ModuleUtil.getModule(FreeLook.class).freelooking && next == CameraType.FIRST_PERSON) {
            return next.cycle();
        }

        return next;
    }
}
