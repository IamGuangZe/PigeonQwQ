package owo.pigeon.mixin.mixins;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import owo.pigeon.Pigeonqwq;
import owo.pigeon.event.events.TickEvent;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {

    @Inject(method = "tick", at = @At("HEAD"))
    public void onClientTickPre(CallbackInfo ci) {
        Pigeonqwq.EVENT_BUS.post(new TickEvent.ClientTickEvent(TickEvent.Phase.PRE)).now();
    }

    @Inject(method = "tick",at = @At("RETURN"))
    public void onClientTickPost(CallbackInfo ci) {
        Pigeonqwq.EVENT_BUS.post(new TickEvent.ClientTickEvent(TickEvent.Phase.POST)).now();
    }
}
