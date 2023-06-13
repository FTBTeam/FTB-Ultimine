package dev.ftb.mods.ftbultimine.shape;

import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.config.FTBUltimineServerConfig;
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

	default boolean actualCheck(BlockState original, BlockState state) {
		return !state.is(FTBUltimine.EXCLUDED_BLOCKS) && check(original, state);
	}

	BlockMatcher MATCH = (original, state) -> original.getBlock() == state.getBlock();
	BlockMatcher TAGS_MATCH_SHAPELESS = FTBUltimineServerConfig.MERGE_TAGS_SHAPELESS::match;
	BlockMatcher TAGS_MATCH_SHAPED = FTBUltimineServerConfig.MERGE_TAGS_SHAPED::match;
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