package owo.pigeon.modules.impl.hypixel;

import com.mojang.blaze3d.vertex.PoseStack;
import net.engio.mbassy.listener.Handler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import owo.pigeon.Pigeon;
import owo.pigeon.event.events.ClientTickEvent;
import owo.pigeon.event.events.MessageEvent;
import owo.pigeon.event.events.RenderEvent;
import owo.pigeon.event.events.WorldChangeEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.ExpandSetting;
import owo.pigeon.settings.ModeSetting;
import owo.pigeon.utils.ColorUtil;
import owo.pigeon.utils.RegexUtil;
import owo.pigeon.utils.ScoreBoardUtil;
import owo.pigeon.utils.WorldUtil;
import owo.pigeon.utils.chat.ChatUtil;
import owo.pigeon.utils.hypixel.HypixelUtil;
import owo.pigeon.utils.player.PlayerUtil;
import owo.pigeon.utils.render.FontUtil;
import owo.pigeon.utils.render.RenderUtil;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static owo.pigeon.Pigeon.mc;

public class MurderHelper extends Module {
    public MurderHelper() {
        super("MurderHelper", Category.HYPIXEL);
    }

    public EnableSetting hud = setting("hud", true, v -> true);
    public EnableSetting hideSpamCurse = setting("hide-spamcurse", true, v -> true);
    public ExpandSetting Esp = setting("esp", v -> true);
    public EnableSetting playerEsp = setting("player-esp", true, v -> Esp.getValue());
    public EnableSetting itemEsp = setting("item-esp", true, v -> Esp.getValue());
    public EnableSetting bowEsp = setting("bow-esp", true, v -> Esp.getValue());
    public EnableSetting bowTracer = setting("bow-tracer", true, v -> bowEsp.isVisible() && bowEsp.getValue());
    public ModeSetting<RenderUtil.ESPMode> espMode = setting("esp-mode", RenderUtil.ESPMode.OUTLINE, v -> Esp.getValue());

    private final Set<String> allPlayers = new HashSet<>();
    private final Set<String> alivePlayers = new HashSet<>();

    private final Set<String> murdererNames = new HashSet<>();
    private final Set<String> playersWithBow = new HashSet<>();

    private static final Set<Item> KNIFE_ITEMS = Set.of(
            Items.IRON_SWORD, Items.STONE_SWORD, Items.IRON_SHOVEL, Items.STICK, Items.WOODEN_AXE,
            Items.WOODEN_SWORD, Items.DEAD_BUSH, Items.SUGAR_CANE, Items.STONE_SHOVEL, Items.BLAZE_ROD, Items.DIAMOND_SHOVEL, Items.QUARTZ,
            Items.PUMPKIN_PIE, Items.GOLDEN_PICKAXE, Items.LEATHER, Items.NAME_TAG, Items.CHARCOAL, Items.FLINT, Items.BONE,
            Items.CARROT, Items.GOLDEN_CARROT, Items.COOKIE, Items.DIAMOND_AXE, Items.ROSE_BUSH, Items.PRISMARINE_SHARD, Items.COOKED_BEEF,
            Items.NETHER_BRICK, Items.COOKED_CHICKEN, Items.GOLDEN_HOE, Items.LAPIS_LAZULI, Items.GOLDEN_SWORD, Items.DIAMOND_SWORD,
            Items.DIAMOND_HOE, Items.SHEARS, Items.SALMON, Items.RED_DYE, Items.BREAD, Items.OAK_BOAT, Items.GLISTERING_MELON_SLICE,
            Items.BOOK, Items.JUNGLE_SAPLING, Items.GOLDEN_AXE, Items.DIAMOND_PICKAXE, Items.GOLDEN_SHOVEL,
            Items.MUSIC_DISC_BLOCKS, Items.MUSIC_DISC_11, Items.MUSIC_DISC_13, Items.MUSIC_DISC_CAT, Items.MUSIC_DISC_CHIRP, Items.MUSIC_DISC_FAR, Items.MUSIC_DISC_MALL, Items.MUSIC_DISC_MELLOHI, Items.MUSIC_DISC_STAL, Items.MUSIC_DISC_STRAD, Items.MUSIC_DISC_WARD, Items.MUSIC_DISC_WAIT
    );

    private final String TEAMMATE_MUR = "Your fellow Murderer is: (.*)";
    private final String TEAMMATE_DET = "Your fellow Detective is: (.*)";
    private final String MURDERERDIDEMESSAGE = "One of the Murderers, (.*), was killed.";
    private final String SPAMCURSE = "YOU HAVE BEEN STRUCK WITH THE CURSE OF SPAM";

    @Override
    public void onEnable() {
        reload();
    }

    @Handler
    public void onTickPre(ClientTickEvent.Pre event) {
        if (WorldUtil.nullCheck()) return;
        if (!isInMurderGame()) return;

        alivePlayers.clear();
        for (AbstractClientPlayer player : mc.level.players()) {
            if (!PlayerUtil.hasUUID(player)) continue;

            String playerName = player.getName().getString();

            alivePlayers.add(playerName);

            String deadName = "&m" + playerName + "&r";
            if (murdererNames.contains(deadName)) {
                murdererNames.remove(deadName);
                murdererNames.add(playerName);
            }
            if (playersWithBow.contains(deadName)) {
                playersWithBow.remove(deadName);
                playersWithBow.add(playerName);
            }

            ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);

            if (isKnife(stack) && !murdererNames.contains(playerName)) {
                double distance = mc.player.distanceTo(player);
                ChatUtil.sendMessage(this.name, "&c" + playerName + " &ris the Murderer, distance: &e" + String.format("%.1f", distance));
                murdererNames.add(playerName);
                playersWithBow.remove(playerName);
            }
            if (stack.is(Items.BOW) && !playersWithBow.contains(playerName) && !murdererNames.contains(playerName)) {
                playersWithBow.add(playerName);
            }
        }

        Set<String> deadThisTick = new HashSet<>(allPlayers);
        deadThisTick.removeAll(alivePlayers);

        if (!deadThisTick.isEmpty()) {
            Set<String> tabPlayers = ScoreBoardUtil.getTabLines().stream()
                    .map(ColorUtil::removeColor)
                    .collect(Collectors.toSet());
            for (String deadName : deadThisTick) {
                if (tabPlayers.contains(deadName)) continue;

                String color = murdererNames.contains(deadName) ? "&c" : (playersWithBow.contains(deadName) ? "&a" : "&7");
                ChatUtil.sendMessage(this.name, color + deadName + " &rhas died.");
                playerDied(deadName);
            }
        }

        allPlayers.clear();
        allPlayers.addAll(alivePlayers);
    }

    @Handler
    public void onRender2D(RenderEvent.Render2DEvent event) {
        GuiGraphics context = event.getContext();

        if (!hud.getValue()) return;
        if (!isInMurderGame()) return;
        FontUtil.drawString(context, "Murder Mystery", 5, 5);
        FontUtil.drawString(context, "Murders : &c" + String.join("&r, &c", murdererNames), 5, 5 + FontUtil.getLineHeight());
        FontUtil.drawString(context, "Who has bow : " + String.join(", ", playersWithBow), 5, 5 + FontUtil.getLineHeight() * 2);
    }

    @Handler
    public void onRender3D(RenderEvent.Render3DEvent event) {
        PoseStack stack = event.getMatrix();

        if (!isInMurderGame()) return;
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof AbstractClientPlayer && !(entity instanceof LocalPlayer) && playerEsp.getValue()) {
                if (!PlayerUtil.hasUUID(entity)) continue;
                String playername = entity.getName().getString();
                if (murdererNames.contains(playername)) {
                    RenderUtil.drawESP(stack, entity, new Color(0xFFFF0000, true), espMode.getValue(), false);
                } else if (playersWithBow.contains(playername)) {
                    RenderUtil.drawESP(stack, entity, new Color(0xFF00FF00, true), espMode.getValue(), false);
                } else {
                    RenderUtil.drawESP(stack, entity, new Color(0xFFFFFFFF, true), espMode.getValue(), false);
                }
            } else if (entity instanceof ItemEntity itemEntity && itemEsp.getValue()) {
                if (!itemEntity.getItem().is(Items.GOLD_INGOT)) continue;
                RenderUtil.drawESP(stack, entity, new Color(0xFFFFFF00, true), RenderUtil.ESPMode.OUTLINE, false);
            } else if (entity instanceof ArmorStand armorStand && bowEsp.getValue()) {
                if (!armorStand.isInvisible()) continue;
                if (!armorStand.getItemBySlot(EquipmentSlot.MAINHAND).is(Items.BOW)) continue;
                AABB box = entity.getBoundingBox().inflate(0.25).move(0.0, 1.0, 0.0);
                RenderUtil.drawESP(stack, entity, box, new Color(0xFF00FF00, true), RenderUtil.ESPMode.OUTLINE, bowTracer.getValue());

            }
        }
    }

    @Handler
    public void onReceiveMessage(MessageEvent.ReceiveMessageEvent event) {
        if (event.isOverlay()) return;
        String message = ColorUtil.removeColor(event.getMessage().getString());

        if (RegexUtil.regexGetPart(MURDERERDIDEMESSAGE, message, 1) != null) {
            String playername = RegexUtil.regexGetPart(MURDERERDIDEMESSAGE, message, 1);
            if (!murdererNames.contains(playername)) {
                murdererNames.add("&m" + playername + "&r");
            }
        }
        if (RegexUtil.regexGetPart(TEAMMATE_MUR, message, 1) != null) {
            murdererNames.add(RegexUtil.regexGetPart(TEAMMATE_MUR, message, 1));
        }
        if (RegexUtil.regexGetPart(TEAMMATE_DET, message, 1) != null) {
            playersWithBow.add(RegexUtil.regexGetPart(TEAMMATE_DET, message, 1));
        }
        if (hideSpamCurse.getValue() && message.contains(SPAMCURSE)) {
            event.setCancelled(true);
        }
    }

    @Handler
    public void onWorldChange(WorldChangeEvent event) {
        reload();
    }

    private boolean isInMurderGame() {
        if (HypixelUtil.isInGame(HypixelUtil.Game.MURDERMYSTERY)) return true;
        return Pigeon.isDebug() && HypixelUtil.isInGame(HypixelUtil.Game.REPLAY);
    }

    private boolean isKnife(ItemStack stack) {
        if (stack.isEmpty()) return false;

        // if (!stack.is(Items.FILLED_MAP) && !stack.is(Items.GOLD_INGOT) && !stack.is(Items.ARMOR_STAND)) {
        //     ChatUtil.sendDebugMessage(this.name, "name: " + stack.getHoverName());
        // }

        TextColor greenColor = TextColor.fromLegacyFormat(ChatFormatting.GREEN);
        boolean hasGreen = greenColor.equals(stack.getHoverName().getStyle().getColor());
        if (!hasGreen) return false;

        return KNIFE_ITEMS.contains(stack.getItem());
    }

    private void playerDied(String playerName) {
        if (playersWithBow.contains(playerName)) {
            playersWithBow.remove(playerName);
            playersWithBow.add("&m" + playerName + "&r");
        }

        if (murdererNames.contains(playerName)) {
            murdererNames.remove(playerName);
            murdererNames.add("&m" + playerName + "&r");
        }
    }

    private void getAllPlayer() {
        allPlayers.clear();
        for (AbstractClientPlayer player : mc.level.players()) {
            String playername = player.getName().getString();
            allPlayers.add(playername);
        }
    }

    private void reload() {
        if (WorldUtil.nullCheck()) return;
        murdererNames.clear();
        playersWithBow.clear();
        getAllPlayer();
    }
}
