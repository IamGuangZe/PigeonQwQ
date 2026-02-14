package owo.pigeon.modules.impl.Skyblock.Slayer;

import net.engio.mbassy.listener.Handler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.world.GameMode;
import owo.pigeon.event.events.TickEvent;
import owo.pigeon.mixin.accessors.IAccessorInGameHud;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.FloatSetting;
import owo.pigeon.settings.IntSetting;
import owo.pigeon.utils.Chat.ChatUtil;
import owo.pigeon.utils.ColorUtil;
import owo.pigeon.utils.Hypixel.SkyblockUtil;
import owo.pigeon.utils.ItemUtil;
import owo.pigeon.utils.KeybindUtil;
import owo.pigeon.utils.Player.PlayerUtil;
import owo.pigeon.utils.WorldUtil;

import static owo.pigeon.Pigeon.mc;

public class VampireSlayer extends Module {
    public VampireSlayer() {
        super("VampireSlayer", Category.SKYBLOCK);
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
    public void onTick(TickEvent.ClientTickEvent event) {
        if (WorldUtil.nullCheck()) return;
        if (!SkyblockUtil.isInIsland(SkyblockUtil.Island.Rift)) return;

        String subtitle = ((IAccessorInGameHud) mc.inGameHud).getSubtitle() == null ? "" : ColorUtil.removeColor(((IAccessorInGameHud) mc.inGameHud).getSubtitle().getString());
        boolean foundTitle = subtitle.startsWith("Impel: ");

        Entity slayer = SkyblockUtil.getSlayer();

        if (!subtitle.isEmpty()) ChatUtil.sendDebugMessage(this.name, "subtitle: " + subtitle);

        if (event instanceof TickEvent.ClientTickEvent.Pre) {
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
                        rawPitch = mc.player.getPitch();
                    }
                }

                if (impelTicks > 0) {
                    switch (impelAction) {
                        case JUMP -> KeybindUtil.setPressed(mc.options.jumpKey, true);
                        case SNEAK -> KeybindUtil.setPressed(mc.options.sneakKey, true);
                        case UP -> {
                            mc.player.setPitch(-90f);
                            if (impelTicks % 2 != 0)
                                PlayerUtil.LeftClick(PlayerUtil.LeftClickMode.MOUSE);
                        }
                        case DOWN -> {
                            mc.player.setPitch(90f);
                            if (impelTicks % 2 != 0)
                                PlayerUtil.LeftClick(PlayerUtil.LeftClickMode.MOUSE);
                        }
                    }
                }
            }

                GameMode targetMode = (changeGamemode.getValue() && slayer != null && mc.player.getPitch() <= 45) ? GameMode.ADVENTURE : GameMode.SURVIVAL;

                if (mc.interactionManager != null && mc.interactionManager.getCurrentGameMode() != targetMode) {

                    boolean wasFlying = mc.player.getAbilities().flying;
                    boolean canFly = mc.player.getAbilities().allowFlying;

                    mc.interactionManager.setGameMode(targetMode);

                    mc.player.getAbilities().allowFlying = canFly;
                    mc.player.getAbilities().flying = wasFlying;

                    // ChatUtil.sendCustomPrefixMessage(this.name, "Changed gamemode to &6" + targetMode.getId().toUpperCase());
                    ChatUtil.sendDebugMessage(this.name, "Client Gamemode -> " + targetMode.getId());
                }

            if (autoHeal.getValue()) {
                int melon = ItemUtil.getSlotFromItemName(MELON);
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
                int ice = ItemUtil.getSlotFromItemName(ICE);
                if (ice == -1 || slayer == null) return;

                boolean foundClaws = false;
                for (ArmorStandEntity stand : mc.world.getEntitiesByClass(ArmorStandEntity.class, slayer.getBoundingBox().expand(0.25, 2.5, 0.25), entity -> true)) {
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
                int steak = ItemUtil.getSlotFromItemName(STEAK);
                if (steak == -1 || slayer == null) return;

                for (ArmorStandEntity stand : mc.world.getEntitiesByClass(ArmorStandEntity.class, slayer.getBoundingBox().expand(0.25, 2.5, 0.25), entity -> true)) {
                    if (stand.getName().getString().contains("҉") && stand.getName().getString().contains("Bloodfiend")) {
                        PlayerUtil.switchItemSlot(steak);
                    }
                }
            }
        }

        if (event instanceof TickEvent.ClientTickEvent.Post) {
            if (autoImpel.getValue()) {
                if (impelTicks > 0) {
                    impelTicks--;

                    if (impelTicks <= 0) {
                        switch (impelAction) {
                            case JUMP -> KeybindUtil.resetPressed(mc.options.jumpKey);
                            case SNEAK -> KeybindUtil.resetPressed(mc.options.sneakKey);
                            case UP, DOWN -> mc.player.setPitch(rawPitch);
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

            KeybindUtil.resetPressed(mc.options.jumpKey);
            KeybindUtil.resetPressed(mc.options.sneakKey);
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

        KeybindUtil.resetPressed(mc.options.jumpKey);
        KeybindUtil.resetPressed(mc.options.sneakKey);

        if (changeGamemode.getValue() && SkyblockUtil.isInIsland(SkyblockUtil.Island.Rift)) {
            boolean wasFlying = mc.player.getAbilities().flying;
            boolean canFly = mc.player.getAbilities().allowFlying;
            mc.interactionManager.setGameMode(GameMode.SURVIVAL);
            mc.player.getAbilities().allowFlying = canFly;
            mc.player.getAbilities().flying = wasFlying;
        }
    }
}
