package owo.pigeon.event.events;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.block.state.BlockState;
import owo.pigeon.event.Event;

public class RenderEvent extends Event {
    public RenderEvent() {

    }

    public static class Render2DEvent extends RenderEvent {
        protected GuiGraphics context;

        public Render2DEvent(GuiGraphics context) {
            this.context = context;
        }

        public GuiGraphics getContext() {
            return context;
        }
    }

    public static class Render3DEvent extends RenderEvent {
        protected final PoseStack matrix;
        protected final float delta;

        public Render3DEvent(PoseStack matrix, float delta) {
            this.matrix = matrix;
            this.delta = delta;
        }

        public PoseStack getMatrix() {
            return matrix;
        }

        public float getDelta() {
            return delta;
        }
    }

    public static class RenderContainerEvent extends RenderEvent {
        private final GuiGraphics context;
        private final AbstractContainerScreen<?> screen;
        private final int mouseX;
        private final int mouseY;
        private final float delta;

        public RenderContainerEvent(AbstractContainerScreen<?> screen, GuiGraphics context, int mouseX, int mouseY, float delta) {
            this.screen = screen;
            this.context = context;
            this.mouseX = mouseX;
            this.mouseY = mouseY;
            this.delta = delta;
        }

        public AbstractContainerScreen<?> getScreen() {
            return screen;
        }

        public GuiGraphics getContext() {
            return context;
        }

        public int getMouseX() {
            return mouseX;
        }

        public int getMouseY() {
            return mouseY;
        }

        public float getDelta() {
            return delta;
        }

        public ChestMenu getContainer() {
            if (screen.getMenu() instanceof ChestMenu container) {
                return container;
            }
            return null;
        }
    }

    public static class RenderBlockEvent extends RenderEvent {
        private BlockPos pos;
        private BlockState state;

        public RenderBlockEvent(BlockPos pos, BlockState state) {
            this.pos = pos;
            this.state = state;
        }

        public RenderBlockEvent() {
            this(null, null);
        }

        public void set(BlockPos pos, BlockState state) {
            this.pos = pos;
            this.state = state;
        }

        public BlockPos getPos() {
            return pos;
        }

        public BlockState getState() {
            return state;
        }
    }
}
