package dev.ftb.mods.ftbultimine.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;

import java.util.OptionalDouble;

/**
 * @author LatvianModder
 */
public class UltimineRenderTypes extends RenderType {
	public static final RenderType LINES_NORMAL = RenderType.create("ultimine_lines_normal", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.DEBUG_LINES, 256, false, false, CompositeState.builder()
			.setShaderState(new ShaderStateShard(GameRenderer::getPositionColorShader))
			.setLineState(new LineStateShard(OptionalDouble.empty()))
			.setLayeringState(NO_LAYERING)
			.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
			.setWriteMaskState(COLOR_WRITE)
			.setCullState(CULL)
			.createCompositeState(false));

	public static final RenderType LINES_TRANSPARENT = RenderType.create("ultimine_lines_transparent", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.DEBUG_LINES, 256, false, false, CompositeState.builder()
			.setShaderState(new ShaderStateShard(GameRenderer::getPositionColorShader))
			.setLineState(new LineStateShard(OptionalDouble.empty()))
			.setLayeringState(NO_LAYERING)
			.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
			.setWriteMaskState(COLOR_DEPTH_WRITE)
			.setCullState(CULL)
			.setDepthTestState(NO_DEPTH_TEST)
			.createCompositeState(false));

	public UltimineRenderTypes(String string, VertexFormat vertexFormat, VertexFormat.Mode mode, int i, boolean bl, boolean bl2, Runnable runnable, Runnable runnable2) {
		super(string, vertexFormat, mode, i, bl, bl2, runnable, runnable2);
	}
}