package dev.ftb.mods.ftbultimine;

import dev.ftb.mods.ftbultimine.net.SendShapePacket;
import dev.ftb.mods.ftbultimine.shape.BlockMatcher;
import dev.ftb.mods.ftbultimine.shape.Shape;
import dev.ftb.mods.ftbultimine.shape.ShapeContext;
import dev.ftb.mods.ftbultimine.shape.ShapeRegistry;
import dev.ftb.mods.ftbultimine.utils.PlatformMethods;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @author LatvianModder
 */
public class FTBUltiminePlayerData {
	private final UUID id;
	private boolean pressed = false;
	private int shapeIndex = 0;

	public BlockPos cachedPos;
	public Direction cachedDirection;
	public List<BlockPos> cachedBlocks;

	public FTBUltiminePlayerData(UUID i) {
		id = i;
	}

	public void clearCache() {
		cachedPos = null;
		cachedDirection = null;
		cachedBlocks = null;
	}

	public boolean isPressed() {
		return pressed;
	}

	public void setPressed(boolean pressed) {
		this.pressed = pressed;
	}

	public static HitResult rayTrace(ServerPlayer player) {
		double distance = PlatformMethods.reach(player);
		return player.pick(player.isCreative() ? distance + 0.5D : distance, 1F, false);
	}

	public Shape getCurrentShape() {
		return ShapeRegistry.getShape(shapeIndex);
	}

	public int getCurrentShapeIndex() {
		return shapeIndex;
	}

	public void cycleShape(boolean next) {
		if (next) {
			if (++shapeIndex >= ShapeRegistry.shapeCount()) {
				shapeIndex = 0;
			}
		} else {
			if (--shapeIndex < 0) {
				shapeIndex = ShapeRegistry.shapeCount() - 1;
			}
		}
	}

	public void checkBlocks(ServerPlayer player, boolean sendUpdate, int maxBlocks) {
		if (!pressed) {
			return;
		}

		HitResult result = rayTrace(player);

		if (!(result instanceof BlockHitResult hitResult) || result.getType() != HitResult.Type.BLOCK) {
			if (cachedBlocks != null && !cachedBlocks.isEmpty()) {
				clearCache();

				if (sendUpdate) {
					new SendShapePacket(getCurrentShapeIndex(), Collections.emptyList()).sendTo(player);
				}
			}

			return;
		}

		if (cachedDirection != hitResult.getDirection() || cachedPos == null || !cachedPos.equals(hitResult.getBlockPos())) {
			updateBlocks(player, hitResult.getBlockPos(), hitResult.getDirection(), sendUpdate, maxBlocks);
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
			BlockState origState = player.level.getBlockState(cachedPos);
			BlockMatcher matcher;
			if (shape.getTagMatcher().actualCheck(origState, origState)) {
				matcher = shape.getTagMatcher();
			} else if (BlockMatcher.CROP_LIKE.actualCheck(origState, origState)) {
				matcher = BlockMatcher.CROP_LIKE;
			} else {
				matcher = BlockMatcher.MATCH;
			}
			context = new ShapeContext(player, cachedPos, cachedDirection, player.level.getBlockState(cachedPos), matcher, maxBlocks);
			cachedBlocks = shape.getBlocks(context);
		}

		if (sendUpdate) {
			new SendShapePacket(getCurrentShapeIndex(), cachedBlocks).sendTo(player);
		}

		return context;
	}

}