package owo.pigeon.utils.Chat;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import owo.pigeon.Pigeon;
import owo.pigeon.utils.ColorUtil;

import static owo.pigeon.Pigeon.mc;

public class ChatUtil {


    private static MutableText getClientPrefix() {
        return Text.literal(ColorUtil.parseColor("&8[&3" + Pigeon.MOD_NAME + "&8]&r "));
    }

    private static MutableText getCustomPrefix(String prefix) {
        return Text.literal(ColorUtil.parseColor("&8[&3" + prefix + "&8]&r "));
    }

    public static void sendRawMessage(String message) {
        SafeMessage.messages.add(Text.literal(message));
    }

    public static void sendRawMessage(Text text) {
        SafeMessage.messages.add(text);
    }

    public static void sendMessage(String message) {
        sendRawMessage(ColorUtil.parseColor("&8[&3" + Pigeon.MOD_NAME + "&8]&r " + message));
    }

    public static void sendMessage(Text text) {
        sendRawMessage(getClientPrefix().append(text));
    }

    public static void sendUncoloredMessage(String message) {
        sendRawMessage(ColorUtil.parseColor("&8[&3" + Pigeon.MOD_NAME + "&8]&r ") + message);
    }

    public static void sendUncoloredMessage(Text text) {
        sendRawMessage(getClientPrefix().append(text));
    }


    public static void sendMessage(String prefix, String message) {
        sendRawMessage(ColorUtil.parseColor("&8[&3" + prefix + "&8]&r " + message));
    }

    public static void sendMessage(String prefix, Text text) {
        sendRawMessage(getCustomPrefix(prefix).append(text));
    }

    public static void sendMultiLineMessage(String message) {
        if (message == null) return;
        String[] lines = message.split("\n");
        for (String line : lines) {
            sendMessage(line);
        }
    }

    public static void sendDebugMessage(String message) {
        int tick = mc.player != null ? mc.player.age : -1;
        if (Pigeon.isDebug())
            sendRawMessage(ColorUtil.parseColor("&c&l[DEBUG] [" + tick + "] &8[&3" + Pigeon.MOD_NAME + "&8]&r " + message));
    }

    public static void sendDebugMessage(Text text) {
        if (!Pigeon.isDebug()) return;
        int tick = mc.player != null ? mc.player.age : -1;
        MutableText debugPrefix = Text.literal(ColorUtil.parseColor("&c&l[DEBUG] [" + tick + "] "))
                .append(getClientPrefix());
        sendRawMessage(debugPrefix.append(text));
    }
    
    public static void sendDebugMessage(String prefix, String message) {
        int tick = mc.player != null ? mc.player.age : -1;
        if (Pigeon.isDebug())
            sendRawMessage(ColorUtil.parseColor("&c&l[DEBUG] [" + tick + "] &8[&3" + prefix + "&8]&r " + message));
    }

    public static void sendDebugMessage(String prefix, Text text) {
        if (!Pigeon.isDebug()) return;
        int tick = mc.player != null ? mc.player.age : -1;
        MutableText debugPrefix = Text.literal(ColorUtil.parseColor("&c&l[DEBUG] [" + tick + "] "))
                .append(getCustomPrefix(prefix));
        sendRawMessage(debugPrefix.append(text));
    }
    
    public static void sendIfHudReadyMessage(String message) {
        if (mc.inGameHud == null || mc.inGameHud.getChatHud() == null) return;
        mc.inGameHud.getChatHud().addMessage(Text.literal(ColorUtil.parseColor("&8[&3" + Pigeon.MOD_NAME + "&8]&r " + message)));
    }

    public static void sendIfHudReadyMessage(Text text) {
        if (mc.inGameHud == null || mc.inGameHud.getChatHud() == null) return;
        mc.inGameHud.getChatHud().addMessage(getClientPrefix().append(text));
    }
}