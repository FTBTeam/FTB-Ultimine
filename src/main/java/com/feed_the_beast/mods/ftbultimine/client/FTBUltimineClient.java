package com.feed_the_beast.mods.ftbultimine.client;

import com.feed_the_beast.mods.ftbultimine.FTBUltimineCommon;
import com.feed_the_beast.mods.ftbultimine.net.FTBUltimineNet;
import com.feed_the_beast.mods.ftbultimine.net.KeyPressedPacket;
import com.feed_the_beast.mods.ftbultimine.net.ModeChangedPacket;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author LatvianModder
 */
public class FTBUltimineClient extends FTBUltimineCommon
{
	private KeyBinding keyBinding, keyBindingSettings;
	private boolean pressed;
	private List<BlockPos> shapeBlocks = Collections.emptyList();
	private List<CachedEdge> cachedEdges = null;

	public FTBUltimineClient()
	{
		MinecraftForge.EVENT_BUS.addListener(this::renderInGame);
		MinecraftForge.EVENT_BUS.addListener(this::renderInfo);
		MinecraftForge.EVENT_BUS.addListener(this::clientTick);
		keyBinding = new KeyBinding("key.ftbultimine", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputMappings.Type.KEYSYM, 96, "key.categories.gameplay");
		keyBindingSettings = new KeyBinding("key.ftbultimine_settings", KeyConflictContext.IN_GAME, KeyModifier.SHIFT, InputMappings.Type.KEYSYM, 96, "key.categories.gameplay");
		ClientRegistry.registerKeyBinding(keyBinding);
		ClientRegistry.registerKeyBinding(keyBindingSettings);
	}

	@Override
	public void setShape(List<BlockPos> blocks)
	{
		shapeBlocks = blocks;
		cachedEdges = null;
		updateEdges();
	}

	private void renderInGame(RenderWorldLastEvent event)
	{
		if (!pressed || cachedEdges == null || cachedEdges.isEmpty())
		{
			return;
		}

		Minecraft mc = Minecraft.getInstance();
		ActiveRenderInfo activeRenderInfo = mc.getRenderManager().info;
		Vec3d projectedView = activeRenderInfo.getProjectedView();

		MatrixStack ms = event.getMatrixStack();
		ms.push();
		ms.translate(-projectedView.x, -projectedView.y, -projectedView.z);
		Matrix4f matrix = ms.getLast().getMatrix();

		IVertexBuilder buffer = mc.getRenderTypeBuffers().getBufferSource().getBuffer(UltimineRenderTypes.LINES_NORMAL);

		for (CachedEdge edge : cachedEdges)
		{
			buffer.pos(matrix, edge.x1, edge.y1, edge.z1).color(255, 255, 255, 255).endVertex();
			buffer.pos(matrix, edge.x2, edge.y2, edge.z2).color(255, 255, 255, 255).endVertex();
		}

		mc.getRenderTypeBuffers().getBufferSource().finish(UltimineRenderTypes.LINES_NORMAL);

		IVertexBuilder buffer2 = mc.getRenderTypeBuffers().getBufferSource().getBuffer(UltimineRenderTypes.LINES_TRANSPARENT);

		for (CachedEdge edge : cachedEdges)
		{
			buffer2.pos(matrix, edge.x1, edge.y1, edge.z1).color(255, 255, 255, 10).endVertex();
			buffer2.pos(matrix, edge.x2, edge.y2, edge.z2).color(255, 255, 255, 10).endVertex();
		}

		mc.getRenderTypeBuffers().getBufferSource().finish(UltimineRenderTypes.LINES_TRANSPARENT);

		ms.pop();
	}

	private void renderInfo(GuiScreenEvent event)
	{
	}

	private void clientTick(TickEvent.ClientTickEvent event)
	{
		Minecraft mc = Minecraft.getInstance();

		if (event.phase != TickEvent.Phase.START || mc.player == null)
		{
			return;
		}

		if (keyBindingSettings.isPressed())
		{
			FTBUltimineNet.MAIN.sendToServer(new ModeChangedPacket());
			return;
		}

		if (keyBinding.isPressed())
		{
			if (!pressed)
			{
				pressed = true;
				FTBUltimineNet.MAIN.sendToServer(new KeyPressedPacket(true));
			}
		}
		else if (pressed && !keyBinding.isKeyDown())
		{
			pressed = false;
			FTBUltimineNet.MAIN.sendToServer(new KeyPressedPacket(false));
		}
	}

	private void updateEdges()
	{
		if (cachedEdges != null)
		{
			return;
		}
		if (shapeBlocks.isEmpty())
		{
			cachedEdges = Collections.emptyList();
			return;
		}

		BlockPos pos = shapeBlocks.get(0);

		double d = 0.005D;
		VoxelShape shape = VoxelShapes.create(-d, -d, -d, 1D + d, 1D + d, 1D + d);
		VoxelShape[] extraShapes = new VoxelShape[shapeBlocks.size() - 1];

		for (int i = 1; i < shapeBlocks.size(); i++)
		{
			BlockPos p = shapeBlocks.get(i);
			extraShapes[i - 1] = shape.withOffset(p.getX() - pos.getX(), p.getY() - pos.getY(), p.getZ() - pos.getZ());
		}

		cachedEdges = new ArrayList<>();

		(extraShapes.length == 0 ? shape : VoxelShapes.or(shape, extraShapes)).forEachEdge((x1, y1, z1, x2, y2, z2) -> {
			CachedEdge edge = new CachedEdge();
			edge.x1 = (float) (x1 + pos.getX());
			edge.y1 = (float) (y1 + pos.getY());
			edge.z1 = (float) (z1 + pos.getZ());
			edge.x2 = (float) (x2 + pos.getX());
			edge.y2 = (float) (y2 + pos.getY());
			edge.z2 = (float) (z2 + pos.getZ());
			cachedEdges.add(edge);
		});
	}
}