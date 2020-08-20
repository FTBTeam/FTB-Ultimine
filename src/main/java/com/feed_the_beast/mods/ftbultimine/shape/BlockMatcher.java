package com.feed_the_beast.mods.ftbultimine.shape;

import net.minecraft.block.BlockState;
import net.minecraft.block.BushBlock;
import net.minecraft.block.CropsBlock;
import net.minecraftforge.common.Tags;

/**
 * @author LatvianModder
 */
public interface BlockMatcher
{
	boolean check(BlockState original, BlockState state);

	BlockMatcher MATCH = (original, state) -> original.getBlock() == state.getBlock();
	BlockMatcher ANY_STONE = (original, state) -> Tags.Blocks.STONE.contains(state.getBlock());
	BlockMatcher BUSH = (original, state) -> state.getBlock() instanceof BushBlock && (original.getBlock() instanceof CropsBlock == state.getBlock() instanceof CropsBlock);
}