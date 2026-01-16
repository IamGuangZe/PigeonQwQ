package owo.pigeon.mixin.mixins;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import owo.pigeon.Pigeonqwq;
import owo.pigeon.event.events.RenderEvent;

@Mixin(InGameHud.class)
public abstract class MixinInGameHud {

    @Inject(method = "render", at = @At("RETURN"))
    public void onRender2DPost(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        Pigeonqwq.EVENT_BUS.post(new RenderEvent.Render2DEvent(context)).now();
    }
}
