package owo.pigeon.mixin.mixins;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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

    @Unique
    boolean firstTime = true;

    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    @ModifyVariable(method = "getMaxZoom", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float onGetMaxZoom(float value) {
        return ModuleUtil.isEnable(ModifyCamera.class) ? ModuleUtil.getModule(ModifyCamera.class).distance.getValue() : 4.0f;
    }

    @Inject(method = "getMaxZoom", at = @At("HEAD"), cancellable = true)
    private void onGetMaxZoom(float f, CallbackInfoReturnable<Float> cir) {
        if (ModuleUtil.isEnable(ModifyCamera.class) && ModuleUtil.getModule(ModifyCamera.class).camNoClip.getValue())
            cir.setReturnValue(f);
    }

    @Inject(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V", ordinal = 1, shift = At.Shift.AFTER))
    private void onSetRotation(Level focusedBlock, Entity cameraEntity, boolean isThirdPerson, boolean isFrontFacing, float tickDelta, CallbackInfo ci) {
        if (ModuleUtil.getModule(FreeLook.class).freelooking && cameraEntity instanceof LocalPlayer) {
            ICameraOverriddenEntity cameraOverriddenEntity = (ICameraOverriddenEntity) cameraEntity;

            if (firstTime && Minecraft.getInstance().player != null) {
                cameraOverriddenEntity.pigeon$setCameraPitch(Minecraft.getInstance().player.getXRot());
                cameraOverriddenEntity.pigeon$setCameraYaw(Minecraft.getInstance().player.getYRot());
                firstTime = false;
            }
            this.setRotation(cameraOverriddenEntity.pigeon$getCameraYaw(), cameraOverriddenEntity.pigeon$getCameraPitch());

        }
        if (!ModuleUtil.getModule(FreeLook.class).freelooking && cameraEntity instanceof LocalPlayer) {
            firstTime = true;
        }
    }
}
