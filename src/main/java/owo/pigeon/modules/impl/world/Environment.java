package owo.pigeon.modules.impl.world;

import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.IntSetting;
import owo.pigeon.settings.ModeSetting;

public class Environment extends Module {
    public Environment() {
        super("Environment", Category.WORLD);
    }

    public enum WeatherMode {
        NONE, CLEAR, RAIN
    }

    public enum TimeMode {
        NONE, PRESET, CUSTOM
    }

    public enum TimePreset {
        DAY(1000),
        NOON(6000),
        NIGHT(13000),
        MIDNIGHT(18000);

        private final int timeOfDay;

        TimePreset(int timeOfDay) {
            this.timeOfDay = timeOfDay;
        }

        public int getTimeOfDay() {
            return timeOfDay;
        }
    }

    public ModeSetting<WeatherMode> weatherMode = setting("weather-mode", WeatherMode.RAIN, v -> true);
    public EnableSetting forceSnow = setting("force-snow", true, v -> true);
    public ModeSetting<TimeMode> timeMode = setting("time-mode", TimeMode.PRESET, v -> true);
    public ModeSetting<TimePreset> timePreset = setting("time-preset", TimePreset.NIGHT, v -> timeMode.getValue() == TimeMode.PRESET);
    public IntSetting customTime = setting("custom-time", 13000, 0, 24000, v -> timeMode.getValue() == TimeMode.CUSTOM);

    public boolean shouldModifyWeather() {
        return weatherMode.getValue() != WeatherMode.NONE;
    }

    public boolean isRaining() {
        return weatherMode.getValue() == WeatherMode.RAIN;
    }

    public boolean shouldModifyTime() {
        return timeMode.getValue() != TimeMode.NONE;
    }

    public int getTimeOfDay() {
        return switch (timeMode.getValue()) {
            case PRESET -> timePreset.getValue().getTimeOfDay();
            case CUSTOM -> customTime.getValue();
            default -> -1;
        };
    }
}
