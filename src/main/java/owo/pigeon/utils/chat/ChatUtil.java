package owo.pigeon.utils.chat;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import owo.pigeon.Pigeon;
import owo.pigeon.utils.ColorUtil;

import static owo.pigeon.Pigeon.mc;

public class ChatUtil {

    private static MutableText getClientPrefix() {
        MutableText bracketLeft = Text.literal("[").styled(style -> style.withColor(Formatting.DARK_GRAY));
        MutableText bracketRight = Text.literal("]").styled(style -> style.withColor(Formatting.DARK_GRAY));

        ColorUtil.Theme theme = ColorUtil.getTheme();
        MutableText nameText = theme.isGradient()
                ? ColorUtil.gradientText(Pigeon.MOD_NAME, theme.getGradient())
                : Text.literal(Pigeon.MOD_NAME).styled(style -> style.withColor(Formatting.WHITE));

        return Text.empty()
                .append(bracketLeft)
                .append(nameText)
                .append(bracketRight)
                .append(Text.literal(" ").styled(style -> style.withFormatting(Formatting.RESET)));
    }

    private static MutableText getCustomPrefix(String prefix) {
        MutableText bracketLeft = Text.literal("[").styled(style -> style.withColor(Formatting.DARK_GRAY));
        MutableText bracketRight = Text.literal("]").styled(style -> style.withColor(Formatting.DARK_GRAY));

        ColorUtil.Theme theme = ColorUtil.getTheme();
        MutableText nameText = theme.isGradient()
                ? ColorUtil.gradientText(prefix, theme.getGradient())
                : Text.literal(prefix).styled(style -> style.withColor(Formatting.WHITE));

        return Text.empty()
                .append(bracketLeft)
                .append(nameText)
                .append(bracketRight)
                .append(Text.literal(" ").styled(style -> style.withFormatting(Formatting.RESET)));
    }

    public static void sendRawMessage(String message) {
        SafeMessage.messages.add(Text.literal(message));
    }

    public static void sendRawMessage(Text text) {
        SafeMessage.messages.add(text);
    }

    public static void sendMessage(String message) {
        sendRawMessage(getClientPrefix().append(Text.literal(ColorUtil.parseColor(message))));
    }

    public static void sendMessage(Text text) {
        sendRawMessage(getClientPrefix().append(text));
    }

    public static void sendUncoloredMessage(String message) {
        MutableText prefix = getClientPrefix();
        sendRawMessage(prefix.append(Text.literal(message)));
    }

    public static void sendUncoloredMessage(Text text) {
        sendRawMessage(getClientPrefix().append(text));
    }

    public static void sendMessage(String prefix, String message) {
        sendRawMessage(getCustomPrefix(prefix).append(Text.literal(ColorUtil.parseColor(message))));
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

    private static MutableText buildDebugPrefix() {
        int tick = mc.player != null ? mc.player.age : -1;
        return Text.empty()
                .append(Text.literal("[DEBUG]").styled(s -> s.withColor(Formatting.RED).withBold(true)))
                .append(Text.literal(" "))
                .append(Text.literal("[" + tick + "]").styled(s -> s.withColor(Formatting.RED).withBold(true)))
                .append(Text.literal(" "));
    }

    public static void sendDebugMessage(String message) {
        if (!Pigeon.isDebug()) return;
        sendRawMessage(buildDebugPrefix().append(getClientPrefix()).append(Text.literal(ColorUtil.parseColor(message))));
    }

    public static void sendDebugMessage(Text text) {
        if (!Pigeon.isDebug()) return;
        sendRawMessage(buildDebugPrefix().append(getClientPrefix()).append(text));
    }

    public static void sendDebugMessage(String prefix, String message) {
        if (!Pigeon.isDebug()) return;
        sendRawMessage(buildDebugPrefix().append(getCustomPrefix(prefix)).append(Text.literal(ColorUtil.parseColor(message))));
    }

    public static void sendDebugMessage(String prefix, Text text) {
        if (!Pigeon.isDebug()) return;
        sendRawMessage(buildDebugPrefix().append(getCustomPrefix(prefix)).append(text));
    }

    public static void sendIfHudReadyMessage(String message) {
        if (mc.inGameHud == null || mc.inGameHud.getChatHud() == null) return;
        mc.inGameHud.getChatHud().addMessage(getClientPrefix().append(Text.literal(ColorUtil.parseColor(message))));
    }

    public static void sendIfHudReadyMessage(Text text) {
        if (mc.inGameHud == null || mc.inGameHud.getChatHud() == null) return;
        mc.inGameHud.getChatHud().addMessage(getClientPrefix().append(text));
    }
}
