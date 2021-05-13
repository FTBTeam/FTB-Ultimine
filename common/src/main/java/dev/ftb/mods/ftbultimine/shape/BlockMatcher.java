package dev.ftb.mods.ftbultimine.shape;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * @author LatvianModder
 */
public interface BlockMatcher {
	boolean check(BlockState original, BlockState state);

	BlockMatcher MATCH = (original, state) -> original.getBlock() == state.getBlock();
	// FIXME: This is NOT permanent. I want to add a "group blocks" option to Ultimine since we'll likely be using json configs,
	//  and this'll check whether two blocks are in the same "group".
	BlockMatcher ANY_STONE = (original, state) -> BlockTags.BASE_STONE_OVERWORLD.contains(state.getBlock());
	BlockMatcher BUSH = (original, state) -> state.getBlock() instanceof BushBlock && getBushType(state.getBlock()) == getBushType(original.getBlock());

	static int getBushType(Block block) {
		if (block instanceof CropBlock) {
			return 1;
		} else if (block instanceof SaplingBlock) {
			return 2;
		}

		return 0;
	}
}