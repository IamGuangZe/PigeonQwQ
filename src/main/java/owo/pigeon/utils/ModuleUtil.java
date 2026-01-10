package owo.pigeon.utils;

import owo.pigeon.modules.Module;

import static owo.pigeon.modules.ModuleManager.modules;

public class ModuleUtil {
    public static Module getModule(Class<? extends Module> clazz) {
        for (Module module : modules)
            if (module.getClass() == clazz)
                return module;
        throw new RuntimeException();
    }

    public static boolean isEnable(Class<? extends Module> clazz) {
        for (Module module : modules)
            if (module.getClass() == clazz)
                return module.isEnable();
        return false;
    }
}
