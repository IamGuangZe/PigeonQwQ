package owo.pigeon.utils;

import net.minecraft.util.StringHelper;
import org.apache.commons.lang3.StringUtils;
import owo.pigeon.modules.impl.Client.PigeonQwQ;
import owo.pigeon.utils.Chat.ChatUtil;

public class CommandUtil {
    public enum errorReason {

        ExpectedFloat("Expected float"),  // 不为浮点
        ExpectedInteger("Expected integer"),    // 不为整数
        UnknownOrIncompleteCommand("Unknown or incomplete command. See below for error"), // 未知指令/参数不完整
        InvalidBoolean("Invalid boolean: expected 'true' or 'false'"), // 不为布尔
        IncorrectArgument("Incorrect argument for command"),  // 参数错误
        UnknownBlock("Unknown block type"),   // 未知方块
        UnknownItem("Unknown item"),    // 未知物品
        UnknownModule("Unknown Module"), // 未知模块
        UnknownSetting("Unknown Setting"); // 未知设置

        private final String message;

        errorReason(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    public static void sendCommandError(errorReason reason, String correctPart, String errorPart) {
        sendCommandError(reason.getMessage(), correctPart, errorPart);
    }

    public static void sendCommandError(errorReason reason, String command, String[] args, int errorIndex) {
        StringBuilder correctPartBuilder = new StringBuilder(command);
        StringBuilder errorPartBuilder = new StringBuilder();

        if (errorIndex >= 0 && errorIndex < args.length) {
            for (int i = 0; i < errorIndex; i++) {
                correctPartBuilder.append(" ").append(args[i]);
            }
            for (int i = errorIndex; i < args.length; i++) {
                if (!errorPartBuilder.isEmpty()) errorPartBuilder.append(" ");
                errorPartBuilder.append(args[i]);
            }
        } else {
            for (String arg : args) {
                correctPartBuilder.append(" ").append(arg);
            }
        }

        String correctPart = correctPartBuilder.toString();
        String errorPart = errorPartBuilder.toString();

        sendCommandError(reason.getMessage(), correctPart, errorPart);
    }


    public static void sendCommandError(String reason, String correctPart, String errorPart) {
        ChatUtil.sendMessage("&c" + reason);

        if (!errorPart.isEmpty() && !correctPart.isEmpty()) {
            correctPart += " ";
        }

        if (correctPart.length() > 10) {
            correctPart = "..." + correctPart.substring(correctPart.length() - 10);
        }

        ChatUtil.sendMessage("&7" + correctPart + "&c&n" + errorPart + "&c&o<--[HERE]");
    }

    public static String normalize(String chatText) {
        return StringHelper.truncateChat(StringUtils.normalizeSpace(chatText.trim()));
    }

    public static char getCommandPrefix() {
        return ((PigeonQwQ) ModuleUtil.getModule(PigeonQwQ.class)).commandPrefix.getValue();
    }
}
