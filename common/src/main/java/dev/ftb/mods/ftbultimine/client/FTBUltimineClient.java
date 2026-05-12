package dev.ftb.mods.ftbultimine.client;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.ftb.mods.ftblibrary.client.gui.GuiHelper;
import dev.ftb.mods.ftblibrary.client.icon.Color4IRenderer;
import dev.ftb.mods.ftblibrary.client.util.ClientUtils;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.platform.client.PlatformClient;
import dev.ftb.mods.ftblibrary.platform.network.Play2ServerNetworking;
import dev.ftb.mods.ftbultimine.CooldownTracker;
import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.api.FTBUltimineAPI;
import dev.ftb.mods.ftbultimine.api.util.CanUltimineResult;
import dev.ftb.mods.ftbultimine.config.FTBUltimineClientConfig;
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
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class FTBUltimineClient {
	public static final Identifier GUI_OVERLAY_ID = FTBUltimineAPI.id("overlay");
	private static final KeyMapping.Category KEY_CATEGORY = new KeyMapping.Category(FTBUltimineAPI.id("default"));

	public static final KeyMapping keyBindUltimine
			= new KeyMapping("key.ftbultimine", InputConstants.Type.KEYSYM, InputConstants.KEY_GRAVE, KEY_CATEGORY);
	public static final KeyMapping keyBindNextMode
			= new KeyMapping("ftbultimine.change_shape.next", InputConstants.Type.KEYSYM, InputConstants.KEY_UP, KEY_CATEGORY);
	public static final KeyMapping keyBindPrevMode
			= new KeyMapping("ftbultimine.change_shape.prev", InputConstants.Type.KEYSYM, InputConstants.KEY_DOWN, KEY_CATEGORY);

	@Nullable
	private static FTBUltimineClient instance;

	private float panelAlphaMult = 0f;
	private boolean ultimineKeyPressed;
	private boolean canUltimine;
	private CanUltimineResult canUltimineStatus = CanUltimineResult.ALLOWED;
	private List<BlockPos> shapeBlocks = Collections.emptyList();
	private int actualBlocks = 0;
	@Nullable
	private List<CachedEdge> cachedEdges = null;
	@Nullable
	private BlockPos cachedPos = null;
	public boolean hasScrolledYet = false;
	private long lastToggle = 0;
	public final int INPUT_DELAY = 125;
	private int shapeIdx = 0;  // shape index of client player's current shape
	private int maxPanelWidth = 100;
	private List<IndentedLine> infoPanelList = List.of();

	public FTBUltimineClient() {
		instance = this;

		PlatformClient.get().registerKeyMapping(FTBUltimineAPI.MOD_ID,
				keyBindUltimine,
				keyBindNextMode,
				keyBindPrevMode
		);
	}

	public static FTBUltimineClient getInstance() {
		return Objects.requireNonNull(instance);
	}

	public void setShape(int shapeIdx, @Nullable List<BlockPos> blocks) {
		this.shapeIdx = shapeIdx;
		if (blocks != null) {
			actualBlocks = blocks.size();
			int maxRendered = Math.min(actualBlocks, FTBUltimineClientConfig.RENDER_OUTLINE.get());
			shapeBlocks = blocks.subList(0, maxRendered);
			cachedEdges = null;
			updateEdges();
		}
		if (!ultimineKeyPressed && FTBUltimineClientConfig.SHAPE_FEEDBACK_MESSAGE.get()) {
			Component shapeName = ShapeRegistry.getInstance(true).getShape(shapeIdx).getDisplayName();
			ClientUtils.getClientPlayer().sendOverlayMessage(
					Component.translatable("key.ftbultimine").append(" : ").append(shapeName));
		}
	}

	@Nullable
	public Collection<BlockPos> getSelectedBlocks() {
		return actualBlocks == 0 || shapeBlocks.isEmpty() ? null : shapeBlocks;
	}

	public void renderInGame(PoseStack stack) {
		if (!ultimineKeyPressed || cachedPos == null || cachedEdges == null || cachedEdges.isEmpty() || !canUltimine) {
			return;
		}

		Minecraft mc = Minecraft.getInstance();
		Camera camera = mc.getEntityRenderDispatcher().camera;
		if (camera == null) {
			return;
		}
		Vec3 cameraPos = camera.position();

		stack.pushPose();
		stack.translate(cachedPos.getX() - cameraPos.x, cachedPos.getY() - cameraPos.y, cachedPos.getZ() - cameraPos.z);
		Matrix4f matrix = stack.last().pose();

		// solid lines on outer edges of blocks
		VertexConsumer buffer = mc.renderBuffers().bufferSource().getBuffer(RenderTypes.LINES);
		for (CachedEdge edge : cachedEdges) {
			buffer.addVertex(matrix, edge.x1(), edge.y1(), edge.z1())
					.setColor(255, 255, 255, 255)
					.setNormal(edge.xn(), edge.yn(), edge.zn())
					.setLineWidth(2f);
			buffer.addVertex(matrix, edge.x2(), edge.y2(), edge.z2())
					.setColor(255, 255, 255, 255)
					.setNormal(edge.xn(), edge.yn(), edge.zn())
					.setLineWidth(2f);
		}

		// translucent lines on hidden edges of blocks
		VertexConsumer buffer2 = mc.renderBuffers().bufferSource().getBuffer(UltimineRenderTypes.LINES_NO_DEPTH_TRANSLUCENT);
		int alpha = FTBUltimineClientConfig.PREVIEW_LINE_ALPHA.get();
		for (CachedEdge edge : cachedEdges) {
			buffer2.addVertex(matrix, edge.x1(), edge.y1(), edge.z1())
					.setColor(255, 255, 255, alpha)
					.setNormal(edge.xn(), edge.yn(), edge.zn())
					.setLineWidth(1f);
			buffer2.addVertex(matrix, edge.x2(), edge.y2(), edge.z2())
					.setColor(255, 255, 255, alpha)
					.setNormal(edge.xn(), edge.yn(), edge.zn())
					.setLineWidth(1f);
		}

		stack.popPose();
	}

	public boolean onMouseScrolled(double amountX, double amountY) {
		if (ultimineKeyPressed && (amountY != 0 || amountX != 0) && isMenuSneaking()) {
			Play2ServerNetworking.send(new ModeChangedPacket(amountX < 0D || amountY < 0D));
			hasScrolledYet = true;
			return true;
		}

		return false;
	}

	public boolean onKeyPress() {
		if ((System.currentTimeMillis() - lastToggle) >= INPUT_DELAY) {
			if (keyBindPrevMode.isDown()) {
				cycleCurrentShape(false);
			} else if (keyBindNextMode.isDown()) {
				cycleCurrentShape(true);
			}
		}
		return false;
	}

	private void cycleCurrentShape(boolean nextMode) {
		if ((ultimineKeyPressed || !FTBUltimineClientConfig.REQUIRE_ULTIMINE_KEY_FOR_CYCLING.get()) && isMenuSneaking()) {
			Play2ServerNetworking.send(new ModeChangedPacket(nextMode));
			lastToggle = System.currentTimeMillis();
			hasScrolledYet = true;
		}
	}

	private boolean isMenuSneaking() {
		if (!FTBUltimineClientConfig.REQUIRE_SNEAK_FOR_MENU.get()) return true;

		return keyBindUltimine.getDefaultKey().getValue() == InputConstants.KEY_LSHIFT
				|| keyBindUltimine.getDefaultKey().getValue() == InputConstants.KEY_RSHIFT ?
				Minecraft.getInstance().hasControlDown() :
				Minecraft.getInstance().hasShiftDown();
	}

	private List<IndentedLine> addPressedInfo() {
		ImmutableList.Builder<IndentedLine> builder = ImmutableList.builder();

		Component msg;
		boolean isActive = true;
		if (CooldownTracker.isOnCooldown(ClientUtils.getClientPlayer())) {
			msg = Component.translatable("ftbultimine.info.cooldown").withStyle(style -> style.withColor(TextColor.fromRgb(0xBFBF6C)));
		} else if (canUltimine && actualBlocks > 0) {
			msg = Component.translatable("ftbultimine.info.active").withStyle(style -> style.withColor(TextColor.fromRgb(0xA3BE8C)));
		} else {
			msg = Component.translatable("ftbultimine.info.not_active").withStyle(style -> style.withColor(TextColor.fromRgb(0xBF616A)));
			isActive = false;
		}
		builder.add(new IndentedLine(0, Component.translatable("ftbultimine.info.base", msg)));
		if (!isActive) {
			builder.add(new IndentedLine(8, Component.translatable(canUltimineStatus.getTranslationKey()).withStyle(style -> style.withColor(TextColor.fromRgb(0xBF616A)))));
		}

		ShapeRegistry shapeRegistry = ShapeRegistry.getInstance(true);
		int context = Math.min((shapeRegistry.shapeCount() - 1) / 2, FTBUltimineClientConfig.SHAPE_MENU_CONTEXT_LINES.get());

		boolean showingMenu = isMenuSneaking();

		if (showingMenu) {
			if (isActive) {
				builder.add(new IndentedLine(0, Component.empty()));
			}
			for (int i = -context; i < 0; i++) {
				builder.add(new IndentedLine(16, shapeRegistry.getShape(shapeIdx + i).getDisplayName().withStyle(ChatFormatting.GRAY)));
			}
		} else {
			builder.add(new IndentedLine(0, Component.translatable("ftbultimine.change_shape.short").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
			builder.add(new IndentedLine(0, Component.empty()));
		}

		builder.add(new IndentedLine(showingMenu ? 16 : 0, shapeRegistry.getShape(shapeIdx).getDisplayName().withStyle(ChatFormatting.YELLOW)));

		if (showingMenu) {
			for (int i = 1; i <= context; i++) {
				builder.add(new IndentedLine(16, shapeRegistry.getShape(shapeIdx + i).getDisplayName().withStyle(ChatFormatting.GRAY)));
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

	public void renderGameOverlay(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(false);
		if (ultimineKeyPressed) {
			panelAlphaMult = Math.min(panelAlphaMult + 0.1f * partialTicks, 1f);
		} else {
			panelAlphaMult = Math.max(panelAlphaMult - 0.1f * partialTicks, 0f);
		}
		if (panelAlphaMult > 0f) {
			Minecraft mc = Minecraft.getInstance();

			Font font = mc.font;
			int width = Math.max(maxPanelWidth, 10 + infoPanelList.stream().map(l -> font.width(l.text)).max(Integer::compareTo).orElse(100));
			maxPanelWidth = width;
			int height = font.lineHeight * infoPanelList.size();
			float scale = FTBUltimineClientConfig.OVERLAY_SCALE.get().floatValue();

			int insetX = FTBUltimineClientConfig.OVERLAY_INSET_X.get();
			int insetY = FTBUltimineClientConfig.OVERLAY_INSET_Y.get();
			var pos = FTBUltimineClientConfig.OVERLAY_POS.get().getPanelPos(
					mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight(),
					(int) (width * scale), (int) (height * scale),
					insetX, insetY
			);

			graphics.pose().pushMatrix();
			graphics.pose().translate(pos.x(), pos.y());
			graphics.pose().scale(scale, scale);

			Color4IRenderer colorRenderer = Color4IRenderer.INSTANCE;
			// panel background
			colorRenderer.render(Color4I.DARK_GRAY.withAlphaf(0.5f * panelAlphaMult), graphics, -2, -2, width + 4, height + 4);
			GuiHelper.drawHollowRect(graphics, -2, -2, width + 4, height + 4, Color4I.GRAY.withAlphaf(0.5f * panelAlphaMult), false);

			// draw cooldown progress bar if needed
			float f = CooldownTracker.getCooldownRemaining(ClientUtils.getClientPlayer());
			if (f < 1f) {
				Color4I col = Color4I.rgb(0x40A040).withAlphaf(0.68f * panelAlphaMult);
				colorRenderer.render(col, graphics,0, 0, (int) (width * f) + 1, font.lineHeight + 1);
			}

			if (isMenuSneaking()) {
				// draw the "scrollbar"
				int barUpper = font.lineHeight * 2;
				int barHeight = font.lineHeight * (ShapeRegistry.getInstance(true).shapeCount() - 1) - 2;
				Color4I col = Color4I.WHITE.withAlphaf(0.75f * panelAlphaMult);
				colorRenderer.render(col, graphics, 3, barUpper, 2, barHeight);
				colorRenderer.render(col, graphics, 2, barUpper + 1, 4, 1);
				colorRenderer.render(col, graphics, 1, barUpper + 2, 6, 1);
				colorRenderer.render(col, graphics, 2, barUpper + barHeight - 2, 4, 1);
				colorRenderer.render(col, graphics, 1, barUpper + barHeight - 3, 6, 1);
			}

			// render the text lines
			int top = 0;
			for (IndentedLine line : infoPanelList) {
				FormattedCharSequence formatted = line.text.getVisualOrderText();
				Color4I col = Color4I.rgb(0xECEFF4).withAlphaf(panelAlphaMult);
				graphics.text(font, formatted, line.indent, top, col.rgba(), true);
				top += font.lineHeight;
			}

			graphics.pose().popMatrix();
		}
	}

	public void clientTick(Minecraft mc) {
		if (mc.player == null) {
			return;
		}

		boolean p = ultimineKeyPressed;

		if ((ultimineKeyPressed = keyBindUltimine.isDown()) != p) {
			Play2ServerNetworking.send(new KeyPressedPacket(ultimineKeyPressed));

			if (ultimineKeyPressed && !hasScrolledYet && mc.player != null) {
				MutableComponent msg1 = Component.translatable(FTBUltimineClientConfig.REQUIRE_SNEAK_FOR_MENU.get() ?
						"ftbultimine.change_shape" : "ftbultimine.change_shape.no_shift");
				mc.player.sendOverlayMessage(msg1);
			}
		}
		canUltimineStatus = mc.hitResult instanceof BlockHitResult b && b.getType() == HitResult.Type.BLOCK ?
				FTBUltimine.getInstance().canUltimine(mc.player, b.getBlockPos(), mc.player.level().getBlockState(b.getBlockPos())) :
				CanUltimineResult.NO_BLOCK_TARGETED;
		canUltimine = ultimineKeyPressed && (canUltimineStatus.isAllowed());

		if (ultimineKeyPressed) {
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
		cachedEdges = new ArrayList<>();

		Collection<VoxelShape> shapes = new HashSet<>();
		for (AABB aabb : ShapeMerger.merge(shapeBlocks, cachedPos)) {
			shapes.add(Shapes.create(aabb.inflate(0.005D)));
		}

		orShapes(shapes).forAllEdges((x1, y1, z1, x2, y2, z2) -> cachedEdges.add(CachedEdge.fromDoubles(x1, y1, z1, x2, y2, z2)));
	}

	static VoxelShape orShapes(Collection<VoxelShape> shapes) {
		VoxelShape combinedShape = Shapes.empty();
		for (VoxelShape shape : shapes) {
			combinedShape = Shapes.joinUnoptimized(combinedShape, shape, BooleanOp.OR);
		}
		return combinedShape.optimize();
	}

	private record IndentedLine(int indent, Component text) {
	}
}
