package owo.pigeon.mixin.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import owo.pigeon.modules.impl.render.ModifyCamera;
import owo.pigeon.utils.ModuleUtil;

@Mixin(ScreenEffectRenderer.class)
public class MixinScreenEffectRenderer {
    @Inject(method = "renderWater", at = @At("HEAD"), cancellable = true)
    private static void onRenderWater(Minecraft client, PoseStack matrices, MultiBufferSource vertexConsumers, CallbackInfo ci) {
        if (ModuleUtil.isEnable(ModifyCamera.class) && ModuleUtil.getModule(ModifyCamera.class).noUnderwaterOverlay.getValue()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderFire", at = @At("HEAD"), cancellable = true)
    private static void onRenderFire(PoseStack matrices, MultiBufferSource vertexConsumers, TextureAtlasSprite sprite, CallbackInfo ci) {
        if (ModuleUtil.isEnable(ModifyCamera.class) && ModuleUtil.getModule(ModifyCamera.class).noFireOverlay.getValue()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderTex", at = @At("HEAD"), cancellable = true)
    private static void onRenderTex(TextureAtlasSprite sprite, PoseStack matrices, MultiBufferSource vertexConsumers, CallbackInfo ci) {
        if (ModuleUtil.isEnable(ModifyCamera.class) && ModuleUtil.getModule(ModifyCamera.class).noInWallOverlay.getValue()) {
            ci.cancel();
        }
    }
}
