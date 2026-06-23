package owo.pigeon.utils.chat;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import owo.pigeon.Pigeon;
import owo.pigeon.utils.ColorUtil;

import static owo.pigeon.Pigeon.mc;

public class ChatUtil {

    private static MutableComponent getClientPrefix() {
        MutableComponent bracketLeft = Component.literal("[").withStyle(style -> style.withColor(ChatFormatting.DARK_GRAY));
        MutableComponent bracketRight = Component.literal("]").withStyle(style -> style.withColor(ChatFormatting.DARK_GRAY));

        ColorUtil.Theme theme = ColorUtil.getTheme();
        MutableComponent nameText = theme.isGradient()
                ? ColorUtil.gradientText(Pigeon.MOD_NAME, theme.getGradient())
                : Component.literal(Pigeon.MOD_NAME).withStyle(style -> style.withColor(ChatFormatting.WHITE));

        return Component.empty()
                .append(bracketLeft)
                .append(nameText)
                .append(bracketRight)
                .append(Component.literal(" ").withStyle(style -> style.applyFormat(ChatFormatting.RESET)));
    }

    private static MutableComponent getCustomPrefix(String prefix) {
        MutableComponent bracketLeft = Component.literal("[").withStyle(style -> style.withColor(ChatFormatting.DARK_GRAY));
        MutableComponent bracketRight = Component.literal("]").withStyle(style -> style.withColor(ChatFormatting.DARK_GRAY));

        ColorUtil.Theme theme = ColorUtil.getTheme();
        MutableComponent nameText = theme.isGradient()
                ? ColorUtil.gradientText(prefix, theme.getGradient())
                : Component.literal(prefix).withStyle(style -> style.withColor(ChatFormatting.WHITE));

        return Component.empty()
                .append(bracketLeft)
                .append(nameText)
                .append(bracketRight)
                .append(Component.literal(" ").withStyle(style -> style.applyFormat(ChatFormatting.RESET)));
    }

    public static void sendRawMessage(String message) {
        SafeMessage.messages.add(Component.literal(message));
    }

    public static void sendRawMessage(Component text) {
        SafeMessage.messages.add(text);
    }

    public static void sendMessage(String message) {
        sendRawMessage(getClientPrefix().append(Component.literal(ColorUtil.parseColor(message))));
    }

    public static void sendMessage(Component text) {
        sendRawMessage(getClientPrefix().append(text));
    }

    public static void sendUncoloredMessage(String message) {
        MutableComponent prefix = getClientPrefix();
        sendRawMessage(prefix.append(Component.literal(message)));
    }

    public static void sendUncoloredMessage(Component text) {
        sendRawMessage(getClientPrefix().append(text));
    }

    public static void sendMessage(String prefix, String message) {
        sendRawMessage(getCustomPrefix(prefix).append(Component.literal(ColorUtil.parseColor(message))));
    }

    public static void sendMessage(String prefix, Component text) {
        sendRawMessage(getCustomPrefix(prefix).append(text));
    }

    public static void sendMultiLineMessage(String message) {
        if (message == null) return;
        String[] lines = message.split("\n");
        for (String line : lines) {
            sendMessage(line);
        }
    }

    private static MutableComponent buildDebugPrefix() {
        int tick = mc.player != null ? mc.player.tickCount : -1;
        return Component.empty()
                .append(Component.literal("[DEBUG]").withStyle(s -> s.withColor(ChatFormatting.RED).withBold(true)))
                .append(Component.literal(" "))
                .append(Component.literal("[" + tick + "]").withStyle(s -> s.withColor(ChatFormatting.RED).withBold(true)))
                .append(Component.literal(" "));
    }

    public static void sendDebugMessage(String message) {
        if (!Pigeon.isDebug()) return;
        sendRawMessage(buildDebugPrefix().append(getClientPrefix()).append(Component.literal(ColorUtil.parseColor(message))));
    }

    public static void sendDebugMessage(Component text) {
        if (!Pigeon.isDebug()) return;
        sendRawMessage(buildDebugPrefix().append(getClientPrefix()).append(text));
    }

    public static void sendDebugMessage(String prefix, String message) {
        if (!Pigeon.isDebug()) return;
        sendRawMessage(buildDebugPrefix().append(getCustomPrefix(prefix)).append(Component.literal(ColorUtil.parseColor(message))));
    }

    public static void sendDebugMessage(String prefix, Component text) {
        if (!Pigeon.isDebug()) return;
        sendRawMessage(buildDebugPrefix().append(getCustomPrefix(prefix)).append(text));
    }

    public static void sendIfHudReadyMessage(String message) {
        if (mc.gui == null || mc.gui.getChat() == null) return;
        mc.gui.getChat().addMessage(getClientPrefix().append(Component.literal(ColorUtil.parseColor(message))));
    }

    public static void sendIfHudReadyMessage(Component text) {
        if (mc.gui == null || mc.gui.getChat() == null) return;
        mc.gui.getChat().addMessage(getClientPrefix().append(text));
    }
}
