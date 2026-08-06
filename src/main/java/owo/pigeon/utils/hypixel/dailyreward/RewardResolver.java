package owo.pigeon.utils.hypixel.dailyreward;

import java.util.List;

public class RewardResolver {

    private final List<String> resolutionFailures;

    public RewardResolver(List<String> resolutionFailures) {
        this.resolutionFailures = resolutionFailures;
    }

    public String resolveRewardName(RewardData rd) {
        String reward = rd.reward;
        if (reward == null) {
            return "";
        }

        // housing_package: 卡面主名 = housing.{category} (canvas 渲染层逻辑)
        if ("housing_package".equals(reward)) {
            String category = DailyRewardI18n.housingCategory(rd.packageValue);
            if (category != null) {
                String name = DailyRewardI18n.t("housing." + category);
                if (name != null) {
                    return name;
                }
            }
            String missingKey = "housing." + (category != null ? category : rd.packageValue);
            reportResolutionFailure(rd, missingKey);
            return "&c<" + missingKey + ">";
        }

        // coins/tokens: canvas 渲染层逻辑, gameType 在 usesTokens 中时显示 type.tokens
        String key = "type." + reward;
        if ("coins".equals(reward) || "tokens".equals(reward)) {
            if (DailyRewardI18n.usesTokens(rd.gameType)) {
                key = "type.tokens";
            }
        }
        String name = DailyRewardI18n.t(key);
        if (name == null) {
            reportResolutionFailure(rd, key);
            return "&c<" + key + ">";
        }

        if (name.contains("{$game}")) {
            String filled = resolveTemplate(rd, name);
            if (filled == null) {
                return "&c<gameType." + (rd.gameType != null ? rd.gameType : "null") + ">";
            }
            name = filled;
        }
        return name;
    }

    public String resolveDescription(RewardData rd) {
        String reward = rd.reward;
        if (reward == null) {
            return null;
        }

        // tokens 或 usesTokens 游戏: type.tokens.description
        if ("tokens".equals(reward) || DailyRewardI18n.usesTokens(rd.gameType)) {
            return resolveDescriptionKey(rd, "type.tokens.description");
        }
        // coins: type.coins.description
        if ("coins".equals(reward)) {
            return resolveDescriptionKey(rd, "type.coins.description");
        }
        // add_vanity: 按 key 分支
        if (rd.keyValue != null && !rd.keyValue.isEmpty()) {
            if (rd.keyValue.contains("suit")) {
                return DailyRewardI18n.t("vanity.suits.description");
            }
            if (rd.keyValue.contains("emote")) {
                return DailyRewardI18n.t("vanity.emotes.description");
            }
            if (rd.keyValue.contains("taunt")) {
                return DailyRewardI18n.t("vanity.gestures.description");
            }
            if (rd.keyValue.contains("housing")) {
                return DailyRewardI18n.t("housing.description");
            }
        }
        return resolveDescriptionKey(rd, "type." + reward + ".description");
    }

    public String resolvePackageName(RewardData rd) {
        String suffix = DailyRewardI18n.housingSkullSuffix(rd.packageValue);
        if (suffix != null) {
            String name = DailyRewardI18n.t("housing.skull." + suffix);
            if (name != null) {
                return name;
            }
        }
        String missingKey = "housing.skull." + (suffix != null ? suffix : rd.packageValue);
        reportResolutionFailure(rd, missingKey);
        return "&c<" + missingKey + ">";
    }

    public void reportResolutionFailure(RewardData rd, String missingKey) {
        resolutionFailures.add("&ci18n key missing: &e" + missingKey + " &7(reward=&f" + rd.reward + "&7, gameType=&f" + rd.gameType + "&7)\n&7raw JSON: &f" + rd.rawJson);
    }

    private String resolveDescriptionKey(RewardData rd, String key) {
        String description = DailyRewardI18n.t(key);
        if (description == null) {
            reportResolutionFailure(rd, key);
            return null;
        }
        if (description.contains("{$game}")) {
            return resolveTemplate(rd, description);
        }
        return description;
    }

    private String resolveTemplate(RewardData rd, String text) {
        String gameName = DailyRewardI18n.gameTypeName(rd.gameType);
        if (gameName != null) {
            return text.replace("{$game}", gameName);
        }
        String gameKey = "gameType." + (rd.gameType != null ? rd.gameType : "null");
        reportResolutionFailure(rd, gameKey);
        return null;
    }
}
