package owo.pigeon.commands.impl;

import net.minecraft.block.Block;
import net.minecraft.client.util.InputUtil;
import net.minecraft.registry.Registries;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
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

    @Override
    public void execute(String[] args) {
        if (args.length == 1) {
            String modulename = args[0];
            if (!ModuleUtil.isModuleExist(modulename)) {
                CommandUtil.sendCommandError(CommandUtil.errorReason.UnknownModule,
                        this.getCommand(),
                        args,
                        0
                );
                return;
            }
            Module module = ModuleUtil.getModule(modulename);
            displayModuleSettings(module);
            return;
        }

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

                if (setting instanceof ListSetting listSetting) {
                    String action = value.toLowerCase();
                    if (action.equals("add")) {
                        if (args.length < 4) {
                            this.sendUsage();
                            return;
                        }
                        String addValue = String.join(" ", java.util.Arrays.copyOfRange(args, 3, args.length));
                        listSetting.add(addValue);
                        ChatUtil.sendMessage("&aAdded &7&l" + addValue + "&r&a to &7&l" + settingname + "&r&a.");
                        return;
                    } else if (action.equals("remove")) {
                        if (args.length < 4) {
                            this.sendUsage();
                            return;
                        }
                        String removeValue = String.join(" ", java.util.Arrays.copyOfRange(args, 3, args.length));
                        if (listSetting.contains(removeValue)) {
                            listSetting.remove(removeValue);
                            ChatUtil.sendMessage("&aRemoved &7&l" + removeValue + "&r&a from &7&l" + settingname + "&r&a.");
                        } else {
                            CommandUtil.sendCommandError(CommandUtil.errorReason.ListItemNotFound,
                                    this.getCommand(),
                                    args,
                                    3
                            );
                        }
                        return;
                    } else if (action.equals("list")) {
                        if (listSetting.size() == 0) {
                            ChatUtil.sendMessage("&7List &7&l" + settingname + "&r&7 is empty.");
                        } else {
                            ChatUtil.sendMessage("&7List &7&l" + settingname + "&r&7 (" + listSetting.size() + " items):");
                            for (int i = 0; i < listSetting.size(); i++) {
                                ChatUtil.sendMessage("&7  " + (i + 1) + ". &f" + listSetting.get(i));
                            }
                        }
                        return;
                    } else {
                        CommandUtil.sendCommandError(CommandUtil.errorReason.UnknownListAction,
                                this.getCommand(),
                                args,
                                2
                        );
                        return;
                    }
                } else if (setting instanceof BlockSetting) {
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
                    CommandUtil.sendCommandError(CommandUtil.errorReason.IncorrectArgument,
                            this.getCommand(),
                            args,
                            1
                    );
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

    private void displayModuleSettings(Module module) {
        ChatUtil.sendMessage("&b" + module.name + " &8(" + module.getSettings().size() + " settings):");

        for (AbstractSetting<?> setting : module.getSettings()) {
            if (!setting.isVisible()) continue;

            String cmdPrefix = String.valueOf(CommandUtil.getCommandPrefix());
            String suggest = cmdPrefix + "setting " + module.name + " " + setting.getName() + " ";

            MutableText line = Text.literal(ColorUtil.parseColor("&7 - " + setting.getName() + "&8: "))
                    .append(setting instanceof ColorSetting s ? buildColorSettingText(s) : Text.literal(ColorUtil.parseColor(formatSettingValue(setting))));

            line.styled(style -> style
                    .withClickEvent(new ClickEvent.SuggestCommand(suggest))
                    .withHoverEvent(new HoverEvent.ShowText(
                            Text.literal(ColorUtil.parseColor(buildHoverText(setting)))
                    ))
            );
            ChatUtil.sendMessage(line);
        }
    }

    private MutableText buildColorSettingText(ColorSetting s) {
        Color c = s.getValue();
        int rgb = c.getRGB() & 0xFFFFFF;

        MutableText hash = Text.literal("#").styled(style -> style.withColor(rgb));
        MutableText r = Text.literal(ColorUtil.parseColor("&c" + String.format("%02X", c.getRed())));
        MutableText g = Text.literal(ColorUtil.parseColor("&a" + String.format("%02X", c.getGreen())));
        MutableText b = Text.literal(ColorUtil.parseColor("&9" + String.format("%02X", c.getBlue())));
        MutableText a = Text.literal(ColorUtil.parseColor("&f" + String.format("%02X", c.getAlpha())));

        return hash.append(r).append(g).append(b).append(a);
    }

    private String formatSettingValue(AbstractSetting<?> setting) {
        if (setting instanceof EnableSetting s) return s.getValue() ? "&atrue" : "&cfalse";
        else if (setting instanceof ExpandSetting s) return s.getValue() ? "&aexpand" : "&ccollapse";
        else if (setting instanceof ListSetting s) return "&b[" + s.size() + " items]";
        else if (setting instanceof ModeSetting<?> s) return "&b" + s.getValue().toString().toUpperCase();
        else if (setting instanceof IntSetting s) {
            String unit = s.getUnit();
            String value = "&e" + s.getValue();
            if (unit != null) value += " &7" + unit;
            return value;
        } else if (setting instanceof FloatSetting s) {
            String unit = s.getUnit();
            String value = "&e" + s.getValue();
            if (unit != null) value += " &7" + unit;
            return value;
        } else if (setting instanceof StringSetting s) {
            return "&7\"" + s.getValue().replace("&", "&&") + "&7\"";
        } else if (setting instanceof CharSetting s) {
            return "&7'" + String.valueOf(s.getValue()).replace("&", "&&") + "&7'";
        } else if (setting instanceof KeySetting s) {
            int code = s.getValue();
            if (code == -1) return "&cNone";
            try {
                return InputUtil.Type.KEYSYM.createFromCode(code)
                        .getTranslationKey().replace("key.keyboard.", "").toUpperCase();
            } catch (Exception e) {
                return String.valueOf(code);
            }
        } else if (setting instanceof BlockSetting s) {
            return s.getValue().getName().getString();
        }

        return "&f" + setting.getValue().toString();
    }

    private String buildHoverText(AbstractSetting<?> setting) {
        if (setting instanceof IntSetting s) {
            String text = "&7Int&8: &f" + s.getMinValue() + " &8- &f" + s.getMaxValue();
            if (s.getUnit() != null) text += " &8(&7" + s.getUnit() + "&8)";
            return text;
        } else if (setting instanceof FloatSetting s) {
            String text = "&7Float&8: &f" + s.getMinValue() + " &8- &f" + s.getMaxValue();
            if (s.getUnit() != null) text += " &8(&7" + s.getUnit() + "&8)";
            return text;
        } else if (setting instanceof ModeSetting<?> s) {
            StringBuilder sb = new StringBuilder("&7Mode&8: ");
            Enum<?>[] values = s.getValue().getDeclaringClass().getEnumConstants();
            for (int i = 0; i < values.length; i++) {
                if (i > 0) sb.append("&8, ");
                sb.append("&f").append(values[i].toString());
            }
            return sb.toString();
        } else if (setting instanceof ColorSetting s) {
            Color c = s.getValue();
            return "&7R&8:&f" + c.getRed() + " &7G&8:&f" + c.getGreen()
                    + " &7B&8:&f" + c.getBlue() + " &7A&8:&f" + c.getAlpha();
        } else if (setting instanceof ListSetting s) {
            return "&7List &8(&f" + s.size() + " &7items&8)";
        } else if (setting instanceof KeySetting s) {
            return "&7Keybind";
        } else if (setting instanceof EnableSetting) return "&7Enable";
        else if (setting instanceof ExpandSetting) return "&7Expand";
        else if (setting instanceof StringSetting) return "&7String";
        else if (setting instanceof CharSetting) return "&7Character";
        else if (setting instanceof BlockSetting) return "&7Block";
        return "&7" + setting.getClass().getSimpleName();
    }

    @Override
    public String getUsage() {
        return CommandUtil.getCommandPrefix() + "setting <module> [<setting> <value>]\n" +
                CommandUtil.getCommandPrefix() + "setting <module> <listsetting> (add|remove|list) <value>";
    }
}
