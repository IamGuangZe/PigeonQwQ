package owo.pigeon.event.events;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import owo.pigeon.event.Event;

public class RenderEvent extends Event {
    public RenderEvent() {

    }

    public static class Render2DEvent extends RenderEvent {
        protected DrawContext context;

        public Render2DEvent(DrawContext context) {
            this.context = context;
        }

        public DrawContext getContext() {
            return context;
        }
    }

    public static class Render3DEvent extends RenderEvent {
        protected final MatrixStack matrix;
        protected final float delta;

        public Render3DEvent(MatrixStack matrix, float delta) {
            this.matrix = matrix;
            this.delta = delta;
        }

        public MatrixStack getMatrix() {
            return matrix;
        }

        public float getDelta() {
            return delta;
        }
    }

    public static class RenderContainerEvent extends RenderEvent {
        private final DrawContext context;
        private final HandledScreen<?> screen;
        private final int mouseX;
        private final int mouseY;
        private final float delta;

        public RenderContainerEvent(HandledScreen<?> screen, DrawContext context, int mouseX, int mouseY, float delta) {
            this.screen = screen;
            this.context = context;
            this.mouseX = mouseX;
            this.mouseY = mouseY;
            this.delta = delta;
        }

        public HandledScreen<?> getScreen() {
            return screen;
        }

        public DrawContext getContext() {
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

        public GenericContainerScreenHandler getContainer() {
            if (screen.getScreenHandler() instanceof GenericContainerScreenHandler container) {
                return container;
            }
            return null;
        }
    }

}
