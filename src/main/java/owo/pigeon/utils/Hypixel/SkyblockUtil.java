package owo.pigeon.utils.Hypixel;

import owo.pigeon.Pigeonqwq;
import owo.pigeon.utils.ColorUtil;
import owo.pigeon.utils.RegexUtil;
import owo.pigeon.utils.ScoreBoardUtil;

import java.util.List;
import java.util.Objects;

import static owo.pigeon.Pigeonqwq.mc;

public class SkyblockUtil {
    public enum Island {
        SinglePlayer("Singleplayer"),
        PrivateIsland("Private Island"),
        Garden("Garden"),
        SpiderDen("Spider's Den"),
        CrimsonIsle("Crimson Isle"),
        TheEnd("The End"),
        GoldMine("Gold Mine"),
        DeepCaverns("Deep Caverns"),
        DwarvenMines("Dwarven Mines"),
        CrystalHollows("Crystal Hollows"),
        FarmingIsland("The Farming Islands"),
        ThePark("The Park"),
        Dungeon("Catacombs"),
        DungeonHub("Dungeon Hub"),
        Hub("Hub"),
        DarkAuction("Dark Auction"),
        JerryWorkshop("Jerry's Workshop"),
        Kuudra("Kuudra"),
        Mineshaft("Mineshaft"),
        Rift("The Rift"),
        BackwaterBayou("Backwater Bayou"),
        Galatea("Galatea"),
        Unknown("(Unknown)");

        private final String displayName;

        Island(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum Floor {
        E("catacombs_floor_entrance",0),
        F1("catacombs_floor_one",1),
        F2("catacombs_floor_two",2),
        F3("catacombs_floor_three",3),
        F4("catacombs_floor_four",4),
        F5("catacombs_floor_five",5),
        F6("catacombs_floor_six",6),
        F7("catacombs_floor_seven",7),
        M1("master_catacombs_floor_one",1),
        M2("master_catacombs_floor_two",2),
        M3("master_catacombs_floor_three",3),
        M4("master_catacombs_floor_four",4),
        M5("master_catacombs_floor_five",5),
        M6("master_catacombs_floor_six",6),
        M7("master_catacombs_floor_seven",7),
        Unknown("",-1);

        private final String floorID;
        private final int floorNum;

        Floor(String floorID,int floorNum) {
            this.floorID = floorID;
            this.floorNum = floorNum;
        }

        public String getFloorID() {
            return floorID;
        }

        public int getFloorNum() {
            return floorNum;
        }
    }

    public static boolean isInSkyblock() {
        if (Pigeonqwq.isDebug() && mc.isInSingleplayer()) return true;
        return HypixelUtil.isInGame(HypixelUtil.Game.SKYBLOCK);
    }

    public static Island getIsland() {
        List<String> tabLines = ScoreBoardUtil.getTabLines();
        if (mc.isInSingleplayer()) return Island.SinglePlayer;
        if (!isInSkyblock() || tabLines.isEmpty()) return Island.Unknown;

        for (String line : tabLines) {
            if (line.startsWith("Area: ") || line.startsWith("Dungeon: ")) {
                for (Island island : Island.values()) {
                    if (line.toLowerCase().contains(island.getDisplayName().toLowerCase())) {
                        return island;
                    }
                }
                break;
            }
        }

        return Island.Unknown;
    }

    public static boolean isInIsland(Island island) {
        if (Pigeonqwq.isDebug() && mc.isInSingleplayer()) return true;
        return getIsland() == island;
    }

    public static Floor getFloor() {
        List<String> sidebarLines = ScoreBoardUtil.getSidebarLines();
        if (mc.isInSingleplayer() || sidebarLines.isEmpty()) return Floor.Unknown;

        return sidebarLines.stream()
                .filter(line -> line.contains("⏣"))
                .map(line -> RegexUtil.regexGetPart(" ⏣ The Catacombs \\((.*)\\)", ColorUtil.removeColor(line), 1))
                .filter(Objects::nonNull)
                .map(SkyblockUtil::getFloor)
                .findFirst()
                .orElse(Floor.Unknown);
    }

    public static Floor getFloor(String name) {
        try {
            return Floor.valueOf(name);
        } catch (IllegalArgumentException e) {
            return Floor.Unknown;
        }
    }

    public static boolean isInFloor(Floor floor) {
        if (Pigeonqwq.isDebug() && mc.isInSingleplayer()) return true;
        return getFloor() == floor;
    }

    public static boolean isInFloor(int floor) {
        if (Pigeonqwq.isDebug() && mc.isInSingleplayer()) return true;
        return getFloor().getFloorNum() == floor;
    }

    public static boolean isInBoss() {
        if (Pigeonqwq.isDebug() && mc.isInSingleplayer()) return true;
        return isInBoss(getFloor());
    }

    public static boolean isInBoss(Floor floor) {
        if (Pigeonqwq.isDebug() && mc.isInSingleplayer()) return true;
        return isInBoss(floor.getFloorNum());
    }

    public static boolean isInBoss(int floor) {
        if (Pigeonqwq.isDebug() && mc.isInSingleplayer()) return true;

        double x = mc.player.getX();
        double z = mc.player.getZ();

        if (floor <= 0) return false;
        if (floor == 1) return x > -71 && z > -39;
        if (floor <= 4) return x > -39 && z > -39;
        if (floor <= 6) return x > -39 && z > -7;
        if (floor == 7) return x > -7 && z > -7;

        return false;
    }

    public static int getFloor7Stage() {
        double y = mc.player.getY();
        if (y < 62) return 5;
        if (y < 107) return 4;
        if (y < 166) return 3;
        if (y < 219) return 2;
        return 1;
    }
}
