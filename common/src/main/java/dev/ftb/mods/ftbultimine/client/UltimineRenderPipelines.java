package dev.ftb.mods.ftbultimine.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.*;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;

public class UltimineRenderPipelines {
    // Note: all snippets are currently private in RenderPipelines, so copying them here for now at least
    // GLOBAL_SNIPPET, MATRICES_PROJECTION_SNIPPET, MATRICES_FOG_SNIPPET
    public static final RenderPipeline LINES_NO_DEPTH_TRANSLUCENT = RenderPipeline.builder()
            .withLocation("pipeline/lines_translucent")
            .withVertexShader("core/rendertype_lines")
            .withFragmentShader("core/rendertype_lines")
//            .withUniform("Globals", UniformType.UNIFORM_BUFFER)
//            .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
//            .withUniform("Projection", UniformType.UNIFORM_BUFFER)
//            .withUniform("Fog", UniformType.UNIFORM_BUFFER)
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
//            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH, RenderPipelines.LINES)
            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
            .withCull(false)
            .build();
}
