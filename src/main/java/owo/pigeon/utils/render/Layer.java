package owo.pigeon.utils.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;

import static owo.pigeon.utils.render.Pipeline.GLOBAL_LINES_PIPELINE;
import static owo.pigeon.utils.render.Pipeline.GLOBAL_QUADS_PIPELINE;

public class Layer {
    public static RenderPipeline getGlobalLines() {
        return GLOBAL_LINES_PIPELINE;
    }

    public static RenderPipeline getGlobalQuads() {
        return GLOBAL_QUADS_PIPELINE;
    }
}
