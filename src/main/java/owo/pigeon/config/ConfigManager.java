package owo.pigeon.config;

import owo.pigeon.config.configs.ClickGuiConfig;
import owo.pigeon.config.configs.SettingConfig;

import java.util.ArrayList;

public class ConfigManager {
    public static final ArrayList<Config> configs = new ArrayList<>();

    public void init() {

        configs.add(new ClickGuiConfig());
        configs.add(new SettingConfig());

        loadAll();
        Runtime.getRuntime().addShutdownHook(new Thread(ConfigManager::saveAll, "Config-Save-Hook"));
    }

    public static void loadAll() {
        for (Config config : configs) {
            config.load();
        }
    }

    public static void saveAll() {
        for (Config config : configs) {
            config.save();
        }
    }
}
