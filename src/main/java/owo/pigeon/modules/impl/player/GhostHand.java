package owo.pigeon.modules.impl.player;

import net.minecraft.world.entity.player.Player;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.utils.player.PlayerUtil;

public class GhostHand extends Module {
    public GhostHand() {
        super("GhostHand", Category.PLAYER);
    }

    public EnableSetting players = setting("players", true, v -> true);
    // public EnableSetting blocks = setting("blocks", true, v -> true);
    // public EnableSetting beds = setting("beds", false, v -> blocks.getValue());
    // public EnableSetting chests = setting("chests", false, v -> blocks.getValue());
    // public EnableSetting dragonEgg = setting("dragon-egg", false, v -> blocks.getValue());


    public boolean shouldIgnore(Object input) {
        if (input instanceof Player player && players.getValue()) {
            return PlayerUtil.hasUUID(player);
        }

        // TODO: 完善 GhostHand 对方块的处理

        /*
        if (input instanceof Block block && blocks.getValue()) {
            if (block instanceof BedBlock) return beds.getValue();
            if (block == Blocks.CHEST || block == Blocks.ENDER_CHEST || block == Blocks.TRAPPED_CHEST) return chests.getValue();
            if (block == Blocks.DRAGON_EGG) return dragonEgg.getValue();
            return true;
        }
        */

        return false;
    }
}
