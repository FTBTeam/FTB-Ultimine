package com.feed_the_beast.mods.ftbultimine.shape;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

/**
 * @author LatvianModder
 */
public class ShapeContext
{
	public ServerPlayerEntity player;
	public BlockPos pos;
	public Direction face;
	public BlockState original;
	public BlockMatcher matcher;
	public int maxBlocks;

	public boolean check(BlockState state)
	{
		return matcher.check(original, state);
	}

	public BlockState block(BlockPos pos)
	{
		return player.world.getBlockState(pos);
	}

	public boolean check(BlockPos pos)
	{
		return check(block(pos));
	}
}