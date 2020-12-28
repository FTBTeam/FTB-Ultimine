package com.feed_the_beast.mods.ftbultimine.client;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import org.lwjgl.opengl.GL11;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import java.util.OptionalDouble;

/**
 * @author LatvianModder
 */
public class UltimineRenderTypes extends RenderStateShard
{
	public static final RenderType LINES_NORMAL = RenderType.create("ultimine_lines_normal", DefaultVertexFormat.POSITION_COLOR, GL11.GL_LINES, 256, RenderType.CompositeState.builder()
			.setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty()))
			.setLayeringState(NO_LAYERING)
			.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
			.setWriteMaskState(COLOR_WRITE)
			.setCullState(CULL)
			.createCompositeState(false));

	public static final RenderType LINES_TRANSPARENT = RenderType.create("ultimine_lines_transparent", DefaultVertexFormat.POSITION_COLOR, GL11.GL_LINES, 256, RenderType.CompositeState.builder()
			.setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty()))
			.setLayeringState(NO_LAYERING)
			.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
			.setWriteMaskState(COLOR_DEPTH_WRITE)
			.setCullState(CULL)
			.setDepthTestState(NO_DEPTH_TEST)
			.createCompositeState(false));

	private UltimineRenderTypes(String s, Runnable r0, Runnable r1)
	{
		super(s, r0, r1);
	}
}