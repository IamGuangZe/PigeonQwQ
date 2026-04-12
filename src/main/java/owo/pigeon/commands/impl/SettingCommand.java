package owo.pigeon.commands.impl;

import net.minecraft.block.Block;
import net.minecraft.client.util.InputUtil;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import owo.pigeon.commands.Command;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.*;
import owo.pigeon.utils.ColorUtil;
import owo.pigeon.utils.CommandUtil;
import owo.pigeon.utils.ModuleUtil;
import owo.pigeon.utils.chat.ChatUtil;

import java.awt.*;

public class SettingCommand extends Command {
    public SettingCommand() {
        super("setting");
    }

    // 无脑嵌套
    @Override
    public void execute(String[] args) {
        if (args.length < 3) {
            CommandUtil.sendCommandError(CommandUtil.errorReason.UnknownOrIncompleteCommand,
                    this.getCommand(),
                    args,
                    args.length
            );
            return;
        }

        String modulename = args[0];
        String settingname = args[1];
        String value = args[2];

        if (!ModuleUtil.isModuleExist(modulename)) {
            CommandUtil.sendCommandError(CommandUtil.errorReason.UnknownModule,
                    this.getCommand(),
                    args,
                    0
            );
            return;
        }

        Module module = ModuleUtil.getModule(modulename);
        modulename = module.name;

        boolean found = false;
        for (AbstractSetting<?> setting : module.getSettings()) {
            if (setting.getName().equalsIgnoreCase(settingname)) {
                found = true;
                settingname = setting.getName();
                if (setting instanceof BlockSetting) {
                    Identifier id = Identifier.tryParse(value.toLowerCase());
                    if (id == null || !Registries.BLOCK.containsId(id)) {
                        CommandUtil.sendCommandError(CommandUtil.errorReason.UnknownBlock,
                                this.getCommand(),
                                args,
                                2
                        );
                        return;
                    }
                    Block blockValue = Registries.BLOCK.get(id);
                    ((BlockSetting) setting).setValue(blockValue);
                    value = blockValue.getName().getString() + "(" + Registries.BLOCK.getId(blockValue) + ")";
                } else if (setting instanceof CharSetting charSetting) {
                    char charValue = value.charAt(0);
                    charSetting.setValue(charValue);
                    value = String.valueOf(charValue);
                } else if (setting instanceof ColorSetting colorSetting) {
                    try {
                        Color colorValue;
                        if (value.startsWith("#") || value.startsWith("0x")) {
                            // HEX format with prefix (e.g. "#39C5BB", "0x39C5BB", "#39C5BBFF", "0x39C5BBFF")
                            colorValue = ColorUtil.parseHexColor(value);
                        } else if (value.matches("(?i)[0-9A-F]{6}") || value.matches("(?i)[0-9A-F]{8}")) {
                            // HEX format without prefix (e.g. "39C5BB", "39C5BBFF")
                            colorValue = ColorUtil.parseHexColor(value);
                        } else if (args.length >= 5) { // Separate R G B [A] inputs (args[2] R, args[3] G, args[4] B, optional args[5] A, e.g., 57 197 187)
                            int r = ColorUtil.colorClamp(Integer.parseInt(args[2]));
                            int g = ColorUtil.colorClamp(Integer.parseInt(args[3]));
                            int b = ColorUtil.colorClamp(Integer.parseInt(args[4]));
                            int a = ColorUtil.colorClamp((args.length > 5) ? Integer.parseInt(args[5]) : 255);
                            colorValue = new Color(r, g, b, a);
                        } else if (value.matches("\\d+")) { // RGBA format (e.g. 3786171 for RGB, 3786171255 for RGBA)
                            long longValue = Long.parseLong(value);
                            if (args.length > 3) {
                                int alpha = Integer.parseInt(args[3]);
                                colorValue = ColorUtil.parseDecimalColor(longValue, alpha);
                            } else {
                                colorValue = ColorUtil.parseDecimalColor(longValue);
                            }
                        } else {
                            CommandUtil.sendCommandError(CommandUtil.errorReason.IncorrectArgument,
                                    this.getCommand(),
                                    args,
                                    2
                            );
                            return;
                        }

                        colorSetting.setValue(colorValue);
                        value = String.format("R:%d G:%d B:%d A:%d",
                                colorValue.getRed(), colorValue.getGreen(), colorValue.getBlue(), colorValue.getAlpha());
                    } catch (NumberFormatException e) {
                        CommandUtil.sendCommandError(CommandUtil.errorReason.ExpectedInteger,
                                this.getCommand(),
                                args,
                                2
                        );
                        return;
                    } catch (IllegalArgumentException e) {
                        CommandUtil.sendCommandError(CommandUtil.errorReason.IncorrectArgument,
                                this.getCommand(),
                                args,
                                2
                        );
                        return;
                    }
                } else if (setting instanceof EnableSetting enableSetting) {
                    boolean booleanValue;
                    if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("enable")) {
                        booleanValue = true;
                    } else if (value.equalsIgnoreCase("false") || value.equalsIgnoreCase("disable")) {
                        booleanValue = false;
                    } else {
                        CommandUtil.sendCommandError(CommandUtil.errorReason.InvalidBoolean,
                                this.getCommand(),
                                args,
                                2
                        );
                        return;
                    }
                    enableSetting.setValue(booleanValue);
                    value = String.valueOf(booleanValue);
                } else if (setting instanceof ExpandSetting expandSetting) {
                    boolean booleanValue;
                    if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("enable")) {
                        booleanValue = true;
                    } else if (value.equalsIgnoreCase("false") || value.equalsIgnoreCase("disable")) {
                        booleanValue = false;
                    } else {
                        CommandUtil.sendCommandError(CommandUtil.errorReason.InvalidBoolean,
                                this.getCommand(),
                                args,
                                2
                        );
                        return;
                    }
                    expandSetting.setValue(booleanValue);
                    value = String.valueOf(booleanValue);
                } else if (setting instanceof FloatSetting floatSetting) {
                    try {
                        float floatValue = Float.parseFloat(value);

                        if (floatValue < floatSetting.getMinValue()) {
                            floatValue = floatSetting.getMinValue();
                        } else if (floatValue > floatSetting.getMaxValue()) {
                            floatValue = floatSetting.getMaxValue();
                        }

                        floatSetting.setValue(floatValue);
                        value = String.valueOf(floatValue);
                    } catch (NumberFormatException e) {
                        CommandUtil.sendCommandError(CommandUtil.errorReason.ExpectedFloat,
                                this.getCommand(),
                                args,
                                2
                        );
                        return;
                    }
                } else if (setting instanceof IntSetting intSetting) {
                    try {
                        Integer intValue = Integer.parseInt(value);

                        if (intValue < intSetting.getMinValue()) {
                            intValue = intSetting.getMinValue();
                        } else if (intValue > intSetting.getMaxValue()) {
                            intValue = intSetting.getMaxValue();
                        }

                        intSetting.setValue(intValue);
                        value = String.valueOf(intValue);
                    } catch (NumberFormatException e) {
                        CommandUtil.sendCommandError(CommandUtil.errorReason.ExpectedInteger,
                                this.getCommand(),
                                args,
                                2
                        );
                        return;
                    }
                } else if (setting instanceof KeySetting keySetting) {
                    Integer keyCode = null;
                    try {
                        keyCode = InputUtil.fromTranslationKey("key.keyboard." + value.toLowerCase()).getCode();
                    } catch (Exception e) {
                        keyCode = -1;
                    }

                    ChatUtil.sendDebugMessage("SettingCommand", "Setting Key: " + keyCode);

                    keySetting.setValue(keyCode);
                    value = keyCode == -1 ?
                            "None" :
                            InputUtil.Type.KEYSYM.createFromCode(keyCode)
                                    .getTranslationKey().replace("key.keyboard.", "").toUpperCase() + " (keycode: " + keyCode + ")";
                } else if (setting instanceof ModeSetting<?> modeSetting) {
                    try {
                        Enum<?> enumValue = Enum.valueOf((Class<Enum>) modeSetting.getValue().getClass(), value.toUpperCase());
                        ((ModeSetting) setting).setValue(enumValue);
                        value = enumValue.toString().toUpperCase();
                    } catch (IllegalArgumentException e) {
                        CommandUtil.sendCommandError(CommandUtil.errorReason.IncorrectArgument,
                                this.getCommand(),
                                args,
                                2
                        );
                        return;
                    }
                } else if (setting instanceof StringSetting) {
                    ((StringSetting) setting).setValue(value);
                } else {
                    this.sendCommandError("Unknown setting type!");
                    return;
                }

                ChatUtil.sendMessage("&aThe &7&l" + settingname + "&r&a of &7&l" + modulename + "&r&a has been changed to &7&l" + value + "&r&a.");
            }
        }
        if (!found) {
            CommandUtil.sendCommandError(CommandUtil.errorReason.UnknownSetting,
                    this.getCommand(),
                    args,
                    1
            );
        }
    }

    @Override
    public String getUsage() {
        return CommandUtil.getCommandPrefix() + "setting <module> <setting> <value>";
    }
}
