package owo.pigeon.config.configs;

import owo.pigeon.Pigeon;
import owo.pigeon.config.Config;
import owo.pigeon.gui.ClickGui.panels.CategoryPanel;
import owo.pigeon.gui.ClickGui.panels.ModulePanel;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

import static owo.pigeon.commands.Command.GSON;

public class ClickGuiConfig extends Config {
    public ClickGuiConfig() {
        super("ClickGui.json");
    }

    @Override
    public void load() {
        if (!this.exists()) return;

        try (FileReader reader = new FileReader(getFile())) {
            Map<?, ?> root = GSON.fromJson(reader, Map.class);
            if (root == null) return;

            for (CategoryPanel categoryPanel : Pigeon.clickGuiScreen.categoryPanels) {
                Object categoryObj = root.get(categoryPanel.getCategory().name());
                if (!(categoryObj instanceof Map<?, ?> categoryMap)) continue;

                Object x = categoryMap.get("x");
                Object y = categoryMap.get("y");
                Object display = categoryMap.get("display");

                if (x instanceof Number n) categoryPanel.x = n.intValue();
                if (y instanceof Number n) categoryPanel.y = n.intValue();
                if (display instanceof Boolean b) categoryPanel.setDisplayModule(b);

                Object moduleObj = categoryMap.get("module");
                if (moduleObj instanceof Map<?, ?> moduleMap) {
                    for (ModulePanel modulePanel : categoryPanel.modulePanels) {
                        Object v = moduleMap.get(modulePanel.getModule().name);
                        if (v instanceof Boolean b) {
                            modulePanel.setDisplaySetting(b);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save() {
        Map<String, Object> root = new HashMap<>();

        for (CategoryPanel categoryPanel : Pigeon.clickGuiScreen.categoryPanels) {
            Map<String, Object> categoryMap = new HashMap<>();

            categoryMap.put("x", categoryPanel.x);
            categoryMap.put("y", categoryPanel.y);
            categoryMap.put("display", categoryPanel.getDisplayModule());

            Map<String, Object> moduleMap = new HashMap<>();
            for (ModulePanel modulePanel : categoryPanel.modulePanels) {
                moduleMap.put(
                        modulePanel.getModule().name,
                        modulePanel.isDisplaySetting()
                );
            }

            categoryMap.put("module", moduleMap);
            root.put(categoryPanel.getCategory().name(), categoryMap);
        }

        try (FileWriter writer = new FileWriter(getFile())) {
            GSON.toJson(root, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
