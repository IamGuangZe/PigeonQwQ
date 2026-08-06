package owo.pigeon.utils.hypixel.dailyreward;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.network.chat.*;
import owo.pigeon.utils.CommandUtil;
import owo.pigeon.utils.chat.ChatUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DailyRewardClaimer {

    // Reference: https://5ixsd.top/skydiao (HypixelRewardClaimer)

    private static DailyRewardClaimer current;

    public final String url;
    public final String rewardCode;
    private final boolean useXSDServer;
    private final boolean autoClaim;
    private final long claimDelay;

    public final List<RewardData> rewards = new ArrayList<>();
    private final List<String> resolutionFailures = new ArrayList<>();

    private final List<String> cookies = new ArrayList<>();
    private Thread connectThread;
    private Thread claimThread;

    public boolean hasData;
    public boolean claimed;
    private Integer target;
    private int highestLevel;
    private int highestLevelTarget;

    public static DailyRewardClaimer getCurrent() {
        return current;
    }

    public static DailyRewardClaimer get(String url, boolean useXSDServer, boolean autoClaim, long claimDelay) {
        return current = new DailyRewardClaimer(url, useXSDServer, autoClaim, claimDelay);
    }

    private DailyRewardClaimer(String url, boolean useXSDServer, boolean autoClaim, long claimDelay) {
        if (!url.contains("hypixel.net/claim-reward/")) {
            throw new RuntimeException("Not a valid Hypixel reward link");
        }
        this.url = url;
        this.rewardCode = RewardPageParser.extractRewardCode(url);
        this.useXSDServer = useXSDServer;
        this.autoClaim = autoClaim;
        this.claimDelay = claimDelay;
        createThread();
    }

    private void createThread() {
        hasData = true;
        connectThread = new Thread(this::doConnect1);
        connectThread.setName("Hyp Daily Reward - Connect");
        claimThread = new Thread(this::doClaim1);
        claimThread.setName("Hyp Daily Reward - Claim");
    }

    public void doConnect() {
        connectThread.start();
    }

    public void doClaim() {
        claimThread.start();
    }

    public void setTargetReward(int index) {
        if (index < 1 || index > 3) {
            throw new IllegalArgumentException("Invalid index: " + index + ", expected 1~3");
        }
        target = index - 1;
    }

    public void setHighestTargetReward() {
        target = highestLevelTarget;
    }

    public void displayRewards() {
        sendFoundSummary();
        RewardResolver resolver = new RewardResolver(resolutionFailures);
        for (int i = 0; i < rewards.size(); i++) {
            RewardData rd = rewards.get(i);
            StringBuilder sb = new StringBuilder();
            sb.append(rd.cacheMessage);
            String hoverText = "§6Click to claim\n" + rd.cacheMessage;
            String description = resolver.resolveDescription(rd);
            if (description != null) {
                hoverText = hoverText + "\n§7" + description;
            }
            if (i == highestLevelTarget) {
                sb.append("  §6★ Best");
            }

            MutableComponent line = Component.literal(sb.toString());
            line.setStyle(Style.EMPTY
                    .withClickEvent(new ClickEvent.SuggestCommand(CommandUtil.getCommandPrefix() + "dailyreward claim " + (i + 1)))
                    .withHoverEvent(new HoverEvent.ShowText(Component.literal(hoverText))));
            ChatUtil.sendMessage("DailyReward", line);
        }
    }

    private void sendFoundSummary() {
        String code = rewardCode != null ? rewardCode : "";
        if (autoClaim) {
            ChatUtil.sendMessage("DailyReward", "&eFound &6" + rewards.size() + " &erewards (code: &6" + code + "&e), claiming the best in &6" + claimDelay + "ms");
        } else {
            ChatUtil.sendMessage("DailyReward", "&eFound &6" + rewards.size() + " &erewards (code: &6" + code + "&e) (enable auto-claim to claim the best automatically)");
        }
    }

    private void doConnect1() {
        try {
            DailyRewardI18n.ensureLoaded();
            String result;
            JsonObject jo;
            String auth = null;

            if (useXSDServer) {
                if (rewardCode == null) {
                    throw new IOException("Failed to extract reward code from url");
                }
                result = request("https://xiaoshadiao.club/xsdwk/claimhypdailyreward?action=get&rewardcode=" + rewardCode, "GET", null, null);
                jo = JsonParser.parseString(result).getAsJsonObject();
                auth = jo.get("auth").getAsString();
                jo = jo.getAsJsonObject("rawdata");
            } else {
                result = request(url, "GET", null, cookies);
                int appDataIndex = result.indexOf("window.appData = '");
                if (appDataIndex == -1) {
                    throw new IOException("Failed to parse reward data from page");
                }
                String appData = RewardPageParser.extractJsQuotedString(result, appDataIndex + "window.appData = '".length(), '\'');
                jo = JsonParser.parseString(appData).getAsJsonObject();
            }

            if (jo.has("error")) {
                ChatUtil.sendMessage("DailyReward", "&cReward link expired or invalid");
                return;
            }

            String securityToken = null;
            if (!useXSDServer) {
                int tokenIndex = result.indexOf("window.securityToken = \"");
                if (tokenIndex != -1) {
                    securityToken = RewardPageParser.extractJsQuotedString(result, tokenIndex + "window.securityToken = \"".length(), '"');
                }
            }

            parseRewards(jo, auth, securityToken);
            displayRewards();

            if (autoClaim) {
                target = highestLevelTarget;
                try {
                    Thread.sleep(claimDelay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                doClaim();
            }
        } catch (Throwable e) {
            createThread();
            ChatUtil.sendMessage("DailyReward", "&cFailed to fetch data: " + e);
        }
    }

    private void parseRewards(JsonObject jo, String auth, String securityToken) {
        rewards.clear();
        resolutionFailures.clear();
        RewardResolver resolver = new RewardResolver(resolutionFailures);
        highestLevel = 0;
        int index = 0;
        for (JsonElement element : jo.get("rewards").getAsJsonArray()) {
            JsonObject reward = element.getAsJsonObject();
            RewardData rd = RewardData.from(reward, auth, securityToken, jo.get("activeAd").getAsInt(), jo.get("id").getAsString(), element.toString());

            String originalRarity = rd.rarity;
            int level = rd.mapRarity();
            ChatUtil.sendDebugMessage("DailyReward", "Reward #" + index + " original: rarity=" + originalRarity + ", gameType=" + rd.gameType + ", reward=" + rd.reward + ", amount=" + rd.amount + ", raw=" + element);
            if (level > highestLevel) {
                highestLevel = level;
                highestLevelTarget = index;
            }

            String rewardName;
            if (rd.keyValue != null && !rd.keyValue.isEmpty()) {
                String vanityKey = rd.keyValue;
                if (rd.keyValue.contains("suit")) {
                    vanityKey = rd.keyValue.replaceFirst("_([a-z]+)$", "");
                }
                String vanityName = DailyRewardI18n.t("vanity." + vanityKey);
                if (vanityName != null) {
                    rewardName = vanityName;
                } else {
                    resolver.reportResolutionFailure(rd, "vanity." + rd.keyValue);
                    rewardName = "&c<vanity." + rd.keyValue + ">";
                }
            } else {
                rewardName = resolver.resolveRewardName(rd);
            }

            StringBuilder sb = new StringBuilder();
            sb.append(rd.rarity);
            sb.append(" ");
            sb.append(rewardName);
            if (rd.amount != 0) {
                sb.append(" [x");
                sb.append(rd.amount);
                sb.append("]");
            } else if (rd.packageValue != null && !rd.packageValue.isEmpty()) {
                sb.append(" [");
                sb.append(resolver.resolvePackageName(rd));
                sb.append("]");
            }
            rd.cacheMessage = sb.toString();

            rewards.add(rd);
            index++;
        }

        for (String failure : resolutionFailures) {
            for (String line : failure.split("\n")) {
                ChatUtil.sendMessage("DailyReward", line);
            }
        }
    }

    private void doClaim1() {
        try {
            if (target == null) {
                throw new IllegalStateException("No claim target set");
            }
            RewardData rd = rewards.get(target);

            if (useXSDServer) {
                request("https://xiaoshadiao.club/xsdwk/claimhypdailyreward?action=claim&rewardcode=" + rd.id + "&claimindex=" + target + "&auth=" + rd.auth, "GET", null, null);
            } else {
                request("https://rewards.hypixel.net/claim-reward/claim?option=" + target + "&id=" + rd.id + "&activeAd=" + rd.activeAd + "&_csrf=" + rd.securityToken + "&watchedFallback=true&skipped=0", "POST", cookiesToString(cookies), null);
            }

            claimed = true;
            ChatUtil.sendMessage("DailyReward", "&aClaimed: " + rd.cacheMessage);
        } catch (Throwable e) {
            ChatUtil.sendMessage("DailyReward", "&cFailed to claim: " + e);
        } finally {
            createThread();
        }
    }

    private String request(String urlStr, String method, String cookieHeader, List<String> cookieSink) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) URI.create(urlStr).toURL().openConnection();
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
        conn.setRequestMethod(method);
        if (cookieHeader != null) {
            conn.addRequestProperty("Cookie", cookieHeader);
        }

        try {
            if (conn.getResponseCode() != 200) {
                throw new IOException("HTTP " + conn.getResponseCode());
            }
            if (cookieSink != null) {
                for (Map.Entry<String, List<String>> entry : conn.getHeaderFields().entrySet()) {
                    if (entry.getKey() != null && entry.getKey().equalsIgnoreCase("set-cookie")) {
                        cookieSink.addAll(entry.getValue());
                    }
                }
            }
            try (InputStream is = conn.getInputStream()) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        } finally {
            conn.disconnect();
        }
    }

    private static String cookiesToString(List<String> cookies) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cookies.size(); i++) {
            if (i > 0) {
                sb.append("; ");
            }
            sb.append(cookies.get(i));
        }
        return sb.toString();
    }
}
