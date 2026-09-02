package owo.pigeon.modules.impl.combat;

import net.engio.mbassy.listener.Handler;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.Enchantments;
import owo.pigeon.event.events.ClientTickEvent;
import owo.pigeon.event.events.PacketEvent;
import owo.pigeon.mixin.accessors.IAccessorMultiPlayerGameMode;
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

import java.util.Arrays;

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
    public EnableSetting delayOnBroken = setting("delay-on-broken", true, v -> true);

    private static final int CYCLE_LENGTH = 20;
    private boolean firstClick = true;
    private int planHead = 0;
    private final int[] clickPlan = new int[CYCLE_LENGTH];
    private long lastClickTime = 0;
    private long lastFinishBreak = 0;

    @Handler
    public void onClientTick(ClientTickEvent.Post event) {
        long currentTime = System.currentTimeMillis();

        if (!KeybindUtil.isPressed(mc.options.keyAttack)) {
            firstClick = true;
            planHead = 0;
            return;
        }

        if (!breakBlocksCheck()) {
            KeybindUtil.setPressed(mc.options.keyAttack, true);
            return;
        }

        if (delayOnBroken.getValue() && System.currentTimeMillis() - lastFinishBreak < 300) {
            return;
        }

        if (!canClick() || !weaponCheck()) return;

        if (vanillaDelay.getValue()) {
            doVanillaClick();
        } else {
            doCpsClick(currentTime);
        }
    }

    @Handler
    public void onPacketSend(PacketEvent.SendPacketEvent event) {
        if (event.getPacket() instanceof ServerboundPlayerActionPacket packet
                && packet.getAction() == ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK) {
            lastFinishBreak = System.currentTimeMillis();
        }
    }

    private void doVanillaClick() {
        if (mc.player.getAttackStrengthScale(0.0F) * 100 >= progress.getValue()) {
            performClick();
        }
    }

    private void doCpsClick(long currentTime) {
        if (firstClick) {
            firstClick = false;
            lastClickTime = currentTime;
            performClick();
            regeneratePlan();
            return;
        }

        if (clickPlan[planHead] > 0 || isEnforcedClick(currentTime)) {
            lastClickTime = currentTime;
            performClick();
        }
        planHead++;

        if (planHead >= CYCLE_LENGTH) {
            planHead = 0;
            regeneratePlan();
        }
    }

    private void performClick() {
        PlayerUtil.leftClick(PlayerUtil.LeftClickMode.MOUSE);
        KeybindUtil.setPressed(mc.options.keyAttack, true);
    }

    private void regeneratePlan() {
        int clicks = RandomUtil.intRandom(minCPS.getValue(), maxCPS.getValue());
        Arrays.fill(clickPlan, 0);
        int interval = clicks > 0 ? CYCLE_LENGTH / clicks : 0;
        int remainder = clicks > 0 ? CYCLE_LENGTH % clicks : 0;
        int currentIndex = 0;
        for (int i = 0; i < clicks; i++) {
            clickPlan[currentIndex % CYCLE_LENGTH]++;
            currentIndex += Math.max(interval, 1);
            if (remainder > 0) {
                currentIndex++;
                remainder--;
            }
        }
        ChatUtil.sendDebugMessage(this.name, "Click plan: " + clicks + " clicks/cycle");
    }

    private boolean isEnforcedClick(long currentTime) {
        return currentTime - lastClickTime >= 1000;
    }

    private boolean canClick() {
        return mc.gui.screen() == null && !mc.player.isBlocking() && !mc.player.isUsingItem();
    }

    private boolean breakBlocksCheck() {
        return !(breakBlocks.getValue() && ((IAccessorMultiPlayerGameMode) mc.gameMode).pigeon$isDestroying());
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
