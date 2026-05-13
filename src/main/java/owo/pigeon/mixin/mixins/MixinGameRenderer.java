package owo.pigeon.mixin.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import owo.pigeon.modules.impl.player.GhostHand;
import owo.pigeon.modules.impl.render.ModifyCamera;
import owo.pigeon.utils.ModuleUtil;

import static owo.pigeon.Pigeon.mc;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {
    @ModifyExpressionValue(method = "renderWorld", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F", ordinal = 0))
    private float applyCameraTransformationsMathHelperLerpProxy(float original) {
        return ModuleUtil.isEnable(ModifyCamera.class) && ModuleUtil.getModule(ModifyCamera.class).noNausea.getValue() ? 0 : original;
    }

    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
    private void onTiltViewWhenHurt(MatrixStack matrices, float tickProgress, CallbackInfo ci) {
        if (ModuleUtil.isEnable(ModifyCamera.class) && ModuleUtil.getModule(ModifyCamera.class).noHurtCam.getValue()) {
            ci.cancel();
        }
    }

    @Inject(method = "updateCrosshairTarget", at = @At("RETURN"))
    private void afterUpdateCrosshairTarget(float tickProgress, CallbackInfo ci) {
        if (!ModuleUtil.isEnable(GhostHand.class)) return;

        Entity camera = mc.getCameraEntity();
        if (camera == null) return;

        double entityRange = mc.player.getEntityInteractionRange();
        Vec3d cameraPos = camera.getCameraPosVec(tickProgress);
        Vec3d rotationVec = camera.getRotationVec(tickProgress);
        Vec3d endPos = cameraPos.add(rotationVec.x * entityRange,
                rotationVec.y * entityRange,
                rotationVec.z * entityRange);
        Box box = camera.getBoundingBox()
                .stretch(rotationVec.multiply(entityRange))
                .expand(1.0);

        double squaredRange = MathHelper.square(entityRange);
        EntityHitResult entityHit = ProjectileUtil.raycast(camera, cameraPos, endPos, box,
                entity -> EntityPredicates.CAN_HIT.test(entity)
                        && !ModuleUtil.getModule(GhostHand.class).shouldIgnore(entity),
                squaredRange);

        if (entityHit != null) {
            mc.crosshairTarget = entityHit;
            mc.targetedEntity = entityHit.getEntity();
        }
    }
}
