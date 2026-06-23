package owo.pigeon.utils.render;

import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.Util;

import java.util.function.Function;

import static owo.pigeon.utils.render.Pipeline.GLOBAL_LINES_PIPELINE;
import static owo.pigeon.utils.render.Pipeline.GLOBAL_QUADS_PIPELINE;


public class Layer {
    private static final RenderType GLOBAL_QUADS;
    private static final Function<Double, RenderType> GLOBAL_LINES;

    public static RenderType getGlobalLines(double width) {
        return GLOBAL_LINES.apply(width);
    }

    public static RenderType getGlobalQuads() {
        return GLOBAL_QUADS;
    }

    static {
        GLOBAL_QUADS = RenderType.create("global_fill",
                RenderSetup.builder(GLOBAL_QUADS_PIPELINE).createRenderSetup());

        GLOBAL_LINES = Util.memoize(l ->
                RenderType.create("global_lines",
                        RenderSetup.builder(GLOBAL_LINES_PIPELINE).createRenderSetup()));
    }
}
