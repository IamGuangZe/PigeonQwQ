package owo.pigeon.mixin.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import owo.pigeon.modules.impl.Player.GhostHand;
import owo.pigeon.modules.impl.Render.ModifyCamera;
import owo.pigeon.utils.ModuleUtil;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {
    @Shadow
    private static HitResult ensureTargetInRange(HitResult hitResult, Vec3d cameraPos, double interactionRange) {
        return null;
    }

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

    @Inject(method = "findCrosshairTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/ProjectileUtil;raycast(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;D)Lnet/minecraft/util/hit/EntityHitResult;"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private void onFindCrosshairTarget(Entity camera, double blockInteractionRange, double entityInteractionRange, float tickProgress, CallbackInfoReturnable<HitResult> cir, double d, double e, Vec3d vec3d, HitResult hitResult, double f, Vec3d vec3d2, Vec3d vec3d3, float g, Box box) {
        if (ModuleUtil.isEnable(GhostHand.class)) {
            EntityHitResult entityHitResult = ProjectileUtil.raycast(
                    camera, vec3d, vec3d3, box,
                    entity -> EntityPredicates.CAN_HIT.test(entity) && !ModuleUtil.getModule(GhostHand.class).shouldIgnore(entity),
                    e
            );

            HitResult finalResult = (entityHitResult != null && entityHitResult.getPos().squaredDistanceTo(vec3d) < f)
                    ? ensureTargetInRange(entityHitResult, vec3d, entityInteractionRange)
                    : ensureTargetInRange(hitResult, vec3d, blockInteractionRange);

            cir.setReturnValue(finalResult);
        }
    }
}
