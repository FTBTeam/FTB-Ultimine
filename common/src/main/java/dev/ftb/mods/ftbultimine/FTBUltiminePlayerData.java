package dev.ftb.mods.ftbultimine;

import dev.ftb.mods.ftblibrary.platform.network.Server2PlayNetworking;
import dev.ftb.mods.ftbultimine.api.rightclick.RightClickHandler;
import dev.ftb.mods.ftbultimine.api.shape.Shape;
import dev.ftb.mods.ftbultimine.api.shape.ShapeContext;
import dev.ftb.mods.ftbultimine.config.FTBUltimineServerConfig;
import dev.ftb.mods.ftbultimine.net.SendShapePacket;
import dev.ftb.mods.ftbultimine.rightclick.RightClickDispatcher;
import dev.ftb.mods.ftbultimine.shape.BlockMatchers;
import dev.ftb.mods.ftbultimine.shape.ShapeRegistry;
import dev.ftb.mods.ftbultimine.utils.XPUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.IntSupplier;

/// Server-side player data
public class FTBUltiminePlayerData {
	private final UUID playerId;
	private boolean pressed = false;
	private int shapeIndex = 0;
	private double pendingXPCost;

	@Nullable
	private BlockPos cachedPos;
	@Nullable
	private Direction cachedDirection;
	@Nullable
	private List<BlockPos> cachedBlocks;

	public FTBUltiminePlayerData(UUID playerId) {
		this.playerId = playerId;
	}

	public void clearCache() {
		cachedPos = null;
		cachedDirection = null;
		cachedBlocks = null;
	}

	public UUID getPlayerId() {
		return playerId;
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isPressed() {
		return pressed;
	}

	public void setPressed(boolean pressed) {
		this.pressed = pressed;
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean hasCachedPositions() {
		return cachedBlocks != null && !cachedBlocks.isEmpty();
	}

	@Nullable
	public Collection<BlockPos> cachedPositions() {
		return cachedBlocks;
	}

	public static HitResult rayTrace(ServerPlayer player) {
		double distance = player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
		return player.pick(player.isCreative() ? distance + 0.5D : distance, 1F, false);
	}

	public Shape getCurrentShape() {
		return ShapeRegistry.getInstance(false).getShape(shapeIndex);
	}

	public int getCurrentShapeIndex() {
		return shapeIndex;
	}

	public void cycleShape(boolean next) {
		int nShapes = ShapeRegistry.getInstance(false).shapeCount();
		if (next) {
			if (++shapeIndex >= nShapes) {
				shapeIndex = 0;
			}
		} else {
			if (--shapeIndex < 0) {
				shapeIndex = nShapes - 1;
			}
		}
	}

	public void addPendingXPCost(ServerPlayer player, int blockCount) {
		pendingXPCost += blockCount * FTBUltimineServerConfig.getExperiencePerBlock(player);
	}

	public void takePendingXP(ServerPlayer player) {
		if (pendingXPCost > 1.0) {
			int toTake = (int) pendingXPCost;
			XPUtils.addPlayerXP(player, -toTake);
			pendingXPCost -= toTake;
		}
	}

	public void checkBlocks(ServerPlayer player, boolean sendUpdate, IntSupplier maxBlocks) {
		if (!pressed) {
			return;
		}

		HitResult result = rayTrace(player);

		if (!(result instanceof BlockHitResult hitResult) || result.getType() != HitResult.Type.BLOCK) {
			if (cachedBlocks != null && !cachedBlocks.isEmpty()) {
				clearCache();

				if (sendUpdate) {
					Server2PlayNetworking.send(player, SendShapePacket.adjustShapeAndBlockPos(getCurrentShapeIndex(), Collections.emptyList()));
				}
			}

			return;
		}

		if (cachedDirection != hitResult.getDirection() || cachedPos == null || !cachedPos.equals(hitResult.getBlockPos())) {
			updateBlocks(player, hitResult.getBlockPos(), hitResult.getDirection(), sendUpdate, maxBlocks.getAsInt());
		}
	}

	@Nullable
	public ShapeContext updateBlocks(ServerPlayer player, BlockPos pos, Direction dir, boolean sendUpdate, int maxBlocks) {
		ShapeContext context = null;
		cachedPos = pos.immutable();
		cachedDirection = dir;

		Shape shape = getCurrentShape();

		if (maxBlocks <= 0) {
			cachedBlocks = Collections.emptyList();
		} else {
			BlockState origState = player.level().getBlockState(cachedPos);
			ShapeContext.Matcher matcher = BlockMatchers.determineBestMatcher(player.level(), cachedPos, origState, shape);
			context = new ShapeContext(player, cachedPos, cachedDirection, origState, matcher, maxBlocks);
			cachedBlocks = shape.getBlocks(context);
			if (FTBUltimineServerConfig.getExperiencePerBlock(player) > 0d) {
				int max = (int) (XPUtils.getPlayerXP(player) / FTBUltimineServerConfig.getExperiencePerBlock(player));
				if (max < cachedBlocks.size()) {
					cachedBlocks = cachedBlocks.subList(0, max);
				}
			}
			for (RightClickHandler handler : RightClickDispatcher.INSTANCE.getHandlers()) {
				List<BlockPos> filtered = handler.filterPreview(player, cachedBlocks);
				if (filtered != null) {
					cachedBlocks = filtered;
					break;
				}
			}
		}

		if (sendUpdate) {
			Server2PlayNetworking.send(player, SendShapePacket.adjustShapeAndBlockPos(getCurrentShapeIndex(), cachedBlocks));
		}

		return context;
	}

}