package dev.ftb.mods.ftbultimine.client;

import dev.ftb.mods.ftbultimine.mixin.RenderTypeAccess;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

public class UltimineRenderTypes {
	public static final RenderType LINES_NO_DEPTH_TRANSLUCENT = RenderTypeAccess.invokeCreate(
			"ultimine_lines_translucent",
			RenderSetup.builder(UltimineRenderPipelines.LINES_NO_DEPTH_TRANSLUCENT)
					.setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
//					.setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
					.createRenderSetup());
}