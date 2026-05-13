package owo.pigeon.utils.render;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.util.Util;

import java.util.function.Function;

import static owo.pigeon.utils.render.Pipeline.GLOBAL_LINES_PIPELINE;
import static owo.pigeon.utils.render.Pipeline.GLOBAL_QUADS_PIPELINE;


public class Layer {
    private static final RenderLayer GLOBAL_QUADS;
    private static final Function<Double, RenderLayer> GLOBAL_LINES;

    public static RenderLayer getGlobalLines(double width) {
        return GLOBAL_LINES.apply(width);
    }

    public static RenderLayer getGlobalQuads() {
        return GLOBAL_QUADS;
    }

    static {
        GLOBAL_QUADS = RenderLayer.of("global_fill",
                RenderSetup.builder(GLOBAL_QUADS_PIPELINE).build());

        GLOBAL_LINES = Util.memoize(l ->
                RenderLayer.of("global_lines",
                        RenderSetup.builder(GLOBAL_LINES_PIPELINE).build()));
    }
}
