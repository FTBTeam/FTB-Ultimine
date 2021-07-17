package dev.ftb.mods.ftbultimine.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.FTBUltimineCommon;
import dev.ftb.mods.ftbultimine.config.FTBUltimineClientConfig;
import dev.ftb.mods.ftbultimine.event.LevelRenderLastEvent;
import dev.ftb.mods.ftbultimine.net.FTBUltimineNet;
import dev.ftb.mods.ftbultimine.net.KeyPressedPacket;
import dev.ftb.mods.ftbultimine.net.ModeChangedPacket;
import dev.ftb.mods.ftbultimine.net.SendShapePacket;
import me.shedaniel.architectury.event.events.GuiEvent;
import me.shedaniel.architectury.event.events.client.ClientRawInputEvent;
import me.shedaniel.architectury.event.events.client.ClientTickEvent;
import me.shedaniel.architectury.registry.KeyBindings;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static dev.ftb.mods.ftbultimine.utils.AccessUtil.getKey;

/**
 * @author LatvianModder
 */
public class FTBUltimineClient extends FTBUltimineCommon {
	private final KeyMapping keyBinding;
	private boolean pressed;
	private List<BlockPos> shapeBlocks = Collections.emptyList();
	private List<CachedEdge> cachedEdges = null;
	public boolean hasScrolled = false;
	private long lastToggle = 0;
	private final int INPUT_DELAY = 125;

	public FTBUltimineClient() {
		keyBinding = new KeyMapping("key.ftbultimine", InputConstants.Type.KEYSYM, 96, "key.categories.ftbultimine");

		KeyBindings.registerKeyBinding(keyBinding);

		FTBUltimineClientConfig.init();

		ClientTickEvent.CLIENT_PRE.register(this::clientTick);
		GuiEvent.RENDER_HUD.register(this::renderGameOverlay);

		// TODO: remove once #6 gets fixed
		LevelRenderLastEvent.EVENT.register(this::renderInGame);

		ClientRawInputEvent.MOUSE_SCROLLED.register(this::mouseEvent);
		ClientRawInputEvent.KEY_PRESSED.register(this::onKeyPress);
	}

	@Override
	public void setShape(List<BlockPos> blocks) {
		shapeBlocks = blocks;
		cachedEdges = null;
		updateEdges();
	}

	public void renderInGame(PoseStack stack) {
		if (!pressed || cachedEdges == null || cachedEdges.isEmpty()) {
			return;
		}

		Minecraft mc = Minecraft.getInstance();

		if (!FTBUltimine.instance.canUltimine(mc.player)) {
			return;
		}

		Camera activeRenderInfo = mc.getEntityRenderDispatcher().camera;
		Vec3 projectedView = activeRenderInfo.getPosition();

		stack.pushPose();
		stack.translate(-projectedView.x, -projectedView.y, -projectedView.z);
		Matrix4f matrix = stack.last().pose();

		VertexConsumer buffer = mc.renderBuffers().bufferSource().getBuffer(UltimineRenderTypes.LINES_NORMAL);

		for (CachedEdge edge : cachedEdges) {
			buffer.vertex(matrix, edge.x1, edge.y1, edge.z1).color(255, 255, 255, 255).endVertex();
			buffer.vertex(matrix, edge.x2, edge.y2, edge.z2).color(255, 255, 255, 255).endVertex();
		}

		mc.renderBuffers().bufferSource().endBatch(UltimineRenderTypes.LINES_NORMAL);

		VertexConsumer buffer2 = mc.renderBuffers().bufferSource().getBuffer(UltimineRenderTypes.LINES_TRANSPARENT);

		for (CachedEdge edge : cachedEdges) {
			buffer2.vertex(matrix, edge.x1, edge.y1, edge.z1).color(255, 255, 255, 10).endVertex();
			buffer2.vertex(matrix, edge.x2, edge.y2, edge.z2).color(255, 255, 255, 10).endVertex();
		}

		mc.renderBuffers().bufferSource().endBatch(UltimineRenderTypes.LINES_TRANSPARENT);

		stack.popPose();
	}

	public InteractionResult mouseEvent(Minecraft client, double amount) {
		if (pressed && amount != 0 && sneak()) {
			hasScrolled = true;
			FTBUltimineNet.MAIN.sendToServer(new ModeChangedPacket(amount < 0D));
			return InteractionResult.FAIL;
		}

		return InteractionResult.PASS;
	}

	public InteractionResult onKeyPress(Minecraft client, int keyCode, int scanCode, int action, int modifiers) {
		{
			if ((System.currentTimeMillis() - lastToggle) < INPUT_DELAY) {
				return InteractionResult.PASS;
			}

			if (keyCode != GLFW.GLFW_KEY_UP && keyCode != GLFW.GLFW_KEY_DOWN) {
				return InteractionResult.PASS;
			}

			if (!pressed || !sneak()) {
				return InteractionResult.PASS;
			}

			hasScrolled = true;
			FTBUltimineNet.MAIN.sendToServer(new ModeChangedPacket(keyCode == GLFW.GLFW_KEY_DOWN));
			lastToggle = System.currentTimeMillis();
		}
		return InteractionResult.PASS;
	}

	private boolean sneak() {
		return getKey(keyBinding).getValue() == GLFW.GLFW_KEY_LEFT_SHIFT || getKey(keyBinding).getValue() == GLFW.GLFW_KEY_RIGHT_SHIFT ? Screen.hasControlDown() : Screen.hasShiftDown();
	}

	private void addPressedInfo(List<MutableComponent> list) {
		list.add(new TranslatableComponent("ftbultimine.active"));

		if (!hasScrolled) {
			list.add(new TranslatableComponent("ftbultimine.change_shape").withStyle(ChatFormatting.GRAY));
		}

		if (SendShapePacket.current != null) {
			if (sneak()) {
				list.add(new TextComponent(""));
				list.add(new TextComponent("^ ").withStyle(ChatFormatting.GRAY).append(new TranslatableComponent("ftbultimine.shape." + SendShapePacket.current.prev.getName())));
			}

			list.add(new TextComponent("- ").append(new TranslatableComponent("ftbultimine.shape." + SendShapePacket.current.getName())));

			if (sneak()) {
				list.add(new TextComponent("v ").withStyle(ChatFormatting.GRAY).append(new TranslatableComponent("ftbultimine.shape." + SendShapePacket.current.next.getName())));
			}
		}
	}

	private final int infoOffset = 0;

	// TODO: reimplement if architectury adds support for getting debug text offset
	/*@SubscribeEvent(priority = EventPriority.LOWEST)
	public void info(RenderGameOverlayEvent.Text event)
	{
		if (FTBUltimineConfig.get().renderTextManually == -1)
		{
			infoOffset = event.getLeft().size();
		}
		else
		{
			infoOffset = FTBUltimineConfig.get().renderTextManually;
		}
	}*/

	public void renderGameOverlay(PoseStack matrices, float tickDelta) {
		if (pressed) {
			RenderSystem.enableBlend();
			RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

			List<MutableComponent> list = new ArrayList<>();
			addPressedInfo(list);
			Minecraft minecraft = Minecraft.getInstance();

			int top = 2 + minecraft.font.lineHeight * infoOffset;

			for (MutableComponent msg : list) {
				GuiComponent.fill(matrices, 1, top - 1, 2 + minecraft.font.width(msg.getString()) + 1, top + minecraft.font.lineHeight - 1, -1873784752);
				minecraft.font.drawShadow(matrices, msg, 2, top, 14737632);
				top += minecraft.font.lineHeight;
			}
		}
	}

	public void clientTick(Minecraft mc) {
		if (Minecraft.getInstance().player == null) {
			return;
		}

		boolean p = pressed;

		if ((pressed = keyBinding.isDown()) != p) {
			FTBUltimineNet.MAIN.sendToServer(new KeyPressedPacket(pressed));
		}
	}

	private void updateEdges() {
		if (cachedEdges != null) {
			return;
		}
		if (shapeBlocks.isEmpty()) {
			cachedEdges = Collections.emptyList();
			return;
		}

		BlockPos pos = shapeBlocks.get(0);

		double d = 0.005D;
		VoxelShape shape = Shapes.box(-d, -d, -d, 1D + d, 1D + d, 1D + d);
		VoxelShape[] extraShapes = new VoxelShape[shapeBlocks.size() - 1];

		for (int i = 1; i < shapeBlocks.size(); i++) {
			BlockPos p = shapeBlocks.get(i);
			extraShapes[i - 1] = shape.move(p.getX() - pos.getX(), p.getY() - pos.getY(), p.getZ() - pos.getZ());
		}

		cachedEdges = new ArrayList<>();

		(extraShapes.length == 0 ? shape : Shapes.or(shape, extraShapes)).forAllEdges((x1, y1, z1, x2, y2, z2) -> {
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