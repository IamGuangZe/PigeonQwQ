package owo.pigeon.modules.impl.skyblock.nether;

import net.engio.mbassy.listener.Handler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import owo.pigeon.event.events.DoAttackEvent;
import owo.pigeon.event.events.RenderEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.utils.hypixel.skyblock.DojoUtil;
import owo.pigeon.utils.player.PlayerUtil;

import static owo.pigeon.Pigeon.mc;

public class DojoHelper extends Module {
    public DojoHelper() {
        super("DojoHelper", Category.NETHER);
    }

    public EnableSetting force = setting("force", true, v -> true);
    public EnableSetting discipline = setting("discipline", true, v -> true);

    @Handler
    public void onDoAttack(DoAttackEvent event) {
        if (discipline.getValue() && DojoUtil.isDojoChallenge(DojoUtil.Dojo.Discipline)) {
            if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.ENTITY) {
                Entity target = ((EntityHitResult) mc.crosshairTarget).getEntity();

                if (target instanceof ZombieEntity zombie) {
                    ItemStack helmet = zombie.getEquippedStack(EquipmentSlot.HEAD);
                    if (helmet.isOf(Items.LEATHER_HELMET)) {
                        PlayerUtil.switchItemSlot(0);
                    } else if (helmet.isOf(Items.IRON_HELMET)) {
                        PlayerUtil.switchItemSlot(1);
                    } else if (helmet.isOf(Items.GOLDEN_HELMET)) {
                        PlayerUtil.switchItemSlot(2);
                    } else if (helmet.isOf(Items.DIAMOND_HELMET)) {
                        PlayerUtil.switchItemSlot(3);
                    }
                }
            }
        }
    }

    @Handler
    public void onRender3D(RenderEvent.Render3DEvent event) {
        if (force.getValue() && DojoUtil.isDojoChallenge(DojoUtil.Dojo.Force)) {
            for (Entity entity : mc.world.getEntities()) {
                if (entity instanceof ZombieEntity zombie &&
                        zombie.getEquippedStack(EquipmentSlot.HEAD).isOf(Items.LEATHER_HELMET))
                    entity.discard();
            }
        }
    }
}
