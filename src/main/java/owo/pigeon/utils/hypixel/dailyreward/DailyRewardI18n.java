package owo.pigeon.utils.hypixel.dailyreward;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import owo.pigeon.utils.chat.ChatUtil;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static owo.pigeon.Pigeon.mc;

public final class DailyRewardI18n {

    private static Map<String, String> I18N_MAP = Map.of();
    private static Map<String, String> GAME_TYPE_MAP = Map.of();
    private static Set<String> USES_TOKENS_SET = Set.of();
    private static Map<String, List<String>> HOUSING_CATEGORIES = Map.of();
    private static List<String> HOUSING_SKULL_SUFFIXES = List.of();
    private static boolean translationsLoaded = false;

    private DailyRewardI18n() {
    }

    public static void ensureLoaded() {
        if (translationsLoaded) {
            return;
        }
        translationsLoaded = true;
        try {
            Identifier resourcePath = Identifier.fromNamespaceAndPath("pigeonqwq", "data/dailyreward.json");
            Optional<Resource> resourceOpt = mc.getResourceManager().getResource(resourcePath);
            if (resourceOpt.isEmpty()) {
                ChatUtil.sendMessage("DailyReward", "&cFailed to load dailyreward.json: resource not found");
                return;
            }

            try (InputStream is = resourceOpt.get().open();
                 InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {

                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

                Map<String, String> i18n = new HashMap<>();
                for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject("i18n").entrySet()) {
                    i18n.put(entry.getKey(), entry.getValue().getAsString());
                }

                Map<String, String> gameType = new HashMap<>();
                for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject("gameType").entrySet()) {
                    gameType.put(entry.getKey(), entry.getValue().getAsString());
                }

                Set<String> usesTokens = new HashSet<>();
                for (JsonElement element : root.getAsJsonArray("usesTokens")) {
                    usesTokens.add(element.getAsString());
                }

                Map<String, List<String>> housingCategories = new HashMap<>();
                for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject("housingCategories").entrySet()) {
                    List<String> packages = new ArrayList<>();
                    for (JsonElement element : entry.getValue().getAsJsonArray()) {
                        packages.add(element.getAsString());
                    }
                    housingCategories.put(entry.getKey(), packages);
                }

                List<String> skullSuffixes = new ArrayList<>();
                for (String key : i18n.keySet()) {
                    if (key.startsWith("housing.skull.")) {
                        skullSuffixes.add(key.substring("housing.skull.".length()));
                    }
                }
                skullSuffixes.sort(Comparator.comparingInt(String::length).reversed());

                I18N_MAP = i18n;
                GAME_TYPE_MAP = gameType;
                USES_TOKENS_SET = usesTokens;
                HOUSING_CATEGORIES = housingCategories;
                HOUSING_SKULL_SUFFIXES = skullSuffixes;
            }
        } catch (Exception e) {
            ChatUtil.sendMessage("DailyReward", "&cFailed to load dailyreward.json: " + e.getMessage());
        }
    }

    public static String t(String key) {
        return I18N_MAP.get(key);
    }

    public static String gameTypeName(String gameType) {
        return GAME_TYPE_MAP.get(gameType);
    }

    public static boolean usesTokens(String gameType) {
        return gameType != null && USES_TOKENS_SET.contains(gameType);
    }

    public static String housingCategory(String packageValue) {
        if (packageValue == null) {
            return null;
        }
        for (Map.Entry<String, List<String>> entry : HOUSING_CATEGORIES.entrySet()) {
            if (entry.getValue().contains(packageValue)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static String housingSkullSuffix(String packageValue) {
        if (packageValue == null) {
            return null;
        }
        for (String suffix : HOUSING_SKULL_SUFFIXES) {
            if (packageValue.endsWith(suffix)) {
                return suffix;
            }
        }
        return null;
    }
}
