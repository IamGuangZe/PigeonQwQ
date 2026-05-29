package owo.pigeon.integration;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import owo.pigeon.Pigeon;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            Pigeon.clickGuiScreen.setParentScreen(parent);
            return Pigeon.clickGuiScreen;
        };
    }
}
