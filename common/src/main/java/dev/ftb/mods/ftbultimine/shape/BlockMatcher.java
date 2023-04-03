package dev.ftb.mods.ftbultimine.shape;

import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.config.FTBUltimineServerConfig;
import net.minecraft.world.level.block.*;
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
	BlockMatcher CROP_LIKE = (original, state) -> (state.getBlock() instanceof BushBlock || state.getBlock() instanceof CocoaBlock)
			&& getBushType(state.getBlock()) == getBushType(original.getBlock());

	static int getBushType(Block block) {
		if (block instanceof CropBlock) {
			return 1;
		} else if (block instanceof SaplingBlock) {
			return 2;
		} else if (block instanceof CocoaBlock) {
			return 3;
		}

		return 0;
	}
}