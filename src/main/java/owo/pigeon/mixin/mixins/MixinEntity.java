package owo.pigeon.mixin.mixins;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import owo.pigeon.interfaces.ICameraOverriddenEntity;
import owo.pigeon.modules.impl.Combat.HitBox;
import owo.pigeon.modules.impl.Render.FreeLook;
import owo.pigeon.utils.ModuleUtil;

@Mixin(Entity.class)
public class MixinEntity implements ICameraOverriddenEntity {

    @Unique
    private float cameraPitch;

    @Unique
    private float cameraYaw;

    @Inject(method = "getTargetingMargin", at = @At("RETURN"), cancellable = true)
    private void onGetTargetingMargin(CallbackInfoReturnable<Float> cir) {
        if (ModuleUtil.isEnable(HitBox.class))
            cir.setReturnValue(ModuleUtil.getModule(HitBox.class).expand.getValue());
    }

    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
    private void onChangeLookDirection(double xDelta, double yDelta, CallbackInfo ci) {
        if (ModuleUtil.getModule(FreeLook.class).freelooking && (Object) this instanceof ClientPlayerEntity) {
            double pitchDelta = yDelta * 0.15;
            double yawDelta = xDelta * 0.15;
            this.cameraPitch = MathHelper.clamp(this.cameraPitch + (float) pitchDelta, -90.0f, 90.0f);
            this.cameraYaw += (float) yawDelta;
            ci.cancel();
        }
    }

    @Override
    @Unique
    public float pigeon$getCameraPitch() {
        return this.cameraPitch;
    }

    @Override
    @Unique
    public float pigeon$getCameraYaw() {
        return this.cameraYaw;
    }

    @Override
    @Unique
    public void pigeon$setCameraPitch(float pitch) {
        this.cameraPitch = pitch;
    }

    @Override
    @Unique
    public void pigeon$setCameraYaw(float yaw) {
        this.cameraYaw = yaw;
    }
}
