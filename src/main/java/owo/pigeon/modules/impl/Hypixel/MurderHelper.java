package owo.pigeon.modules.impl.Hypixel;

import net.engio.mbassy.listener.Handler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import owo.pigeon.event.events.MessageEvent;
import owo.pigeon.event.events.RenderEvent;
import owo.pigeon.event.events.TickEvent;
import owo.pigeon.event.events.WorldChangeEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.ModeSetting;
import owo.pigeon.utils.Chat.ChatUtil;
import owo.pigeon.utils.ColorUtil;
import owo.pigeon.utils.Hypixel.HypixelUtil;
import owo.pigeon.utils.Player.PlayerUtil;
import owo.pigeon.utils.RegexUtil;
import owo.pigeon.utils.Render.RenderUtil;
import owo.pigeon.utils.Render.TextRendererUtil;
import owo.pigeon.utils.WorldUtil;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

import static owo.pigeon.Pigeon.mc;

public class MurderHelper extends Module {
    public MurderHelper() {
        super("MurderHelper", Category.HYPIXEL);
    }

    public EnableSetting hud = setting("hud", true, v -> true);
    public EnableSetting playerEsp = setting("player-esp", true, v -> true);
    public ModeSetting<RenderUtil.ESPMode> espMode = setting("esp-mode", RenderUtil.ESPMode.OUTLINE, v -> playerEsp.getValue());
    public EnableSetting itemEsp = setting("item-esp", true, v -> true);
    public EnableSetting hideSpamCurse = setting("hide-spamcurse", true, v -> true);

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
    public void onTickPre(TickEvent.ClientTickEvent.Pre event) {
        if (WorldUtil.nullCheck()) return;
        if (!HypixelUtil.isInGame(HypixelUtil.Game.MURDERMYSTERY)) return;

        alivePlayers.clear();
        for (AbstractClientPlayerEntity player : mc.world.getPlayers()) {
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

            ItemStack stack = player.getStackInHand(Hand.MAIN_HAND);

            if (isKnife(stack) && !murdererNames.contains(playerName)) {
                ChatUtil.sendMessage(this.name, "&c" + playerName + " &ris Murderer!");
                murdererNames.add(playerName);
                playersWithBow.remove(playerName);
            }
            if (stack.isOf(Items.BOW) && !playersWithBow.contains(playerName) && !murdererNames.contains(playerName)) {
                playersWithBow.add(playerName);
            }
        }

        Set<String> deadThisTick = new HashSet<>(allPlayers);
        deadThisTick.removeAll(alivePlayers);

        for (String deadName : deadThisTick) {
            String color = murdererNames.contains(deadName) ? "&c" : (playersWithBow.contains(deadName) ? "&a" : "&7");
            ChatUtil.sendMessage(this.name, color + deadName + " &rhas died.");
            playerDied(deadName);
        }

        allPlayers.clear();
        allPlayers.addAll(alivePlayers);
    }

    @Handler
    public void onRender2D(RenderEvent.Render2DEvent event) {
        DrawContext context = event.getContext();

        if (!hud.getValue()) return;
        if (!HypixelUtil.isInGame(HypixelUtil.Game.MURDERMYSTERY)) return;
        TextRendererUtil.drawString(context,"Murder Mystery", 5, 5);
        TextRendererUtil.drawString(context,"Murders : &c" + String.join("&r, &c", murdererNames), 5, 5 + TextRendererUtil.getLineHeight());
        TextRendererUtil.drawString(context,"Who has bow : " + String.join(", ", playersWithBow), 5, 5 + TextRendererUtil.getLineHeight() * 2);
    }

    @Handler
    public void onRender3D(RenderEvent.Render3DEvent event) {
        MatrixStack stack = event.getMatrix();

        if (!HypixelUtil.isInGame(HypixelUtil.Game.MURDERMYSTERY)) return;
        for (Entity entity : mc.world.getEntities()) {
            if (playerEsp.getValue()) {
                if (entity instanceof AbstractClientPlayerEntity && !(entity instanceof ClientPlayerEntity)) {
                    if (!PlayerUtil.hasUUID(entity)) continue;
                    String playername = entity.getName().getString();
                    if (murdererNames.contains(playername)) {
                        RenderUtil.drawESP(stack,entity,new Color(0xFFFF0000,true),espMode.getValue(),false);
                    } else if (playersWithBow.contains(playername)) {
                        RenderUtil.drawESP(stack,entity,new Color(0xFF00FF00,true),espMode.getValue(),false);
                    } else {
                        RenderUtil.drawESP(stack,entity,new Color(0xFFFFFFFF,true),espMode.getValue(),false);
                    }
                }
            }

            if (itemEsp.getValue()) {
                if (entity instanceof ItemEntity itemEntity) {
                    if (!itemEntity.getStack().isOf(Items.GOLD_INGOT)) continue;
                    RenderUtil.drawESP(stack, entity, new Color(0xFFFFFF00, true), RenderUtil.ESPMode.OUTLINE, false);
                }
            }
        }
    }

    @Handler
    public void onReceiveMessage(MessageEvent.ReceiveMessageEvent event) {
        if (event.isOverlay()) return;
        String message = ColorUtil.removeColor(event.getMessage().getString());

        if (RegexUtil.regexGetPart(MURDERERDIDEMESSAGE, message, 1) != null) {
            String playername = RegexUtil.regexGetPart(MURDERERDIDEMESSAGE, message, 1);
            if (!murdererNames.contains(playername) && !murdererNames.contains("&m" + playername + "&r")) {
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

    private boolean isKnife(ItemStack stack) {
        if (stack.isEmpty()) return false;

        if (!stack.isOf(Items.FILLED_MAP) && !stack.isOf(Items.GOLD_INGOT) && !stack.isOf(Items.ARMOR_STAND)) {
            ChatUtil.sendDebugMessage(this.name, "name: " + stack.getName());
        }

        TextColor greenColor = TextColor.fromFormatting(Formatting.GREEN);
        boolean hasGreenInSiblings = stack.getName().getSiblings().stream()
                .anyMatch(sibling -> greenColor.equals(sibling.getStyle().getColor()));
        if (!hasGreenInSiblings) return false;

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
        for (AbstractClientPlayerEntity player : mc.world.getPlayers()) {
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
