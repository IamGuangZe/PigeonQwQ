package owo.pigeon.modules.impl.skyblock.slayer;

import net.engio.mbassy.listener.Handler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.GameType;
import owo.pigeon.event.events.ClientTickEvent;
import owo.pigeon.mixin.accessors.IAccessorInGameHud;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.FloatSetting;
import owo.pigeon.settings.IntSetting;
import owo.pigeon.utils.ColorUtil;
import owo.pigeon.utils.ItemUtil;
import owo.pigeon.utils.KeybindUtil;
import owo.pigeon.utils.WorldUtil;
import owo.pigeon.utils.chat.ChatUtil;
import owo.pigeon.utils.hypixel.skyblock.SkyblockUtil;
import owo.pigeon.utils.player.PlayerUtil;

import static owo.pigeon.Pigeon.mc;

public class VampireSlayer extends Module {
    public VampireSlayer() {
        super("VampireSlayer", Category.SLAYER);
    }

    public EnableSetting changeGamemode = setting("change-gamemode", true, v -> true);
    public EnableSetting autoHeal = setting("auto-heal", true, v -> true);
    public FloatSetting healHealth = setting("heal-health", 6.0F, 1.0F, 20.0F, v -> autoHeal.getValue());
    public EnableSetting autoIce = setting("auto-ice", true, v -> true);
    public IntSetting iceDelay = setting("ice-delay", 20, 0, 30, "tick", v -> autoIce.getValue());
    public EnableSetting autoSteak = setting("auto-steak", true, v -> true);
    public EnableSetting autoImpel = setting("auto-impel", true, v -> true);
    public EnableSetting autoImpelJump = setting("auto-impel-jump", true, v -> autoImpel.getValue());
    public EnableSetting autoImpelSneak = setting("auto-impel-sneak", true, v -> autoImpel.getValue());
    public EnableSetting autoImpelUp = setting("auto-impel-up", false, v -> autoImpel.getValue());
    public EnableSetting autoImpelDown = setting("auto-impel-down", false, v -> autoImpel.getValue());

    private enum ImpelAction {
        NONE, JUMP, SNEAK, UP, DOWN
    }

    private boolean hasHeal, hasIced, hasImpel;
    private int iceTicks, impelTicks;
    private ImpelAction impelAction = ImpelAction.NONE;
    private float rawPitch;

    private static final String MELON = "Healing Melon";
    private static final String ICE = "Holy Ice";
    private static final String STEAK = "Steak Stake";

    @Override
    public void onEnable() {
        impelAction = ImpelAction.NONE;
        hasHeal = false;
        hasIced = false;
        hasImpel = false;
        iceTicks = 0;
        impelTicks = 0;
    }

    @Handler
    public void onTick(ClientTickEvent event) {
        if (WorldUtil.nullCheck()) return;
        if (!SkyblockUtil.isInIsland(SkyblockUtil.Island.THE_RIFT)) return;

        String subtitle = ((IAccessorInGameHud) mc.gui).pigeon$getSubtitle() == null ? "" : ColorUtil.removeColor(((IAccessorInGameHud) mc.gui).pigeon$getSubtitle().getString());
        boolean foundTitle = subtitle.startsWith("Impel: ");

        Entity slayer = SkyblockUtil.getSlayer();

        if (!subtitle.isEmpty()) ChatUtil.sendDebugMessage(this.name, "subtitle: " + subtitle);

        if (event instanceof ClientTickEvent.Pre) {
            if (autoImpel.getValue()) {
                if (impelTicks <= 0 && foundTitle && !hasImpel) {
                    if (subtitle.contains("JUMP") && autoImpelJump.getValue()) impelAction = ImpelAction.JUMP;
                    else if (subtitle.contains("SNEAK") && autoImpelSneak.getValue()) impelAction = ImpelAction.SNEAK;
                    else if (subtitle.contains("CLICK UP") && autoImpelUp.getValue()) impelAction = ImpelAction.UP;
                    else if (subtitle.contains("CLICK DOWN") && autoImpelDown.getValue())
                        impelAction = ImpelAction.DOWN;

                    if (impelAction != ImpelAction.NONE) {
                        impelTicks = 5;
                        hasImpel = true;
                        rawPitch = mc.player.getXRot();
                    }
                }

                if (impelTicks > 0) {
                    switch (impelAction) {
                        case JUMP -> KeybindUtil.setPressed(mc.options.keyJump, true);
                        case SNEAK -> KeybindUtil.setPressed(mc.options.keyShift, true);
                        case UP -> {
                            mc.player.setXRot(-90f);
                            if (impelTicks % 2 != 0)
                                PlayerUtil.leftClick(PlayerUtil.LeftClickMode.MOUSE);
                        }
                        case DOWN -> {
                            mc.player.setXRot(90f);
                            if (impelTicks % 2 != 0)
                                PlayerUtil.leftClick(PlayerUtil.LeftClickMode.MOUSE);
                        }
                    }
                }
            }

            GameType targetMode = (changeGamemode.getValue() && slayer != null && mc.player.getXRot() <= 45) ? GameType.ADVENTURE : GameType.SURVIVAL;

            if (mc.gameMode != null && mc.gameMode.getPlayerMode() != targetMode) {

                boolean wasFlying = mc.player.getAbilities().flying;
                boolean canFly = mc.player.getAbilities().mayfly;

                mc.gameMode.setLocalMode(targetMode);

                mc.player.getAbilities().mayfly = canFly;
                mc.player.getAbilities().flying = wasFlying;

                // ChatUtil.sendCustomPrefixMessage(this.name, "Changed gamemode to &6" + targetMode.getId().toUpperCase());
                ChatUtil.sendDebugMessage(this.name, "Client Gamemode -> " + targetMode.getName());
            }

            if (autoHeal.getValue()) {
                int melon = ItemUtil.getSlotFromItemName(MELON, true);
                float health = mc.player.getHealth();

                if (melon != -1 && health <= healHealth.getValue() && !hasHeal) {
                    ChatUtil.sendDebugMessage(this.name, "Heal");
                    PlayerUtil.InstantUseItem(melon, PlayerUtil.RightClickMode.MOUSE);
                    hasHeal = true;
                    return;
                }

                if (health > healHealth.getValue()) hasHeal = false;
            }

            if (autoIce.getValue()) {
                int ice = ItemUtil.getSlotFromItemName(ICE, true);
                if (ice == -1 || slayer == null) return;

                boolean foundClaws = false;
                for (ArmorStand stand : mc.level.getEntitiesOfClass(ArmorStand.class, slayer.getBoundingBox().inflate(0.25, 2.5, 0.25), entity -> true)) {
                    ChatUtil.sendDebugMessage(this.name, "stand: " + stand.getName().getString());

                    if (stand.getName().getString().contains("TWINCLAWS")) {
                        foundClaws = true;
                        break;
                    }
                }

                if (foundClaws) {
                    if (!hasIced) {
                        if (iceTicks < iceDelay.getValue()) {
                            iceTicks++;
                        } else {
                            ChatUtil.sendDebugMessage(this.name, "ICE");
                            PlayerUtil.InstantUseItem(ice, PlayerUtil.RightClickMode.MOUSE);
                            hasIced = true;
                            iceTicks = 0;
                            return;
                        }
                    }
                } else {
                    hasIced = false;
                    iceTicks = 0;
                }
            }

            if (autoSteak.getValue()) {
                int steak = ItemUtil.getSlotFromItemName(STEAK, true);
                if (steak == -1 || slayer == null) return;

                for (ArmorStand stand : mc.level.getEntitiesOfClass(ArmorStand.class, slayer.getBoundingBox().inflate(0.25, 2.5, 0.25), entity -> true)) {
                    if (stand.getName().getString().contains("҉") && stand.getName().getString().contains("Bloodfiend")) {
                        PlayerUtil.switchItemSlot(steak);
                    }
                }
            }
        }

        if (event instanceof ClientTickEvent.Post) {
            if (autoImpel.getValue()) {
                if (impelTicks > 0) {
                    impelTicks--;

                    if (impelTicks <= 0) {
                        switch (impelAction) {
                            case JUMP -> KeybindUtil.resetPressed(mc.options.keyJump);
                            case SNEAK -> KeybindUtil.resetPressed(mc.options.keyShift);
                            case UP, DOWN -> mc.player.setXRot(rawPitch);
                        }

                        impelAction = ImpelAction.NONE;
                    }
                }
            }
        }

        if (!foundTitle) {
            if (impelTicks != 0)
                ChatUtil.sendDebugMessage(this.name, "reset impel.");

            hasImpel = false;
            impelTicks = 0;
            impelAction = ImpelAction.NONE;

            KeybindUtil.resetPressed(mc.options.keyJump);
            KeybindUtil.resetPressed(mc.options.keyShift);
        }
    }

    @Override
    public void onDisable() {
        impelAction = ImpelAction.NONE;
        hasHeal = false;
        hasIced = false;
        hasImpel = false;
        iceTicks = 0;
        impelTicks = 0;

        KeybindUtil.resetPressed(mc.options.keyJump);
        KeybindUtil.resetPressed(mc.options.keyShift);

        if (changeGamemode.getValue() && SkyblockUtil.isInIsland(SkyblockUtil.Island.THE_RIFT)) {
            boolean wasFlying = mc.player.getAbilities().flying;
            boolean canFly = mc.player.getAbilities().mayfly;
            mc.gameMode.setLocalMode(GameType.SURVIVAL);
            mc.player.getAbilities().mayfly = canFly;
            mc.player.getAbilities().flying = wasFlying;
        }
    }
}
