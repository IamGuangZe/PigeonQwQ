package owo.pigeon.utils.hypixel.dailyreward;

import com.google.gson.JsonObject;

public class RewardData {
    public String gameType;
    public String rarity;
    public String reward;
    public String id;
    public String securityToken;
    public int activeAd;
    public int amount;
    public String packageValue;
    public String keyValue;
    public String rawJson;
    public String cacheMessage;
    public String auth;

    public static RewardData from(JsonObject reward, String auth, String securityToken, int activeAd, String id, String rawJson) {
        RewardData rd = new RewardData();
        rd.auth = auth;
        rd.securityToken = securityToken;
        rd.activeAd = activeAd;
        rd.id = id;
        rd.rawJson = rawJson;
        rd.amount = reward.has("amount") ? reward.get("amount").getAsInt() : 0;
        rd.gameType = reward.has("gameType") ? reward.get("gameType").getAsString() : null;
        rd.rarity = reward.has("rarity") ? reward.get("rarity").getAsString() : null;
        rd.reward = reward.has("reward") ? reward.get("reward").getAsString() : null;
        rd.packageValue = reward.has("package") ? reward.get("package").getAsString() : null;
        rd.keyValue = reward.has("key") ? reward.get("key").getAsString() : null;
        return rd;
    }

    public int mapRarity() {
        String rarity = this.rarity;
        if (rarity == null) {
            return 1;
        }
        switch (rarity) {
            case "COMMON":
                this.rarity = "§f" + capitalizeWords(rarity);
                return 1;
            case "UNCOMMON":
                this.rarity = "§a" + capitalizeWords(rarity);
                return 2;
            case "RARE":
                this.rarity = "§9" + capitalizeWords(rarity);
                return 3;
            case "EPIC":
                this.rarity = "§d" + capitalizeWords(rarity);
                return 4;
            case "LEGENDARY":
                this.rarity = "§6" + capitalizeWords(rarity);
                return 5;
            default:
                return 1;
        }
    }

    private static String capitalizeWords(String text) {
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ' ') {
                capitalizeNext = true;
                result.append(c);
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
        }
        return result.toString();
    }
}
