package com.feed_the_beast.mods.ftbultimine.shape;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BushBlock;
import net.minecraft.block.CropsBlock;
import net.minecraft.block.SaplingBlock;
import net.minecraftforge.common.Tags;

/**
 * @author LatvianModder
 */
public interface BlockMatcher
{
	boolean check(BlockState original, BlockState state);

	BlockMatcher MATCH = (original, state) -> original.getBlock() == state.getBlock();
	BlockMatcher ANY_STONE = (original, state) -> Tags.Blocks.STONE.contains(state.getBlock());
	BlockMatcher BUSH = (original, state) -> state.getBlock() instanceof BushBlock && getBushType(state.getBlock()) == getBushType(original.getBlock());

	static int getBushType(Block block)
	{
		if (block instanceof CropsBlock)
		{
			return 1;
		}
		else if (block instanceof SaplingBlock)
		{
			return 2;
		}

		return 0;
	}
}