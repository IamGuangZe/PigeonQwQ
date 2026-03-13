package owo.pigeon.mixin.mixins;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import owo.pigeon.Pigeon;
import owo.pigeon.event.events.RenderEvent;
import owo.pigeon.modules.impl.Render.ModifyCamera;
import owo.pigeon.utils.ModuleUtil;

@Mixin(InGameHud.class)
public abstract class MixinInGameHud {
    @Inject(method = "render", at = @At("RETURN"))
    private void onRender2DPost(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        Pigeon.EVENT_BUS.post(new RenderEvent.Render2DEvent(context)).now();
    }

    @Inject(method = "renderPortalOverlay", at = @At("HEAD"), cancellable = true)
    private void onRenderPortalOverlay(DrawContext context, float nauseaStrength, CallbackInfo ci) {
        if (ModuleUtil.isEnable(ModifyCamera.class) && ModuleUtil.getModule(ModifyCamera.class).noPortalOverlay.getValue()) {
            ci.cancel();
        }
    }

    @ModifyArgs(method = "renderMiscOverlays", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderOverlay(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/util/Identifier;F)V", ordinal = 0))
    private void onRenderPumpkinOverlay(Args args) {
        if (ModuleUtil.isEnable(ModifyCamera.class) && ModuleUtil.getModule(ModifyCamera.class).noPumpkinOverlay.getValue()) {
            args.set(2, 0f);
        }
    }

    @Inject(method = "renderNauseaOverlay", at = @At("HEAD"), cancellable = true)
    private void onRenderNauseaOverlay(DrawContext context, float distortionStrength, CallbackInfo ci) {
        if (ModuleUtil.isEnable(ModifyCamera.class) && ModuleUtil.getModule(ModifyCamera.class).noNausea.getValue()) {
            ci.cancel();
        }
    }
}
