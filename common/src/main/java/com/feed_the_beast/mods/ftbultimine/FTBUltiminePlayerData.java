package com.feed_the_beast.mods.ftbultimine;

import com.feed_the_beast.mods.ftbultimine.net.FTBUltimineNet;
import com.feed_the_beast.mods.ftbultimine.net.SendShapePacket;
import com.feed_the_beast.mods.ftbultimine.shape.BlockMatcher;
import com.feed_the_beast.mods.ftbultimine.shape.Shape;
import com.feed_the_beast.mods.ftbultimine.shape.ShapeContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @author LatvianModder
 */
public class FTBUltiminePlayerData
{
	public final UUID id;
	public boolean pressed = false;
	public Shape shape = Shape.get("");

	public BlockPos cachedPos;
	public Direction cachedDirection;
	public List<BlockPos> cachedBlocks;

	public FTBUltiminePlayerData(UUID i)
	{
		id = i;
	}

	public void clearCache()
	{
		cachedPos = null;
		cachedDirection = null;
		cachedBlocks = null;
	}

	public static HitResult rayTrace(ServerPlayer player)
	{
		double distance = player.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue();
		return player.pick(player.isCreative() ? distance : distance - 0.5D, 1F, false);
	}

	public void checkBlocks(ServerPlayer player, boolean sendUpdate, int maxBlocks)
	{
		if (!pressed)
		{
			return;
		}

		HitResult result = rayTrace(player);

		if (!(result instanceof BlockHitResult) || result.getType() != HitResult.Type.BLOCK)
		{
			if (cachedBlocks != null && !cachedBlocks.isEmpty())
			{
				clearCache();

				if (sendUpdate)
				{
					FTBUltimineNet.MAIN.send(PacketDistributor.PLAYER.with(() -> player), new SendShapePacket(shape, Collections.emptyList()));
				}
			}

			return;
		}

		BlockHitResult r = (BlockHitResult) result;

		if (cachedDirection != r.getDirection() || cachedPos == null || !cachedPos.equals(r.getBlockPos()))
		{
			updateBlocks(player, r.getBlockPos(), r.getDirection(), sendUpdate, maxBlocks);
		}
	}

	@Nullable
	public ShapeContext updateBlocks(ServerPlayer player, BlockPos p, Direction d, boolean sendUpdate, int maxBlocks)
	{
		ShapeContext context = null;
		cachedPos = p;
		cachedDirection = d;

		if (maxBlocks <= 0)
		{
			cachedBlocks = Collections.emptyList();
		}
		else
		{
			context = new ShapeContext();
			context.player = player;
			context.pos = cachedPos;
			context.face = cachedDirection;
			context.matcher = BlockMatcher.MATCH;
			context.maxBlocks = maxBlocks;
			context.original = player.level.getBlockState(cachedPos);

			if (FTBUltimineConfig.mergeStone && BlockMatcher.ANY_STONE.check(context.original, context.original))
			{
				context.matcher = BlockMatcher.ANY_STONE;
			}
			else if (BlockMatcher.BUSH.check(context.original, context.original))
			{
				context.matcher = BlockMatcher.BUSH;
			}

			cachedBlocks = shape.getBlocks(context);
		}

		if (sendUpdate)
		{
			FTBUltimineNet.MAIN.send(PacketDistributor.PLAYER.with(() -> player), new SendShapePacket(shape, cachedBlocks));
		}

		return context;
	}
}