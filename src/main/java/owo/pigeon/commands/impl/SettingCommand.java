package owo.pigeon.commands.impl;

import net.minecraft.block.Block;
import net.minecraft.client.util.InputUtil;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import owo.pigeon.commands.Command;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.*;
import owo.pigeon.utils.Chat.ChatUtil;
import owo.pigeon.utils.ColorUtil;
import owo.pigeon.utils.CommandUtil;
import owo.pigeon.utils.ModuleUtil;

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
                    Identifier id = Identifier.tryParse(value);
                    if (id == null) {
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
                } else if (setting instanceof CharSetting) {
                    char charValue = value.charAt(0);
                    ((CharSetting) setting).setValue(charValue);
                    value = String.valueOf(charValue);
                } else if (setting instanceof ColorSetting) {
                    try {
                        Color colorValue;
                        if (value.startsWith("#")) { // HEX (e.g. "39C5BB", "#39C5BB")
                            colorValue = Color.decode(value);
                        } else if (value.startsWith("0x")) {
                            colorValue = Color.decode(value.replace("0x","#"));
                        } else if (value.matches("(?i)[0-9A-F]{6,8}")) {
                            colorValue = Color.decode("#" + value);
                        } else if (value.matches("\\d+")) { // RGB/ARGB (e.g. 4280763835)
                            colorValue = new Color(Integer.parseInt(value),true);
                        } else if (args.length >= 5) { // Separate R G B [A] inputs (args[2] R, args[3] G, args[4] B, optional args[5] A, e.g., 57 197 187)
                            int r = ColorUtil.colorClamp(Integer.parseInt(args[2]));
                            int g = ColorUtil.colorClamp(Integer.parseInt(args[3]));
                            int b = ColorUtil.colorClamp(Integer.parseInt(args[4]));
                            int a = ColorUtil.colorClamp((args.length > 5) ? Integer.parseInt(args[5]) : 255);
                            colorValue = new Color(r, g, b, a);
                        } else {
                            CommandUtil.sendCommandError(CommandUtil.errorReason.IncorrectArgument,
                                    this.getCommand(),
                                    args,
                                    2
                            );
                            return;
                        }

                        ((ColorSetting) setting).setValue(colorValue);
                        value = String.format("R:%d G:%d B:%d A:%d",
                                colorValue.getRed(), colorValue.getGreen(), colorValue.getBlue(), colorValue.getAlpha());
                    } catch (NumberFormatException e) {
                        CommandUtil.sendCommandError(CommandUtil.errorReason.ExpectedInteger,
                                this.getCommand(),
                                args,
                                2
                        );
                        return;
                    }
                } else if (setting instanceof EnableSetting) {
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
                    ((EnableSetting) setting).setValue(booleanValue);
                    value = String.valueOf(booleanValue);
                } else if (setting instanceof FloatSetting) {
                    try {
                        FloatSetting floatSetting = (FloatSetting) setting;
                        float floatValue = Float.parseFloat(value);

                        if (floatValue < floatSetting.getMinValue()) {
                            floatValue = floatSetting.getMinValue();
                        } else if (floatValue > floatSetting.getMaxValue()) {
                            floatValue = floatSetting.getMaxValue();
                        }

                        ((FloatSetting) setting).setValue(floatValue);
                        value = String.valueOf(floatValue);
                    } catch (NumberFormatException e) {
                        CommandUtil.sendCommandError(CommandUtil.errorReason.ExpectedFloat,
                                this.getCommand(),
                                args,
                                2
                        );
                        return;
                    }
                } else if (setting instanceof IntSetting) {
                    try {
                        IntSetting intSetting = (IntSetting) setting;
                        Integer intValue = Integer.parseInt(value);

                        if (intValue < intSetting.getMinValue()) {
                            intValue = intSetting.getMinValue();
                        } else if (intValue > intSetting.getMaxValue()) {
                            intValue = intSetting.getMaxValue();
                        }

                        ((IntSetting) setting).setValue(intValue);
                        value = String.valueOf(intValue);
                    } catch (NumberFormatException e) {
                        CommandUtil.sendCommandError(CommandUtil.errorReason.ExpectedInteger,
                                this.getCommand(),
                                args,
                                2
                        );
                        return;
                    }
                } else if (setting instanceof KeySetting) {
                    Integer keyCode = null;
                    try {
                        keyCode = InputUtil.fromTranslationKey("key.keyboard." + value.toLowerCase()).getCode();
                    } catch (Exception e) {
                        keyCode = -1;
                    }

                    ChatUtil.sendDebugMessage("SettingCommand","Setting Key: " + keyCode);

                    ((KeySetting) setting).setValue(keyCode);
                    value = keyCode == -1 ?
                            "None" :
                            InputUtil.Type.KEYSYM.createFromCode(keyCode)
                                    .getTranslationKey().replace("key.keyboard.","").toUpperCase() + " (keycode: " + keyCode + ")" ;
                } else if (setting instanceof ModeSetting) {
                    try {
                        ModeSetting<?> modeSetting = (ModeSetting<?>) setting;
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
