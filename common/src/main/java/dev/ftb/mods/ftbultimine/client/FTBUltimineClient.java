package dev.ftb.mods.ftbultimine.client;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.client.ClientRawInputEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import dev.ftb.mods.ftblibrary.config.ui.EditConfigScreen;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.ui.GuiHelper;
import dev.ftb.mods.ftbultimine.CooldownTracker;
import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.FTBUltimineCommon;
import dev.ftb.mods.ftbultimine.config.FTBUltimineClientConfig;
import dev.ftb.mods.ftbultimine.config.FTBUltimineServerConfig;
import dev.ftb.mods.ftbultimine.event.LevelRenderLastEvent;
import dev.ftb.mods.ftbultimine.net.EditConfigPacket;
import dev.ftb.mods.ftbultimine.net.KeyPressedPacket;
import dev.ftb.mods.ftbultimine.net.ModeChangedPacket;
import dev.ftb.mods.ftbultimine.shape.ShapeRegistry;
import dev.ftb.mods.ftbultimine.utils.ShapeMerger;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;

import java.util.*;

public class FTBUltimineClient extends FTBUltimineCommon {
	public static KeyMapping keyBinding;
	private boolean pressed;
	private boolean canUltimine;
	private List<BlockPos> shapeBlocks = Collections.emptyList();
	private int actualBlocks = 0;
	private List<CachedEdge> cachedEdges = null;
	private BlockPos cachedPos = null;
	public boolean hasScrolledYet = false;
	private long lastToggle = 0;
	public final int INPUT_DELAY = 125;
	private int shapeIdx = 0;  // shape index of client player's current shape
	private int maxPanelWidth = 100;
	private List<IndentedLine> infoPanelList = List.of();

	public FTBUltimineClient() {
		KeyMappingRegistry.register(keyBinding = new KeyMapping("key.ftbultimine", InputConstants.Type.KEYSYM, InputConstants.KEY_GRAVE, "key.categories.ftbultimine"));

		ClientLifecycleEvent.CLIENT_LEVEL_LOAD.register(__ -> FTBUltimineClientConfig.load());
		ClientLifecycleEvent.CLIENT_SETUP.register(this::onClientSetup);

		ClientTickEvent.CLIENT_PRE.register(this::clientTick);
		ClientGuiEvent.RENDER_HUD.register(this::renderGameOverlay);

		// TODO: remove once #6 gets fixed
		LevelRenderLastEvent.EVENT.register(this::renderInGame);

		ClientRawInputEvent.MOUSE_SCROLLED.register(this::onMouseScrolled);
		ClientRawInputEvent.KEY_PRESSED.register(this::onKeyPress);
	}

	public static Player getClientPlayer() {
		return Minecraft.getInstance().player;
	}

	private void onClientSetup(Minecraft minecraft) {
		ShapeRegistry.freeze();
	}

	@Override
	public void setShape(int shapeIdx, List<BlockPos> blocks) {
		this.shapeIdx = shapeIdx;
		if (blocks != null) {
			actualBlocks = blocks.size();
			int maxRendered = Math.min(actualBlocks, FTBUltimineClientConfig.RENDER_OUTLINE.get());
			shapeBlocks = blocks.subList(0, maxRendered);
			cachedEdges = null;
			updateEdges();
		}
	}

	public static void editConfig(Player player, EditConfigPacket.ConfigType configType) {
		switch (configType) {
			case CLIENT -> editClientConfig();
			case SERVER -> {
				if (player.hasPermissions(Commands.LEVEL_GAMEMASTERS)) {
					editServerConfig();
				}
			}
			case CHOOSE -> {
				if (player.hasPermissions(Commands.LEVEL_GAMEMASTERS)) {
					new ChooseConfigScreen().openGui();
				} else {
					editClientConfig();
				}
			}
		}
	}

	public static void editServerConfig() {
		new EditConfigScreen(FTBUltimineServerConfig.getConfigGroup())
				.setAutoclose(true).setOpenPrevScreenOnClose(false).openGui();
	}

	public static void editClientConfig() {
		new EditConfigScreen(FTBUltimineClientConfig.getConfigGroup())
				.setAutoclose(true).setOpenPrevScreenOnClose(false).openGui();
	}

	public void renderInGame(PoseStack stack) {
		if (!pressed || cachedPos == null || cachedEdges == null || cachedEdges.isEmpty()) {
			return;
		}

		Minecraft mc = Minecraft.getInstance();

		if (!canUltimine) {
			return;
		}

		// Rewrite this to use shader that does outline instead

		Camera activeRenderInfo = mc.getEntityRenderDispatcher().camera;
		Vec3 projectedView = activeRenderInfo.getPosition();

		stack.pushPose();
		stack.translate(cachedPos.getX() - projectedView.x, cachedPos.getY() - projectedView.y, cachedPos.getZ() - projectedView.z);
		Matrix4f matrix = stack.last().pose();

		VertexConsumer buffer = mc.renderBuffers().bufferSource().getBuffer(UltimineRenderTypes.LINES_NORMAL);

		for (CachedEdge edge : cachedEdges) {
			buffer.addVertex(matrix, edge.x1, edge.y1, edge.z1).setColor(255, 255, 255, 255);
			buffer.addVertex(matrix, edge.x2, edge.y2, edge.z2).setColor(255, 255, 255, 255);
		}

		mc.renderBuffers().bufferSource().endBatch(UltimineRenderTypes.LINES_NORMAL);

		VertexConsumer buffer2 = mc.renderBuffers().bufferSource().getBuffer(UltimineRenderTypes.LINES_TRANSPARENT);

		int alpha = FTBUltimineClientConfig.PREVIEW_LINE_ALPHA.get();
		for (CachedEdge edge : cachedEdges) {
			buffer2.addVertex(matrix, edge.x1, edge.y1, edge.z1).setColor(255, 255, 255, alpha);
			buffer2.addVertex(matrix, edge.x2, edge.y2, edge.z2).setColor(255, 255, 255, alpha);
		}

		mc.renderBuffers().bufferSource().endBatch(UltimineRenderTypes.LINES_TRANSPARENT);

		stack.popPose();
	}

	public EventResult onMouseScrolled(Minecraft client, double amountX, double amountY) {
		if (pressed && (amountY != 0 || amountX != 0) && isMenuSneaking()) {
			NetworkManager.sendToServer(new ModeChangedPacket(amountX < 0D || amountY < 0D));
			hasScrolledYet = true;
			return EventResult.interruptFalse();
		}

		return EventResult.pass();
	}

	public EventResult onKeyPress(Minecraft client, int keyCode, int scanCode, int action, int modifiers) {
		if ((System.currentTimeMillis() - lastToggle) < INPUT_DELAY) {
			return EventResult.pass();
		}

		if (keyCode != InputConstants.KEY_UP && keyCode != InputConstants.KEY_DOWN) {
			return EventResult.pass();
		}

		if (!pressed || !isMenuSneaking()) {
			return EventResult.pass();
		}

		NetworkManager.sendToServer(new ModeChangedPacket(keyCode == InputConstants.KEY_DOWN));
		lastToggle = System.currentTimeMillis();
		hasScrolledYet = true;
		return EventResult.pass();
	}

	private boolean isMenuSneaking() {
		if (!FTBUltimineClientConfig.REQUIRE_SNEAK_FOR_MENU.get()) return true;

		return keyBinding.matches(InputConstants.KEY_LSHIFT, 0) || keyBinding.matches(InputConstants.KEY_RSHIFT, 0) ?
				Screen.hasControlDown() :
				Screen.hasShiftDown();
	}

	private List<IndentedLine> addPressedInfo() {
		ImmutableList.Builder<IndentedLine> builder = ImmutableList.builder();

		Component msg;
		if (CooldownTracker.isOnCooldown(getClientPlayer())) {
			msg = Component.translatable("ftbultimine.info.cooldown").withStyle(style -> style.withColor(TextColor.fromRgb(0xBFBF8C)));
		} else if (canUltimine && actualBlocks > 0) {
			msg = Component.translatable("ftbultimine.info.active").withStyle(style -> style.withColor(TextColor.fromRgb(0xA3BE8C)));
		} else {
			msg = Component.translatable("ftbultimine.info.not_active").withStyle(style -> style.withColor(TextColor.fromRgb(0xBF616A)));
		}
		builder.add(new IndentedLine(0, Component.translatable("ftbultimine.info.base", msg)));

		int context = Math.min((ShapeRegistry.shapeCount() - 1) / 2, FTBUltimineClientConfig.SHAPE_MENU_CONTEXT_LINES.get());

		boolean showingMenu = isMenuSneaking();

		if (showingMenu) {
			builder.add(new IndentedLine(0, Component.empty()));
			for (int i = -context; i < 0; i++) {
				builder.add(new IndentedLine(16, ShapeRegistry.getShape(shapeIdx + i).getDisplayName().withStyle(ChatFormatting.GRAY)));
			}
		} else {
			builder.add(new IndentedLine(0, Component.translatable("ftbultimine.change_shape.short").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
			builder.add(new IndentedLine(0, Component.empty()));
		}

		builder.add(new IndentedLine(showingMenu ? 16 : 0, ShapeRegistry.getShape(shapeIdx).getDisplayName().withStyle(ChatFormatting.YELLOW)));

		if (showingMenu) {
			for (int i = 1; i <= context; i++) {
				builder.add(new IndentedLine(16, ShapeRegistry.getShape(shapeIdx + i).getDisplayName().withStyle(ChatFormatting.GRAY)));
			}
		}

		if (canUltimine && actualBlocks != 0) {
			MutableComponent mining = Component.empty();
			mining.append(Component.translatable("ftbultimine.info.blocks", actualBlocks));
			if (actualBlocks > shapeBlocks.size()) {
				mining.append(", ").append(Component.translatable("ftbultimine.info.partial_render", shapeBlocks.size()));
			}
			builder.add(new IndentedLine(0, Component.empty()));
			builder.add(new IndentedLine(0, mining.withColor(0xA3BE8C)));
		}

		return builder.build();
	}

	public void renderGameOverlay(GuiGraphics graphics, DeltaTracker tickDelta) {
		if (pressed) {
			Minecraft mc = Minecraft.getInstance();

			RenderSystem.enableBlend();
			RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

			Font font = mc.font;
			int width = Math.max(maxPanelWidth, 10 + infoPanelList.stream().map(l -> font.width(l.text)).max(Integer::compareTo).orElse(100));
			maxPanelWidth = width;
			int height = font.lineHeight * infoPanelList.size();
			float scale = 1f;

			int insetX = FTBUltimineClientConfig.OVERLAY_INSET_X.get();
			int insetY = FTBUltimineClientConfig.OVERLAY_INSET_Y.get();
			var pos = FTBUltimineClientConfig.OVERLAY_POS.get().getPanelPos(
					mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight(),
					(int) (width * scale), (int) (height * scale),
					insetX, insetY
			);

			graphics.pose().pushPose();
			graphics.pose().translate(pos.x(), pos.y(), 100);
			graphics.pose().scale(scale, scale, 1F);

			// panel background
			Color4I.DARK_GRAY.withAlpha(128).draw(graphics, -2, -2, width + 4, height + 4);
			GuiHelper.drawHollowRect(graphics, -2, -2, width + 4, height + 4, Color4I.GRAY.withAlpha(128), false);

			// draw cooldown progress bar if needed
			float f = CooldownTracker.getCooldownRemaining(getClientPlayer());
			if (f < 1f) {
				Color4I.rgba(0xAA40A040).draw(graphics,0, 0, (int) (width * f) + 1, font.lineHeight + 1);
			}

			if (isMenuSneaking()) {
				// draw the "scrollbar"
				int barUpper = font.lineHeight * 2;
				int barHeight = font.lineHeight * (ShapeRegistry.shapeCount() - 1) - 2;
				Color4I col = Color4I.WHITE.withAlpha(192);
				col.draw(graphics, 3, barUpper, 2, barHeight);
				col.draw(graphics, 2, barUpper + 1, 4, 1);
				col.draw(graphics, 1, barUpper + 2, 6, 1);
				col.draw(graphics, 2, barUpper + barHeight - 2, 4, 1);
				col.draw(graphics, 1, barUpper + barHeight - 3, 6, 1);
			}

			// render the text lines
			int top = 0;
			for (IndentedLine line : infoPanelList) {
				FormattedCharSequence formatted = line.text.getVisualOrderText();
				graphics.drawString(font, formatted, line.indent, top, 0xECEFF4, true);
				top += font.lineHeight;
			}

			graphics.pose().popPose();
		}
	}

	public void clientTick(Minecraft mc) {
		if (Minecraft.getInstance().player == null) {
			return;
		}

		boolean p = pressed;

		if ((pressed = keyBinding.isDown()) != p) {
			NetworkManager.sendToServer(new KeyPressedPacket(pressed));

			if (pressed && !hasScrolledYet && mc.player != null) {
				MutableComponent msg1 = Component.translatable(FTBUltimineClientConfig.REQUIRE_SNEAK_FOR_MENU.get() ?
						"ftbultimine.change_shape" : "ftbultimine.change_shape.no_shift");
				mc.player.displayClientMessage(msg1, true);
			}
		}

		canUltimine = pressed && FTBUltimine.instance.canUltimine(mc.player);

		if (pressed) {
			infoPanelList = addPressedInfo();
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

		cachedPos = shapeBlocks.getFirst();

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

	private record IndentedLine(int indent, Component text) {
	}
}
