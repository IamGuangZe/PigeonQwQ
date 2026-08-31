package owo.pigeon.mixin.mixins;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import owo.pigeon.Pigeon;
import owo.pigeon.event.events.ClientTickEvent;
import owo.pigeon.event.events.StartAttackEvent;
import owo.pigeon.event.events.StartUseItemEvent;
import owo.pigeon.modules.impl.combat.AutoClicker;
import owo.pigeon.modules.impl.combat.NoHitDelay;
import owo.pigeon.modules.impl.player.FastPlace;
import owo.pigeon.modules.impl.player.GhostHand;
import owo.pigeon.modules.impl.render.FreeLook;
import owo.pigeon.utils.ModuleUtil;

import static owo.pigeon.Pigeon.mc;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Shadow
    public int missTime;

    @Shadow
    private int rightClickDelay;

    @Unique
    private ClientLevel lastProcessedLevel;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onClientTickPre(CallbackInfo ci) {
        Pigeon.EVENT_BUS.post(new ClientTickEvent.Pre()).now();
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void onClientTickPost(CallbackInfo ci) {
        Pigeon.EVENT_BUS.post(new ClientTickEvent.Post()).now();
    }

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void onStartAttack(CallbackInfoReturnable<Boolean> cir) {
        StartAttackEvent.Pre event = new StartAttackEvent.Pre();
        Pigeon.EVENT_BUS.post(event).now();
        if (event.isCancelled()) {
            cir.cancel();
        }


        if (ModuleUtil.isEnable(NoHitDelay.class) || ModuleUtil.isEnable(AutoClicker.class))
            this.missTime = 0;
    }

    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void onStartUseItemPre(CallbackInfo ci) {
        StartUseItemEvent.Pre event = new StartUseItemEvent.Pre();
        Pigeon.EVENT_BUS.post(event).now();
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "startUseItem", at = @At("RETURN"))
    private void onStartUseItemPost(CallbackInfo ci) {
        Pigeon.EVENT_BUS.post(new StartUseItemEvent.Post()).now();

        if (ModuleUtil.isEnable(FastPlace.class) && ModuleUtil.getModule(FastPlace.class).canFastPlace()) {
            rightClickDelay = ModuleUtil.getModule(FastPlace.class).delay.getValue();
        }
    }

    @Redirect(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/CameraType;cycle()Lnet/minecraft/client/CameraType;"))
    private CameraType onCycleCameraType(CameraType instance) {
        CameraType next = instance.cycle();

        if (ModuleUtil.getModule(FreeLook.class).freelooking && next == CameraType.FIRST_PERSON) {
            return next.cycle();
        }

        return next;
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
