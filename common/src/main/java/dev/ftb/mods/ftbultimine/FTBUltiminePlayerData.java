package dev.ftb.mods.ftbultimine;

import dev.ftb.mods.ftbultimine.net.SendShapePacket;
import dev.ftb.mods.ftbultimine.shape.BlockMatcher;
import dev.ftb.mods.ftbultimine.shape.Shape;
import dev.ftb.mods.ftbultimine.shape.ShapeContext;
import dev.ftb.mods.ftbultimine.utils.PlatformMethods;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
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
	public final UUID id;
	public boolean pressed = false;
	public Shape shape = Shape.get("");

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

	public static HitResult rayTrace(ServerPlayer player) {
		double distance = PlatformMethods.reach(player);
		return player.pick(player.isCreative() ? distance + 0.5D : distance, 1F, false);
	}

	public void checkBlocks(ServerPlayer player, boolean sendUpdate, int maxBlocks) {
		if (!pressed) {
			return;
		}

		HitResult result = rayTrace(player);

		if (!(result instanceof BlockHitResult) || result.getType() != HitResult.Type.BLOCK) {
			if (cachedBlocks != null && !cachedBlocks.isEmpty()) {
				clearCache();

				if (sendUpdate) {
					new SendShapePacket(shape, Collections.emptyList()).sendTo(player);
				}
			}

			return;
		}

		BlockHitResult r = (BlockHitResult) result;

		if (cachedDirection != r.getDirection() || cachedPos == null || !cachedPos.equals(r.getBlockPos())) {
			updateBlocks(player, r.getBlockPos(), r.getDirection(), sendUpdate, maxBlocks);
		}
	}

	@Nullable
	public ShapeContext updateBlocks(ServerPlayer player, BlockPos p, Direction d, boolean sendUpdate, int maxBlocks) {
		ShapeContext context = null;
		cachedPos = p;
		cachedDirection = d;

		if (maxBlocks <= 0) {
			cachedBlocks = Collections.emptyList();
		} else {
			context = new ShapeContext();
			context.player = player;
			context.pos = cachedPos;
			context.face = cachedDirection;
			context.matcher = BlockMatcher.MATCH;
			context.maxBlocks = maxBlocks;
			context.original = player.level.getBlockState(cachedPos);

			if (shape.getTagMatcher().actualCheck(context.original, context.original)) {
				context.matcher = shape.getTagMatcher();
			} else if (BlockMatcher.BUSH.actualCheck(context.original, context.original)) {
				context.matcher = BlockMatcher.BUSH;
			}

			cachedBlocks = shape.getBlocks(context);
		}

		if (sendUpdate) {
			new SendShapePacket(shape, cachedBlocks).sendTo(player);
		}

		return context;
	}
}