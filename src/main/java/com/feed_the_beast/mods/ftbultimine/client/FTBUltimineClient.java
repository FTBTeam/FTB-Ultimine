package com.feed_the_beast.mods.ftbultimine.client;

import com.feed_the_beast.mods.ftbultimine.FTBUltimineCommon;
import com.feed_the_beast.mods.ftbultimine.FTBUltimineConfig;
import com.feed_the_beast.mods.ftbultimine.net.FTBUltimineNet;
import com.feed_the_beast.mods.ftbultimine.net.KeyPressedPacket;
import com.feed_the_beast.mods.ftbultimine.net.ModeChangedPacket;
import com.feed_the_beast.mods.ftbultimine.net.SendShapePacket;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author LatvianModder
 */
public class FTBUltimineClient extends FTBUltimineCommon
{
	private final KeyBinding keyBinding;
	private boolean pressed;
	private List<BlockPos> shapeBlocks = Collections.emptyList();
	private List<CachedEdge> cachedEdges = null;
	public boolean hasScrolled = false;
	private long lastToggle = 0;
	private final int INPUT_DELAY = 125;

	public FTBUltimineClient()
	{
		MinecraftForge.EVENT_BUS.register(this);
		keyBinding = new KeyBinding("key.ftbultimine", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputMappings.Type.KEYSYM, 96, "key.categories.gameplay");
		ClientRegistry.registerKeyBinding(keyBinding);
	}

	@Override
	public void setShape(List<BlockPos> blocks)
	{
		shapeBlocks = blocks;
		cachedEdges = null;
		updateEdges();
	}

	@SubscribeEvent
	public void renderInGame(RenderWorldLastEvent event)
	{
		if (!pressed || cachedEdges == null || cachedEdges.isEmpty())
		{
			return;
		}

		Minecraft mc = Minecraft.getInstance();
		ActiveRenderInfo activeRenderInfo = mc.getRenderManager().info;
		Vector3d projectedView = activeRenderInfo.getProjectedView();

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

	@SubscribeEvent
	public void mouseEvent(InputEvent.MouseScrollEvent event)
	{
		if (pressed && event.getScrollDelta() != 0 && sneak())
		{
			hasScrolled = true;
			FTBUltimineNet.MAIN.sendToServer(new ModeChangedPacket(event.getScrollDelta() < 0D));
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onKeyPress(InputEvent.KeyInputEvent event)
	{
		if ((System.currentTimeMillis() - lastToggle) < INPUT_DELAY) {
			return;
		}

		if (event.getKey() != GLFW.GLFW_KEY_UP && event.getKey() != GLFW.GLFW_KEY_DOWN) {
			return;
		}

		if (!pressed || !sneak()) {
			return;
		}

		hasScrolled = true;
		FTBUltimineNet.MAIN.sendToServer(new ModeChangedPacket(event.getKey() == GLFW.GLFW_KEY_DOWN));
		lastToggle = System.currentTimeMillis();
	}

	private boolean sneak()
	{
		return keyBinding.getKey().getKeyCode() == GLFW.GLFW_KEY_LEFT_SHIFT || keyBinding.getKey().getKeyCode() == GLFW.GLFW_KEY_RIGHT_SHIFT ? Screen.hasControlDown() : Screen.hasShiftDown();
	}

	private void addPressedInfo(List<IFormattableTextComponent> list)
	{
		list.add(new TranslationTextComponent("ftbultimine.active"));

		if (!hasScrolled)
		{
			list.add(new TranslationTextComponent("ftbultimine.change_shape").mergeStyle(TextFormatting.GRAY));
		}

		if (SendShapePacket.current != null)
		{
			if (sneak())
			{
				list.add(new StringTextComponent(""));
				list.add(new StringTextComponent("^ ").mergeStyle(TextFormatting.GRAY).append(new TranslationTextComponent("ftbultimine.shape." + SendShapePacket.current.prev.getName())));
			}

			list.add(new StringTextComponent("- ").append(new TranslationTextComponent("ftbultimine.shape." + SendShapePacket.current.getName())));

			if (sneak())
			{
				list.add(new StringTextComponent("v ").mergeStyle(TextFormatting.GRAY).append(new TranslationTextComponent("ftbultimine.shape." + SendShapePacket.current.next.getName())));
			}
		}
	}

	private int infoOffset = 0;

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void info(RenderGameOverlayEvent.Text event)
	{
		if (FTBUltimineConfig.renderTextManually == -1)
		{
			infoOffset = event.getLeft().size();
		}
		else
		{
			infoOffset = FTBUltimineConfig.renderTextManually;
		}
	}

	@SubscribeEvent
	public void renderGameOverlay(RenderGameOverlayEvent.Post event)
	{
		if (pressed && event.getType() == RenderGameOverlayEvent.ElementType.ALL)
		{
			RenderSystem.enableBlend();
			RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

			List<IFormattableTextComponent> list = new ArrayList<>();
			addPressedInfo(list);
			Minecraft minecraft = Minecraft.getInstance();

			int top = 2 + minecraft.fontRenderer.FONT_HEIGHT * infoOffset;

			for (IFormattableTextComponent msg : list)
			{
				AbstractGui.fill(event.getMatrixStack(), 1, top - 1, 2 + minecraft.fontRenderer.getStringWidth(msg.getString()) + 1, top + minecraft.fontRenderer.FONT_HEIGHT - 1, -1873784752);
				minecraft.fontRenderer.func_243246_a(event.getMatrixStack(), msg, 2, top, 14737632);
				top += minecraft.fontRenderer.FONT_HEIGHT;
			}
		}
	}

	@SubscribeEvent
	public void clientTick(TickEvent.ClientTickEvent event)
	{
		Minecraft mc = Minecraft.getInstance();

		if (event.phase != TickEvent.Phase.START || mc.player == null)
		{
			return;
		}

		boolean p = pressed;

		if ((pressed = keyBinding.isKeyDown()) != p)
		{
			FTBUltimineNet.MAIN.sendToServer(new KeyPressedPacket(pressed));
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