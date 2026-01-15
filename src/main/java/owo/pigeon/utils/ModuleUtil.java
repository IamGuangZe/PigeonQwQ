package owo.pigeon.utils;

import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;

import java.util.ArrayList;
import java.util.List;

import static owo.pigeon.modules.ModuleManager.modules;

public class ModuleUtil {
    public static Module getModule(Class<? extends Module> clazz) {
        for (Module module : modules)
            if (module.getClass() == clazz)
                return module;
        throw new RuntimeException();
    }

    public static Module getModule(String moduleName) {
        for (Module module : modules) {
            if (module.name.equalsIgnoreCase(moduleName)) {
                return module;
            }
        }
        throw new RuntimeException();
    }

    public static boolean isEnable(Class<? extends Module> clazz) {
        for (Module module : modules)
            if (module.getClass() == clazz)
                return module.isEnable();
        return false;
    }

    public static boolean isModuleExist(String moduleName) {
        for (Module module : modules) {
            if (module.name.equalsIgnoreCase(moduleName)) {
                return true;
            }
        }
        return false;
    }

    public static void enableModule(Class<? extends Module> clazz) {
        getModule(clazz).enable();
    }

    public static void disableModule(Class<? extends Module> clazz) {
        getModule(clazz).disable();
    }

    public static List<Module> getAllModule() {
        return modules;
    }

    public static List<Module> getAllModule(Category category) {
        List<Module> matching = new ArrayList<>();
        for (Module module : modules) {
            if (module.category == category) {
                matching.add(module);
            }
        }
        return matching;
    }
}
