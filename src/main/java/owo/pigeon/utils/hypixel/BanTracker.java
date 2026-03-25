package owo.pigeon.utils.hypixel;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import owo.pigeon.modules.impl.hypixel.BannedStats;
import owo.pigeon.utils.ModuleUtil;
import owo.pigeon.utils.chat.ChatUtil;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BanTracker {
    private static final String API_URL = "https://api.plancke.io/hypixel/v1/punishmentStats";
    private final List<Long> staffHistory = new ArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public void start() {
        scheduler.scheduleAtFixedRate(this::fetchAndProcess, 0, 1, TimeUnit.MINUTES);
    }

    private void fetchAndProcess() {
        try {
            long currentTotal = fetchStaffTotal();
            synchronized (staffHistory) {
                if (currentTotal == -1) {
                    staffHistory.add(-1L);
                    ChatUtil.sendDebugMessage("BanTracker", "Failed to fetch API data. Added placeholder (-1).");
                } else {
                    staffHistory.add(currentTotal);

                    long diff = 0;
                    if (staffHistory.size() >= 2) {
                        long prev = staffHistory.get(staffHistory.size() - 2);
                        diff = (prev == -1) ? 0 : (currentTotal - prev);
                    }

                    handleMissingData();

                    if (diff != 0 && ModuleUtil.isEnable(BannedStats.class)) {
                        String personText = diff == 1 ? "person" : "people";
                        ChatUtil.sendMessage("BannedStats", "Staff has banned " + diff + " " + personText + " in last 1 minute");
                    }

                    ChatUtil.sendDebugMessage("BanTracker", "Fetched Staff Total: " + currentTotal + " (+" + diff + ")");
                }
            }
        } catch (Exception e) {
            synchronized (staffHistory) {
                staffHistory.add(-1L);
            }
            ChatUtil.sendDebugMessage("BanTracker", "Error in fetch thread: " + e.getMessage());
        }
    }

    private long fetchStaffTotal() throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");

        if (conn.getResponseCode() == 200) {
            JsonObject json = JsonParser.parseReader(new InputStreamReader(conn.getInputStream())).getAsJsonObject();
            if (json.has("success") && json.get("success").getAsBoolean()) {
                return json.getAsJsonObject("record").get("staff_total").getAsLong();
            }
        }
        return -1;
    }

    private void handleMissingData() {
        int lastIndex = staffHistory.size() - 1;
        if (lastIndex <= 0 || staffHistory.get(lastIndex) == -1) return;

        int prevValidIndex = -1;
        for (int i = lastIndex - 1; i >= 0; i--) {
            if (staffHistory.get(i) != -1) {
                prevValidIndex = i;
                break;
            }
        }

        if (prevValidIndex != -1 && (lastIndex - prevValidIndex) > 1) {
            long totalDiff = staffHistory.get(lastIndex) - staffHistory.get(prevValidIndex);
            int gaps = lastIndex - prevValidIndex;
            long averageInc = totalDiff / gaps;
            long remainder = totalDiff % gaps;

            for (int i = 1; i < gaps; i++) {
                long extra = (i <= remainder) ? 1 : 0;
                staffHistory.set(prevValidIndex + i, staffHistory.get(prevValidIndex + i - 1) + averageInc + extra);
            }
        } else if (prevValidIndex == -1) {
            for (int i = 0; i < lastIndex; i++) {
                if (staffHistory.get(i) == -1) staffHistory.set(i, staffHistory.get(lastIndex));
            }
        }
    }

    public long getBansInLast(int minutes) {
        synchronized (staffHistory) {
            if (staffHistory.size() < 2) return 0;
            int currentIndex = staffHistory.size() - 1;
            int pastIndex = Math.max(0, currentIndex - minutes);

            long current = staffHistory.get(currentIndex);
            long past = staffHistory.get(pastIndex);
            if (current == -1 || past == -1) return 0;

            return current - past;
        }
    }

    public int getTrackedMinutes() {
        synchronized (staffHistory) {
            return staffHistory.size();
        }
    }

    public long getLatestDiff() {
        return getBansInLast(1);
    }
}