package owo.pigeon.modules.impl.combat;

import net.engio.mbassy.listener.Handler;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.Enchantments;
import owo.pigeon.event.events.RenderEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.ExpandSetting;
import owo.pigeon.settings.IntSetting;
import owo.pigeon.utils.ItemUtil;
import owo.pigeon.utils.KeybindUtil;
import owo.pigeon.utils.RandomUtil;
import owo.pigeon.utils.chat.ChatUtil;
import owo.pigeon.utils.player.PlayerUtil;

import static owo.pigeon.Pigeon.mc;

public class AutoClicker extends Module {
    public AutoClicker() {
        super("AutoClicker", Category.COMBAT);
    }

    public EnableSetting vanillaDelay = setting("vanilla-delay", false, v -> true);
    public IntSetting progress = setting("progress", 80, 0, 100, "%", v -> vanillaDelay.getValue());
    public IntSetting minCPS = setting("min-cps", 12, 1, 20, v -> !vanillaDelay.getValue());
    public IntSetting maxCPS = setting("max-cps", 18, 1, 20, v -> !vanillaDelay.getValue());
    public ExpandSetting weapon = setting("weapon", v -> true);
    public EnableSetting any = setting("any", true, v -> weapon.getValue());
    public EnableSetting sword = setting("sword", false, v -> weapon.getValue());
    public EnableSetting axe = setting("axe", false, v -> weapon.getValue());
    public EnableSetting trident = setting("trident", false, v -> weapon.getValue());
    public EnableSetting spear = setting("spear", false, v -> weapon.getValue());
    public EnableSetting mace = setting("mace", false, v -> weapon.getValue());
    public EnableSetting pickaxe = setting("pickaxe", false, v -> weapon.getValue());
    public EnableSetting shovel = setting("shovel", false, v -> weapon.getValue());
    public EnableSetting hoe = setting("hoe", false, v -> weapon.getValue());
    public EnableSetting knockback = setting("knockback", false, v -> weapon.getValue());
    public EnableSetting fireAspect = setting("fire-aspect", false, v -> weapon.getValue());
    public EnableSetting breakBlocks = setting("break-blocks", true, v -> true);

    private boolean firstClick = true;
    private long nextClickTime = 0;

    @Handler
    public void onRender3D(RenderEvent.Render3DEvent event) {
        long currentTime = System.currentTimeMillis();

        if (!KeybindUtil.isPressed(mc.options.keyAttack)) {
            firstClick = true;
            return;
        }

        if (!breakBlocksCheck()) {
            KeybindUtil.setPressed(mc.options.keyAttack, true);
            return;
        }

        if (!canClick() || !weaponCheck()) return;

        if (vanillaDelay.getValue()) {
            if (mc.player.getAttackStrengthScale(0.0F) * 100 >= progress.getValue()) {
                KeybindUtil.setPressed(mc.options.keyAttack, false);
                PlayerUtil.leftClick(PlayerUtil.LeftClickMode.MOUSE);
            }
        } else {
            if (firstClick) {
                firstClick = false;
                nextClickTime = currentTime + (1000 / RandomUtil.intRandom(minCPS.getValue(), maxCPS.getValue()));
            } else if (currentTime >= nextClickTime) {
                KeybindUtil.setPressed(mc.options.keyAttack, false);
                PlayerUtil.leftClick(PlayerUtil.LeftClickMode.MOUSE);
                int randomCPS = RandomUtil.intRandom(minCPS.getValue(), maxCPS.getValue());
                nextClickTime = currentTime + (1000 / randomCPS);
                ChatUtil.sendDebugMessage(this.name, "Click Random CPS: " + randomCPS);
            }
        }
    }

    private boolean canClick() {
        return mc.screen == null && !mc.player.isBlocking() && !mc.player.isUsingItem();
    }

    private boolean breakBlocksCheck() {
        // ChatUtil.sendDebugMessage(this.name,"isBreakingBlock: " + mc.gameMode.isBreakingBlock());
        return !(breakBlocks.getValue() && PlayerUtil.isBreakingBlock());
    }

    private boolean weaponCheck() {
        if (any.getValue()) return true;
        ItemStack stack = mc.player.getMainHandItem();
        if (stack.isEmpty()) return false;

        if (sword.getValue() && stack.is(ItemTags.SWORDS)) return true;
        if (axe.getValue() && stack.is(ItemTags.AXES)) return true;
        if (trident.getValue() && stack.getItem() instanceof TridentItem) return true;
        if (spear.getValue() && stack.is(ItemTags.SPEARS)) return true;
        if (mace.getValue() && stack.getItem() instanceof MaceItem) return true;
        if (pickaxe.getValue() && stack.is(ItemTags.PICKAXES)) return true;
        if (shovel.getValue() && stack.is(ItemTags.SHOVELS)) return true;
        if (hoe.getValue() && stack.is(ItemTags.HOES)) return true;

        if (knockback.getValue() && ItemUtil.hasEnchantment(stack, Enchantments.KNOCKBACK)) return true;
        if (fireAspect.getValue() && ItemUtil.hasEnchantment(stack, Enchantments.FIRE_ASPECT)) return true;

        return false;
    }

    @Override
    public String getSuffix() {
        return vanillaDelay.getValue() ? progress.getValue() + "%" : minCPS.getValue() + "-" + maxCPS.getValue();
    }
}
