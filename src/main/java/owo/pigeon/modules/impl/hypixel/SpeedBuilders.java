package owo.pigeon.modules.impl.hypixel;

import com.mojang.blaze3d.vertex.PoseStack;
import net.engio.mbassy.listener.Handler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import owo.pigeon.Pigeon;
import owo.pigeon.event.events.*;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.IntSetting;
import owo.pigeon.utils.ColorUtil;
import owo.pigeon.utils.ItemUtil;
import owo.pigeon.utils.ScoreBoardUtil;
import owo.pigeon.utils.WorldUtil;
import owo.pigeon.utils.chat.ChatUtil;
import owo.pigeon.utils.hypixel.HypixelStateCache;
import owo.pigeon.utils.hypixel.HypixelUtil;
import owo.pigeon.utils.player.PlayerUtil;
import owo.pigeon.utils.render.FontUtil;
import owo.pigeon.utils.render.RenderUtil;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static owo.pigeon.Pigeon.mc;

// Reference: https://github.com/Kopamed/Raven-BPLUS/blob/master/src/main/java/keystrokesmod/module/impl/minigames/SpeedBuilders.java

public class SpeedBuilders extends Module {
    public SpeedBuilders() {
        super("SpeedBuilders", Category.HYPIXEL);
    }

    public enum GamePhase {
        INACTIVE, SHOWING, STARTING, BUILDING, JUDGING
    }

    public EnableSetting antiMiss = setting("anti-miss", false, v -> true);
    public EnableSetting autoSwap = setting("auto-swap", true, v -> true);
    public EnableSetting hoverPlace = setting("hover-place", true, v -> true);
    public IntSetting placeDelay = setting("place-delay", 1, 0, 20, "tick", v -> hoverPlace.getValue());
    public EnableSetting renderBlocks = setting("render-blocks", true, v -> true);
    public EnableSetting renderOnlyPlaceable = setting("render-only-placeable", false, v -> renderBlocks.getValue());

    private static final List<BlockPos> PLATFORM_POSITIONS = List.of(
            new BlockPos(45, 71, -18),
            new BlockPos(-16, 71, 45),
            new BlockPos(18, 71, 45),
            new BlockPos(45, 71, 16),
            new BlockPos(-18, 71, -45),
            new BlockPos(-45, 71, -16),
            new BlockPos(-45, 71, 18),
            new BlockPos(16, 71, -45)
    );

    private final Map<BlockPos, BuildBlockInfo> buildInfo = new ConcurrentHashMap<>();
    private BlockPos platformCenter;
    private boolean doneCollecting;
    private int lastPlaceTick;
    private boolean eliminated;
    private String lastMessage = "";
    private boolean awaitingPlatform;

    @Override
    public void onDisable() {
        lastPlaceTick = 0;
    }

    @Handler
    public void onTickPre(ClientTickEvent.Pre event) {
        if (WorldUtil.nullCheck()) return;
        if (awaitingPlatform && platformCenter == null) {
            if (Math.abs(mc.player.getX()) <= 200 && Math.abs(mc.player.getZ()) <= 200) {
                platformCenter = findCenter(mc.player.position());
                if (platformCenter != null) {
                    awaitingPlatform = false;
                    ChatUtil.sendDebugMessage(this.name, "platform found: " + platformCenter);
                }
            }
        }
        GamePhase phase = getGameStatus();
        if (phase == GamePhase.INACTIVE || platformCenter == null) {
            return;
        }
        if (phase == GamePhase.SHOWING) {
            doneCollecting = true;
        }
        if (phase == GamePhase.STARTING && !doneCollecting) {
            collectBuildInfo();
        }
        if (phase == GamePhase.BUILDING) {
            doneCollecting = false;
            updatePlacedStatus();
            autoPlace();
        }
    }

    @Handler
    public void onRender3D(RenderEvent.Render3DEvent event) {
        if (WorldUtil.nullCheck() || getGameStatus() != GamePhase.BUILDING || !renderBlocks.getValue()) return;
        PoseStack stack = event.getMatrix();
        for (Map.Entry<BlockPos, BuildBlockInfo> entry : buildInfo.entrySet()) {
            BuildBlockInfo info = entry.getValue();
            if (info.isPlaced) continue;
            if (!ItemUtil.holdingBlockState(info.requiredState)) continue;
            BlockPos pos = entry.getKey();
            boolean placeable = hasAdjacentBlock(pos);
            if (renderOnlyPlaceable.getValue() && !placeable) continue;
            RenderUtil.renderBlockModel(stack, info.requiredState, pos, placeable ? 0.45F : 0.15F);
            RenderUtil.drawBox(stack, pos, placeable ? new Color(0xFF1FFF16, true) : new Color(0xFFB8FFB7, true), 1.0);
        }
    }

    @Handler
    public void onRender2D(RenderEvent.Render2DEvent event) {
        if (!Pigeon.isDebug()) return;
        int placed = 0;
        for (BuildBlockInfo info : buildInfo.values()) {
            if (info.isPlaced) placed++;
        }
        String sidebarTitle = ScoreBoardUtil.getSidebarTitle();
        List<String> lines = List.of(
                "Speed Builders - Status: " + getGameStatus(),
                "Platform: " + (platformCenter == null
                        ? "not detected"
                        : platformCenter.getX() + ", " + platformCenter.getY() + ", " + platformCenter.getZ()),
                "Blocks: " + placed + "/" + buildInfo.size(),
                "Game: " + HypixelStateCache.currentGame,
                "Sidebar: " + (sidebarTitle == null ? "null" : ColorUtil.removeColor(sidebarTitle)),
                "SidebarLines: " + ScoreBoardUtil.getSidebarLines().size(),
                "LastMsg: " + lastMessage
        );
        FontUtil.drawStringList(event.getContext(), lines, 5, 5);
    }

    @Handler
    public void onReceiveMessage(MessageEvent.ReceiveMessageEvent event) {
        if (event.isOverlay()) return;
        String stripped = ColorUtil.removeColor(event.getMessage().getString());
        if (stripped.isEmpty()) return;
        lastMessage = stripped.length() > 60 ? stripped.substring(0, 60) : stripped;
        if (stripped.contains("Perfectly recreate the build you are shown each")) {
            ChatUtil.sendDebugMessage(this.name, "got start message, player pos: " + mc.player.position());
            awaitingPlatform = true;
            return;
        }
        String playerName = mc.player.getName().getString();
        if (stripped.startsWith(playerName) && stripped.contains(" got a perfect build in ") && stripped.endsWith("s!")) {
            buildInfo.clear();
            doneCollecting = false;
            return;
        }
        if (stripped.startsWith("You were eliminated because your build was the least accurate!")) {
            eliminated = true;
        }
    }

    @Handler
    public void onWorldChange(WorldChangeEvent event) {
        buildInfo.clear();
        platformCenter = null;
        doneCollecting = false;
        eliminated = false;
        lastPlaceTick = 0;
        awaitingPlatform = false;
    }

    @Handler
    public void onStartUseItemPre(StartUseItemEvent.Pre event) {
        if (!antiMiss.getValue() || WorldUtil.nullCheck() || getGameStatus() != GamePhase.BUILDING) return;
        BlockHitResult hit = getLookInfo();
        if (hit == null) return;
        BlockPos facePos = hit.getBlockPos().relative(hit.getDirection());
        BuildBlockInfo info = buildInfo.get(facePos);
        if (info == null || !ItemUtil.holdingBlockState(info.requiredState) || !correctPlaceState(info.requiredState, hit)) {
            event.setCancelled(true);
        }
    }

    private void autoPlace() {
        if (eliminated) return;
        BlockHitResult hit = getLookInfo();
        if (hit == null) return;
        BlockPos facePos = hit.getBlockPos().relative(hit.getDirection());
        BuildBlockInfo info = buildInfo.get(facePos);
        if (info == null || info.isPlaced) return;
        if (autoSwap.getValue()) {
            int slot = ItemUtil.getSlotFromBlockState(info.requiredState);
            if (slot != -1 && slot != mc.player.getInventory().getSelectedSlot()) {
                PlayerUtil.switchItemSlot(slot);
            }
        }
        if (!hoverPlace.getValue()) return;
        if (!ItemUtil.holdingBlockState(info.requiredState)) return;
        if (!correctPlaceState(info.requiredState, hit)) return;
        if (lastPlaceTick++ < placeDelay.getValue()) return;
        lastPlaceTick = 0;
        PlayerUtil.rightClick(PlayerUtil.RightClickMode.MOUSE);
    }

    private boolean correctPlaceState(BlockState requiredState, BlockHitResult hit) {
        if (requiredState.getBlock() instanceof LeavesBlock
                || requiredState.getBlock() instanceof ButtonBlock
                || requiredState.getBlock() instanceof StairBlock) return true;
        ItemStack held = mc.player.getMainHandItem();
        if (held.isEmpty() || !(held.getItem() instanceof BlockItem blockItem)) return false;
        BlockPlaceContext context = new BlockPlaceContext(mc.player, InteractionHand.MAIN_HAND, held, hit);
        BlockState simulatedState = blockItem.getBlock().getStateForPlacement(context);
        if (simulatedState == null) return false;
        if (simulatedState.getBlock() != requiredState.getBlock()) return false;
        if (simulatedState.hasProperty(BlockStateProperties.FACING) && requiredState.hasProperty(BlockStateProperties.FACING)) {
            if (simulatedState.getValue(BlockStateProperties.FACING) != requiredState.getValue(BlockStateProperties.FACING))
                return false;
        }
        return true;
    }

    private void collectBuildInfo() {
        buildInfo.clear();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = platformCenter.getX() - 3; x <= platformCenter.getX() + 3; x++) {
            for (int z = platformCenter.getZ() - 3; z <= platformCenter.getZ() + 3; z++) {
                for (int y = platformCenter.getY() + 1; y <= platformCenter.getY() + 31; y++) {
                    pos.set(x, y, z);
                    BlockState state = mc.level.getBlockState(pos);
                    if (state.isAir()) continue;
                    buildInfo.put(pos.immutable(), new BuildBlockInfo(state));
                }
            }
        }
    }

    private void updatePlacedStatus() {
        for (Map.Entry<BlockPos, BuildBlockInfo> entry : buildInfo.entrySet()) {
            BlockState currentState = mc.level.getBlockState(entry.getKey());
            BuildBlockInfo info = entry.getValue();
            info.isPlaced = !currentState.isAir() && currentState.is(info.requiredState.getBlock());
        }
    }

    private boolean hasAdjacentBlock(BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (!mc.level.getBlockState(pos.relative(direction)).isAir()) return true;
        }
        return false;
    }

    private BlockHitResult getLookInfo() {
        if (mc.hitResult == null || mc.hitResult.getType() != HitResult.Type.BLOCK) return null;
        return (BlockHitResult) mc.hitResult;
    }

    private BlockPos findCenter(Vec3 position) {
        BlockPos closestPos = null;
        double closestDistSq = Double.MAX_VALUE;
        double maxDistance = 30.0;
        double maxDistSq = maxDistance * maxDistance;
        for (BlockPos pos : PLATFORM_POSITIONS) {
            double dx = pos.getX() - position.x;
            double dy = pos.getY() - position.y;
            double dz = pos.getZ() - position.z;
            double distSq = dx * dx + dy * dy + dz * dz;
            if (distSq <= maxDistSq && distSq < closestDistSq) {
                closestDistSq = distSq;
                closestPos = pos;
            }
        }
        return closestPos;
    }

    private GamePhase getGameStatus() {
        if (!HypixelUtil.isInGame(HypixelUtil.Game.BUILDBATTLE)) return GamePhase.INACTIVE;
        List<String> sidebar = ScoreBoardUtil.getSidebarLines();
        if (sidebar == null || sidebar.isEmpty()) return GamePhase.INACTIVE;
        String prevLine = null;
        int index = -1;
        for (String rawLine : sidebar) {
            index++;
            String line = ColorUtil.removeColor(rawLine);
            if (prevLine != null && prevLine.startsWith("Round:")) {
                if (line.startsWith("Starts In: 00:03")
                        && index + 2 < sidebar.size()
                        && ColorUtil.removeColor(sidebar.get(index + 2)).startsWith("Theme:")) {
                    return GamePhase.SHOWING;
                }
                if (line.startsWith("Starts In:")) {
                    return GamePhase.STARTING;
                }
                if (line.startsWith("Time Left:")) {
                    return GamePhase.BUILDING;
                }
                if (line.startsWith("Judging:")) {
                    return GamePhase.JUDGING;
                }
            }
            prevLine = line;
        }
        return GamePhase.INACTIVE;
    }

    private static class BuildBlockInfo {
        public final BlockState requiredState;
        public boolean isPlaced;

        public BuildBlockInfo(BlockState state) {
            this.requiredState = state;
            this.isPlaced = false;
        }
    }
}
