package owo.pigeon.modules.impl.hypixel;

import net.engio.mbassy.listener.Handler;
import owo.pigeon.event.events.MessageEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.IntSetting;
import owo.pigeon.utils.chat.ChatUtil;
import owo.pigeon.utils.hypixel.dailyreward.DailyRewardClaimer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DailyReward extends Module {

    // Reference: https://5ixsd.top/skydiao (HypixelRewardClaimer)

    public DailyReward() {
        super("DailyReward", Category.HYPIXEL);
    }

    public EnableSetting autoClaim = setting("auto-claim", false, v -> true);
    public IntSetting claimDelay = setting("claim-delay", 2000, 0, 10000, "ms", v -> true);
    public EnableSetting useXSDServer = setting("use-xsd-server", false, v -> true);

    private static final Pattern REWARD_URL_PATTERN = Pattern.compile("https?://(?:(?:www|rewards)\\.)?hypixel\\.net/claim-reward/[0-9a-fA-F]+");

    @Handler
    public void onReceiveMessage(MessageEvent.ReceiveMessageEvent event) {
        if (event.isOverlay()) return;

        String message = event.getMessage().getString();
        Matcher matcher = REWARD_URL_PATTERN.matcher(message);
        if (!matcher.find()) return;

        String url = matcher.group();
        if (!url.contains("hypixel.net/claim-reward/")) return;

        ChatUtil.sendMessage(this.name, "&eDetected daily reward link, fetching data...");
        DailyRewardClaimer.get(url, useXSDServer.getValue(), autoClaim.getValue(), claimDelay.getValue()).doConnect();
    }
}
