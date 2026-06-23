package owo.pigeon.mixin.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
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
    @ModifyExpressionValue(method = "renderLevel", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F", ordinal = 0))
    private float applyCameraTransformationsMathHelperLerpProxy(float original) {
        return ModuleUtil.isEnable(ModifyCamera.class) && ModuleUtil.getModule(ModifyCamera.class).noNausea.getValue() ? 0 : original;
    }

    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
    private void onbobHurt(PoseStack matrices, float tickProgress, CallbackInfo ci) {
        if (ModuleUtil.isEnable(ModifyCamera.class) && ModuleUtil.getModule(ModifyCamera.class).noHurtCam.getValue()) {
            ci.cancel();
        }
    }

    @Inject(method = "pick", at = @At("RETURN"))
    private void onPickTail(float tickProgress, CallbackInfo ci) {
        if (!ModuleUtil.isEnable(GhostHand.class)) return;

        Entity camera = mc.getCameraEntity();
        if (camera == null) return;

        double entityRange = mc.player.entityInteractionRange();
        Vec3 cameraPos = camera.getEyePosition(tickProgress);
        Vec3 rotationVec = camera.getViewVector(tickProgress);
        Vec3 endPos = cameraPos.add(rotationVec.x * entityRange,
                rotationVec.y * entityRange,
                rotationVec.z * entityRange);
        AABB box = camera.getBoundingBox()
                .expandTowards(rotationVec.scale(entityRange))
                .inflate(1.0);

        double squaredRange = Mth.square(entityRange);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(camera, cameraPos, endPos, box,
                entity -> EntitySelector.CAN_BE_PICKED.test(entity)
                        && !ModuleUtil.getModule(GhostHand.class).shouldIgnore(entity),
                squaredRange);

        if (entityHit != null) {
            mc.hitResult = entityHit;
            mc.crosshairPickEntity = entityHit.getEntity();
        }
    }
}
