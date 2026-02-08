package owo.pigeon.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

import static owo.pigeon.Pigeon.mc;

public class WorldUtil {
    public static boolean nullCheck() {
        return mc.world == null || mc.player == null;
    }

    public static void setBlock(BlockPos pos, BlockState state) {
        mc.world.setBlockState(pos,state,Block.NOTIFY_ALL);
    }

    public static void setBlock(BlockPos pos, Block block) {
        setBlock(pos,block.getDefaultState());
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

        BlockPos.Mutable pos = new BlockPos.Mutable();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    pos.set(x, y, z);
                    mc.world.setBlockState(pos, state, Block.NOTIFY_ALL);
                }
            }
        }
    }

    public static void fillBlock(BlockPos start, BlockPos end, Block block) {
        fillBlock(start, end, block.getDefaultState());
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
