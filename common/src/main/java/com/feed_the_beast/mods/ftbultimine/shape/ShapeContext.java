package com.feed_the_beast.mods.ftbultimine.shape;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

/**
 * @author LatvianModder
 */
public class ShapeContext {
	public ServerPlayer player;
	public BlockPos pos;
	public Direction face;
	public BlockState original;
	public BlockMatcher matcher;
	public int maxBlocks;

	public boolean check(BlockState state) {
		return matcher.check(original, state);
	}

	public BlockState block(BlockPos pos) {
		return player.level.getBlockState(pos);
	}

	public boolean check(BlockPos pos) {
		return check(block(pos));
	}
}