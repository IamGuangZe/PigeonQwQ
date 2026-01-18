package owo.pigeon.modules.impl.Player;

import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.IntSetting;
import owo.pigeon.settings.ModeSetting;
import owo.pigeon.utils.Chat.ChatUtil;
import owo.pigeon.utils.PlayerUtil;
import owo.pigeon.utils.WorldUtil;

import static owo.pigeon.Pigeonqwq.mc;

public class AutoFish extends Module {
    public AutoFish() {
        super("AutoFish", Category.PLAYER);
    }

    public ModeSetting<PlayerUtil.RightClickMode> castMode = setting("cast-mode", PlayerUtil.RightClickMode.MOUSE, v -> true);
    public EnableSetting stopInGui = setting("stop-in-gui", true, v -> true);
    public EnableSetting rethrow = setting("rethrow", true, v -> true);
    public IntSetting rethrowTick = setting("rethrow-tick", 10, 1, 20, v -> rethrow.getValue());
    public EnableSetting slugfishMode = setting("slugfish-mode", false, v -> true);

    private int rethrowTicks;
    private boolean fishIncoming;

    @Override
    public void onEnable() {
        rethrowTicks = rethrowTick.getValue() + 1;
    }

    @Override
    public void onTickPost() {
        if (WorldUtil.nullCheck()) return;
        if (stopInGui.getValue() && mc.currentScreen != null) return;

        // fishHookAge
        int fishHookAge;
        if (mc.player.fishHook != null) fishHookAge = mc.player.fishHook.age;
        else fishHookAge = 0;

        // Rethrow
        if (mc.player.fishHook == null && rethrowTicks < rethrowTick.getValue() + 1) rethrowTicks++;
        if (isHeldRod() && rethrowTicks == rethrowTick.getValue()) PlayerUtil.RightClick(castMode.getValue());

        // Catch Fish
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof ArmorStandEntity armorStand) {
                String name = armorStand.getDisplayName().getString();

                if (name.equals("?") || name.matches("\\d\\.\\d")) {
                    fishIncoming = true;
                }

                if (name.equals("!!!") && fishIncoming) {
                    fishIncoming = false;

                    if (!slugfishMode.getValue() || fishHookAge > 22 * 20) {
                        PlayerUtil.RightClick(castMode.getValue());
                        rethrowTicks = 0;
                    }
                }
            }
        }

        if (mc.player.fishHook == null) ChatUtil.sendDebugMessage(this.name, "Player's bobber not found.");
        else if (!fishIncoming) ChatUtil.sendDebugMessage(this.name, "Fish not incoming.");
        else ChatUtil.sendDebugMessage(this.name, "Waiting to catch.");
    }

    private boolean isHeldRod() {
        ItemStack handStack = mc.player.getMainHandStack();
        if (handStack != null) return mc.player.getMainHandStack().getItem() instanceof FishingRodItem;
        else return false;
    }
}
