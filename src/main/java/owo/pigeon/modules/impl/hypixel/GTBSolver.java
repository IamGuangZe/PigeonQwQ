package owo.pigeon.modules.impl.hypixel;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.engio.mbassy.listener.Handler;
import net.minecraft.util.Identifier;
import owo.pigeon.event.events.MessageEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.ModeSetting;
import owo.pigeon.utils.ColorUtil;
import owo.pigeon.utils.chat.ChatUtil;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static owo.pigeon.Pigeon.mc;

public class GTBSolver extends Module {
    public GTBSolver() {
        super("GTBSolver", Category.HYPIXEL);
    }

    public enum LANGUAGE {
        EN, ZH_CN
    }

    public ModeSetting<LANGUAGE> language = setting("language", LANGUAGE.EN, v -> true);
    public EnableSetting secondaryDisplay = setting("secondary-display", false, v -> true);
    public ModeSetting<LANGUAGE> secondaryLanguage = setting("secondary-language", LANGUAGE.ZH_CN, v -> secondaryDisplay.getValue());

    private static class GTBWord {
        String enName, zh_cnName;
        String[] enParts, zh_cnParts;
        int[] enLengths, zh_cnLengths;

        GTBWord(String enName, String zh_cnName) {
            this.enName = enName;
            this.zh_cnName = zh_cnName;

            this.enParts = enName.split(" ");
            this.enLengths = new int[enParts.length];
            for (int i = 0; i < enParts.length; i++) enLengths[i] = enParts[i].length();

            this.zh_cnParts = zh_cnName.split(" ");
            this.zh_cnLengths = new int[zh_cnParts.length];
            for (int i = 0; i < zh_cnParts.length; i++) zh_cnLengths[i] = zh_cnParts[i].length();
        }

        String getFormatted(LANGUAGE primary, boolean showSecondary, LANGUAGE secondary) {
            String pStr = (primary == LANGUAGE.EN) ? enName : zh_cnName;
            if (showSecondary) {
                String sStr = (secondary == LANGUAGE.EN) ? enName : zh_cnName;
                return String.format("%s(%s)", pStr, sStr);
            }
            return pStr;
        }
    }

    private String theme;
    private final List<GTBWord> wordDatabase = new ArrayList<>();

    @Override
    public void onEnable() {
        reload();
    }

    @Handler
    public void onMessageReceive(MessageEvent.ReceiveMessageEvent event) {
        if (wordDatabase.isEmpty() && mc.getResourceManager() != null) {
            loadWords();
        }

        String message = ColorUtil.removeColor(event.getMessage().getString());

        if (event.isOverlay()) {
            if (message.contains("The theme is ") || message.contains("主题是")) {
                String newTheme = message.replace("The theme is ", "")
                        .replace("主题是", "")
                        .trim();

                if (Objects.equals(theme, newTheme) || !newTheme.contains("_")) {
                    return;
                }

                theme = newTheme;
                List<String> guesses = guess();

                if (!guesses.isEmpty()) {
                    if (guesses.size() > 75) {
                        ChatUtil.sendMessage(this.name, "&aPossible Words: &6" + guesses.size() + " &7(Too many to display)");
                    } else {
                        String output = String.join("&r, &6", guesses);
                        ChatUtil.sendMessage(this.name, "&aPossible Words (" + guesses.size() + "): &6" + output);
                    }
                } else {
                    ChatUtil.sendMessage(this.name, "&cNo words match this theme.");
                }
            }
        } else {
            if (message.contains("Round:") || message.contains("回合：")) {
                reload();
            }
        }
    }

    private void reload() {
        theme = null;
    }

    private void loadWords() {
        wordDatabase.clear();
        try {
            Identifier resourcePath = Identifier.of("pigeonqwq", "data/build_battle_themes.json");
            var resourceOpt = mc.getResourceManager().getResource(resourcePath);

            if (resourceOpt.isEmpty()) return;

            try (InputStream is = resourceOpt.get().getInputStream();
                 InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {

                JsonElement root = JsonParser.parseReader(reader);
                if (root != null && root.isJsonArray()) {
                    JsonArray array = root.getAsJsonArray();
                    for (JsonElement element : array) {
                        JsonObject obj = element.getAsJsonObject();
                        if (obj.has("en") && obj.has("zh_cn")) {
                            wordDatabase.add(new GTBWord(
                                    obj.get("en").getAsString(),
                                    obj.get("zh_cn").getAsString()
                            ));
                        }
                    }
                    ChatUtil.sendDebugMessage(this.name, "&aLoaded " + wordDatabase.size() + " words.");
                }
            }
        } catch (Exception ignored) {}
    }

    private List<String> guess() {
        List<String> result = new ArrayList<>();
        if (theme == null || theme.isEmpty()) return result;

        String[] themeParts = theme.split(" ");
        int[] themeLengths = new int[themeParts.length];
        for (int i = 0; i < themeParts.length; i++) {
            themeLengths[i] = themeParts[i].length();
        }

        for (GTBWord data : wordDatabase) {
            if (check(data.enParts, data.enLengths, themeParts, themeLengths) ||
                    check(data.zh_cnParts, data.zh_cnLengths, themeParts, themeLengths)) {
                result.add(data.getFormatted(language.getValue(), secondaryDisplay.getValue(), secondaryLanguage.getValue()));
            }
        }
        return result;
    }

    private boolean check(String[] wParts, int[] wLens, String[] tParts, int[] tLens) {
        if (wParts.length != tParts.length) return false;
        for (int i = 0; i < wParts.length; i++) {
            if (wLens[i] != tLens[i]) return false;
        }
        for (int i = 0; i < wParts.length; i++) {
            String wp = wParts[i].toLowerCase();
            String tp = tParts[i].toLowerCase();
            for (int j = 0; j < tp.length(); j++) {
                char tc = tp.charAt(j);
                if (tc != '_' && tc != wp.charAt(j)) return false;
            }
        }
        return true;
    }
}