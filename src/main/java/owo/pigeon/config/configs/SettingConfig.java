package owo.pigeon.config.configs;

import com.google.gson.reflect.TypeToken;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import owo.pigeon.Pigeonqwq;
import owo.pigeon.config.Config;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.*;
import owo.pigeon.utils.Chat.ChatUtil;
import owo.pigeon.utils.ModuleUtil;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static owo.pigeon.commands.Command.GSON;

public class SettingConfig extends Config {

    private String settingName;

    public SettingConfig(String fileName) {
        super(fileName + ".json");
        this.settingName = fileName;
    }

    public SettingConfig() {
        super("default.json");
        this.settingName = "default";
    }

    @Override
    protected File getBaseDir() {
        return new File("config/" + Pigeonqwq.MOD_ID + "/settings");
    }

    @Override
    public void load() {
        if (!this.exists()) {
            loadDefault();
            return;
        }

        try (FileReader reader = new FileReader(this.getFile())) {
            Type type = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> root = GSON.fromJson(reader, type);

            if (root == null) {
                ChatUtil.sendMessage("&cFailed to read config!");
                return;
            }

            for (Category category : Category.values()) {
                Object categoryObj = root.get(category.name());
                if (!(categoryObj instanceof Map<?, ?> categoryMap)) continue;

                for (Module module : ModuleUtil.getAllModule(category)) {
                    Object moduleObj = categoryMap.get(module.name);
                    if (!(moduleObj instanceof Map<?, ?> moduleMap)) continue;

                    /* enable */
                    Object enableObj = moduleMap.get("enable");
                    if (enableObj instanceof Boolean enable) {
                        if (enable != module.isEnable()) {
                            if (enable) ModuleUtil.enableModule(module.getClass());
                            else ModuleUtil.disableModule(module.getClass());
                        }
                    } else {
                        ModuleUtil.disableModule(module.getClass());
                    }

                    /* hide */
                    Object hideObj = moduleMap.get("hide");
                    if (hideObj instanceof Boolean hide) {
                        module.setHide(hide);
                    } else {
                        module.setHide(false);
                    }

                    /* key */
                    Object keyObj = moduleMap.get("key");
                    if (keyObj instanceof Number num) {
                        module.setKey(num.intValue());
                    } else {
                        module.setKey(-1);
                    }

                    /* settings */
                    for (AbstractSetting<?> setting : module.getSettings()) {
                        if (!moduleMap.containsKey(setting.getName())) continue;

                        Object value = moduleMap.get(setting.getName());
                        try {
                            applySetting(setting, value);
                        } catch (Exception e) {
                            setting.resetValue();
                            ChatUtil.sendMessage(
                                    "&cSetting &l" + setting.getName() +
                                            "&r&c in &l" + module.name +
                                            "&r&c was invalid and reset."
                            );
                        }
                    }
                }
            }

            ChatUtil.sendMessage("&aConfig &o" + settingName + ".json &r&ahas been loaded.");
        } catch (Exception e) {
            ChatUtil.sendMessage("&cFailed to load config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void save() {
        Map<String, Object> root = new HashMap<>();

        for (Category category : Category.values()) {
            Map<String, Object> categoryMap= new HashMap<>();

            for (Module module : ModuleUtil.getAllModule(category)) {
                Map<String, Object> moduleMap = new HashMap<>();

                moduleMap.put("enable", module.isEnable());
                moduleMap.put("hide", module.isHide());
                moduleMap.put("key", module.getKey());

                for (AbstractSetting<?> setting : module.getSettings()) {
                    if (setting instanceof BlockSetting blockSetting) {
                        Block block = blockSetting.getValue();
                        Identifier id = Registries.BLOCK.getId(block);
                        moduleMap.put(setting.getName(), id.toString());
                    } else if (setting instanceof ColorSetting colorSetting) {
                        moduleMap.put(setting.getName(), colorSetting.getRGB());
                    } else {
                        moduleMap.put(setting.getName(), setting.getValue());
                    }
                }

                categoryMap.put(module.name, moduleMap);
            }

            root.put(category.name(), categoryMap);
        }

        try (FileWriter writer = new FileWriter(this.getFile())) {
            GSON.toJson(root, writer);
            ChatUtil.sendMessage("&aConfig &o" + settingName + ".json &r&ahas been saved.");
        } catch (Exception e) {
            ChatUtil.sendMessage("&cFailed to save config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadDefault() {
        for (Module module : ModuleUtil.getAllModule()) {
            ModuleUtil.disableModule(module.getClass());
            module.setHide(true);
            module.setKey(-1);
            for (AbstractSetting<?> setting : module.getSettings()) {
                setting.resetValue();
            }
        }
        ChatUtil.sendMessage("&aDefault config has been loaded.");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void applySetting(AbstractSetting<?> setting, Object value) {

        if (setting instanceof BlockSetting blockSetting) {
            Identifier id = Identifier.tryParse(value.toString());
            if (id == null) throw new IllegalArgumentException();
            blockSetting.setValue(Registries.BLOCK.get(id));

        } else if (setting instanceof ColorSetting colorSetting) {
            colorSetting.setRGB(((Number) value).intValue());

        } else if (setting instanceof EnableSetting enableSetting) {
            enableSetting.setValue((Boolean) value);

        } else if (setting instanceof FloatSetting floatSetting) {
            float f = ((Number) value).floatValue();
            floatSetting.setValue(
                    Math.max(floatSetting.getMinValue(),
                            Math.min(floatSetting.getMaxValue(), f))
            );

        } else if (setting instanceof IntSetting intSetting) {
            int i = ((Number) value).intValue();
            intSetting.setValue(
                    Math.max(intSetting.getMinValue(),
                            Math.min(intSetting.getMaxValue(), i))
            );

        } else if (setting instanceof KeySetting keySetting) {
            keySetting.setValue(((Number) value).intValue());

        } else if (setting instanceof ModeSetting modeSetting) {
            Enum<?> enumValue = Enum.valueOf(
                    (Class<Enum>) modeSetting.getValue().getClass(),
                    value.toString().toUpperCase()
            );
            modeSetting.setValue(enumValue);

        } else if (setting instanceof StringSetting stringSetting) {
            stringSetting.setValue(value.toString());
        }
    }

    public String getSettingName() {
        return settingName;
    }
}
