package owo.pigeon.event.events;

import net.minecraft.client.gui.DrawContext;
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
        public Render3DEvent() {
            super();
        }
    }

}
