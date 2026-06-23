package owo.pigeon.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import static owo.pigeon.Pigeon.mc;

public class WorldUtil {
    public static boolean nullCheck() {
        return mc.level == null || mc.player == null;
    }

    public static void setBlock(BlockPos pos, BlockState state) {
        mc.level.setBlock(pos, state, Block.UPDATE_ALL);
    }

    public static void setBlock(BlockPos pos, Block block) {
        setBlock(pos, block.defaultBlockState());
    }

    public static void setBlock(int x, int y, int z, Block block) {
        setBlock(new BlockPos(x, y, z), block);
    }

    public static void fillBlock(BlockPos start, BlockPos end, BlockState state) {
        int minX = Math.min(start.getX(), end.getX());
        int minY = Math.min(start.getY(), end.getY());
        int minZ = Math.min(start.getZ(), end.getZ());
        int maxX = Math.max(start.getX(), end.getX());
        int maxY = Math.max(start.getY(), end.getY());
        int maxZ = Math.max(start.getZ(), end.getZ());

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    pos.set(x, y, z);
                    mc.level.setBlock(pos, state, Block.UPDATE_ALL);
                }
            }
        }
    }

    public static void fillBlock(BlockPos start, BlockPos end, Block block) {
        fillBlock(start, end, block.defaultBlockState());
    }

    public static Double parseCoordinate(String arg, double base) {
        try {
            if (arg.equals("~")) {
                return base;
            } else if (arg.startsWith("~")) {
                String offset = arg.substring(1);
                if (offset.isEmpty()) {
                    return base;
                } else {
                    return base + Double.parseDouble(offset);
                }
            } else {
                return Double.parseDouble(arg);
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
