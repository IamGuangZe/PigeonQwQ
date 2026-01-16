package owo.pigeon.utils.Chat;

import net.minecraft.text.Text;
import owo.pigeon.Pigeonqwq;
import owo.pigeon.utils.ColorUtil;

import static owo.pigeon.Pigeonqwq.mc;

public class ChatUtil {
    // 发送客户端消息
    public static void sendRawMessage(String message) {
        SafeMessage.messages.add(message);
    }

    // 发送不带前缀且可上色的消息
    public static void sendColoredRawMessage(String message) {
        sendRawMessage(ColorUtil.parseColor(message));
    }

    // 发送带客户端前缀且可上色的消息
    public static void sendMessage(String message) {
        sendRawMessage(ColorUtil.parseColor("&8[&3" + Pigeonqwq.MOD_NAME + "&8]&r " + message));
    }

    // 发送带客户端前缀且不可上色的消息
    public static void sendUncoloredMessage(String message) {
        sendRawMessage(ColorUtil.parseColor("&8[&3" + Pigeonqwq.MOD_NAME + "&8]&r ") + message);
    }

    // 发送自定义前缀且可上色的消息
    public static void sendCustomPrefixMessage(String prefix, String message) {
        sendRawMessage(ColorUtil.parseColor("&8[&3" + prefix + "&8]&r " + message));
    }

    // 发送支持换行的带客户端前缀且可上色消息
    public static void sendMultiLineMessage(String message) {
        if (message == null) return;
        String[] lines = message.split("\n");
        for (String line : lines) {
            sendMessage(line);
        }
    }

    // 发送带客户端前缀且可上色的调试信息
    public static void sendDebugMessage(String message) {
        int tick = mc.player != null ? mc.player.age : -1;
        if (Pigeonqwq.isDebug())
            sendRawMessage(ColorUtil.parseColor("&c&l[DEBUG] [" + tick + "] &8[&3" + Pigeonqwq.MOD_NAME + "&8]&r " + message));
    }

    // 发送自定义前缀且可上色的调试信息
    public static void sendDebugMessage(String prefix, String message) {
        int tick = mc.player != null ? mc.player.age : -1;
        if (Pigeonqwq.isDebug())
            sendRawMessage(ColorUtil.parseColor("&c&l[DEBUG] [" + tick + "] &8[&3" + prefix + "&8]&r " + message));
    }

    public static void sendIfHudReadyMessage(String message) {
        if (mc.inGameHud == null || mc.inGameHud.getChatHud() == null) return;
        mc.inGameHud.getChatHud().addMessage(Text.of(ColorUtil.parseColor("&8[&3" + Pigeonqwq.MOD_NAME + "&8]&r " + message)));
    }
}
