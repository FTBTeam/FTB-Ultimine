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
import dev.ftb.mods.ftbultimine.utils.ShapeMerger;
import me.shedaniel.architectury.event.events.GuiEvent;
import me.shedaniel.architectury.event.events.client.ClientLifecycleEvent;
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
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * @author LatvianModder
 */
public class FTBUltimineClient extends FTBUltimineCommon {
	private final KeyMapping keyBinding;
	private boolean pressed;
	private boolean canUltimine;
	private List<BlockPos> shapeBlocks = Collections.emptyList();
	private int actualBlocks = 0;
	private List<CachedEdge> cachedEdges = null;
	private BlockPos cachedPos = null;
	public boolean hasScrolled = false;
	private long lastToggle = 0;
	private final int INPUT_DELAY = 125;

	public FTBUltimineClient() {
		keyBinding = new KeyMapping("key.ftbultimine", InputConstants.Type.KEYSYM, 96, "key.categories.ftbultimine");

		KeyBindings.registerKeyBinding(keyBinding);

		ClientLifecycleEvent.CLIENT_WORLD_LOAD.register(__ -> FTBUltimineClientConfig.load());

		ClientTickEvent.CLIENT_PRE.register(this::clientTick);
		GuiEvent.RENDER_HUD.register(this::renderGameOverlay);

		// TODO: remove once #6 gets fixed
		LevelRenderLastEvent.EVENT.register(this::renderInGame);

		ClientRawInputEvent.MOUSE_SCROLLED.register(this::mouseEvent);
		ClientRawInputEvent.KEY_PRESSED.register(this::onKeyPress);
	}

	@Override
	public void setShape(List<BlockPos> blocks) {
		actualBlocks = blocks.size();
		int maxRendered = Math.min(actualBlocks, FTBUltimineClientConfig.renderOutline.get());
		shapeBlocks = blocks.subList(0, maxRendered);
		cachedEdges = null;
		updateEdges();
	}

	public void renderInGame(PoseStack stack) {
		if (!pressed || cachedPos == null || cachedEdges == null || cachedEdges.isEmpty()) {
			return;
		}

		Minecraft mc = Minecraft.getInstance();

		if (!canUltimine) {
			return;
		}

		Camera activeRenderInfo = mc.getEntityRenderDispatcher().camera;
		Vec3 projectedView = activeRenderInfo.getPosition();

		stack.pushPose();
		stack.translate(cachedPos.getX() - projectedView.x, cachedPos.getY() - projectedView.y, cachedPos.getZ() - projectedView.z);
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
		return keyBinding.key.getValue() == GLFW.GLFW_KEY_LEFT_SHIFT || keyBinding.key.getValue() == GLFW.GLFW_KEY_RIGHT_SHIFT ? Screen.hasControlDown() : Screen.hasShiftDown();
	}

	private void addPressedInfo(List<MutableComponent> list) {
		list.add(new TranslatableComponent("ftbultimine.info.base",
				canUltimine ? new TranslatableComponent("ftbultimine.info.active").withStyle(style -> style.withColor(TextColor.fromRgb(0x7FB069)))
						: new TranslatableComponent("ftbultimine.info.not_active").withStyle(style -> style.withColor(TextColor.fromRgb(0xF25F5C)))
		));

		if (!hasScrolled) {
			list.add(new TranslatableComponent("ftbultimine.change_shape").withStyle(ChatFormatting.GRAY));
		}

		if (SendShapePacket.current != null) {
			if (sneak()) {
				list.add(new TextComponent(""));
				list.add(new TextComponent("^ ").withStyle(ChatFormatting.GRAY)
						.append(new TranslatableComponent("ftbultimine.shape." + SendShapePacket.current.prev.getName())));
			}

			MutableComponent mining = new TextComponent("- ")
					.append(new TranslatableComponent("ftbultimine.shape." + SendShapePacket.current.getName()));

			if (canUltimine && actualBlocks != 0) {
				mining.append(" (").append(new TranslatableComponent("ftbultimine.info.blocks", actualBlocks));
				if (actualBlocks > shapeBlocks.size()) {
					mining.append(", ").append(new TranslatableComponent("ftbultimine.info.partial_render", shapeBlocks.size()));
				}
				mining.append(")");
			}
			list.add(mining);

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
				FormattedCharSequence formatted = msg.getVisualOrderText();
				GuiComponent.fill(matrices, 1, top - 1, 2 + minecraft.font.width(formatted) + 1, top + minecraft.font.lineHeight - 1, -1873784752);
				minecraft.font.drawShadow(matrices, formatted, 2, top, 14737632);
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

		canUltimine = pressed && FTBUltimine.instance.canUltimine(mc.player);
	}

	private void updateEdges() {
		if (cachedEdges != null) {
			return;
		}
		if (shapeBlocks.isEmpty()) {
			cachedEdges = Collections.emptyList();
			return;
		}

		cachedPos = shapeBlocks.get(0);

		double d = 0.005D;

		cachedEdges = new ArrayList<>();

		Collection<VoxelShape> shapes = new HashSet<>();
		for (AABB aabb : ShapeMerger.merge(shapeBlocks, cachedPos)) {
			shapes.add(Shapes.create(aabb.inflate(d)));
		}

		orShapes(shapes).forAllEdges((x1, y1, z1, x2, y2, z2) -> {
			CachedEdge edge = new CachedEdge();
			edge.x1 = (float) x1;
			edge.y1 = (float) y1;
			edge.z1 = (float) z1;
			edge.x2 = (float) x2;
			edge.y2 = (float) y2;
			edge.z2 = (float) z2;
			cachedEdges.add(edge);
		});
	}

	static VoxelShape orShapes(Collection<VoxelShape> shapes) {
		VoxelShape combinedShape = Shapes.empty();
		for (VoxelShape shape : shapes) {
			combinedShape = Shapes.joinUnoptimized(combinedShape, shape, BooleanOp.OR);
		}
		return combinedShape;
	}
}