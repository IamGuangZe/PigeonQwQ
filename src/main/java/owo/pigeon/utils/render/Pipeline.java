package owo.pigeon.utils.render;

import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;

import static net.minecraft.client.renderer.RenderPipelines.DEBUG_FILLED_SNIPPET;
import static net.minecraft.client.renderer.RenderPipelines.LINES_SNIPPET;

class Pipeline {
    static final RenderPipeline GLOBAL_QUADS_PIPELINE = RenderPipeline.builder(DEBUG_FILLED_SNIPPET)
            .withLocation("pipeline/global_fill_pipeline")
            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
            .build();

    static final RenderPipeline GLOBAL_LINES_PIPELINE = RenderPipeline.builder(LINES_SNIPPET)
            .withLocation("pipeline/global_lines_pipeline")
            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
            .build();
}
