package owo.pigeon.mixin.mixins;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import owo.pigeon.interfaces.ICameraOverriddenEntity;
import owo.pigeon.modules.impl.render.FreeLook;
import owo.pigeon.modules.impl.render.ModifyCamera;
import owo.pigeon.utils.ModuleUtil;

@Mixin(Camera.class)
public abstract class MixinCamera {

    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    @Shadow
    private Entity entity;

    @ModifyVariable(method = "getMaxZoom", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float onGetMaxZoom(float value) {
        return ModuleUtil.isEnable(ModifyCamera.class) ? ModuleUtil.getModule(ModifyCamera.class).distance.getValue() : 4.0f;
    }

    @Inject(method = "getMaxZoom", at = @At("HEAD"), cancellable = true)
    private void onGetMaxZoom(float f, CallbackInfoReturnable<Float> cir) {
        if (ModuleUtil.isEnable(ModifyCamera.class) && ModuleUtil.getModule(ModifyCamera.class).camNoClip.getValue())
            cir.setReturnValue(f);
    }

    @Inject(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getMaxZoom(F)F"))
    private void onSetRotation(float partialTicks, CallbackInfo ci) {
        if (ModuleUtil.getModule(FreeLook.class).freelooking && this.entity instanceof LocalPlayer) {
            ICameraOverriddenEntity cameraOverriddenEntity = (ICameraOverriddenEntity) this.entity;

            float yaw = cameraOverriddenEntity.pigeon$getCameraYaw();
            float pitch = cameraOverriddenEntity.pigeon$getCameraPitch();
            if (Minecraft.getInstance().options.getCameraType().isMirrored()) {
                yaw += 180.0f;
                pitch = -pitch;
            }
            this.setRotation(yaw, pitch);

        }
    }
}
