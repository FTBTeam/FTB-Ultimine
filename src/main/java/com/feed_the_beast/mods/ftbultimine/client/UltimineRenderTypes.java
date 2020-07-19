package com.feed_the_beast.mods.ftbultimine.client;

import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.util.OptionalDouble;

/**
 * @author LatvianModder
 */
public class UltimineRenderTypes extends RenderState
{
	public static final RenderType LINES_NORMAL = RenderType.makeType("ultimine_lines_normal", DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINES, 256, RenderType.State.getBuilder()
			.line(new RenderState.LineState(OptionalDouble.empty()))
			.layer(NO_LAYERING)
			.transparency(TRANSLUCENT_TRANSPARENCY)
			.writeMask(COLOR_WRITE)
			.cull(CULL_ENABLED)
			.build(false));

	public static final RenderType LINES_TRANSPARENT = RenderType.makeType("ultimine_lines_transparent", DefaultVertexFormats.POSITION_COLOR, GL11.GL_LINES, 256, RenderType.State.getBuilder()
			.line(new RenderState.LineState(OptionalDouble.empty()))
			.layer(NO_LAYERING)
			.transparency(TRANSLUCENT_TRANSPARENCY)
			.writeMask(COLOR_DEPTH_WRITE)
			.cull(CULL_ENABLED)
			.depthTest(DEPTH_ALWAYS)
			.build(false));

	private UltimineRenderTypes(String s, Runnable r0, Runnable r1)
	{
		super(s, r0, r1);
	}
}