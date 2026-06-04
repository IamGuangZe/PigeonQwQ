package owo.pigeon.commands.impl;

import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import owo.pigeon.commands.Command;
import owo.pigeon.utils.CommandUtil;
import owo.pigeon.utils.WorldUtil;

import static owo.pigeon.Pigeon.mc;

public class FillCommand extends Command {
    public FillCommand() {
        super("fill");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 7) {
            CommandUtil.sendCommandError(CommandUtil.ErrorReason.UnknownOrIncompleteCommand,
                    this.getCommand(),
                    args,
                    args.length
            );
            return;
        }

        Identifier id = Identifier.tryParse(args[6]);
        if (id == null || !Registries.BLOCK.containsId(id)) {
            CommandUtil.sendCommandError(
                    CommandUtil.ErrorReason.UnknownBlock,
                    getCommand(),
                    args,
                    6
            );
            return;
        }
        Block block = Registries.BLOCK.get(id);

        Double startX = WorldUtil.parseCoordinate(args[0], mc.player.getX());
        Double startY = WorldUtil.parseCoordinate(args[1], mc.player.getY());
        Double startZ = WorldUtil.parseCoordinate(args[2], mc.player.getZ());
        Double endX = WorldUtil.parseCoordinate(args[3], mc.player.getX());
        Double endY = WorldUtil.parseCoordinate(args[4], mc.player.getY());
        Double endZ = WorldUtil.parseCoordinate(args[5], mc.player.getZ());

        if (startX == null) {
            CommandUtil.sendCommandError(CommandUtil.ErrorReason.ExpectedInteger, getCommand(), args, 0);
            return;
        }
        if (startY == null) {
            CommandUtil.sendCommandError(CommandUtil.ErrorReason.ExpectedInteger, getCommand(), args, 1);
            return;
        }
        if (startZ == null) {
            CommandUtil.sendCommandError(CommandUtil.ErrorReason.ExpectedInteger, getCommand(), args, 2);
            return;
        }
        if (endX == null) {
            CommandUtil.sendCommandError(CommandUtil.ErrorReason.ExpectedInteger, getCommand(), args, 3);
            return;
        }
        if (endY == null) {
            CommandUtil.sendCommandError(CommandUtil.ErrorReason.ExpectedInteger, getCommand(), args, 4);
            return;
        }
        if (endZ == null) {
            CommandUtil.sendCommandError(CommandUtil.ErrorReason.ExpectedInteger, getCommand(), args, 5);
            return;
        }


        BlockPos startPos = BlockPos.ofFloored(startX, startY, startZ);
        BlockPos endPos = BlockPos.ofFloored(endX, endY, endZ);

        WorldUtil.fillBlock(startPos, endPos, block);
    }

    @Override
    public String getUsage() {
        return CommandUtil.getCommandPrefix() + "fill <from> <to> <block>";
    }
}
