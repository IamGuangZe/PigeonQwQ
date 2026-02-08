package owo.pigeon.utils;

import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static owo.pigeon.modules.ModuleManager.modules;

public class ModuleUtil {
    public static <T extends Module> T getModule(Class<T> clazz) {
        for (Module module : modules) {
            if (clazz.isInstance(module)) {
                return clazz.cast(module);
            }
        }
        return null;
    }

    public static Module getModule(String moduleName) {
        for (Module module : modules) {
            if (module.name.equalsIgnoreCase(moduleName)) {
                return module;
            }
        }
        return null;
    }

    public static boolean isEnable(Class<? extends Module> clazz) {
        Module module = getModule(clazz);
        return module != null && module.isEnable();
    }

    public static boolean isModuleExist(String moduleName) {
        return getModule(moduleName) != null;
    }

    public static void enableModule(Class<? extends Module> clazz) {
        Objects.requireNonNull(getModule(clazz)).enable();
    }

    public static void enableModule(String moduleName) {
        Objects.requireNonNull(getModule(moduleName)).enable();
    }

    public static void disableModule(Class<? extends Module> clazz) {
        Objects.requireNonNull(getModule(clazz)).disable();
    }

    public static void disableModule(String moduleName) {
        Objects.requireNonNull(getModule(moduleName)).disable();
    }

    public static void toggleModule(Class<? extends Module> clazz) {
        Objects.requireNonNull(getModule(clazz)).toggle();
    }

    public static void toggleModule(String moduleName) {
        Objects.requireNonNull(getModule(moduleName)).toggle();
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
